package com.kjooy0305.apkextractor

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kjooy0305.apkextractor.databinding.ActivityMainBinding
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val adapter = AppAdapter { updateSummary() }
    private val io = Executors.newSingleThreadExecutor()
    private var all: List<AppItem> = emptyList()
    private var visible: List<AppItem> = emptyList()
    private var busy = false
    @Volatile private var cancelRequested = false
    private var wakeLock: PowerManager.WakeLock? = null

    private val storagePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startExtract() else showMessage(getString(R.string.permission_denied))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.appList.layoutManager = LinearLayoutManager(this)
        b.appList.adapter = adapter

        b.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) = applyFilter()
        })
        b.systemToggle.setOnCheckedChangeListener { _, _ -> applyFilter() }
        b.selectAllButton.setOnClickListener { adapter.selectAllVisible() }
        b.clearButton.setOnClickListener { adapter.clearSelection() }
        b.extractButton.setOnClickListener { if (busy) { cancelRequested = true; b.extractButton.isEnabled = false } else onExtractClicked() }

        io.execute {
            val list = AppRepository.load(this)
            runOnUiThread {
                all = list
                applyFilter()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 화면이 닫히면 진행 중인 저장을 멈추고 만들던 파일을 지운다
        if (busy) cancelRequested = true
        io.shutdown()
    }

    private fun applyFilter() {
        val query = b.searchInput.text.toString().trim().lowercase()
        val withSystem = b.systemToggle.isChecked
        visible = all.filter { item ->
            (withSystem || !item.isSystem) &&
                (query.isEmpty() || item.label.lowercase().contains(query) ||
                    item.packageName.lowercase().contains(query))
        }
        adapter.submit(visible)
        b.stateText.visibility = if (visible.isEmpty()) View.VISIBLE else View.GONE
        b.stateText.setText(if (all.isEmpty()) R.string.loading else R.string.empty)
        updateSummary()
    }

    private fun updateSummary() {
        val n = adapter.selected.size
        b.countText.text = getString(R.string.app_count, visible.size, n)
        if (busy) return
        b.extractButton.isEnabled = n > 0
        b.extractButton.text =
            if (n > 0) getString(R.string.extract_n, n) else getString(R.string.extract_none)
    }

    private fun onExtractClicked() {
        val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        else startExtract()
    }

    private fun startExtract() {
        if (busy) return
        val targets = all.filter { it.packageName in adapter.selected }
        if (targets.isEmpty()) return
        busy = true
        cancelRequested = false
        b.extractButton.isEnabled = true
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        acquireWakeLock()
        startGuard(getString(R.string.guard_text))
        io.execute {
            val failed = mutableListOf<String>()
            var saved = 0
            var cancelled = false
            for ((i, item) in targets.withIndex()) {
                if (cancelRequested) { cancelled = true; break }
                try {
                    ApkExtractor.extract(
                        this, item,
                        isCancelled = { cancelRequested },
                        onProgress = { done, total ->
                            val pct = if (total > 0) (done * 100 / total).toInt() else 0
                            runOnUiThread {
                                if (!isDestroyed) b.extractButton.text =
                                    getString(R.string.extracting_bytes, i + 1, targets.size, item.label, pct)
                            }
                        },
                    )
                    saved++
                } catch (e: ExtractCancelled) {
                    cancelled = true
                    break
                } catch (e: Throwable) {
                    failed.add("${item.label}: ${e.message ?: e.javaClass.simpleName}")
                }
            }
            runOnUiThread {
                busy = false
                releaseWakeLock()
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                stopService(Intent(this, ExtractGuardService::class.java))
                if (isDestroyed) return@runOnUiThread
                adapter.clearSelection()
                updateSummary()
                var msg = if (cancelled) getString(R.string.done_cancelled) + "\n\n" else ""
                msg += getString(R.string.done_body, saved)
                if (saved > 0) msg += getString(R.string.done_verified)
                if (failed.isNotEmpty()) msg += getString(R.string.done_failed, failed.size, failed.joinToString("\n"))
                showMessage(msg)
            }
        }
    }

    private fun startGuard(text: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            // 알림이 보이지 않아도 저장은 계속된다. 허용하면 진행 중 알림이 보인다.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        try {
            ContextCompat.startForegroundService(
                this, Intent(this, ExtractGuardService::class.java).putExtra(ExtractGuardService.EXTRA_TEXT, text)
            )
        } catch (_: Exception) {
            // 서비스를 못 띄워도 저장은 진행한다
        }
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "apkextractor:extract").apply {
            setReferenceCounted(false)
            acquire(60 * 60 * 1000L) // 최대 1시간
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun showMessage(message: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.done_title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
}
