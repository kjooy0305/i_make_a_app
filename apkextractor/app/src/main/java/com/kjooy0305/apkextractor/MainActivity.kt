package com.kjooy0305.apkextractor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
        b.extractButton.setOnClickListener { onExtractClicked() }

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
        b.extractButton.isEnabled = n > 0 && !busy
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
        b.extractButton.isEnabled = false
        io.execute {
            val failed = mutableListOf<String>()
            targets.forEachIndexed { i, item ->
                runOnUiThread { b.extractButton.text = getString(R.string.extracting, i + 1, targets.size) }
                runCatching { ApkExtractor.extract(this, item) }.onFailure { failed.add(item.label) }
            }
            runOnUiThread {
                busy = false
                adapter.clearSelection()
                var msg = getString(R.string.done_body, targets.size - failed.size)
                if (failed.isNotEmpty()) msg += getString(R.string.done_failed, failed.size, failed.joinToString(", "))
                showMessage(msg)
            }
        }
    }

    private fun showMessage(message: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.done_title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
}
