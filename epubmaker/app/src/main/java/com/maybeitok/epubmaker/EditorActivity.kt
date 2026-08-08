package com.maybeitok.epubmaker

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Base64
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.maybeitok.epubmaker.adapter.ChapterAdapter
import com.maybeitok.epubmaker.adapter.GalleryImage
import com.maybeitok.epubmaker.adapter.ImageGalleryAdapter
import com.maybeitok.epubmaker.databinding.ActivityEditorBinding
import com.maybeitok.epubmaker.databinding.DialogImageGalleryBinding
import com.maybeitok.epubmaker.epub.EpubWriter
import com.maybeitok.epubmaker.model.CustomFont
import com.maybeitok.epubmaker.model.EpubChapter
import com.maybeitok.epubmaker.model.EpubDocument
import com.maybeitok.epubmaker.model.ImageData
import org.json.JSONArray
import java.io.File

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private lateinit var document: EpubDocument
    private lateinit var chapterAdapter: ChapterAdapter
    private var currentChapterIndex = 0
    private var pageLoaded = false
    private var pendingScrollFilename: String? = null
    private var currentEditorFont: String = ""

    private val imageCacheDir: File by lazy {
        File(cacheDir, "epub_images")
    }

    private val autoSaveHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val autoSaveRunnable = object : Runnable {
        override fun run() {
            performAutoSave()
            autoSaveHandler.postDelayed(this, 60_000L)
        }
    }

    companion object {
        const val REQUEST_IMAGE_PICK = 2001
        const val REQUEST_FONT_MANAGER = 2002
        const val REQUEST_MEDIA_PERMISSION = 2003
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        val lang = AppPreferences(newBase).language
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        document = DocumentStore.document ?: EpubDocument()

        setupToolbar()
        setupWebView()
        setupChapterDrawer()
        setupFormattingToolbar()
        setupBottomBar()
    }

    private fun setupToolbar() {
        binding.toolbar.title = document.title
        binding.toolbar.setNavigationOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(binding.navDrawer)) {
                binding.drawerLayout.closeDrawer(binding.navDrawer)
            } else {
                binding.drawerLayout.openDrawer(binding.navDrawer)
            }
        }
    }

    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = true
        }
        binding.webView.addJavascriptInterface(EditorBridge(), "Android")
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                pageLoaded = true
                loadCurrentChapter()
                loadCustomFonts()
                if (currentEditorFont.isNotEmpty()) {
                    val safe = currentEditorFont.replace("'", "\\'")
                    binding.webView.evaluateJavascript("setFontName('$safe')", null)
                }
                pendingScrollFilename?.let { fname ->
                    pendingScrollFilename = null
                    binding.webView.postDelayed({
                        val safe = fname.replace("'", "\\'")
                        binding.webView.evaluateJavascript("scrollToImageByFilename('$safe')", null)
                    }, 400)
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return true // Prevent navigation
            }
        }
        binding.webView.loadUrl("file:///android_asset/editor.html")
    }

    private fun setupChapterDrawer() {
        chapterAdapter = ChapterAdapter(
            onItemClick = { index ->
                switchToChapter(index)
                binding.drawerLayout.closeDrawer(binding.navDrawer)
            },
            onItemLongClick = { index ->
                binding.drawerLayout.closeDrawer(binding.navDrawer)
                showEditChapterTitleDialog(index)
            }
        )
        binding.chapterRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chapterRecyclerView.adapter = chapterAdapter
        refreshChapterList()

        binding.etChapterSearch.addTextChangedListener { editable ->
            chapterAdapter.filter(editable?.toString() ?: "")
        }
    }

    private fun showEditChapterTitleDialog(index: Int) {
        val input = android.widget.EditText(this)
        input.setText(document.chapters[index].title)
        input.selectAll()
        val padding = (16 * resources.displayMetrics.density).toInt()
        input.setPadding(padding, padding / 2, padding, padding / 2)
        AlertDialog.Builder(this)
            .setTitle("챕터 이름 변경")
            .setView(input)
            .setPositiveButton("변경") { _, _ ->
                val newTitle = input.text.toString().trim()
                    .ifEmpty { document.chapters[index].title }
                document.chapters[index].title = newTitle
                refreshChapterList()
                if (index == currentChapterIndex) binding.toolbar.subtitle = newTitle
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setupFormattingToolbar() {
        // Bold button: 굵은 B
        val boldSpan = SpannableString("B")
        boldSpan.setSpan(StyleSpan(Typeface.BOLD), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.btnBold.text = boldSpan

        // Underline button: 밑줄 U
        val ulSpan = SpannableString("U")
        ulSpan.setSpan(UnderlineSpan(), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ulSpan.setSpan(StyleSpan(Typeface.BOLD), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.btnUnderline.text = ulSpan

        binding.btnBold.setOnClickListener {
            binding.webView.evaluateJavascript("bold()", null)
            it.isSelected = !it.isSelected
        }
        binding.btnItalic.setOnClickListener {
            binding.webView.evaluateJavascript("italic()", null)
            it.isSelected = !it.isSelected
        }
        binding.btnUnderline.setOnClickListener {
            binding.webView.evaluateJavascript("underline()", null)
            it.isSelected = !it.isSelected
        }
        binding.btnAlignLeft.setOnClickListener {
            binding.webView.evaluateJavascript("alignLeft()", null)
        }
        binding.btnAlignCenter.setOnClickListener {
            binding.webView.evaluateJavascript("alignCenter()", null)
        }
        binding.btnAlignRight.setOnClickListener {
            binding.webView.evaluateJavascript("alignRight()", null)
        }
        binding.btnFontSize.setOnClickListener { showFontSizeDialog() }
        binding.btnTextColor.setOnClickListener { showTextColorDialog() }
    }

    private fun showFontSizeDialog() {
        val sizes = arrayOf("10px", "12px", "14px", "16px", "18px", "20px", "24px", "28px", "32px", "36px", "48px")
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_size))
            .setItems(sizes) { _, i ->
                val px = sizes[i].removeSuffix("px").toInt()
                binding.webView.evaluateJavascript("setSelectionFontSize($px)", null)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showTextColorDialog() {
        val colors = listOf(
            "#000000", "#1A1A1A", "#444444", "#888888",
            "#FFFFFF", "#E53935", "#D81B60", "#8E24AA",
            "#3949AB", "#1E88E5", "#039BE5", "#00ACC1",
            "#43A047", "#7CB342", "#F4511E", "#FB8C00"
        )
        val names = listOf(
            "검정", "진회색", "회색", "연회색",
            "흰색", "빨강", "분홍", "보라",
            "남색", "파랑", "하늘", "청록",
            "초록", "연두", "주황", "황색"
        )
        val density = resources.displayMetrics.density
        val sz = (52 * density).toInt()
        val margin = (4 * density).toInt()

        val grid = androidx.recyclerview.widget.RecyclerView(this).apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 4)
            setPadding(16, 8, 16, 8)
        }

        var dialog: AlertDialog? = null
        val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, t: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
                val v = android.widget.FrameLayout(parent.context).apply {
                    layoutParams = androidx.recyclerview.widget.RecyclerView.LayoutParams(sz, sz)
                        .also { it.setMargins(margin, margin, margin, margin) }
                }
                return object : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {}
            }
            override fun onBindViewHolder(h: androidx.recyclerview.widget.RecyclerView.ViewHolder, pos: Int) {
                val color = colors[pos]
                (h.itemView as android.widget.FrameLayout).apply {
                    setBackgroundColor(android.graphics.Color.parseColor(color))
                    // 흰색은 테두리
                    if (color == "#FFFFFF") foreground = android.graphics.drawable.GradientDrawable().apply {
                        setStroke((1 * density).toInt(), android.graphics.Color.LTGRAY)
                    }
                    setOnClickListener {
                        binding.webView.evaluateJavascript("setTextColor('$color')", null)
                        dialog?.dismiss()
                    }
                }
            }
            override fun getItemCount() = colors.size
        }
        grid.adapter = adapter

        dialog = AlertDialog.Builder(this)
            .setTitle("글자색")
            .setView(grid)
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog?.show()
    }

    private fun setupBottomBar() {
        binding.btnPrevChapter.setOnClickListener {
            if (currentChapterIndex > 0) switchToChapter(currentChapterIndex - 1)
        }
        binding.btnNextChapter.setOnClickListener {
            if (currentChapterIndex < document.chapters.lastIndex) switchToChapter(currentChapterIndex + 1)
        }
        binding.btnAddChapter.setOnClickListener { showAddChapterDialog() }
        binding.btnDeleteChapter.setOnClickListener { showDeleteChapterDialog() }
        binding.btnInsertImage.setOnClickListener { pickImage() }
        binding.btnFont.setOnClickListener { showFontDialog() }
        binding.btnToc.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(binding.navDrawer)) {
                binding.drawerLayout.closeDrawer(binding.navDrawer)
            } else {
                binding.drawerLayout.openDrawer(binding.navDrawer)
            }
        }
        binding.btnImageGallery.setOnClickListener { showImageGallery() }
        binding.btnSave.setOnClickListener { saveDocument() }
    }

    private fun showImageGallery() {
        val allImages = mutableListOf<GalleryImage>()
        for ((idx, chapter) in document.chapters.withIndex()) {
            for (img in chapter.embeddedImages) {
                allImages.add(GalleryImage(imageData = img, chapterTitle = chapter.title, chapterIndex = idx))
            }
        }

        val dialogBinding = DialogImageGalleryBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setView(dialogBinding.root)
            .setNegativeButton("닫기", null)
            .create()

        if (allImages.isEmpty()) {
            dialogBinding.tvNoImages.visibility = android.view.View.VISIBLE
            dialogBinding.rvImages.visibility = android.view.View.GONE
        } else {
            dialogBinding.tvNoImages.visibility = android.view.View.GONE
            val adapter = ImageGalleryAdapter(allImages) { selected ->
                dialog.dismiss()
                val fname = selected.imageData.filename
                if (selected.chapterIndex != currentChapterIndex) {
                    // 다른 챕터: 챕터 전환 후 스크롤
                    pendingScrollFilename = fname
                    switchToChapter(selected.chapterIndex)
                } else {
                    // 현재 챕터: 바로 스크롤
                    val safe = fname.replace("'", "\\'")
                    binding.webView.evaluateJavascript("scrollToImageByFilename('$safe')", null)
                }
            }
            dialogBinding.rvImages.layoutManager =
                androidx.recyclerview.widget.GridLayoutManager(this, 2)
            dialogBinding.rvImages.adapter = adapter
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.75).toInt()
        )
    }

    private fun showImageContextMenu(src: String, filename: String) {
        val displayName = filename.substringAfterLast("__").ifEmpty { filename }
        val items = arrayOf("이미지만 보기", "다운로드", "복사", "잘라내기 (복사+삭제)", "삭제")
        AlertDialog.Builder(this)
            .setTitle(displayName.ifEmpty { "이미지" })
            .setItems(items) { _, i ->
                val safe = filename.replace("'", "\\'")
                when (i) {
                    0 -> viewImageFullscreen(src, filename)
                    1 -> downloadImageToDownloads(src, filename)
                    2 -> copyImageToClipboard(src, filename)
                    3 -> binding.webView.evaluateJavascript("cutImageByFilename('$safe')", null)
                    4 -> binding.webView.evaluateJavascript("deleteImageByFilename('$safe')", null)
                }
            }.show()
    }

    private fun viewImageFullscreen(src: String, filename: String) {
        Thread {
            try {
                val bitmap = when {
                    src.startsWith("file://") -> {
                        val path = src.removePrefix("file://")
                        BitmapFactory.decodeFile(path)
                    }
                    src.startsWith("data:") -> {
                        val b64 = src.substringAfter("base64,")
                        val bytes = Base64.decode(b64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                    else -> null
                } ?: run {
                    runOnUiThread { Toast.makeText(this, "이미지를 표시할 수 없습니다", Toast.LENGTH_SHORT).show() }
                    return@Thread
                }
                runOnUiThread {
                    val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                    val iv = ImageView(this).apply {
                        setImageBitmap(bitmap)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        setBackgroundColor(Color.BLACK)
                    }
                    dialog.setContentView(iv)
                    iv.setOnClickListener { dialog.dismiss() }
                    dialog.show()
                }
            } catch (_: Exception) {}
        }.start()
    }

    private fun downloadImageToDownloads(src: String, filename: String) {
        Thread {
            try {
                val bytes = imageSourceToBytes(src) ?: run {
                    runOnUiThread { Toast.makeText(this, "이미지를 읽을 수 없습니다", Toast.LENGTH_SHORT).show() }
                    return@Thread
                }
                val safeFilename = filename.ifEmpty { "image_${System.currentTimeMillis()}.jpg" }
                val mime = guessMimeTypeFromFilename(safeFilename)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, safeFilename)
                        put(MediaStore.Downloads.MIME_TYPE, mime)
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }
                    val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    if (uri != null) {
                        contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                        values.clear()
                        values.put(MediaStore.Downloads.IS_PENDING, 0)
                        contentResolver.update(uri, values, null, null)
                        runOnUiThread { Toast.makeText(this, "다운로드 완료: $safeFilename", Toast.LENGTH_SHORT).show() }
                    }
                } else {
                    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    File(dir, safeFilename).writeBytes(bytes)
                    runOnUiThread { Toast.makeText(this, "다운로드 완료: $safeFilename", Toast.LENGTH_SHORT).show() }
                }
            } catch (_: Exception) {
                runOnUiThread { Toast.makeText(this, "다운로드 실패", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }

    private fun copyImageToClipboard(src: String, filename: String) {
        Thread {
            try {
                val bytes = imageSourceToBytes(src) ?: run {
                    runOnUiThread { Toast.makeText(this, "이미지를 읽을 수 없습니다", Toast.LENGTH_SHORT).show() }
                    return@Thread
                }
                val safeFilename = filename.ifEmpty { "image_${System.currentTimeMillis()}.jpg" }
                val tmpFile = File(cacheDir, "clipboard_$safeFilename")
                tmpFile.writeBytes(bytes)
                val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", tmpFile)
                val clip = ClipData.newUri(contentResolver, safeFilename, uri)
                val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(clip)
                runOnUiThread { Toast.makeText(this, "클립보드에 복사됨", Toast.LENGTH_SHORT).show() }
            } catch (_: Exception) {
                runOnUiThread { Toast.makeText(this, "복사 실패", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }

    private fun imageSourceToBytes(src: String): ByteArray? {
        return when {
            src.startsWith("file://") -> {
                val f = File(src.removePrefix("file://"))
                if (f.exists()) f.readBytes() else null
            }
            src.startsWith("data:") -> {
                val b64 = src.substringAfter("base64,")
                Base64.decode(b64, Base64.DEFAULT)
            }
            else -> null
        }
    }

    private fun guessMimeTypeFromFilename(filename: String): String {
        return when (filename.substringAfterLast('.').lowercase()) {
            "png"  -> "image/png"
            "gif"  -> "image/gif"
            "webp" -> "image/webp"
            "svg"  -> "image/svg+xml"
            "bmp"  -> "image/bmp"
            else   -> "image/jpeg"
        }
    }

    private fun refreshChapterList() {
        chapterAdapter.submitList(document.chapters, currentChapterIndex)
    }

    private fun loadCurrentChapter() {
        if (!pageLoaded) return
        val chapter = document.chapters.getOrNull(currentChapterIndex) ?: return
        val b64 = Base64.encodeToString(chapter.content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        binding.webView.evaluateJavascript("setContentBase64('$b64')", null)
        binding.toolbar.subtitle = chapter.title
        chapterAdapter.setSelectedIndex(currentChapterIndex)
    }

    private fun loadCustomFonts() {
        for (font in document.customFonts) {
            try {
                val fontFile = File(font.filePath)
                if (fontFile.exists()) {
                    val bytes = fontFile.readBytes()
                    val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    val safeName = font.name.replace("'", "\\'")
                    val safeMime = font.mimeType.replace("'", "\\'")
                    binding.webView.evaluateJavascript(
                        "addCustomFont('$safeName', '$b64', '$safeMime')", null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun switchToChapter(index: Int) {
        if (index == currentChapterIndex) return
        saveCurrentChapterContent {
            currentChapterIndex = index
            loadCurrentChapter()
        }
    }

    private fun saveCurrentChapterContent(callback: () -> Unit) {
        if (!pageLoaded) {
            callback()
            return
        }
        binding.webView.evaluateJavascript("getContent()") { result ->
            val html = decodeJsString(result)
            document.chapters.getOrNull(currentChapterIndex)?.content = html
            runOnUiThread { callback() }
        }
    }

    private fun decodeJsString(jsResult: String?): String {
        if (jsResult == null || jsResult == "null") return ""
        return try {
            JSONArray("[$jsResult]").getString(0)
        } catch (e: Exception) {
            jsResult.removeSurrounding("\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\u003C", "<")
                .replace("\\u003E", ">")
                .replace("\\u0026", "&")
        }
    }

    private fun showAddChapterDialog() {
        val input = android.widget.EditText(this)
        input.hint = getString(R.string.chapter_title)
        val num = document.chapters.size + 1
        input.setText(getString(R.string.chapter_n, num))
        AlertDialog.Builder(this)
            .setTitle(R.string.add_chapter)
            .setView(input)
            .setPositiveButton(R.string.add) { _, _ ->
                val title = input.text.toString().trim().ifEmpty { getString(R.string.chapter_n, num) }
                saveCurrentChapterContent {
                    document.chapters.add(EpubChapter(title = title))
                    currentChapterIndex = document.chapters.lastIndex
                    refreshChapterList()
                    loadCurrentChapter()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteChapterDialog() {
        if (document.chapters.size <= 1) {
            Toast.makeText(this, R.string.cannot_delete_last_chapter, Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_chapter)
            .setMessage(
                getString(
                    R.string.delete_chapter_confirm,
                    document.chapters[currentChapterIndex].title
                )
            )
            .setPositiveButton(R.string.delete) { _, _ ->
                document.chapters.removeAt(currentChapterIndex)
                currentChapterIndex = (currentChapterIndex - 1).coerceAtLeast(0)
                refreshChapterList()
                loadCurrentChapter()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    REQUEST_MEDIA_PERMISSION
                )
                return
            }
        }
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    // displayName to CSS font name
    private val builtinFonts = listOf(
        "Serif" to "Serif",
        "Sans-serif" to "sans-serif",
        "Monospace" to "monospace",
        "Cursive" to "cursive",
        "그리운X국한박 오춘기 김작가" to "GriunXHangeul_OCHUNGI.KIM-Rg"
    )

    // CSS font name to assets path (for bundled fonts)
    private val builtinFontAssets = mapOf(
        "GriunXHangeul_OCHUNGI.KIM-Rg" to "fonts/GriunXHangeul_OCHUNGI.KIM-Rg.ttf"
    )

    private fun showFontDialog() {
        val bgColor = Color.parseColor("#1a1a2e")
        val textColor = Color.WHITE
        val secondaryColor = Color.parseColor("#AAAACC")

        val displayNames = mutableListOf<String>()
        val cssNames = mutableListOf<String>()

        for ((display, css) in builtinFonts) {
            displayNames.add(display)
            cssNames.add(css)
        }
        for (font in document.customFonts) {
            displayNames.add(font.name)
            cssNames.add(font.name)
        }
        displayNames.add(getString(R.string.manage_fonts))
        cssNames.add("__manage__")

        val listView = ListView(this)
        listView.setBackgroundColor(bgColor)

        val adapter = object : android.widget.ArrayAdapter<String>(
            this, android.R.layout.simple_list_item_1, displayNames
        ) {
            override fun getView(pos: Int, convertView: android.view.View?, parent: ViewGroup): android.view.View {
                val view = super.getView(pos, convertView, parent) as TextView
                val isBuiltin = pos < builtinFonts.size
                val isManage = cssNames[pos] == "__manage__"
                view.setTextColor(if (isManage) secondaryColor else textColor)
                view.textSize = 15f
                view.setPadding(48, 28, 48, 28)
                view.setBackgroundColor(bgColor)
                return view
            }
        }
        listView.adapter = adapter
        listView.divider = null

        val dialog = AlertDialog.Builder(this)
            .setTitle(null)
            .setView(listView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        listView.setOnItemClickListener { _, _, pos, _ ->
            when (val css = cssNames[pos]) {
                "__manage__" -> {
                    dialog.dismiss()
                    val intent = Intent(this, FontManagerActivity::class.java)
                    @Suppress("DEPRECATION")
                    startActivityForResult(intent, REQUEST_FONT_MANAGER)
                }
                else -> {
                    val safe = css.replace("'", "\\'")
                    // Load bundled font from assets if available
                    val assetPath = builtinFontAssets[css]
                    if (assetPath != null) {
                        Thread {
                            try {
                                val bytes = assets.open(assetPath).readBytes()
                                val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                runOnUiThread {
                                    binding.webView.evaluateJavascript(
                                        "addCustomFont('$safe', '$b64', 'font/ttf')", null
                                    )
                                    currentEditorFont = css
                                    binding.webView.evaluateJavascript("setFontName('$safe')", null)
                                }
                            } catch (_: Exception) {
                                runOnUiThread {
                                    currentEditorFont = css
                                    binding.webView.evaluateJavascript("setFontName('$safe')", null)
                                }
                            }
                        }.start()
                    } else {
                        currentEditorFont = css
                        binding.webView.evaluateJavascript("setFontName('$safe')", null)
                    }
                    dialog.dismiss()
                }
            }
        }

        listView.setOnItemLongClickListener { _, _, pos, _ ->
            val customIdx = pos - builtinFonts.size
            if (customIdx >= 0 && customIdx < document.customFonts.size) {
                dialog.dismiss()
                showFontEditDialog(customIdx)
            }
            true
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showFontEditDialog(index: Int) {
        val font = document.customFonts.getOrNull(index) ?: return
        AlertDialog.Builder(this)
            .setTitle(font.name)
            .setItems(arrayOf("이름 변경", "삭제")) { _, which ->
                when (which) {
                    0 -> {
                        val input = android.widget.EditText(this)
                        input.setText(font.name)
                        input.selectAll()
                        val pad = (16 * resources.displayMetrics.density).toInt()
                        input.setPadding(pad, pad / 2, pad, pad / 2)
                        AlertDialog.Builder(this)
                            .setTitle("이름 변경")
                            .setView(input)
                            .setPositiveButton("변경") { _, _ ->
                                val newName = input.text.toString().trim().ifEmpty { font.name }
                                document.customFonts[index] = font.copy(name = newName)
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .show()
                    }
                    1 -> {
                        document.customFonts.removeAt(index)
                        Toast.makeText(this, "글꼴이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun saveDocument() {
        saveCurrentChapterContent {
            Thread {
                try {
                    val safeTitle = document.title
                        .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                        .trim().ifEmpty { "epub" }
                    val fileName = "$safeTitle.epub"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        saveViaMediaStore(fileName)
                    } else {
                        saveViaFile(fileName)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this, R.string.save_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }

    private fun saveViaFile(fileName: String) {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "EpubMaker"
        ).also { it.mkdirs() }
        val file = if (document.filePath != null) File(document.filePath!!) else File(dir, fileName)
        EpubWriter.write(document, file)
        document.filePath = file.absolutePath
        runOnUiThread {
            Snackbar.make(binding.root, getString(R.string.saved, file.name), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun saveViaMediaStore(fileName: String) {
        val resolver = contentResolver

        // 기존 파일이 있으면 덮어쓰기
        val existingFilePath = document.filePath
        if (existingFilePath != null) {
            val existingFile = File(existingFilePath)
            if (existingFile.exists()) {
                try {
                    EpubWriter.write(document, existingFile)
                    runOnUiThread {
                        Snackbar.make(binding.root, getString(R.string.saved, existingFile.name), Snackbar.LENGTH_SHORT).show()
                    }
                    return
                } catch (_: SecurityException) { }
            }
        }

        // 새 파일 생성
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/epub+zip")
            put(MediaStore.Downloads.RELATIVE_PATH, "Download/EpubMaker")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: run {
            runOnUiThread { Toast.makeText(this, R.string.save_error, Toast.LENGTH_SHORT).show() }
            return
        }

        resolver.openOutputStream(uri)?.use { stream ->
            EpubWriter.write(document, stream)
        }

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        // 실제 파일 경로 저장
        resolver.query(uri, arrayOf(MediaStore.Downloads.DATA), null, null, null)
            ?.use { c -> if (c.moveToFirst()) document.filePath = c.getString(0) }

        runOnUiThread {
            Snackbar.make(binding.root, getString(R.string.saved, fileName), Snackbar.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_PICK -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri -> insertImageFromUri(uri) }
                }
            }
            REQUEST_FONT_MANAGER -> {
                if (resultCode == Activity.RESULT_OK) {
                    val font = data?.getSerializableExtra(FontManagerActivity.EXTRA_SELECTED_FONT) as? CustomFont
                    if (font != null) {
                        if (!document.customFonts.any { it.filePath == font.filePath }) {
                            document.customFonts.add(font)
                        }
                        loadCustomFonts()
                        val safeName = font.name.replace("'", "\\'")
                        currentEditorFont = font.name
                        binding.webView.evaluateJavascript("setFontName('$safeName')", null)
                    }
                }
            }
        }
    }

    private fun insertImageFromUri(uri: Uri) {
        Thread {
            try {
                val bytes = contentResolver.openInputStream(uri)?.readBytes() ?: return@Thread
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val ext = mimeType.substringAfter("/").replace("+xml", "")
                val filename = "img_${System.currentTimeMillis()}.$ext"
                val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

                val imageData = ImageData(filename = filename, base64 = b64, mimeType = mimeType)
                document.chapters.getOrNull(currentChapterIndex)?.embeddedImages?.add(imageData)

                val safeMime = mimeType.replace("'", "\\'")
                val safeFilename = filename.replace("'", "\\'")
                runOnUiThread {
                    binding.webView.evaluateJavascript(
                        "insertImageBase64('$b64', '$safeMime', '$safeFilename')", null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_MEDIA_PERMISSION && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            pickImage()
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(binding.navDrawer)) {
            binding.drawerLayout.closeDrawer(binding.navDrawer)
        } else {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        autoSaveHandler.postDelayed(autoSaveRunnable, 60_000L)
    }

    override fun onStop() {
        super.onStop()
        autoSaveHandler.removeCallbacks(autoSaveRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Auto-save current content to memory (not disk)
        if (pageLoaded) {
            binding.webView.evaluateJavascript("getContent()") { result ->
                val html = decodeJsString(result)
                document.chapters.getOrNull(currentChapterIndex)?.content = html
            }
        }
    }

    private fun performAutoSave() {
        if (!pageLoaded) return
        binding.webView.evaluateJavascript("getContent()") { result ->
            val html = decodeJsString(result)
            document.chapters.getOrNull(currentChapterIndex)?.content = html
            Thread {
                try {
                    val dir = File(filesDir, "autosaves").also { it.mkdirs() }
                    com.maybeitok.epubmaker.epub.EpubWriter.write(document, File(dir, "${document.id}.epub"))
                } catch (_: Exception) {}
            }.start()
        }
    }

    inner class EditorBridge {
        @JavascriptInterface
        fun onContentChanged(html: String) {
            document.chapters.getOrNull(currentChapterIndex)?.content = html
        }

        @JavascriptInterface
        fun onImageInserted(name: String, base64: String, mime: String) {
            val chapter = document.chapters.getOrNull(currentChapterIndex) ?: return
            if (chapter.embeddedImages.none { it.filename == name }) {
                chapter.embeddedImages.add(ImageData(filename = name, base64 = base64, mimeType = mime))
            }
        }

        @JavascriptInterface
        fun onSelectionChanged(bold: Boolean, italic: Boolean, underline: Boolean) {
            runOnUiThread {
                binding.btnBold.isSelected = bold
                binding.btnItalic.isSelected = italic
                binding.btnUnderline.isSelected = underline
            }
        }

        @JavascriptInterface
        fun onImageLongPress(src: String, filename: String) {
            runOnUiThread { showImageContextMenu(src, filename) }
        }

        @JavascriptInterface
        fun onImageCut(src: String, filename: String) {
            copyImageToClipboard(src, filename)
        }
    }
}
