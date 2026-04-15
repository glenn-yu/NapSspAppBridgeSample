# 웹뷰 하이브리드 샘플

이 문서는 nap ssp 네이티브 SDK를 **WebView/WKWebView 안에서 하이브리드로 연동**할 때의 연결 지점을 정리한다.

## 목표

- 앱 화면은 웹뷰로 구성한다.
- 네이티브 SDK는 브리지 역할로 붙인다.
- 웹 쪽에서 광고 이벤트를 호출하고, 네이티브가 SDK 로드를 담당한다.

## 공통 브리지 규약

웹 페이지는 다음 이름으로 네이티브에 메시지를 보낸다.

- Android: `window.NapSspBridge.postMessage(...)`
- iOS: `window.webkit.messageHandlers.NapSspBridge.postMessage(...)`

### 메시지 예시

- `init`
- `loadBanner`
- `loadNative`
- `loadVideo`
- `loadRewardVideo`
- `loadInterstitialVideo`
- `getStatus`

## Android

### 권장 구조

- `WebView`를 메인 화면으로 띄운다.
- `addJavascriptInterface(...)`로 브리지를 만든다.
- 브리지에서 `NapSspInitializer.initialize()`를 먼저 호출한다.
- 이후 네이티브 SDK 호출은 `SdkHooks` 쪽으로 넘긴다.

### 웹에서 호출하는 예시

```html
<script>
  window.NapSspBridge.postMessage(JSON.stringify({ type: 'init' }))
  window.NapSspBridge.postMessage(JSON.stringify({ type: 'loadBanner' }))
</script>
```

## iOS

### 권장 구조

- `WKWebView`를 메인 화면으로 띄운다.
- `WKScriptMessageHandler`로 브리지를 만든다.
- 브리지에서 `NapSspInitializer.initialize()`를 먼저 호출한다.
- 이후 네이티브 SDK 호출은 `SdkHooks` 쪽으로 넘긴다.

### 웹에서 호출하는 예시

```html
<script>
  window.webkit.messageHandlers.NapSspBridge.postMessage({ type: 'init' })
  window.webkit.messageHandlers.NapSspBridge.postMessage({ type: 'loadBanner' })
</script>
```

## 샘플에서 지금 확인되는 것

- 웹뷰 화면이 실제로 뜬다.
- 브리지 메시지가 네이티브 로그로 들어온다.
- 웹 쪽에서 메시지 규약만 맞추면 바로 확장할 수 있다.

## 주의할 점

- 웹만으로 광고를 붙이지 말고, 실제 SDK 호출은 네이티브가 한다.
- 광고 단위 ID와 미디어 키는 네이티브 쪽에서 관리한다.
- 메시지 이름은 웹/앱 둘 다에서 고정해 둔다.
- 브리지 실패 시 웹뷰 화면이 죽지 않게 에러를 돌려준다.
