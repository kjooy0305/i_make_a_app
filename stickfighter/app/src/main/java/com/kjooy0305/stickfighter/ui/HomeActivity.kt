package com.kjooy0305.stickfighter.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.kjooy0305.stickfighter.BuildConfig
import com.kjooy0305.stickfighter.R
import com.kjooy0305.stickfighter.data.Repository
import com.kjooy0305.stickfighter.data.Sfa
import com.kjooy0305.stickfighter.databinding.ActivityHomeBinding
import com.kjooy0305.stickfighter.model.Animation
import com.kjooy0305.stickfighter.model.Figure
import com.kjooy0305.stickfighter.model.Frame
import com.kjooy0305.stickfighter.model.Templates
import java.util.UUID

class HomeActivity : AppCompatActivity() {

    private lateinit var b: ActivityHomeBinding
    private lateinit var repo: Repository
    private lateinit var adapter: ItemAdapter
    private var figuresTab = false
    private var exporting: Animation? = null

    private val importPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importFrom(uri)
    }

    private val exportPicker = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        val animation = exporting
        exporting = null
        if (uri == null || animation == null) return@registerForActivityResult
        val ok = runCatching { contentResolver.openOutputStream(uri)!!.use { Sfa.write(animation, it) } }.isSuccess
        Toast.makeText(this, if (ok) R.string.backup_saved else R.string.backup_failed, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(b.root)
        repo = Repository(filesDir)

        adapter = ItemAdapter(onOpen = { open(it) }, onMenu = { row, anchor -> showMenu(row, anchor) })
        b.list.layoutManager = LinearLayoutManager(this)
        b.list.adapter = adapter

        b.tabs.addTab(b.tabs.newTab().setText(R.string.tab_animations))
        b.tabs.addTab(b.tabs.newTab().setText(R.string.tab_figures))
        b.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                figuresTab = tab.position == 1
                b.fab.contentDescription = getString(if (figuresTab) R.string.new_figure else R.string.new_animation)
                refresh()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        b.fab.setOnClickListener { if (figuresTab) createFigure() else createAnimation() }
        b.menuButton.setOnClickListener { showTopMenu(it) }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) importFrom(intent.data!!)
    }

    private fun refresh() {
        val rows = if (figuresTab) {
            repo.figures().map { Row(it.id.toString(), it.name, "", Thumbnails.figure(it)) }
        } else {
            repo.animations().map {
                Row(it.id, it.title.ifEmpty { getString(R.string.figure_title_untitled) },
                    getString(R.string.frame_count_duration, it.frames.size, it.durationSeconds),
                    Thumbnails.animation(it))
            }
        }
        adapter.submit(rows)
        b.emptyText.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
        b.emptyText.setText(if (figuresTab) R.string.empty_figures else R.string.empty_animations)
    }

    // ---- 만들기 ----

    private fun createAnimation() {
        Dialogs.prompt(this, R.string.new_animation, R.string.hint_title, "", R.string.error_title) { title ->
            val base = Animation(repo.newId(), title)
            val starter = Templates.create("stickman", UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
                base.width / 2f, base.height * 0.55f)
            val saved = repo.save(base.copy(frames = listOf(Frame(listOf(starter)))))
            startActivity(EditorActivity.forAnimation(this, saved.id))
        }
    }

    private fun createFigure() {
        Dialogs.prompt(this, R.string.new_figure, R.string.hint_name, "", R.string.error_name) { name ->
            val labels = Templates.all.map { EditorActivity.templateName(this, it.key) }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle(R.string.title_choose_figure)
                .setItems(labels) { _, which ->
                    val id = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
                    val figure = Templates.all[which].build(id, 0f, 0f).copy(name = name)
                    repo.saveFigure(figure)
                    startActivity(EditorActivity.forFigure(this, id))
                }
                .show()
        }
    }

    // ---- 목록 동작 ----

    private fun open(row: Row) {
        if (figuresTab) startActivity(EditorActivity.forFigure(this, row.key.toLong()))
        else startActivity(EditorActivity.forAnimation(this, row.key))
    }

    private fun showMenu(row: Row, anchor: View) {
        PopupMenu(this, anchor).apply {
            menu.add(0, 1, 0, R.string.open)
            if (!figuresTab) {
                menu.add(0, 2, 1, R.string.copy)
                menu.add(0, 3, 2, R.string.share)
                menu.add(0, 4, 3, R.string.save_backup)
            }
            menu.add(0, 5, 4, R.string.delete)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    1 -> open(row)
                    2 -> duplicate(row)
                    3 -> repo.animation(row.key)?.let { a -> Share.backup(this@HomeActivity, a) }
                    4 -> repo.animation(row.key)?.let { a ->
                        exporting = a
                        exportPicker.launch(Share.fileName(a.title))
                    }
                    5 -> delete(row)
                }
                true
            }
            show()
        }
    }

    private fun duplicate(row: Row) {
        val source = repo.animation(row.key) ?: return
        repo.save(source.copy(id = repo.newId(), title = getString(R.string.copy_suffix, source.title)))
        refresh()
    }

    private fun delete(row: Row) {
        val message = getString(if (figuresTab) R.string.confirm_delete_figure else R.string.confirm_delete_animation, row.title)
        Dialogs.confirm(this, message, R.string.delete) {
            if (figuresTab) repo.deleteFigure(row.key.toLong()) else repo.deleteAnimation(row.key)
            refresh()
            Toast.makeText(this, getString(R.string.deleted, row.title), Toast.LENGTH_SHORT).show()
        }
    }

    // ---- 가져오기 · 메뉴 ----

    private fun importFrom(uri: Uri) {
        val animation = runCatching {
            contentResolver.openInputStream(uri)!!.use { Sfa.read(it) }
        }.getOrNull()
        if (animation == null) {
            Dialogs.message(this, getString(R.string.import_failed))
            return
        }
        val saved = repo.save(animation)
        refresh()
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.import_done, saved.title))
            .setPositiveButton(R.string.open) { _, _ -> startActivity(EditorActivity.forAnimation(this, saved.id)) }
            .setNegativeButton(R.string.close, null)
            .show()
    }

    private fun showTopMenu(anchor: View) {
        PopupMenu(this, anchor).apply {
            menu.add(0, 1, 0, R.string.import_backup)
            menu.add(0, 2, 1, R.string.help)
            menu.add(0, 3, 2, R.string.about)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    1 -> importPicker.launch(arrayOf("*/*"))
                    2 -> Dialogs.message(this@HomeActivity, getString(R.string.help_text))
                    3 -> Dialogs.message(this@HomeActivity, getString(R.string.about_text, BuildConfig.VERSION_NAME))
                }
                true
            }
            show()
        }
    }
}
