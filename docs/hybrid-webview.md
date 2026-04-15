# 웹뷰 하이브리드 샘플

이 문서는 nap ssp 네이티브 SDK를 **WebView/WKWebView 안에서 하이브리드로 연동**할 때의 연결 지점을 정리한다.

## 목표

- 앱 화면은 웹뷰로 구성한다.
- 네이티브 SDK는 브리지 역할로 붙인다.
- 웹 쪽에서 광고 이벤트를 호출하고, 네이티브가 SDK 로드를 담당한다.

## Android

### 권장 구조

- `WebView`를 메인 화면으로 띄운다.
- `addJavascriptInterface(...)` 또는 `WebMessageListener`로 브리지를 만든다.
- 브리지에서 `NapSspInitializer.initialize()`를 먼저 호출한다.
- 이후 네이티브 SDK 호출은 `SdkHooks` 쪽으로 넘긴다.

### 기본 흐름

1. WebView를 띄운다.
2. JS 브리지를 연결한다.
3. 웹에서 `loadAd`, `showAd`, `ready` 같은 메시지를 보낸다.
4. Android 네이티브가 SDK를 호출한다.
5. 결과를 다시 웹으로 돌려준다.

## iOS

### 권장 구조

- `WKWebView`를 메인 화면으로 띄운다.
- `WKScriptMessageHandler`로 브리지를 만든다.
- 브리지에서 `NapSspInitializer.initialize()`를 먼저 호출한다.
- 이후 네이티브 SDK 호출은 `SdkHooks` 쪽으로 넘긴다.

### 기본 흐름

1. WKWebView를 띄운다.
2. JS 메시지 핸들러를 연결한다.
3. 웹에서 `loadAd`, `showAd`, `ready` 같은 메시지를 보낸다.
4. iOS 네이티브가 SDK를 호출한다.
5. 결과를 다시 웹으로 돌려준다.

## 브리지에서 다룰 메시지 예시

- `init`
- `loadBanner`
- `loadNative`
- `loadVideo`
- `loadRewardVideo`
- `loadInterstitialVideo`
- `getStatus`

## 주의할 점

- 웹만으로 광고를 붙이지 말고, 실제 SDK 호출은 네이티브가 한다.
- 광고 단위 ID와 미디어 키는 네이티브 쪽에서 관리한다.
- 메시지 이름은 웹/앱 둘 다에서 고정해 둔다.
- 브리지 실패 시 웹뷰 화면이 죽지 않게 에러를 돌려준다.
