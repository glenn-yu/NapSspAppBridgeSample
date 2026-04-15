# iOS 시작하기

## 먼저 하는 일

앱이 시작할 때 `NapSspInitializer.initialize()`를 먼저 호출한다.
그 다음에 포맷별 화면에서 배너 / 네이티브 / 동영상 / 리워드 / 전면 동영상을 붙인다.

## 구조

- `ContentView` — 샘플 화면 진입점
- `SampleFormat` — 포맷 목록
- `SampleViewModel` — 선택 상태
- `SdkHooks` — 실제 SDK 코드가 들어갈 자리
- `NapSspInitializer` — SDK 초기화 자리

## 다음 단계

실제 nap ssp iOS SDK를 넣으면, 각 포맷의 hook 함수 안에 광고 뷰 생성과 로드를 넣는다.
