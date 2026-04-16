# 웹뷰 하이브리드

이 문서는 WebView 버튼이 네이티브 광고 코드로 가는 흐름을 보여준다.

## 순서

1. WebView에서 `init` 누르기
2. `loadBanner` 또는 다른 광고 버튼 누르기
3. 상태창에서 응답 확인

## Android

- `window.NapSspBridge.postMessage(...)`
- 네이티브가 메시지를 받아 SDK를 부름
- 실제 SDK 클래스가 없으면 실패/폴백 메시지가 남음

## iOS

- `window.webkit.messageHandlers.NapSspBridge.postMessage(...)`
- 네이티브가 메시지를 받아 SDK를 부름
- 실제 SDK 클래스가 없으면 실패/폴백 메시지가 남음
