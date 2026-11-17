package com.kjooy0305.adskip

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 광고 건너뛰기의 핵심.
 *
 * 1. 시스템이 유튜브(또는 브라우저) 화면이 바뀔 때마다 이벤트를 보내 준다(onAccessibilityEvent).
 * 2. 그때마다 현재 화면의 접근성 트리(rootInActiveWindow)를 받아
 *    리소스 아이디 → 글자 순서로 '광고 건너뛰기' 버튼을 찾는다.
 * 3. 찾으면 performAction(ACTION_CLICK) 으로 클릭한다.
 *    클릭을 거절하는 뷰면 버튼 위치(getBoundsInScreen)의 가운데를 손가락 대신 탭한다(dispatchGesture).
 * 4. 버튼이 나타나는 순간 이벤트가 없을 수도 있어 조금 뒤에 몇 번 더 살펴본다(burst retry).
 */
class SkipService : AccessibilityService() {

    private lateinit var prefs: Prefs
    private var worker: HandlerThread? = null
    private var workerHandler: Handler? = null
    private val main = Handler(Looper.getMainLooper())
    private var lastClickAt = 0L
    private val fmt = SimpleDateFormat("MM-dd HH:mm:ss", Locale.KOREA)

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == Prefs.KEY_BROWSERS) applyTargets()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = Prefs(this)
        prefs.sp.registerOnSharedPreferenceChangeListener(prefListener)
        if (worker == null) {
            worker = HandlerThread("adskip-worker").also { it.start() }
            workerHandler = Handler(worker!!.looper)
        }
        applyTargets()
        running = true
        notifyUi()
        Log.d(TAG, "connected")
    }

    /** 사용자 설정(브라우저 포함 여부)에 맞춰 이벤트를 받을 앱을 정한다. */
    private fun applyTargets() {
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
            AccessibilityEvent.TYPE_VIEW_SCROLLED or
            AccessibilityEvent.TYPE_WINDOWS_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = info.flags or
            AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
            AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
            AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        info.notificationTimeout = 50
        info.packageNames = prefs.targetPackages()
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg !in prefs.targetPackages()) return
        val h = workerHandler ?: return
        // 이벤트가 몰려도 한 번만 검사하고, 버튼이 활성화되는 짧은 틈을 대비해 두 번 더 본다.
        h.removeCallbacks(trySkipRunnable)
        h.post(trySkipRunnable)
        h.postDelayed(trySkipRunnable, 350)
        h.postDelayed(trySkipRunnable, 1000)
    }

    private val trySkipRunnable = Runnable { trySkip() }

    private fun trySkip() {
        val root: AccessibilityNodeInfo = try { rootInActiveWindow } catch (e: Exception) { null } ?: return
        try {
            var how = "아이디"
            var node = findById(root)
            if (node == null) { node = findByText(root); how = "글자" }
            if (node != null) {
                clickOrTap(node, how)
                safeRecycle(node)
            }
        } catch (e: Exception) {
            Log.w(TAG, "trySkip", e)
        } finally {
            safeRecycle(root)
        }
    }

    private fun findById(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        for (id in SkipMatcher.VIEW_IDS) {
            val list = try { root.findAccessibilityNodeInfosByViewId(id) } catch (e: Exception) { null } ?: continue
            val hit = list.firstOrNull { it.isVisibleToUser }
            val result = hit?.let { AccessibilityNodeInfo.obtain(it) }
            list.forEach { safeRecycle(it) }
            if (result != null) return result
        }
        return null
    }

    private fun findByText(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        for (t in SkipMatcher.SEARCH_TEXTS) {
            val list = try { root.findAccessibilityNodeInfosByText(t) } catch (e: Exception) { null } ?: continue
            val hit = list.firstOrNull {
                it.isVisibleToUser && (it.isClickable || it.isFocusable || it.parent?.isClickable == true) &&
                    SkipMatcher.isSkipLabel(it.text, it.contentDescription)
            }
            val result = hit?.let { AccessibilityNodeInfo.obtain(it) }
            list.forEach { safeRecycle(it) }
            if (result != null) return result
        }
        return null
    }

    /** 클릭 가능한 조상까지 올라가 ACTION_CLICK, 안 되면 좌표 탭. */
    private fun clickOrTap(target: AccessibilityNodeInfo, how: String) {
        val now = System.currentTimeMillis()
        if (!SkipMatcher.canClickAgain(lastClickAt, now)) return
        var node: AccessibilityNodeInfo? = AccessibilityNodeInfo.obtain(target)
        var hops = 0
        while (node != null && hops < 6) {
            if (node.isVisibleToUser && node.isEnabled && node.isClickable) {
                val ok = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                lastClickAt = System.currentTimeMillis()
                val r = Rect(); node.getBoundsInScreen(r)
                if (ok) { onSkipped(how) } else { tapCenter(r); onSkipped("$how+탭") }
                safeRecycle(node)
                return
            }
            val parent = node.parent
            safeRecycle(node)
            node = parent
            hops++
        }
        // 클릭 가능한 노드가 없음: 보이는 위치를 직접 탭
        val r = Rect(); target.getBoundsInScreen(r)
        if (!r.isEmpty) {
            tapCenter(r)
            lastClickAt = System.currentTimeMillis()
            onSkipped("$how 탭")
        }
    }

    private fun tapCenter(r: Rect) {
        val dm = resources.displayMetrics
        if (!r.intersect(0, 0, dm.widthPixels, dm.heightPixels)) return
        val path = Path().apply {
            moveTo(r.exactCenterX(), r.exactCenterY())
            lineTo(r.exactCenterX() + 1f, r.exactCenterY() + 1f)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 80))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun onSkipped(how: String) {
        prefs.recordSkip("${fmt.format(Date())}|$how")
        notifyUi()
        if (prefs.toast) main.post { Toast.makeText(this, "광고 건너뜀 (${prefs.count}회)", Toast.LENGTH_SHORT).show() }
    }

    private fun notifyUi() {
        sendBroadcast(Intent(ACTION_UPDATED).setPackage(packageName))
    }

    private fun safeRecycle(n: AccessibilityNodeInfo?) {
        try { @Suppress("DEPRECATION") n?.recycle() } catch (_: Exception) {}
    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        running = false
        try { prefs.sp.unregisterOnSharedPreferenceChangeListener(prefListener) } catch (_: Exception) {}
        notifyUi()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        running = false
        worker?.quitSafely(); worker = null; workerHandler = null
        super.onDestroy()
    }

    companion object {
        const val TAG = "AdSkip"
        const val ACTION_UPDATED = "com.kjooy0305.adskip.UPDATED"
        @Volatile var running = false
    }
}
