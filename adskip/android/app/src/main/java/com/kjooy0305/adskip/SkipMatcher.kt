package com.kjooy0305.adskip

/**
 * '광고 건너뛰기' 버튼을 알아보는 순수 규칙 모음.
 * 안드로이드 클래스에 의존하지 않아 JVM 단위 테스트로 바로 검증한다.
 *
 * 원리: 접근성 트리의 노드는 (1) 리소스 아이디, (2) 글자(text), (3) 설명(contentDescription)을 갖는다.
 * 유튜브 앱은 건너뛰기 버튼에 고정된 아이디를 쓰고, 웹(브라우저)은 글자만 노출하므로 둘 다 본다.
 */
object SkipMatcher {

    /** 유튜브 앱이 건너뛰기 버튼에 붙여 온 리소스 아이디들(버전마다 이름이 달라 여러 개를 본다). */
    val VIEW_IDS: List<String> = listOf(
        "com.google.android.youtube:id/skip_ad_button",
        "com.google.android.youtube:id/skip_ad_text",
        "com.google.android.youtube:id/skip_button",
        "com.google.android.youtube:id/player_skip_ad_button",
        "com.google.android.youtube:id/ad_skip_button",
    )

    /** 버튼에 적힌 글자. 정확히 일치할 때만 누른다(‘5초 후 건너뛰기’ 같은 안내 글자는 제외). */
    val TEXTS: List<String> = listOf(
        "광고 건너뛰기",
        "광고 건너뛰기 ",
        "건너뛰기",
        "Skip ads",
        "Skip ad",
        "Skip Ad",
        "Skip",
    )

    /** 글자 정규화: 앞뒤 공백 제거, 연속 공백 하나로, 소문자. */
    fun normalize(s: CharSequence?): String =
        s?.toString()?.trim()?.replace(Regex("\\s+"), " ")?.lowercase() ?: ""

    private val normalizedTexts: Set<String> by lazy { TEXTS.map { normalize(it) }.toSet() }

    /** 검색에 쓸 글자 목록(중복 제거, 정규화 전 원문). */
    val SEARCH_TEXTS: List<String> = TEXTS.map { it.trim() }.distinct()

    fun isSkipViewId(viewId: CharSequence?): Boolean {
        val id = viewId?.toString() ?: return false
        if (id in VIEW_IDS) return true
        // 아이디 끝부분만으로도 판단(패키지 접두어가 다른 유튜브 변형 앱 대비).
        val tail = id.substringAfterLast('/')
        return tail == "skip_ad_button" || tail == "player_skip_ad_button" || tail == "skip_ad_text"
    }

    /** text 또는 contentDescription 이 건너뛰기 글자와 정확히 같으면 true. */
    fun isSkipLabel(text: CharSequence?, contentDescription: CharSequence?): Boolean {
        val t = normalize(text)
        val d = normalize(contentDescription)
        return (t.isNotEmpty() && t in normalizedTexts) || (d.isNotEmpty() && d in normalizedTexts)
    }

    /** 노드 하나를 보고 건너뛰기 버튼인지 판단. */
    fun matches(viewId: CharSequence?, text: CharSequence?, contentDescription: CharSequence?): Boolean =
        isSkipViewId(viewId) || isSkipLabel(text, contentDescription)

    /** 같은 버튼을 연타하지 않도록 두는 최소 간격(ms). */
    const val MIN_GAP_MS = 800L

    fun canClickAgain(lastClickAt: Long, now: Long): Boolean = lastClickAt == 0L || now - lastClickAt >= MIN_GAP_MS
}
