package com.kjooy0305.stickfighter.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kjooy0305.stickfighter.R
import com.kjooy0305.stickfighter.data.Repository
import com.kjooy0305.stickfighter.data.Sfa
import com.kjooy0305.stickfighter.databinding.ActivityEditorBinding
import com.kjooy0305.stickfighter.model.Animation
import com.kjooy0305.stickfighter.model.Figure
import com.kjooy0305.stickfighter.model.FigureEdit
import com.kjooy0305.stickfighter.model.Frame
import com.kjooy0305.stickfighter.model.Shape
import com.kjooy0305.stickfighter.model.Templates
import com.kjooy0305.stickfighter.video.Quality
import com.kjooy0305.stickfighter.video.VideoExporter
import java.util.UUID
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors

/** 애니메이션 편집 화면. 같은 화면이 '내 피규어' 만들기에도 쓰인다(피규어 모드). */
class EditorActivity : AppCompatActivity(), EditorView.Listener {

    private lateinit var b: ActivityEditorBinding
    private lateinit var repo: Repository
    private lateinit var strip: FrameStripAdapter
    private lateinit var anim: Animation

    private var figureMode = false
    private var customFigureId = 0L
    private var frameIndex = 0
    private var selectedFigure: Long? = null
    private var selectedSegment: Int? = null

    private class Snapshot(val frames: List<Frame>, val index: Int)
    private val undoStack = ArrayDeque<Snapshot>()

    private val handler = Handler(Looper.getMainLooper())
    private val saveRunnable = Runnable { save() }
    private val worker = Executors.newSingleThreadExecutor()
    private var cancelVideo = false

