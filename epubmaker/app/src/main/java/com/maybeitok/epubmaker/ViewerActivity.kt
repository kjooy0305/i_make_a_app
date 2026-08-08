package com.maybeitok.epubmaker

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.maybeitok.epubmaker.adapter.GalleryImage
import com.maybeitok.epubmaker.adapter.ImageGalleryAdapter
import com.maybeitok.epubmaker.databinding.ActivityViewerBinding
import com.maybeitok.epubmaker.model.Bookmark
import com.maybeitok.epubmaker.model.EpubDocument
import kotlin.math.abs

class ViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewerBinding
    private lateinit var prefs: AppPreferences
    private lateinit var document: EpubDocument
    private lateinit var bookmarkManager: BookmarkManager

    private var currentChapterIndex = 0

    // Fix: track whether viewer.html has finished loading; separate from chapter load state
    private var pageLoaded = false

    // Fix: chapter to load once viewer.html is ready (avoids evaluateJavascript before page is ready)
    private var pendingChapterIndex = -1

    private var pendingScrollY: Int = 0

    // Gesture
    private lateinit var gestureDetector: GestureDetector

    // Search state
    private var searchActive = false
    private var searchQuery = ""
    private var searchMatchCount = 0
    private var allChapterMatches: List<Int> = emptyList()
    private var allChapterMatchIdx = 0
    private var searchScopeAll = false

    companion object {
        const val EXTRA_FILE_PATH = "file_path"
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = AppPreferences(newBase).language
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        prefs = AppPreferences(this)
        document = DocumentStore.document ?: run {
            finish(); return
        }

        val filePath = intent.getStringExtra(EXTRA_FILE_PATH) ?: document.filePath ?: ""
        bookmarkManager = BookmarkManager(this, filePath)

        setupWebView()
        setupGesture()
        setupSearch()
        setupBottomBar()

        // loadChapter(0) is called from onPageFinished once viewer.html is ready.
        // Calling it here just records the pending index; actual JS call happens after load.
        loadChapter(0)
    }

    // ── WebView ───────────────────────────────────────────────────────
    @Suppress("SetJavaScriptEnabled", "DEPRECATION")
    private fun setupWebView() {
        val ws: WebSettings = binding.webView.settings
        ws.javaScriptEnabled = true
        // Fix: allowFileAccess is required for WebView to load file:// image src paths.
        // Editor has this set; viewer was missing it, causing images to silently fail on API 30+.
        ws.allowFileAccess = true
        ws.allowFileAccessFromFileURLs = true
        ws.allowUniversalAccessFromFileURLs = true
        ws.domStorageEnabled = true

        binding.webView.addJavascriptInterface(ViewerBridge(), "Android")
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                pageLoaded = true
                applyCurrentSettings()
                // Fix: load the first (or any pending) chapter now that the page is ready.
                if (pendingChapterIndex >= 0) {
                    val idx = pendingChapterIndex
                    pendingChapterIndex = -1
                    loadChapter(idx)
                }
            }
        }
        binding.webView.loadUrl("file:///android_asset/viewer.html")
    }

    private fun applyCurrentSettings() {
        val sz   = prefs.viewerFontSize
        val clip = prefs.viewerPreventImageClip
        val locked = prefs.viewerScrollLocked
        binding.webView.evaluateJavascript("setFontSize($sz)", null)
        binding.webView.evaluateJavascript("setPreventImageClip($clip)", null)
        binding.webView.evaluateJavascript("setScrollEnabled(${!locked})", null)
    }

    // ── Chapter loading ───────────────────────────────────────────────
    private fun loadChapter(index: Int) {
        if (index < 0 || index >= document.chapters.size) return
        currentChapterIndex = index
        updateChapterInfo()
        supportActionBar?.subtitle = document.chapters[index].title

        // Fix: if viewer.html isn't ready yet, save the index and return.
        // onPageFinished will call loadChapter again once the page is ready.
        if (!pageLoaded) {
            pendingChapterIndex = index
            return
        }

        // Do NOT set pageLoaded = false here; that flag tracks viewer.html ready state, not chapter state.
        val chapter = document.chapters[index]
        val b64 = Base64.encodeToString(chapter.content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        binding.webView.evaluateJavascript("setContentBase64('$b64')", null)

        // Fix: handle pendingScrollY here (not in onPageFinished which only fires once for viewer.html).
        if (pendingScrollY > 0) {
            val y = pendingScrollY
            pendingScrollY = 0
            binding.webView.postDelayed({
                binding.webView.evaluateJavascript("scrollToY($y)", null)
            }, 400)
        }

        // Rerun search highlight when chapter changes while search is active.
        if (searchActive && searchQuery.isNotEmpty()) {
            binding.webView.postDelayed({ doSearchInChapter(searchQuery) }, 500)
        }
    }

    private fun updateChapterInfo() {
        val total = document.chapters.size
        val cur   = currentChapterIndex + 1
        binding.tvChapterInfo.text = getString(R.string.chapter_of, cur, total)
        binding.btnPrevChapter.isEnabled = currentChapterIndex > 0
        binding.btnNextChapter.isEnabled = currentChapterIndex < document.chapters.size - 1
    }

    // ── Gesture / tap navigation ──────────────────────────────────────
    private fun setupGesture() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
                val mode    = prefs.viewerNavMode
                val reverse = prefs.viewerReverseNav
                if (mode == "swipe_lr" && e1 != null) {
                    val dx = e2.x - e1.x
                    if (abs(dx) > 100 && abs(dx) > abs(e2.y - e1.y)) {
                        val goNext = if (!reverse) dx < 0 else dx > 0
                        if (goNext) nextChapter() else prevChapter()
                        return true
                    }
                }
                if (mode == "swipe_tb" && e1 != null) {
                    val dy = e2.y - e1.y
                    if (abs(dy) > 100 && abs(dy) > abs(e2.x - e1.x)) {
                        val goNext = if (!reverse) dy < 0 else dy > 0
                        if (goNext) nextChapter() else prevChapter()
                        return true
                    }
                }
                return false
            }
        })

        binding.webView.setOnTouchListener { _, ev ->
            gestureDetector.onTouchEvent(ev)
            false
        }
    }

    private fun handleTap(zone: String) {
        val mode    = prefs.viewerNavMode
        val reverse = prefs.viewerReverseNav
        when (mode) {
            "tap_lr" -> when (zone) {
                "left"  -> if (!reverse) prevChapter() else nextChapter()
                "right" -> if (!reverse) nextChapter() else prevChapter()
            }
            "tap_tb" -> when (zone) {
                "top"    -> if (!reverse) prevChapter() else nextChapter()
                "bottom" -> if (!reverse) nextChapter() else prevChapter()
            }
        }
    }

    private fun prevChapter() {
        if (currentChapterIndex > 0) loadChapter(currentChapterIndex - 1)
    }

    private fun nextChapter() {
        if (currentChapterIndex < document.chapters.size - 1) loadChapter(currentChapterIndex + 1)
    }

    // ── Search ────────────────────────────────────────────────────────
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener {
            searchQuery = it?.toString() ?: ""
            if (searchQuery.isEmpty()) clearSearch()
        }
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            runSearch(); true
        }
        binding.btnSearchNext.setOnClickListener {
            if (searchScopeAll) allMatchNext() else chapterMatchNext()
        }
        binding.btnSearchPrev.setOnClickListener {
            if (searchScopeAll) allMatchPrev() else chapterMatchPrev()
        }
        binding.btnSearchClose.setOnClickListener {
            binding.searchBar.visibility = View.GONE
            searchActive = false
            clearSearch()
        }
    }

    private fun showSearchBar() {
        searchActive = true
        binding.searchBar.visibility = View.VISIBLE
        binding.etSearch.requestFocus()
        AlertDialog.Builder(this)
            .setTitle(R.string.search)
            .setItems(arrayOf(
                getString(R.string.search_scope_current),
                getString(R.string.search_scope_all)
            )) { _, which ->
                searchScopeAll = which == 1
                binding.etSearch.text?.clear()
                binding.etSearch.requestFocus()
            }
            .show()
    }

    private fun runSearch() {
        if (searchQuery.isEmpty()) return
        if (searchScopeAll) searchAllChapters(searchQuery) else doSearchInChapter(searchQuery)
    }

    private fun doSearchInChapter(q: String) {
        // Fix: escape backslash first, then single-quote (order matters).
        val safe = q.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "")
        binding.webView.evaluateJavascript("findText('$safe')") { result ->
            searchMatchCount = result?.toIntOrNull() ?: 0
            runOnUiThread {
                if (searchMatchCount == 0) {
                    Toast.makeText(this, R.string.no_results, Toast.LENGTH_SHORT).show()
                }
                updateSearchCount(1, searchMatchCount)
            }
        }
    }

    private fun chapterMatchNext() {
        binding.webView.evaluateJavascript("nextMatch()", null)
    }

    private fun chapterMatchPrev() {
        binding.webView.evaluateJavascript("prevMatch()", null)
    }

    private fun searchAllChapters(q: String) {
        val matchChapters = mutableListOf<Int>()
        document.chapters.forEachIndexed { i, ch ->
            if (ch.content.contains(q, ignoreCase = true)) matchChapters.add(i)
        }
        allChapterMatches   = matchChapters
        allChapterMatchIdx  = 0
        if (matchChapters.isEmpty()) {
            Toast.makeText(this, R.string.no_results, Toast.LENGTH_SHORT).show()
            updateSearchCount(0, 0)
        } else {
            updateSearchCount(1, matchChapters.size)
            // loadChapter will call doSearchInChapter internally (searchActive == true)
            loadChapter(matchChapters[0])
        }
    }

    private fun allMatchNext() {
        if (allChapterMatches.isEmpty()) return
        allChapterMatchIdx = (allChapterMatchIdx + 1) % allChapterMatches.size
        updateSearchCount(allChapterMatchIdx + 1, allChapterMatches.size)
        // Fix: don't call doSearchInChapter again here; loadChapter handles it.
        loadChapter(allChapterMatches[allChapterMatchIdx])
    }

    private fun allMatchPrev() {
        if (allChapterMatches.isEmpty()) return
        allChapterMatchIdx = (allChapterMatchIdx - 1 + allChapterMatches.size) % allChapterMatches.size
        updateSearchCount(allChapterMatchIdx + 1, allChapterMatches.size)
        loadChapter(allChapterMatches[allChapterMatchIdx])
    }

    private fun updateSearchCount(cur: Int, total: Int) {
        binding.tvSearchCount.text = if (total > 0) "$cur/$total" else "0"
    }

    private fun clearSearch() {
        binding.webView.evaluateJavascript("clearHighlight()", null)
        searchMatchCount   = 0
        allChapterMatches  = emptyList()
        binding.tvSearchCount.text = ""
    }

    // ── Bottom bar ────────────────────────────────────────────────────
    private fun setupBottomBar() {
        binding.btnPrevChapter.setOnClickListener { prevChapter() }
        binding.btnNextChapter.setOnClickListener { nextChapter() }
    }

    // ── Menu ──────────────────────────────────────────────────────────
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home     -> { finish(); true }
            R.id.menuSearch       -> { showSearchBar(); true }
            R.id.menuBookmark     -> { addBookmark(); true }
            R.id.menuBookmarkList -> { showBookmarkList(); true }
            R.id.menuImageGallery -> { showImageGallery(); true }
            R.id.menuChapters     -> { showChapterList(); true }
            R.id.menuSettings     -> { showSettings(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ── Bookmarks ─────────────────────────────────────────────────────
    private fun addBookmark() {
        val chapter = document.chapters.getOrNull(currentChapterIndex) ?: return
        val input = EditText(this).apply {
            hint = getString(R.string.bookmark_label_hint)
            val pad = (12 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad / 2, pad, pad / 2)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.bookmark)
            .setView(input)
            .setPositiveButton(R.string.add) { _, _ ->
                binding.webView.evaluateJavascript("getScrollY()") { scrollStr ->
                    val scrollY = scrollStr?.toIntOrNull() ?: 0
                    val label = input.text.toString().trim().ifEmpty { chapter.title }
                    bookmarkManager.add(
                        Bookmark(
                            chapterIndex  = currentChapterIndex,
                            chapterTitle  = chapter.title,
                            scrollY       = scrollY,
                            label         = label
                        )
                    )
                    runOnUiThread {
                        Toast.makeText(this, R.string.bookmark_added, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showBookmarkList() {
        val bookmarks = bookmarkManager.getAll()
        if (bookmarks.isEmpty()) {
            Toast.makeText(this, R.string.no_bookmarks, Toast.LENGTH_SHORT).show()
            return
        }
        val labels = bookmarks.map { "${it.label} (${it.chapterTitle})" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(R.string.bookmark_list)
            .setItems(labels) { _, i ->
                val bm = bookmarks[i]
                pendingScrollY = bm.scrollY
                loadChapter(bm.chapterIndex)
            }
            .setNeutralButton(R.string.delete) { _, _ -> showBookmarkDelete(bookmarks) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showBookmarkDelete(bookmarks: List<Bookmark>) {
        val labels = bookmarks.map { it.label }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setItems(labels) { _, i ->
                bookmarkManager.remove(i)
                Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // ── Chapter list ──────────────────────────────────────────────────
    private fun showChapterList() {
        val titles = document.chapters.map { it.title }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(R.string.chapters)
            .setItems(titles) { _, i -> loadChapter(i) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // ── Image gallery ─────────────────────────────────────────────────
    private fun showImageGallery() {
        val images = mutableListOf<GalleryImage>()
        document.chapters.forEachIndexed { ci, ch ->
            ch.embeddedImages.forEach { img -> images.add(GalleryImage(img, ch.title, ci)) }
        }
        if (images.isEmpty()) {
            Toast.makeText(this, R.string.no_images, Toast.LENGTH_SHORT).show()
            return
        }
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_gallery, null)
        val rv = dialogView.findViewById<RecyclerView>(R.id.rvImages)
        rv.layoutManager = GridLayoutManager(this, 2)
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.image_gallery)
            .setView(dialogView)
            .setNegativeButton(R.string.cancel, null)
            .create()
        val galleryAdapter = ImageGalleryAdapter(images) { item ->
            dialog.dismiss()
            pendingScrollY = 0
            loadChapter(item.chapterIndex)
        }
        rv.adapter = galleryAdapter
        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels  * 0.92).toInt(),
            (resources.displayMetrics.heightPixels * 0.80).toInt()
        )
    }

    // ── Settings ──────────────────────────────────────────────────────
    private fun showSettings() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_viewer_settings, null)

        // Nav mode
        val navModes  = listOf("scroll", "tap_lr", "tap_tb", "swipe_lr", "swipe_tb")
        val navLabels = arrayOf(
            getString(R.string.nav_scroll),
            getString(R.string.nav_tap_lr),
            getString(R.string.nav_tap_tb),
            getString(R.string.nav_swipe_lr),
            getString(R.string.nav_swipe_tb)
        )
        val spinnerNav = view.findViewById<android.widget.Spinner>(R.id.spinnerNavMode)
        val navAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, navLabels)
        navAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNav.adapter = navAdapter
        spinnerNav.setSelection(navModes.indexOf(prefs.viewerNavMode).coerceAtLeast(0))

        // Reverse nav
        val switchReverse = view.findViewById<android.widget.Switch>(R.id.switchReverse)
        switchReverse.isChecked = prefs.viewerReverseNav

        // Font size
        val seekSize  = view.findViewById<SeekBar>(R.id.seekFontSize)
        val tvSizeVal = view.findViewById<TextView>(R.id.tvFontSizeValue)
        seekSize.max      = 32  // 12–44
        seekSize.progress = prefs.viewerFontSize - 12
        tvSizeVal.text    = "${prefs.viewerFontSize}px"
        seekSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                tvSizeVal.text = "${p + 12}px"
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        // Prevent image clip
        val switchClip = view.findViewById<android.widget.Switch>(R.id.switchPreventClip)
        switchClip.isChecked = prefs.viewerPreventImageClip

        // Scroll lock
        val switchScrollLock = view.findViewById<android.widget.Switch>(R.id.switchScrollLock)
        switchScrollLock.isChecked = prefs.viewerScrollLocked

        AlertDialog.Builder(this)
            .setTitle(R.string.viewer_settings)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                val modeIdx = spinnerNav.selectedItemPosition
                prefs.viewerNavMode          = navModes.getOrElse(modeIdx) { "scroll" }
                prefs.viewerReverseNav       = switchReverse.isChecked
                prefs.viewerFontSize         = seekSize.progress + 12
                prefs.viewerPreventImageClip = switchClip.isChecked
                prefs.viewerScrollLocked     = switchScrollLock.isChecked
                applyCurrentSettings()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // ── JS Bridge ─────────────────────────────────────────────────────
    inner class ViewerBridge {
        @JavascriptInterface
        fun onTap(zone: String) {
            runOnUiThread { handleTap(zone) }
        }
    }
}
