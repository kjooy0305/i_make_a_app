package com.kjooy0305.padmacro

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class MacroRunnerTest {

    /** 가짜 시계: sleep 하면 시간이 그만큼 바로 흐른다 */
    open class FakeClock : MacroRunner.Clock {
        var t = 0L
        override fun now() = t
        override fun sleep(ms: Long) { t += ms }
    }

    class FakeActions : MacroRunner.Actions {
        val calls = mutableListOf<String>()
        var textAppearsAt = Long.MAX_VALUE
        var clock: FakeClock? = null
        override fun tap(x: Int, y: Int, count: Int) = true.also { calls += "tap $x,$y x$count" }
        override fun longPress(x: Int, y: Int, durationMs: Long) = true.also { calls += "long $x,$y $durationMs" }
        override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, durationMs: Long) = true.also { calls += "swipe" }
        override fun capture(label: String) = true.also { calls += "capture $label" }
        override fun launchApp(packageName: String) = true.also { calls += "app $packageName" }
        override fun global(action: String) = true.also { calls += "global $action" }
        override fun clickText(text: String, exact: Boolean) = ((clock?.t ?: 0) >= textAppearsAt).also { calls += "clicktext $text" }
        override fun textVisible(text: String) = ((clock?.t ?: 0) >= textAppearsAt).also { calls += "textvisible $text" }
        override fun openUrl(url: String) = true.also { calls += "url $url" }
        override fun vibrate(ms: Long) { calls += "vibrate $ms" }
    }

    class Rec : MacroRunner.Listener {
        val steps = mutableListOf<String>()
        val countdowns = mutableListOf<Long>()
        var result: MacroRunner.Result? = null
        override fun onCountdown(remainingMs: Long) { countdowns += remainingMs }
        override fun onStep(iteration: Int, stepIndex: Int, step: Step) { steps += "$iteration:$stepIndex" }
        override fun onFinished(result: MacroRunner.Result) { this.result = result }
    }

    private fun run(macro: Macro, actions: FakeActions = FakeActions(), clock: FakeClock = FakeClock()): Triple<FakeActions, Rec, FakeClock> {
        actions.clock = clock
        val rec = Rec()
        MacroRunner(macro, actions, rec, clock).run()
        return Triple(actions, rec, clock)
    }

    @Test
    fun `runs steps in order with gap and repeats`() {
        val m = Macro(steps = mutableListOf(Step.Tap(10, 20), Step.Wait(1000), Step.Capture("a")), repeat = 2, gapMs = 500, startDelayMs = 0)
        val (a, rec, clock) = run(m)
        assertEquals(listOf("tap 10,20 x1", "capture a", "tap 10,20 x1", "capture a"), a.calls)
        assertEquals(listOf("1:0", "1:1", "1:2", "2:0", "2:1", "2:2"), rec.steps)
        assertEquals(MacroRunner.Result.DONE, rec.result)
        // 반복마다: tap 뒤 gap 500 + wait 1000 (wait 뒤엔 gap 없음) + capture(마지막, gap 없음) = 1500, 반복 사이 500
        assertEquals(1500 + 500 + 1500, clock.t)
    }

    @Test
    fun `countdown reports each second then zero`() {
        val m = Macro(steps = mutableListOf(Step.Tap(1, 1)), startDelayMs = 2500, gapMs = 0)
        val (_, rec, clock) = run(m)
        assertEquals(listOf(2500L, 2000L, 1000L, 0L), rec.countdowns)
        assertEquals(2500, clock.t)
    }

    @Test
    fun `stop during wait ends with STOPPED`() {
        val m = Macro(steps = mutableListOf(Step.Wait(60_000), Step.Tap(1, 1)), startDelayMs = 0)
        val actions = FakeActions()
        val clock = object : FakeClock() {
            var runner: MacroRunner? = null
            override fun sleep(ms: Long) { super.sleep(ms); if (t >= 1000) runner?.stop() }
        }
        actions.clock = clock
        val rec = Rec()
        val runner = MacroRunner(m, actions, rec, clock)
        clock.runner = runner
        runner.run()
        assertEquals(MacroRunner.Result.STOPPED, rec.result)
        assertTrue(actions.calls.isEmpty())
    }

    @Test
    fun `click text retries until it appears`() {
        val m = Macro(steps = mutableListOf(Step.ClickText("확인", timeoutMs = 5000)), startDelayMs = 0)
        val a = FakeActions().apply { textAppearsAt = 1000 }
        val (actions, rec, _) = run(m, a)
        assertEquals(MacroRunner.Result.DONE, rec.result)
        assertTrue(actions.calls.count { it.startsWith("clicktext") } >= 3)
    }

    @Test
    fun `click text timeout fails macro with message`() {
        val m = Macro(steps = mutableListOf(Step.ClickText("없는글자", timeoutMs = 1000), Step.Tap(1, 1)), startDelayMs = 0)
        val a = FakeActions()
        val rec = Rec(); val clock = FakeClock(); a.clock = clock
        val runner = MacroRunner(m, a, rec, clock); runner.run()
        assertEquals(MacroRunner.Result.FAILED, rec.result)
        assertTrue(runner.lastError!!.contains("없는글자"))
        assertFalse(a.calls.any { it.startsWith("tap") })
    }

    @Test
    fun `wait text without stop continues`() {
        val m = Macro(steps = mutableListOf(Step.WaitText("x", 500, stopIfMissing = false), Step.Tap(1, 1)), startDelayMs = 0, gapMs = 0)
        val (a, rec, _) = run(m)
        assertEquals(MacroRunner.Result.DONE, rec.result)
        assertTrue(a.calls.contains("tap 1,1 x1"))
    }

    @Test
    fun `infinite repeat runs until stopped`() {
        val m = Macro(steps = mutableListOf(Step.Tap(1, 1)), repeat = 0, gapMs = 100, startDelayMs = 0)
        val a = FakeActions()
        val clock = object : FakeClock() {
            var runner: MacroRunner? = null
            override fun sleep(ms: Long) { super.sleep(ms); if (t >= 1000) runner?.stop() }
        }
        a.clock = clock
        val rec = Rec()
        val runner = MacroRunner(m, a, rec, clock); clock.runner = runner
        runner.run()
        assertEquals(MacroRunner.Result.STOPPED, rec.result)
        assertTrue(a.calls.size in 9..12)
    }

    @Test
    fun `empty macro`() {
        val (_, rec, _) = run(Macro())
        assertEquals(MacroRunner.Result.EMPTY, rec.result)
    }

    @Test
    fun `step and macro json roundtrip`() {
        val m = Macro(
            name = "테스트", repeat = 3, gapMs = 250, startDelayMs = 1500, repeatGapMs = 2000, vibrateOnEnd = false,
            steps = mutableListOf(
                Step.Tap(1, 2, 2), Step.LongPress(3, 4, 700), Step.Swipe(1, 2, 3, 4, 350), Step.Wait(1200),
                Step.Capture("앗"), Step.LaunchApp("com.x", "엑스"), Step.Global(Step.HOME),
                Step.ClickText("확인", 3000, true), Step.WaitText("로딩", 4000, false), Step.OpenUrl("https://a.b"), Step.Vibrate(200),
            ),
        )
        val back = Macro.fromJson(JSONObject(m.toJson().toString()))
        assertEquals(m, back)
    }

    @Test
    fun `unknown step type is skipped`() {
        val json = """{"id":"a","name":"n","steps":[{"type":"zzz"},{"type":"tap","x":1,"y":2}]}"""
        val m = Macro.fromJson(JSONObject(json))
        assertEquals(1, m.steps.size)
        assertEquals(Step.Tap(1, 2), m.steps[0])
    }

    @Test
    fun `describe is readable`() {
        assertEquals("탭  (10, 20)", Step.Tap(10, 20).describe())
        assertEquals("대기  1.5초", Step.Wait(1500).describe())
        assertEquals("대기  2초", Step.Wait(2000).describe())
        assertEquals("대기  300ms", Step.Wait(300).describe())
    }
}
