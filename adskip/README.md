# 광고 건너뛰기 (AdSkip)

유튜브 광고가 뜨면 ‘광고 건너뛰기’ 버튼을 대신 눌러 주는 도구. 튜브캐시(`com.yourtube.cash`)의 `YtService` 접근성 서비스가 쓰는 방식을 그대로 옮기고, 같은 생각을 브라우저용으로도 만들었다.

설명 페이지: https://kjooy0305.github.io/i_make_a_app/adskip/

```
adskip/
├── android/     ← 네이티브 Kotlin 앱 (AccessibilityService). SkipMatcher.kt = 버튼 판별 규칙(순수), SkipService.kt = 찾기·클릭·탭
├── extension/   ← 크롬·엣지·파이어폭스 MV3 확장 (content.js 가 본체, popup 은 설정·횟수)
├── userscript/  ← Tampermonkey 용. tools/build-userscript.js 가 extension/content.js 를 포장해 만든다 (직접 고치지 말 것)
├── web/         ← GitHub Pages 설명 페이지
├── test/        ← 실제 Chromium 검증: fake-player.html(유튜브 흉내) + run.js(스크립트) + run-ext.js(설치된 확장)
└── tools/
```

## 원리

1. 접근성 서비스는 앱 화면의 버튼·글자를 트리로 읽을 수 있고, 화면이 바뀔 때 이벤트를 받는다.
2. 유튜브 화면이 바뀔 때마다 `rootInActiveWindow` 에서 아이디(`com.google.android.youtube:id/skip_ad_button` 등) → 글자(“광고 건너뛰기”, “Skip ads” …) 순으로 버튼을 찾는다.
3. 클릭 가능한 조상까지 올라가 `performAction(ACTION_CLICK)`, 거절되면 `dispatchGesture` 로 버튼 가운데를 탭. 0.8초 안에 같은 버튼을 다시 누르지 않고, 이벤트 뒤 0.35초·1초에 한 번씩 더 본다.

브라우저: 플레이어의 `ad-showing` 클래스를 신호로, 건너뛰기 버튼 클릭 → 없으면 광고 `<video>` 를 `currentTime = duration` 으로 끝내기 → 광고 중 음소거 후 원복, 배너 칸 CSS 숨김.

## 빌드·검증

```
cd adskip/android && ./gradlew testDebugUnitTest assembleDebug      # JUnit 6건 + APK
node adskip/tools/build-userscript.js                                # content.js → userscript
node adskip/test/run.js <playwright 모듈 경로>                        # 실제 Chromium 에 content.js 주입
CHROMIUM=<chrome 경로> node adskip/test/run-ext.js <playwright 경로>  # 확장을 설치해 youtube.com(가짜 페이지로 라우팅)에서 확인
```

릴리즈: 태그 `adskip-vX.Y.Z` 푸시 → `release-adskip.yml` 이 APK · 확장 zip · 유저스크립트를 릴리즈에 올린다. 버전은 `android/app/build.gradle`(versionName) 과 `extension/manifest.json`(version) 두 곳.
