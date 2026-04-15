# 웹뷰 하이브리드 샘플

이 문서는 "웹페이지 버튼 → 네이티브 광고 코드" 흐름을 아주 쉽게 보여준다.

## 쉽게 말하면

1. 웹페이지에서 버튼을 누른다.
2. WebView가 그 메시지를 앱에 보낸다.
3. 앱이 nap ssp SDK를 부른다.
4. 광고가 뜨거나, 안 뜨면 폴백 화면이 보인다.

## Android

- 웹페이지 버튼이 `window.NapSspBridge.postMessage(...)`를 호출한다.
- `NapSspHybridBridge`가 메시지를 받아 SDK 호출로 넘긴다.
- 메시지 예시: `init`, `loadBanner`, `loadNative`, `loadVideo`, `loadRewardVideo`, `loadInterstitialVideo`

## iOS

- 웹페이지 버튼이 `window.webkit.messageHandlers.NapSspBridge.postMessage(...)`를 호출한다.
- `NapSspHybridBridge`가 메시지를 받아 SDK 호출로 넘긴다.
- 메시지 예시는 Android와 같다.

## 초보자 팁

- WebView는 화면만 담당한다.
- 실제 광고는 네이티브 앱 코드가 담당한다.
- 먼저 `init` 버튼부터 눌러보면 된다.
