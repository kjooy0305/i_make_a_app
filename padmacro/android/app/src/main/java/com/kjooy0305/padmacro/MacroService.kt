package com.kjooy0305.padmacro

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.view.Display
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 매크로 접근성 서비스.
 *  - MacroRunner 의 [MacroRunner.Actions] 를 실제 안드로이드 동작으로 구현: 제스처 주입(dispatchGesture),
 *    화면 글자 찾아 클릭(rootInActiveWindow), 캡쳐(takeScreenshot, 안드로이드 11+), 앱 실행, 시스템 동작.
 *  - 플로팅 패널(FloatingPanel)과 좌표 찍기/녹화 오버레이(PickerOverlay)를 TYPE_ACCESSIBILITY_OVERLAY 로 띄운다
 *    (접근성 서비스는 '다른 앱 위에 표시' 권한 없이도 오버레이를 띄울 수 있다).
 *  - 볼륨 아래 버튼 두 번 → 선택된 매크로 시작/중지.
 */
class MacroService : AccessibilityService(), MacroRunner.Actions {

    companion object {
        @Volatile var instance: MacroService? = null
            private set

        /** 좌표 찍기 결과. EditorActivity 가 onResume 에서 가져간다 */
        @Volatile var picked: Step? = null
        @Volatile var pickedMacroId: String? = null
        /** 녹화가 끝나 매크로에 단계가 추가됐으면 그 매크로 id */
        @Volatile var recordedMacroId: String? = null

        fun isRunning() = instance?.runner != null
        fun consumePicked(): Step? = picked.also { picked = null }

        /** 서비스가 켜져 있는지 (instance 가 살아 있으면 켜진 것) */
        fun enabled() = instance != null

        /** 상태가 바뀔 때 MainActivity 등에 알리는 브로드캐스트 */
        const val ACTION_STATE = "com.kjooy0305.padmacro.STATE"
    }

    lateinit var store: MacroStore
        private set
    val handler = Handler(Looper.getMainLooper())

    @Volatile var runner: MacroRunner? = null
        private set
    private var runThread: Thread? = null
    var currentMacro: Macro? = null
        private set

    var panel: FloatingPanel? = null
        private set
    private var picker: PickerOverlay? = null

    private var lastVolumeDown = 0L

