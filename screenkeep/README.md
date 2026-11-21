# 화면 유지 (screenkeep)

전원 버튼을 누르기 전에는 화면이 저절로 꺼지지 않게 하는 안드로이드 앱. 패키지 `com.kjooy0305.screenkeep`, 흑백 화면.

## 동작 방식
- **켜기**: 포그라운드 서비스가 다른 앱 위에 보이지 않는 1픽셀 창(`TYPE_APPLICATION_OVERLAY`, `FLAG_KEEP_SCREEN_ON`, 터치·포커스 없음)을 띄운다. 창이 떠 있는 동안 자동 꺼짐이 막히고, 전원 버튼은 평소처럼 화면을 끈다.
- **유지 시간**: 계속 / 15분 / 30분 / 1시간 / 2시간 / 4시간 / 직접(1~1440분). 끝나면 자동으로 해제되고 기기 기본 꺼짐 시간을 따른다.
- **기기 기본 꺼짐 시간**: `WRITE_SETTINGS` 허용 후 15초~30분 중에서 바꾼다.
- **이유 없이 꺼지는 문제 대응**: 30초마다 창이 살아 있는지 확인해 다시 띄우고, `START_STICKY`와 앱을 열 때의 복구로 기기가 서비스를 정리해도 되살린다. 배터리 최적화 제외를 안내한다.
- **꺼짐 기록**: 화면 꺼짐/켜짐, 서비스 종료·재시작을 배터리·충전·절전 모드와 함께 남겨 원인을 비교할 수 있다. 유지 중에 꺼진 기록은 굵게 표시한다.
- 빠른 설정 칸, 알림의 끄기 버튼, 부팅 시 자동 시작(설정).

## 권한
`SYSTEM_ALERT_WINDOW`(필수), `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS`, `WRITE_SETTINGS`, `RECEIVE_BOOT_COMPLETED`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.

## 시험
`./gradlew :app:testDebugUnitTest` — 시간 계산·기록(LogicTest), 서비스가 올바른 창을 만들고 권한 없음·만료·재시작을 처리하는지(ServiceTest, Robolectric), 화면 캡처(ScreenshotTest). 실제 기기에서의 화면 유지·빠른 설정 칸은 시험 환경에서 확인할 수 없다.

## 릴리스
태그 `screenkeep-vX.Y.Z` 를 push 하면 `.github/workflows/release-screenkeep.yml` 이 서명된 APK를 만든다.
