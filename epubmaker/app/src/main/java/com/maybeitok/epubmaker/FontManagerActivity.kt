package com.maybeitok.epubmaker

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.maybeitok.epubmaker.adapter.FontAdapter
import com.maybeitok.epubmaker.databinding.ActivityFontManagerBinding
import com.maybeitok.epubmaker.model.CustomFont
import java.io.File

class FontManagerActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: android.content.Context) {
        val lang = AppPreferences(newBase).language
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }

    private lateinit var binding: ActivityFontManagerBinding
    private lateinit var adapter: FontAdapter
    private val fontListFile: File by lazy { File(filesDir, "fonts.txt") }
    private val fontDir: File by lazy { File(filesDir, "fonts") }

    companion object {
        const val EXTRA_SELECTED_FONT = "selected_font"
        const val REQUEST_FONT_FILE = 3001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFontManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fontDir.mkdirs()
        setupRecyclerView()
        setupFab()
        loadFonts()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        adapter = FontAdapter(
            onItemClick = { font -> onFontClicked(font) },
            onItemLongClick = { font -> showDeleteFontDialog(font) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fab.setOnClickListener { pickFontFile() }
    }

    private fun loadFonts() {
        val fonts = readFontsFromFile()
        adapter.submitList(fonts)
    }

    private fun onFontClicked(font: CustomFont) {
        // Show preview dialog
        val preview = TextView(this).apply {
            text = "가나다 ABC 123\nThe quick brown fox\n빠른 갈색 여우"
            textSize = 20f
            setPadding(40, 40, 40, 40)
            try {
                val tf = Typeface.createFromFile(font.filePath)
                typeface = tf
            } catch (e: Exception) {
                // use default if font can't be loaded
            }
        }
        AlertDialog.Builder(this)
            .setTitle(font.name)
            .setView(preview)
            .setPositiveButton(R.string.select) { _, _ ->
                val result = Intent()
                result.putExtra(EXTRA_SELECTED_FONT, font)
                setResult(Activity.RESULT_OK, result)
                finish()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteFontDialog(font: CustomFont) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage(getString(R.string.delete_font_confirm, font.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteFont(font)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteFont(font: CustomFont) {
        val file = File(font.filePath)
        file.delete()
        val fonts = readFontsFromFile().toMutableList()
        fonts.removeAll { it.filePath == font.filePath }
        saveFontsToFile(fonts)
        adapter.submitList(fonts)
        Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show()
    }

    private fun pickFontFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("font/ttf", "font/otf", "application/octet-stream", "application/x-font-ttf", "application/x-font-otf")
            )
        }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_FONT_FILE)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FONT_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri -> showFontNameDialog(uri) }
        }
    }

    private fun showFontNameDialog(uri: Uri) {
        val filename = getFilenameFromUri(uri) ?: "font"
        val defaultName = filename.substringBeforeLast(".")
        val input = EditText(this).apply {
            setText(defaultName)
            hint = getString(R.string.font_name)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.font_name)
            .setView(input)
            .setPositiveButton(R.string.add) { _, _ ->
                val name = input.text.toString().trim().ifEmpty { defaultName }
                importFont(uri, filename, name)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun importFont(uri: Uri, originalFilename: String, fontName: String) {
        Thread {
            try {
                val ext = originalFilename.substringAfterLast(".", "ttf")
                val safeFilename = fontName.replace(Regex("[\\\\/:*?\"<>|]"), "_") + ".$ext"
                val destFile = File(fontDir, safeFilename)
                contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val mimeType = if (ext.equals("otf", ignoreCase = true)) "font/otf" else "font/ttf"
                val font = CustomFont(name = fontName, filePath = destFile.absolutePath, mimeType = mimeType)
                val fonts = readFontsFromFile().toMutableList()
                fonts.removeAll { it.filePath == font.filePath } // prevent duplicates
                fonts.add(font)
                saveFontsToFile(fonts)
                runOnUiThread {
                    adapter.submitList(fonts)
                    Toast.makeText(this, getString(R.string.font_added, fontName), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, R.string.font_add_error, Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun getFilenameFromUri(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (idx != -1) name = it.getString(idx)
                }
            }
        }
        if (name == null) name = uri.lastPathSegment
        return name
    }

    private fun readFontsFromFile(): List<CustomFont> {
        if (!fontListFile.exists()) return emptyList()
        return try {
            fontListFile.readLines(Charsets.UTF_8).mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size >= 3) {
                    CustomFont(name = parts[0], filePath = parts[1], mimeType = parts[2])
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveFontsToFile(fonts: List<CustomFont>) {
        try {
            fontListFile.writeText(
                fonts.joinToString("\n") { "${it.name}|${it.filePath}|${it.mimeType}" },
                Charsets.UTF_8
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
