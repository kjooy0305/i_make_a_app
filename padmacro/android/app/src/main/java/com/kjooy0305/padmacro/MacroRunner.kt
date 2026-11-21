package com.kjooy0305.padmacro

/**
 * 매크로를 순서대로 실행하는 엔진. 안드로이드에 의존하지 않아 단위 테스트가 가능하다.
 * 실제 터치·캡쳐는 [Actions] 구현(MacroService)이 맡고, 여기서는 순서·반복·대기·중단만 다룬다.
 *
 * 별도 스레드에서 run() 을 돌리고, stop() 은 어느 스레드에서든 부를 수 있다.
 */
class MacroRunner(
    private val macro: Macro,
    private val actions: Actions,
    private val listener: Listener,
    private val clock: Clock = SystemClock,
) {
    interface Actions {
        fun tap(x: Int, y: Int, count: Int): Boolean
        fun longPress(x: Int, y: Int, durationMs: Long): Boolean
        fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, durationMs: Long): Boolean
        fun capture(label: String): Boolean
        fun launchApp(packageName: String): Boolean
        fun global(action: String): Boolean
        fun clickText(text: String, exact: Boolean): Boolean
        fun textVisible(text: String): Boolean
        fun openUrl(url: String): Boolean
        fun vibrate(ms: Long)
    }

    interface Listener {
        /** 시작 전 카운트다운. remainingMs 가 0 이면 곧 시작 */
        fun onCountdown(remainingMs: Long)
        /** iteration 은 1부터, 무한 반복이면 total 은 0 */
        fun onStep(iteration: Int, stepIndex: Int, step: Step)
        fun onFinished(result: Result)
    }

    interface Clock {
        fun now(): Long
        fun sleep(ms: Long)
    }

    object SystemClock : Clock {
        override fun now() = System.currentTimeMillis()
        override fun sleep(ms: Long) = Thread.sleep(ms)
    }

    enum class Result(val label: String) {
        DONE("끝까지 실행"),
        STOPPED("사용자가 중지"),
        FAILED("단계 실패로 중단"),
        EMPTY("단계가 없음"),
    }

    @Volatile private var stopped = false
    @Volatile var lastError: String? = null
        private set

    fun stop() { stopped = true }

    /** 중지 요청을 50ms 마다 살피며 기다린다. 중지되면 false */
    private fun wait(ms: Long): Boolean {
        val end = clock.now() + ms
        while (!stopped) {
            val left = end - clock.now()
            if (left <= 0) return true
            clock.sleep(minOf(50, left))
        }
        return false
    }

    fun run() {
        if (macro.steps.isEmpty()) { listener.onFinished(Result.EMPTY); return }

        // 시작 전 카운트다운 (1초 단위로 알림)
        var remaining = macro.startDelayMs
        while (remaining > 0 && !stopped) {
            listener.onCountdown(remaining)
            val chunk = if (remaining % 1000 == 0L) 1000 else remaining % 1000
            if (!wait(chunk)) break
            remaining -= chunk
        }
        if (stopped) { listener.onFinished(Result.STOPPED); return }
        listener.onCountdown(0)

        var iteration = 0
        while (!stopped && (macro.repeat <= 0 || iteration < macro.repeat)) {
            iteration++
            for ((i, step) in macro.steps.withIndex()) {
                if (stopped) break
                listener.onStep(iteration, i, step)
                val ok = exec(step)
                if (stopped) break
                if (!ok) {
                    if (lastError == null) lastError = "${i + 1}번째 단계 실패: ${step.describe()}"
                    listener.onFinished(Result.FAILED); return
                }
                // 대기 단계 뒤에는 기본 사이 대기를 또 넣지 않는다
                if (step !is Step.Wait && i < macro.steps.size - 1 && macro.gapMs > 0) wait(macro.gapMs)
            }
            if (!stopped && (macro.repeat <= 0 || iteration < macro.repeat)) {
                val gap = if (macro.repeatGapMs > 0) macro.repeatGapMs else macro.gapMs
                if (gap > 0) wait(gap)
            }
        }
        listener.onFinished(if (stopped) Result.STOPPED else Result.DONE)
    }

    private fun exec(step: Step): Boolean = try {
        when (step) {
            is Step.Tap -> actions.tap(step.x, step.y, step.count)
            is Step.LongPress -> actions.longPress(step.x, step.y, step.durationMs)
            is Step.Swipe -> actions.swipe(step.x1, step.y1, step.x2, step.y2, step.durationMs)
            is Step.Wait -> wait(step.ms) || stopped
            is Step.Capture -> actions.capture(step.label)
            is Step.LaunchApp -> actions.launchApp(step.packageName)
            is Step.Global -> actions.global(step.action)
            is Step.ClickText -> untilTimeout(step.timeoutMs) { actions.clickText(step.text, step.exact) }
                .also { if (!it) lastError = "“${step.text}” 글자를 ${Step.fmtMs(step.timeoutMs)} 안에 못 찾음" }
            is Step.WaitText -> {
                val found = untilTimeout(step.timeoutMs) { actions.textVisible(step.text) }
                if (!found && step.stopIfMissing) lastError = "“${step.text}” 글자가 ${Step.fmtMs(step.timeoutMs)} 안에 안 나타남"
                found || !step.stopIfMissing
            }
            is Step.OpenUrl -> actions.openUrl(step.url)
            is Step.Vibrate -> { actions.vibrate(step.ms); true }
        }
    } catch (e: Exception) {
        lastError = e.message ?: e.javaClass.simpleName
        false
    }

    /** 조건이 참이 될 때까지 0.3초 간격으로 재시도 */
    private fun untilTimeout(timeoutMs: Long, probe: () -> Boolean): Boolean {
        val end = clock.now() + timeoutMs
        while (!stopped) {
            if (probe()) return true
            if (clock.now() >= end) return false
            if (!wait(300)) return false
        }
        return false
    }
}
