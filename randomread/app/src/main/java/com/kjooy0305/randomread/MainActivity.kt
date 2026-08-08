package com.kjooy0305.randomread

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kjooy0305.randomread.databinding.ActivityMainBinding
import java.text.Collator
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var repo: BookRepository
    private lateinit var adapter: BookAdapter
    private val books = mutableListOf<BookItem>()

    // 파일 다중 선택
    private val filePicker = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> if (uris.isNotEmpty()) addFiles(uris) }

    // 텍스트 파일 내보내기
    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri -> uri?.let { exportTo(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)

        repo = BookRepository(this)
        books.addAll(repo.load())

        adapter = BookAdapter { book -> confirmDelete(book) }
        b.recyclerView.layoutManager = LinearLayoutManager(this)
        b.recyclerView.adapter = adapter

        b.btnAdd.setOnClickListener {
            filePicker.launch(arrayOf("*/*"))
        }
        b.btnRandom.setOnClickListener { pickRandom() }
        b.btnExport.setOnClickListener {
            if (books.isEmpty()) toast("목록이 비어 있습니다")
            else exportLauncher.launch("randomread_목록.txt")
        }

        refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_clear) {
            confirmClearAll(); return true
        }
        return super.onOptionsItemSelected(item)
    }

    // ── 파일 처리 ────────────────────────────────────────

    private fun addFiles(uris: List<Uri>) {
        var added = 0
        uris.forEach { uri ->
            val raw = getFileName(uri) ?: return@forEach
            val type = when {
                raw.endsWith(".epub", ignoreCase = true) -> BookType.EPUB
                raw.endsWith(".txt",  ignoreCase = true) -> BookType.TXT
                else -> return@forEach
            }
            // 확장자 제거 → _ + 를 공백으로
            val name = raw.substringBeforeLast(".")
                .replace('_', ' ')
                .replace('+', ' ')
                .trim()
            if (name.isNotEmpty() && books.none { it.name == name && it.type == type }) {
                books.add(BookItem(name, type))
                added++
            }
        }
        if (added > 0) {
            repo.save(books)
            refresh()
            toast("${added}개 추가됨")
        } else {
            toast("추가할 파일이 없습니다 (이미 있거나 미지원 형식)")
        }
    }

    private fun getFileName(uri: Uri): String? =
        contentResolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (c.moveToFirst() && idx >= 0) c.getString(idx) else null
        }

    // ── 목록 정렬 & 갱신 ─────────────────────────────────

    private fun sorted(type: BookType): List<BookItem> {
        val col = Collator.getInstance(Locale.KOREAN)
        return books.filter { it.type == type }
            .sortedWith { a, b -> col.compare(a.name, b.name) }
    }

    private fun refresh() {
        val epubs = sorted(BookType.EPUB)
        val txts  = sorted(BookType.TXT)

        val list = mutableListOf<ListItem>()
        var idx = 1
        if (epubs.isNotEmpty()) {
            list += ListItem.Header(BookType.EPUB, epubs.size)
            epubs.forEach { list += ListItem.Book(it, idx++) }
        }
        if (txts.isNotEmpty()) {
            list += ListItem.Header(BookType.TXT, txts.size)
            txts.forEach { list += ListItem.Book(it, idx++) }
        }

        adapter.submit(list)

        b.tvStats.text = when {
            books.isEmpty() -> "목록이 비어 있습니다"
            else -> "EPUB ${epubs.size}${BookType.EPUB.unit}  ·  TXT ${txts.size}${BookType.TXT.unit}  (총 ${books.size}개)"
        }
        b.layoutEmpty.visibility  = if (books.isEmpty()) View.VISIBLE else View.GONE
        b.recyclerView.visibility = if (books.isEmpty()) View.GONE   else View.VISIBLE
    }

    // ── 랜덤 뽑기 ────────────────────────────────────────

    private fun pickRandom() {
        if (books.isEmpty()) { toast("목록이 비어 있습니다"); return }

        val pick = books.random()
        val typeColor = if (pick.type == BookType.EPUB) 0xFFA78BFA.toInt() else 0xFF34D399.toInt()

        val dialogView = layoutInflater.inflate(R.layout.dialog_random_result, null)
        dialogView.findViewById<TextView>(R.id.tvResultName).text = pick.name
        dialogView.findViewById<TextView>(R.id.tvResultType).apply {
            text = pick.type.label
            setTextColor(typeColor)
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("확인", null)
            .setNeutralButton("다시 뽑기") { _, _ -> pickRandom() }
            .show()
    }

    // ── 삭제 ────────────────────────────────────────────

    private fun confirmDelete(book: BookItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle("삭제")
            .setMessage("'${book.name}'을(를) 삭제할까요?")
            .setPositiveButton("삭제") { _, _ ->
                books.remove(book); repo.save(books); refresh()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun confirmClearAll() {
        if (books.isEmpty()) { toast("목록이 비어 있습니다"); return }
        MaterialAlertDialogBuilder(this)
            .setTitle("전체 삭제")
            .setMessage("목록을 모두 지울까요?")
            .setPositiveButton("삭제") { _, _ ->
                books.clear(); repo.clear(); refresh()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // ── 내보내기 ─────────────────────────────────────────

    private fun exportTo(uri: Uri) {
        try {
            val epubs = sorted(BookType.EPUB)
            val txts  = sorted(BookType.TXT)
            val sb = StringBuilder()
            if (epubs.isNotEmpty()) {
                sb.appendLine("=== EPUB (${epubs.size}권) ===")
                epubs.forEachIndexed { i, it -> sb.appendLine("${i + 1}. ${it.name}") }
                sb.appendLine()
            }
            if (txts.isNotEmpty()) {
                sb.appendLine("=== TXT (${txts.size}개) ===")
                txts.forEachIndexed { i, it -> sb.appendLine("${i + 1}. ${it.name}") }
            }
            contentResolver.openOutputStream(uri)?.use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
            toast("내보내기 완료")
        } catch (e: Exception) {
            toast("내보내기 실패: ${e.message}")
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