    private val createBackup = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri == null) return@registerForActivityResult
        val ok = runCatching {
            contentResolver.openOutputStream(uri)!!.use { Sfa.write(anim, it) }
        }.isSuccess
        Toast.makeText(this, if (ok) R.string.backup_saved else R.string.backup_failed, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(b.root)
        repo = Repository(filesDir)

        figureMode = intent.hasExtra(EXTRA_FIGURE)
        if (figureMode) {
            customFigureId = intent.getLongExtra(EXTRA_FIGURE, 0L)
            val figure = repo.figures().firstOrNull { it.id == customFigureId }
            if (figure == null) { finish(); return }
            val base = Animation("figure", figure.name)
            anim = base.copy(frames = listOf(Frame(listOf(figure.copy(
                x = base.width / 2f, y = base.height * 0.55f, scale = 1f, flip = false)))))
        } else {
            val loaded = repo.animation(intent.getStringExtra(EXTRA_ANIMATION) ?: "")
            if (loaded == null) { finish(); return }
            anim = loaded
        }
        selectedFigure = anim.frames.first().figures.lastOrNull()?.id

        b.titleText.text = anim.title
        b.editorView.listener = this
        strip = FrameStripAdapter(
            onSelect = { selectFrame(it) },
            onAdd = { addFrame() },
            onMenu = { index, anchor -> showFrameMenu(index, anchor) },
            showAdd = !figureMode
        )
        b.frameStrip.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        b.frameStrip.adapter = strip

        b.backButton.setOnClickListener { finish() }
        b.undoButton.setOnClickListener { undo() }
        b.playButton.setOnClickListener { play() }
        b.moreButton.setOnClickListener { showMore(it) }
        b.modePose.setOnClickListener { setMode(EditMode.POSE) }
        b.modeStretch.setOnClickListener { setMode(EditMode.STRETCH) }
        b.modeFlip.setOnClickListener { setMode(EditMode.FLIP) }
        b.sizeUp.setOnClickListener { resize(1.15f) }
        b.sizeDown.setOnClickListener { resize(1f / 1.15f) }
        b.addItem.setOnClickListener { if (figureMode) addPart() else addFigure() }
        b.removeItem.setOnClickListener { if (figureMode) removePart() else removeFigure() }

        if (figureMode) {
            b.playButton.visibility = View.GONE
            b.moreButton.visibility = View.GONE
            b.addItem.contentDescription = getString(R.string.add_part)
            b.removeItem.contentDescription = getString(R.string.remove_part)
            b.addItem.setImageResource(R.drawable.ic_add)
            b.sizeUp.visibility = View.GONE
            b.sizeDown.visibility = View.GONE
            b.frameStrip.visibility = View.GONE
        }
        setMode(EditMode.POSE)
        refreshAll()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(saveRunnable)
        save()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelVideo = true
        worker.shutdown()
    }

    // ---- 상태 ----

    private fun frame(): Frame = anim.frames[frameIndex]

    private fun setFrames(frames: List<Frame>, index: Int = frameIndex) {
        frameIndex = index.coerceIn(0, frames.size - 1)
        anim = anim.copy(frames = frames)
        refreshAll()
        scheduleSave()
    }

    private fun refreshAll() {
        if (selectedFigure != null && frame().figures.none { it.id == selectedFigure }) {
            selectedFigure = frame().figures.lastOrNull()?.id
            selectedSegment = null
        }
        b.editorView.animation = anim
        b.editorView.frame = frame()
        b.editorView.selectedFigureId = selectedFigure
        b.editorView.selectedSegmentId = selectedSegment
        strip.submit(anim, frameIndex)
        b.frameStrip.scrollToPosition(frameIndex)
        b.undoButton.alpha = if (undoStack.isEmpty()) 0.4f else 1f
        updateHint()
    }

    private fun pushUndo() {
        undoStack.addLast(Snapshot(anim.frames, frameIndex))
        while (undoStack.size > 60) undoStack.removeFirst()
        b.undoButton.alpha = 1f
    }

    private fun undo() {
        val snap = undoStack.removeLastOrNull() ?: return
        setFrames(snap.frames, snap.index)
    }

    private fun scheduleSave() {
        handler.removeCallbacks(saveRunnable)
        handler.postDelayed(saveRunnable, 800)
    }

    private fun save() {
        if (figureMode) {
            val figure = anim.frames.first().figures.firstOrNull() ?: return
            repo.saveFigure(figure.copy(id = customFigureId, name = anim.title))
        } else {
            anim = repo.save(anim)
        }
    }

    private fun setMode(mode: EditMode) {
        b.editorView.mode = mode
        b.modePose.isSelected = mode == EditMode.POSE
        b.modeStretch.isSelected = mode == EditMode.STRETCH
        b.modeFlip.isSelected = mode == EditMode.FLIP
        updateHint()
    }

    private fun updateHint() {
        b.hintText.text = when {
            selectedFigure == null -> getString(R.string.hint_select)
            figureMode && b.editorView.mode == EditMode.POSE -> getString(R.string.select_part_first)
            b.editorView.mode == EditMode.POSE -> getString(R.string.hint_pose)
            b.editorView.mode == EditMode.STRETCH -> getString(R.string.hint_stretch)
            else -> getString(R.string.hint_flip)
        }
    }

    // ---- EditorView.Listener ----

    override fun onGestureStart() = pushUndo()

    override fun onFrameChanged(frame: Frame) {
        val frames = anim.frames.toMutableList()
        frames[frameIndex] = frame
        anim = anim.copy(frames = frames)
        scheduleSave()
    }

    override fun onGestureEnd() {
        strip.submit(anim, frameIndex)
        b.undoButton.alpha = 1f
    }

    override fun onSelectionChanged(figureId: Long?, segmentId: Int?) {
        selectedFigure = figureId
        selectedSegment = segmentId
        updateHint()
    }

    override fun onModifyRequested(figureId: Long, segmentId: Int) {
        val figure = frame().figures.firstOrNull { it.id == figureId } ?: return
        val segment = figure.segments.firstOrNull { it.id == segmentId } ?: return
        val pane = Dialogs.ColorPane(this, segment.color)
        val (thicknessRow, thicknessBar) = Dialogs.labeledSeek(this, FigureEdit.MAX_THICKNESS.toInt() - 1,
            segment.thickness.toInt() - 1) { getString(R.string.thickness, it + 1) }
        val whole = CheckBox(this).apply { setText(R.string.apply_whole_figure) }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Dialogs.dp(context, 20), Dialogs.dp(context, 8), Dialogs.dp(context, 20), 0)
            addView(pane.view)
            addView(thicknessRow)
            addView(whole)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.title_modify)
            .setView(content)
            .setPositiveButton(R.string.apply) { _, _ ->
                pushUndo()
                val edited = FigureEdit.style(figure, if (whole.isChecked) null else segmentId,
                    pane.color(), (thicknessBar.progress + 1).toFloat())
                setFrames(anim.frames.toMutableList().also {
                    it[frameIndex] = Frame(frame().figures.map { f -> if (f.id == edited.id) edited else f })
                })
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // ---- 도구 ----

    private fun selectedFigureOrNull(): Figure? = frame().figures.firstOrNull { it.id == selectedFigure }

    private fun resize(factor: Float) {
        val figure = selectedFigureOrNull() ?: return
        pushUndo()
        val edited = FigureEdit.scaleBy(figure, factor)
        setFrames(anim.frames.toMutableList().also {
            it[frameIndex] = Frame(frame().figures.map { f -> if (f.id == edited.id) edited else f })
        })
    }

    private fun newFigureId(): Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE

    private fun addFigure() {
        val custom = repo.figures()
        val names = Templates.all.map { templateName(this, it.key) } + custom.map { it.name }
        AlertDialog.Builder(this)
            .setTitle(R.string.title_choose_figure)
            .setItems(names.toTypedArray()) { _, which ->
                val cx = anim.width / 2f
                val cy = anim.height * 0.55f
                val figure = if (which < Templates.all.size) {
                    Templates.all[which].build(newFigureId(), cx, cy)
                } else {
                    custom[which - Templates.all.size].copy(id = newFigureId(), x = cx, y = cy, scale = 1f, flip = false)
                }
                pushUndo()
                // 새 피규어는 지금 프레임부터 끝까지 같은 모습으로 놓인다.
                val frames = anim.frames.mapIndexed { i, f -> if (i >= frameIndex) Frame(f.figures + figure) else f }
                selectedFigure = figure.id
                selectedSegment = null
                setFrames(frames)
            }
            .show()
    }

    private fun removeFigure() {
        val id = selectedFigure ?: return
        pushUndo()
        val frames = anim.frames.mapIndexed { i, f ->
            if (i >= frameIndex) Frame(f.figures.filter { it.id != id }) else f
        }
        selectedFigure = null
        setFrames(frames)
    }

    private fun addPart() {
        val figure = selectedFigureOrNull() ?: return
        val labels = arrayOf(getString(R.string.part_line), getString(R.string.part_ring), getString(R.string.part_disc))
        AlertDialog.Builder(this)
            .setTitle(R.string.add_part)
            .setItems(labels) { _, which ->
                val shape = arrayOf(Shape.LINE, Shape.RING, Shape.DISC)[which]
                pushUndo()
                val length = if (shape == Shape.LINE) 70f else 50f
                val edited = FigureEdit.addSegment(figure, selectedSegment ?: -1, shape, length, Templates.DEFAULT_COLOR, 7f)
                selectedSegment = edited.segments.last().id
                setFrames(listOf(Frame(listOf(edited))))
            }
            .show()
    }

    private fun removePart() {
        val figure = selectedFigureOrNull() ?: return
        val seg = selectedSegment ?: return
        pushUndo()
        selectedSegment = null
        setFrames(listOf(Frame(listOf(FigureEdit.removeSegment(figure, seg)))))
    }

    // ---- 프레임 ----

    private fun selectFrame(index: Int) {
        frameIndex = index
        refreshAll()
    }

    private fun addFrame() = copyFrame(frameIndex, announce = false)

    private fun copyFrame(index: Int, announce: Boolean = true) {
        pushUndo()
        val frames = anim.frames.toMutableList()
        frames.add(index + 1, frames[index])
        setFrames(frames, index + 1)
        if (announce) Toast.makeText(this, getString(R.string.frame_copied, index + 1), Toast.LENGTH_SHORT).show()
    }

    private fun deleteFrame(index: Int) {
        if (anim.frames.size <= 1) {
            Toast.makeText(this, R.string.last_frame_only, Toast.LENGTH_SHORT).show()
            return
        }
        pushUndo()
        val frames = anim.frames.toMutableList()
        frames.removeAt(index)
        setFrames(frames, if (index >= frames.size) frames.size - 1 else index)
        Toast.makeText(this, getString(R.string.frame_deleted, index + 1), Toast.LENGTH_SHORT).show()
    }

    private fun showFrameMenu(index: Int, anchor: View) {
        PopupMenu(this, anchor).apply {
            menu.add(0, 1, 0, R.string.copy)
            menu.add(0, 2, 1, R.string.delete)
            setOnMenuItemClickListener {
                if (it.itemId == 1) copyFrame(index) else deleteFrame(index)
                true
            }
            show()
        }
    }

    // ---- 재생 · 설정 · 저장 ----

    private fun play() {
        if (anim.frames.size < 2) {
            Toast.makeText(this, R.string.need_two_frames, Toast.LENGTH_SHORT).show()
            return
        }
        handler.removeCallbacks(saveRunnable)
        save()
        startActivity(Intent(this, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_ANIMATION, anim.id))
    }

    private fun showMore(anchor: View) {
        PopupMenu(this, anchor).apply {
            menu.add(0, 1, 0, R.string.menu_settings)
            menu.add(0, 2, 1, R.string.menu_background)
            menu.add(0, 3, 2, R.string.menu_video)
            menu.add(0, 4, 3, R.string.save_backup)
            menu.add(0, 5, 4, R.string.share)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    1 -> showSettings()
                    2 -> Dialogs.pickColor(this@EditorActivity, R.string.title_background, anim.background) { color ->
                        anim = anim.copy(background = color)
                        refreshAll()
                        scheduleSave()
                    }
                    3 -> showVideoDialog()
                    4 -> createBackup.launch(Share.fileName(anim.title))
                    5 -> { save(); Share.backup(this@EditorActivity, anim) }
                }
                true
            }
            show()
        }
    }

    private fun showSettings() {
        val (speedRow, speedBar) = Dialogs.labeledSeek(this, 19, (anim.keyFramesPerSecond * 2).toInt() - 1) {
            getString(R.string.settings_speed, (it + 1) / 2f)
        }
        val (fpsRow, fpsBar) = Dialogs.labeledSeek(this, 10, (anim.fps - 10) / 5) {
            getString(R.string.settings_fps, 10 + it * 5)
        }
        val repeat = CheckBox(this).apply { setText(R.string.settings_repeat); isChecked = anim.repeat }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Dialogs.dp(context, 20), Dialogs.dp(context, 12), Dialogs.dp(context, 20), 0)
            addView(speedRow)
            addView(fpsRow)
            addView(repeat)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.menu_settings)
            .setView(content)
            .setPositiveButton(R.string.apply) { _, _ ->
                anim = anim.copy(
                    keyFramesPerSecond = (speedBar.progress + 1) / 2f,
                    fps = 10 + fpsBar.progress * 5,
                    repeat = repeat.isChecked
                )
                scheduleSave()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showVideoDialog() {
        if (anim.frames.size < 2) {
            Toast.makeText(this, R.string.need_two_frames, Toast.LENGTH_SHORT).show()
            return
        }
        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@EditorActivity, android.R.layout.simple_spinner_dropdown_item,
                Quality.values().map { it.label })
            setSelection(Quality.HD.ordinal)
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Dialogs.dp(context, 20), Dialogs.dp(context, 12), Dialogs.dp(context, 20), 0)
            addView(TextView(context).apply { setText(R.string.video_quality) })
            addView(spinner)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.menu_video)
            .setView(content)
            .setPositiveButton(R.string.ok) { _, _ -> exportVideo(Quality.values()[spinner.selectedItemPosition]) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun exportVideo(quality: Quality) {
        handler.removeCallbacks(saveRunnable)
        save()
        val snapshot = anim
        cancelVideo = false
        val bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = 100 }
        val label = TextView(this).apply { text = getString(R.string.video_encoding, 0) }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Dialogs.dp(context, 20), Dialogs.dp(context, 16), Dialogs.dp(context, 20), 0)
            addView(label)
            addView(bar)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.menu_video)
            .setView(content)
            .setNegativeButton(R.string.cancel) { _, _ -> cancelVideo = true }
            .setCancelable(false)
            .show()
        worker.execute {
            val result = runCatching {
                VideoExporter.export(applicationContext, snapshot, quality, { p ->
                    runOnUiThread {
                        bar.progress = (p * 100).toInt()
                        label.text = getString(R.string.video_encoding, (p * 100).toInt())
                    }
                }, { cancelVideo })
            }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                dialog.dismiss()
                result.onSuccess { done ->
                    val where = if (done.uri != null) getString(R.string.video_done)
                    else getString(R.string.video_done_app_dir, done.file.parent ?: "")
                    AlertDialog.Builder(this)
                        .setMessage(where)
                        .setPositiveButton(R.string.video_share) { _, _ -> Share.video(this, done.file) }
                        .setNegativeButton(R.string.close, null)
                        .show()
                }.onFailure {
                    if (it !is CancellationException) Dialogs.message(this, getString(R.string.video_failed))
                }
            }
        }
    }

    companion object {
        const val EXTRA_ANIMATION = "animation"
        const val EXTRA_FIGURE = "figure"

        fun forAnimation(context: Context, id: String) =
            Intent(context, EditorActivity::class.java).putExtra(EXTRA_ANIMATION, id)

        fun forFigure(context: Context, id: Long) =
            Intent(context, EditorActivity::class.java).putExtra(EXTRA_FIGURE, id)

        fun templateName(context: Context, key: String): String = context.getString(
            when (key) {
                "stickman" -> R.string.figure_stickman
                "bighead" -> R.string.figure_bighead
                "circle" -> R.string.figure_circle
                "disc" -> R.string.figure_disc
                "line" -> R.string.figure_line
                else -> R.string.figure_sword
            }
        )
    }
}
