# 패드 매크로 (PadMacro)

패드·폰에서 터치를 대신 하는 매크로 앱. `android/` 가 전부이며 Kotlin 네이티브입니다.

## 구조
- `Step.kt` / `Macro.kt` — 단계·매크로 모델과 JSON 저장 형식 (type 이름은 바꾸지 말 것)
- `MacroRunner.kt` — 실행 엔진(순서·반복·대기·중단). 안드로이드 의존 없음 → `MacroRunnerTest`
- `MacroService.kt` — 접근성 서비스. `MacroRunner.Actions` 구현(dispatchGesture, takeScreenshot, 글자 찾기, 앱 실행, 시스템 동작), 오버레이 관리, 볼륨키
- `FloatingPanel.kt` — 떠 있는 조작 패널(TYPE_ACCESSIBILITY_OVERLAY, 권한 불필요)
- `PickerOverlay.kt` — 좌표 찍기 / 터치 녹화 오버레이(기록한 터치를 즉시 다시 보내 아래 앱이 반응하게 함)
- `MainActivity.kt` — 상태·목록·설정·기록, `EditorActivity.kt` + `StepEditor.kt` — 편집·단계 대화상자

## 빌드
```
cd padmacro/android && ./gradlew testDebugUnitTest assembleDebug
```
릴리즈: 태그 `padmacro-vX.Y.Z` → `.github/workflows/release-padmacro.yml`
