package com.maybeitok.epubmaker

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.maybeitok.epubmaker.adapter.EpubListAdapter
import com.maybeitok.epubmaker.databinding.ActivityMainBinding
import com.maybeitok.epubmaker.databinding.DialogBookInfoBinding
import com.maybeitok.epubmaker.epub.EpubParser
import com.maybeitok.epubmaker.model.EpubDocument
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: EpubListAdapter
    private lateinit var prefs: AppPreferences

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { importEpubFromUri(it) }
    }

    private val epubDir: File
        get() = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "EpubMaker"
        )
    private val libraryDir: File get() = File(filesDir, "library")
    private val imageCacheDir: File get() = File(cacheDir, "epub_images")

    companion object {
        const val EXTRA_EPUB_DOCUMENT = "epub_document"
        const val REQUEST_PERMISSION = 1001
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = AppPreferences(newBase).language
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        prefs = AppPreferences(this)

        epubDir.mkdirs()

        setupModeTab()
        setupRecyclerView()
        setupFab()
        requestStoragePermissionIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        refreshFileList()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuManual -> {
                startActivity(Intent(this, ManualActivity::class.java))
                true
            }
            R.id.menuLanguage -> {
                showLanguageDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLanguageDialog() {
        val langs = arrayOf("한국어", "English")
        val codes = arrayOf("ko", "en")
        val cur = codes.indexOf(prefs.language).coerceAtLeast(0)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.language))
            .setSingleChoiceItems(langs, cur) { dialog, which ->
                dialog.dismiss()
                if (codes[which] != prefs.language) {
                    prefs.language = codes[which]
                    recreate()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setupModeTab() {
        binding.tabMode.addTab(binding.tabMode.newTab().setText(R.string.mode_editor))
        binding.tabMode.addTab(binding.tabMode.newTab().setText(R.string.mode_viewer))

        val curIdx = if (prefs.openMode == "viewer") 1 else 0
        binding.tabMode.getTabAt(curIdx)?.select()

        binding.tabMode.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                prefs.openMode = if (tab.position == 1) "viewer" else "editor"
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = EpubListAdapter(
            onItemClick = { file -> openFile(file) },
            onItemLongClick = { file -> showDeleteDialog(file) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fab.setOnClickListener { showBookInfoDialog() }
        binding.fabImport.setOnClickListener {
            importLauncher.launch(arrayOf("application/epub+zip", "application/octet-stream"))
        }
    }

    private fun openFile(file: File) {
        val mode = prefs.openMode
        val progress = AlertDialog.Builder(this)
            .setTitle(getString(R.string.opening_file))
            .setMessage("파일을 읽고 있습니다...")
            .setCancelable(false)
            .create()
        progress.show()

        Thread {
            val fontCacheDir = File(filesDir, "fonts_cache")
            val document = EpubParser.parse(file, fontCacheDir, imageCacheDir) { msg ->
                runOnUiThread { progress.setMessage(msg) }
            }
            runOnUiThread {
                progress.dismiss()
                if (document != null) {
                    if (mode == "viewer") openViewer(document, file.absolutePath)
                    else openEditor(document)
                } else {
                    Toast.makeText(this, R.string.parse_error, Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun importEpubFromUri(uri: Uri) {
        val available = cacheDir.freeSpace
        val fileSize = try {
            contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        } catch (_: Exception) { 0L }

        if (fileSize > 0 && fileSize > available * 0.8) {
            AlertDialog.Builder(this)
                .setTitle("저장 공간 부족")
                .setMessage("파일 크기(${fileSize / 1024 / 1024}MB)에 비해 여유 공간(${available / 1024 / 1024}MB)이 부족합니다.")
                .setPositiveButton("확인", null).show()
            return
        }

        val progress = AlertDialog.Builder(this)
            .setTitle("EPUB 불러오는 중...")
            .setMessage("파일을 분석하고 있습니다...")
            .setCancelable(false).create()
        progress.show()

        Thread {
            val fontCacheDir = File(filesDir, "fonts_cache")
            val document = EpubParser.parseFromUri(this, uri, fontCacheDir, imageCacheDir) { msg ->
                runOnUiThread { progress.setMessage(msg) }
            }
            if (document != null) {
                try {
                    libraryDir.mkdirs()
                    val safeTitle = document.title.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                        .trim().ifEmpty { "epub_${System.currentTimeMillis()}" }
                    val libFile = File(libraryDir, "$safeTitle.epub")
                    contentResolver.openInputStream(uri)?.use { input ->
                        libFile.outputStream().use { input.copyTo(it) }
                    }
                    document.filePath = libFile.absolutePath
                } catch (_: Exception) {}
            }
            runOnUiThread {
                progress.dismiss()
                if (document != null) {
                    val mode = prefs.openMode
                    if (mode == "viewer") openViewer(document, document.filePath ?: "")
                    else openEditor(document)
                } else {
                    Toast.makeText(this, R.string.parse_error, Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun refreshFileList() {
        Thread {
            val files = mutableListOf<File>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val projection = arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.DATA, MediaStore.Downloads.DATE_MODIFIED)
                val selection = "${MediaStore.Downloads.DISPLAY_NAME} LIKE ?"
                val args = arrayOf("%.epub")
                val sortOrder = "${MediaStore.Downloads.DATE_MODIFIED} DESC"
                try {
                    contentResolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, projection, selection, args, sortOrder)?.use { cursor ->
                        val dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATA)
                        while (cursor.moveToNext()) {
                            val path = cursor.getString(dataIdx) ?: continue
                            val f = File(path)
                            if (f.exists()) files.add(f)
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            } else {
                try {
                    epubDir.mkdirs()
                    epubDir.listFiles { f -> f.extension.equals("epub", ignoreCase = true) }?.let { files.addAll(it) }
                } catch (e: SecurityException) { e.printStackTrace() }
            }

            try {
                libraryDir.mkdirs()
                libraryDir.listFiles { f -> f.extension.equals("epub", ignoreCase = true) }?.let { files.addAll(it) }
            } catch (e: Exception) { e.printStackTrace() }

            val sorted = files.sortedByDescending { it.lastModified() }
            runOnUiThread {
                adapter.submitList(sorted)
                binding.tvEmpty.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
            }
        }.start()
    }

    private fun showBookInfoDialog() {
        val dialogBinding = DialogBookInfoBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.new_epub)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.create, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = dialogBinding.etTitle.text?.toString()?.trim()
                    .let { t -> if (t.isNullOrEmpty()) getString(R.string.untitled) else t }
                val author = dialogBinding.etAuthor.text?.toString()?.trim() ?: ""
                val document = EpubDocument(title = title, author = author)
                dialog.dismiss()
                openEditor(document)
            }
        }
        dialog.show()
    }

    private fun openEditor(document: EpubDocument) {
        DocumentStore.document = document
        startActivity(Intent(this, EditorActivity::class.java))
    }

    private fun openViewer(document: EpubDocument, filePath: String) {
        DocumentStore.document = document
        startActivity(Intent(this, ViewerActivity::class.java).apply {
            putExtra(ViewerActivity.EXTRA_FILE_PATH, filePath)
        })
    }

    private fun showDeleteDialog(file: File) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage(getString(R.string.delete_confirm, file.nameWithoutExtension))
            .setPositiveButton(R.string.delete) { _, _ ->
                val isLibraryFile = file.absolutePath.startsWith(filesDir.absolutePath)
                if (isLibraryFile) {
                    file.delete()
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val selection = "${MediaStore.Downloads.DATA} = ?"
                    val args = arrayOf(file.absolutePath)
                    contentResolver.delete(MediaStore.Downloads.EXTERNAL_CONTENT_URI, selection, args)
                } else {
                    file.delete()
                }
                refreshFileList()
                Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun requestStoragePermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val permsNeeded = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permsNeeded.isNotEmpty())
                ActivityCompat.requestPermissions(this, permsNeeded.toTypedArray(), REQUEST_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) refreshFileList()
    }
}
