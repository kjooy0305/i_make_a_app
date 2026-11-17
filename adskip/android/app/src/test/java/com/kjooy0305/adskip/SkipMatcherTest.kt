package com.kjooy0305.adskip

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkipMatcherTest {
    @Test fun `matches youtube skip button ids`() {
        assertTrue(SkipMatcher.isSkipViewId("com.google.android.youtube:id/skip_ad_button"))
        assertTrue(SkipMatcher.isSkipViewId("com.google.android.youtube:id/player_skip_ad_button"))
        assertTrue(SkipMatcher.isSkipViewId("app.revanced.android.youtube:id/skip_ad_button"))
        assertFalse(SkipMatcher.isSkipViewId("com.google.android.youtube:id/player_control_play_pause_replay_button"))
        assertFalse(SkipMatcher.isSkipViewId(null))
    }

    @Test fun `matches korean and english labels exactly`() {
        assertTrue(SkipMatcher.isSkipLabel("광고 건너뛰기", null))
        assertTrue(SkipMatcher.isSkipLabel(" 광고  건너뛰기 ", null))
        assertTrue(SkipMatcher.isSkipLabel(null, "Skip ads"))
        assertTrue(SkipMatcher.isSkipLabel("SKIP AD", null))
        assertTrue(SkipMatcher.isSkipLabel("건너뛰기", null))
    }

    @Test fun `does not match countdown or unrelated text`() {
        assertFalse(SkipMatcher.isSkipLabel("5초 후 건너뛰기", null))
        assertFalse(SkipMatcher.isSkipLabel("광고 · 5초", null))
        assertFalse(SkipMatcher.isSkipLabel("다음 동영상 건너뛰기", null))
        assertFalse(SkipMatcher.isSkipLabel("", ""))
        assertFalse(SkipMatcher.isSkipLabel(null, null))
    }

    @Test fun `normalize collapses whitespace and case`() {
        assertEquals("skip ad", SkipMatcher.normalize("  Skip\n Ad "))
        assertEquals("", SkipMatcher.normalize(null))
    }

    @Test fun `click gap`() {
        assertFalse(SkipMatcher.canClickAgain(1000, 1500))
        assertTrue(SkipMatcher.canClickAgain(1000, 1800))
        assertTrue(SkipMatcher.canClickAgain(0, 1))
    }

    @Test fun `search texts have no duplicates and no blanks`() {
        assertEquals(SkipMatcher.SEARCH_TEXTS.size, SkipMatcher.SEARCH_TEXTS.toSet().size)
        assertTrue(SkipMatcher.SEARCH_TEXTS.none { it.isBlank() })
    }
}
