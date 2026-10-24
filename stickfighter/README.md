# Stick Fighter

스틱맨 애니메이션 제작 앱. 7.1.1 APK를 디컴파일해서 복원한 소스입니다.

## 7.1.1 → 7.2.0 변경점

- targetSdk 27 → 34, minSdk 19 → 21, Support Library → AndroidX
- 광고(AdMob)와 INTERNET 권한 제거
- 파일 가져오기는 시스템 파일 선택기(SAF)를 사용. 자체 파일 선택기와 저장소 권한 제거
- 내보내기 파일(.sfa, .xml, .mp4)은 앱 전용 폴더(`Android/data/com.tweber.stickfighter.activities/files/`)에 저장하고, 공유는 FileProvider로 처리
- 진행률 계산에서 정수 나눗셈으로 0%에 머물던 부분을 실수 나눗셈으로 수정

## 구조

복원 과정에서 클래스 이름이 난독화된 채로 남아 있습니다(`p037g/C0887k` 등). 동작은 같지만 이름은 의미가 없습니다.
화면 클래스(`activities/*`)만 원래 이름이 남아 있습니다.

## 빌드

```
./gradlew assembleDebug
```