    // ------------------------------------------------------------------ 생명주기

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        store = MacroStore(this)
        store.log("서비스 켜짐")
        refreshPanel()
        broadcast()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* 이벤트는 쓰지 않는다 */ }
    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        teardown()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        teardown()
        super.onDestroy()
    }

    private fun teardown() {
        stopMacro(silent = true)
        picker?.remove(); picker = null
        panel?.remove(); panel = null
        if (instance === this) instance = null
        broadcast()
    }

    private fun broadcast() {
        try { sendBroadcast(Intent(ACTION_STATE).setPackage(packageName)) } catch (_: Exception) {}
    }

    /** 설정에 따라 플로팅 패널을 보이거나 숨긴다 */
    fun refreshPanel() {
        handler.post {
            if (store.showPanel) {
                if (panel == null) panel = FloatingPanel(this)
                panel?.show()
                panel?.update()
            } else {
                panel?.remove(); panel = null
            }
        }
    }

    // ------------------------------------------------------------------ 볼륨키

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!store.volumeTrigger) return false
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && event.action == KeyEvent.ACTION_DOWN) {
            val now = System.currentTimeMillis()
            if (now - lastVolumeDown < 500) {
                lastVolumeDown = 0
                handler.post { toggleSelected() }
                return true
            }
            lastVolumeDown = now
        }
        return false
    }

    // ------------------------------------------------------------------ 실행 제어

    /** 선택된 매크로를 시작하거나, 실행 중이면 멈춘다 */
    fun toggleSelected() {
        if (runner != null) { stopMacro(); return }
        val m = store.selected()
        if (m == null) { toast("매크로가 없습니다. 앱에서 먼저 만들어 주세요"); return }
        startMacro(m)
    }

    fun startMacro(macro: Macro) {
        stopMacro(silent = true)
        if (picker != null) { toast("녹화/좌표 찍기 중에는 실행할 수 없습니다"); return }
        currentMacro = macro
        store.log("시작: ${macro.name}")
        val r = MacroRunner(macro, this, object : MacroRunner.Listener {
            override fun onCountdown(remainingMs: Long) {
                handler.post {
                    panel?.setStatus(if (remainingMs > 0) "${(remainingMs + 999) / 1000}초 뒤 시작" else "실행 중")
                    if (remainingMs > 0 && remainingMs % 1000 == 0L && remainingMs <= 3000) vibrate(30)
                }
            }
            override fun onStep(iteration: Int, stepIndex: Int, step: Step) {
                handler.post {
                    val rep = if (macro.repeat <= 0) "${iteration}회" else "${iteration}/${macro.repeat}회"
                    panel?.setStatus("${stepIndex + 1}/${macro.steps.size} · $rep")
                }
            }
            override fun onFinished(result: MacroRunner.Result) {
                val err = runner?.lastError
                handler.post {
                    runner = null; runThread = null
                    val msg = when (result) {
                        MacroRunner.Result.FAILED -> "중단: ${err ?: "단계 실패"}"
                        else -> result.label
                    }
                    store.log("${macro.name} — $msg")
                    if (macro.vibrateOnEnd) vibrate(if (result == MacroRunner.Result.FAILED) 600 else 200)
                    if (store.toast || result == MacroRunner.Result.FAILED) toast("${macro.name}: $msg")
                    panel?.update()
                    broadcast()
                }
            }
        })
        runner = r
        runThread = Thread({ r.run() }, "macro-runner").also { it.isDaemon = true; it.start() }
        panel?.update()
        broadcast()
    }

    fun stopMacro(silent: Boolean = false) {
        val r = runner ?: return
        r.stop()
        runThread?.interrupt()
        runner = null; runThread = null
        if (!silent) {
            store.log("${currentMacro?.name ?: ""} — 사용자가 중지")
            if (store.toast) toast("매크로 중지")
        }
        panel?.update()
        broadcast()
    }

    // ------------------------------------------------------------------ 좌표 찍기 / 녹화

    /** 화면에서 한 번 터치(또는 드래그)를 받아 Step 으로 돌려준다. 끝나면 EditorActivity 를 다시 띄운다 */
    fun startPick(macroId: String, wantSwipe: Boolean) {
        if (picker != null) return
        panel?.hide()
        picker = PickerOverlay(this, if (wantSwipe) PickerOverlay.Mode.SWIPE else PickerOverlay.Mode.POINT, macroId, object : PickerOverlay.Callback {
            override fun onPicked(step: Step) {
                picked = step; pickedMacroId = macroId
                finishPicker()
                openEditor(macroId)
            }
            override fun onRecorded(steps: List<Step>) {}
            override fun onCancelled() { finishPicker(); openEditor(macroId) }
        }).also { it.show() }
    }

    /** 터치를 그대로 따라 하면서 기록한다. 끝내면 매크로 끝에 단계를 붙인다 */
    fun startRecord(macroId: String) {
        if (picker != null) return
        stopMacro(silent = true)
        panel?.hide()
        picker = PickerOverlay(this, PickerOverlay.Mode.RECORD, macroId, object : PickerOverlay.Callback {
            override fun onPicked(step: Step) {}
            override fun onRecorded(steps: List<Step>) {
                finishPicker()
                val m = store.get(macroId)
                if (m != null && steps.isNotEmpty()) {
                    m.steps.addAll(steps)
                    store.put(m)
                    recordedMacroId = macroId
                    store.log("녹화 ${steps.size}단계 추가: ${m.name}")
                    toast("${steps.count { it !is Step.Wait }}개 터치를 ‘${m.name}’ 에 추가했습니다")
                } else toast("기록된 터치가 없습니다")
                openEditor(macroId)
            }
            override fun onCancelled() { finishPicker(); openEditor(macroId) }
        }).also { it.show() }
    }

    private fun finishPicker() {
        picker?.remove(); picker = null
        if (store.showPanel) panel?.show()
    }

    fun isPicking() = picker != null

    /** 편집 화면을 다시 앞으로. 안드로이드 10+ 는 '다른 앱 위에 표시' 권한이 있어야 뒤에서 띄울 수 있다 */
    private fun openEditor(macroId: String) {
        val i = Intent(this, EditorActivity::class.java)
            .putExtra(EditorActivity.EXTRA_ID, macroId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        try { startActivity(i) } catch (e: Exception) { toast("앱으로 돌아가면 반영됩니다") }
        if (!Perms.canDrawOverlays(this)) toast("앱으로 돌아가면 반영됩니다")
    }

    fun openMain() {
        val i = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        try { startActivity(i) } catch (_: Exception) {}
    }

    // ------------------------------------------------------------------ Actions 구현 (실행 스레드에서 호출)

    /** 제스처를 보내고 끝날 때까지 기다린다 */
    fun gesture(build: GestureDescription.Builder.() -> Unit, timeoutMs: Long): Boolean {
        val latch = CountDownLatch(1)
        var ok = false
        handler.post {
            try {
                val g = GestureDescription.Builder().apply(build).build()
                val sent = dispatchGesture(g, object : GestureResultCallback() {
                    override fun onCompleted(d: GestureDescription?) { ok = true; latch.countDown() }
                    override fun onCancelled(d: GestureDescription?) { ok = false; latch.countDown() }
                }, handler)
                if (!sent) latch.countDown()
            } catch (e: Exception) { latch.countDown() }
        }
        latch.await(timeoutMs + 2000, TimeUnit.MILLISECONDS)
        return ok
    }

    private fun strokeAt(x: Int, y: Int, duration: Long) =
        GestureDescription.StrokeDescription(Path().apply { moveTo(x.toFloat().coerceAtLeast(0f), y.toFloat().coerceAtLeast(0f)) }, 0, duration)

    override fun tap(x: Int, y: Int, count: Int): Boolean {
        var ok = true
        for (i in 0 until count.coerceIn(1, 10)) {
            ok = gesture({ addStroke(strokeAt(x, y, 60)) }, 60) && ok
            if (count > 1 && i < count - 1) Thread.sleep(90)
        }
        return ok
    }

    override fun longPress(x: Int, y: Int, durationMs: Long): Boolean =
        gesture({ addStroke(strokeAt(x, y, durationMs.coerceIn(100, 10_000))) }, durationMs)

    override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, durationMs: Long): Boolean {
        val d = durationMs.coerceIn(50, 10_000)
        val path = Path().apply {
            moveTo(x1.toFloat().coerceAtLeast(0f), y1.toFloat().coerceAtLeast(0f))
            lineTo(x2.toFloat().coerceAtLeast(0f), y2.toFloat().coerceAtLeast(0f))
        }
        return gesture({ addStroke(GestureDescription.StrokeDescription(path, 0, d)) }, d)
    }

    override fun capture(label: String): Boolean {
        if (Build.VERSION.SDK_INT < 30) {
            runner?.let { store.log("캡쳐는 안드로이드 11 이상에서만 됩니다") }
            toastBg("캡쳐는 안드로이드 11 이상에서만 됩니다")
            return false
        }
        // 패널이 같이 찍히지 않도록 잠깐 숨긴다
        handler.post { panel?.hide() }
        Thread.sleep(150)
        var bmp: Bitmap? = null
        var error = ""
        for (attempt in 0 until 3) {
            val latch = CountDownLatch(1)
            handler.post {
                try {
                    takeScreenshot(Display.DEFAULT_DISPLAY, { it.run() }, object : TakeScreenshotCallback {
                        override fun onSuccess(result: ScreenshotResult) {
                            try {
                                val hw = Bitmap.wrapHardwareBuffer(result.hardwareBuffer, result.colorSpace)
                                bmp = hw?.copy(Bitmap.Config.ARGB_8888, false)
                                hw?.recycle()
                                result.hardwareBuffer.close()
                            } catch (e: Exception) { error = e.message ?: "bitmap" }
                            latch.countDown()
                        }
                        override fun onFailure(code: Int) { error = "code $code"; latch.countDown() }
                    })
                } catch (e: Exception) { error = e.message ?: "takeScreenshot"; latch.countDown() }
            }
            latch.await(5, TimeUnit.SECONDS)
            if (bmp != null) break
            Thread.sleep(500) // 너무 자주 찍으면 거절(ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT)되므로 쉬고 재시도
        }
        handler.post { if (store.showPanel && picker == null) panel?.show() }
        val b = bmp ?: run { store.log("캡쳐 실패 $error"); return false }
        return try {
            val name = buildString {
                append("매크로_")
                if (label.isNotBlank()) append(label.replace(Regex("[\\\\/:*?\"<>|]"), "_")).append("_")
                append(SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date()))
                append(".png")
            }
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/패드매크로")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
            resolver.openOutputStream(uri)?.use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
            values.clear(); values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            store.log("캡쳐 저장: Pictures/패드매크로/$name")
            true
        } catch (e: Exception) {
            store.log("캡쳐 저장 실패: ${e.message}")
            false
        } finally { b.recycle() }
    }

    override fun launchApp(packageName: String): Boolean {
        val i = packageManager.getLaunchIntentForPackage(packageName) ?: return false
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return try { startActivity(i); true } catch (e: Exception) { false }
    }

    override fun global(action: String): Boolean {
        val code = when (action) {
            Step.BACK -> GLOBAL_ACTION_BACK
            Step.HOME -> GLOBAL_ACTION_HOME
            Step.RECENTS -> GLOBAL_ACTION_RECENTS
            Step.NOTIFICATIONS -> GLOBAL_ACTION_NOTIFICATIONS
            Step.QUICK_SETTINGS -> GLOBAL_ACTION_QUICK_SETTINGS
            Step.LOCK -> if (Build.VERSION.SDK_INT >= 28) GLOBAL_ACTION_LOCK_SCREEN else return false
            Step.POWER -> GLOBAL_ACTION_POWER_DIALOG
            Step.SCREENSHOT_SYS -> if (Build.VERSION.SDK_INT >= 28) GLOBAL_ACTION_TAKE_SCREENSHOT else return false
            else -> return false
        }
        val latch = CountDownLatch(1); var ok = false
        handler.post { ok = try { performGlobalAction(code) } catch (e: Exception) { false }; latch.countDown() }
        latch.await(3, TimeUnit.SECONDS)
        return ok
    }

    override fun clickText(text: String, exact: Boolean): Boolean {
        val node = findText(text, exact) ?: return false
        // 눌리는 조상(또는 자신)을 찾아 클릭, 없으면 가운데 좌표를 탭
        var n: AccessibilityNodeInfo? = node
        var hops = 0
        while (n != null && hops < 6) {
            if (n.isClickable) {
                val latch = CountDownLatch(1); var ok = false
                val target = n
                handler.post { ok = try { target.performAction(AccessibilityNodeInfo.ACTION_CLICK) } catch (e: Exception) { false }; latch.countDown() }
                latch.await(2, TimeUnit.SECONDS)
                if (ok) return true
                break
            }
            n = n.parent; hops++
        }
        val r = Rect(); node.getBoundsInScreen(r)
        if (r.isEmpty) return false
        return tap(r.centerX(), r.centerY(), 1)
    }

    override fun textVisible(text: String): Boolean = findText(text, false) != null

    /** 모든 창(대화상자 포함)에서 글자가 보이는 노드를 찾는다 */
    private fun findText(text: String, exact: Boolean): AccessibilityNodeInfo? {
        val roots = mutableListOf<AccessibilityNodeInfo>()
        try { rootInActiveWindow?.let { roots.add(it) } } catch (_: Exception) {}
        try { windows?.forEach { w -> w.root?.let { r -> if (roots.none { it == r }) roots.add(r) } } } catch (_: Exception) {}
        val needle = text.trim()
        for (root in roots) {
            val found = try { root.findAccessibilityNodeInfosByText(needle) } catch (_: Exception) { null } ?: continue
            for (n in found) {
                if (!n.isVisibleToUser) continue
                if (exact) {
                    val t = n.text?.toString()?.trim(); val d = n.contentDescription?.toString()?.trim()
                    if (!t.equals(needle, true) && !d.equals(needle, true)) continue
                }
                return n
            }
        }
        return null
    }

    override fun openUrl(url: String): Boolean {
        val u = if (url.contains("://")) url else "https://$url"
        return try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); true
        } catch (e: Exception) { false }
    }

    override fun vibrate(ms: Long) {
        try {
            val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            v.vibrate(VibrationEffect.createOneShot(ms.coerceIn(10, 3000), VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {}
    }

    // ------------------------------------------------------------------ 기타

    fun toast(msg: String) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
    private fun toastBg(msg: String) { handler.post { toast(msg) } }
}
