# nap ssp 샘플 가이드

이 문서는 처음 보는 사람이 바로 따라할 수 있게 만든 짧은 안내서다.

## 1. 이 샘플이 하는 일

- Android와 iOS에서 nap ssp 광고를 붙이는 방법을 보여준다
- 일반 광고와 WebView 하이브리드 광고를 둘 다 보여준다
- 실제 SDK를 넣을 위치를 보여준다
- SDK가 없으면 폴백 화면과 상태 메시지가 보인다
- 광고가 안 뜨면 SDK가 아직 연결되지 않은 것이다

## 2. 먼저 할 일

1. 앱을 실행한다
2. 포맷 하나를 고른다
3. `광고 띄우기`를 누르거나, 하이브리드면 WebView 버튼을 누른다
4. 화면에 결과가 뜨는지 본다

## 3. 어떤 포맷을 고를까

- `banner` — 아래에 붙는 작은 광고
- `native` — 화면에 자연스럽게 섞는 광고
- `video` — 앱 안에서 재생되는 광고
- `rewardVideo` — 보면 보상이 있는 광고
- `interstitialVideo` — 화면 전체를 덮는 광고
- `hybridWebView` — 웹 버튼으로 네이티브 광고를 여는 방식

## 4. 하이브리드 WebView는 이렇게 쓴다

1. `init`을 누른다
2. `loadBanner` 또는 다른 광고 버튼을 누른다
3. 상태창에서 응답을 확인한다

## 5. 준비물

- Android Studio 또는 Xcode
- JDK / Android SDK 또는 iOS 빌드 환경
- 미디어 키와 광고 단위 ID

## 6. 막히면 볼 것

- `quickstart.md` — 전체 흐름
- `install.md` — 준비물
- `hybrid-webview.md` — WebView 연결
- `troubleshooting.md` — 문제 해결
