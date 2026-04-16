# 🔗 하이브리드 앱 브릿지 (Web ↔ Native) 상세 가이드

이 문서는 웹뷰 내부의 JavaScript와 앱 네이티브 코드가 데이터를 주고받는 상세 메커니즘을 설명합니다.

---

## 1. Web → Native (명령 전달)

웹에서 네이티브 기능을 호출할 때 사용하는 인터페이스입니다.

### [Android]
Android는 `addJavascriptInterface`를 사용하여 JS 객체를 노출합니다.
```javascript
// JS 호출부
window.NapSspBridge.postMessage('loadBanner');
```
- **네이티브 수신**: `NapSspHybridBridge.postMessage(message: String)` 함수가 실행됩니다.

### [iOS]
iOS는 `WKScriptMessageHandler`를 사용합니다.
```javascript
// JS 호출부
window.webkit.messageHandlers.NapSspBridge.postMessage('loadBanner');
```
- **네이티브 수신**: `userContentController(_:didReceive:)` 델리게이트 메서드에서 메시지를 받습니다.

---

## 2. Native → Web (이벤트 콜백)

네이티브 광고 SDK에서 발생한 이벤트(로드 완료, 클릭 등)를 다시 웹으로 알려주는 방식입니다.

### [공통 JavaScript 코드]
웹 페이지에는 이벤트를 수신할 전역 함수가 정의되어 있어야 합니다.
```javascript
window.__napSspAck = function(message) {
  console.log("Native로부터 수신:", message);
  document.getElementById('log').textContent = message;
};
```

### [네이티브 전달 방식]
- **Android**: `webView.evaluateJavascript("window.__napSspAck('...')", null)`
- **iOS**: `webView.evaluateJavaScript("window.__napSspAck('...')")`

---

## 3. 전체 메시지 시나리오

브릿지를 통해 전달되는 주요 메시지 타입입니다.

| 메시지 명칭 | 방향 | 설명 |
| :--- | :---: | :--- |
| `init` | Web → Native | SDK 초기화 명령 |
| `loadBanner` | Web → Native | 배너 광고 로드 요청 |
| `SDK Event: loaded` | Native → Web | 광고 로드 성공 알림 |
| `SDK Event: clicked`| Native → Web | 광고 클릭 이벤트 알림 |
| `SDK Event: failed` | Native → Web | 광고 로드 실패 및 원인 전달 |

---

## 4. 보안 주의사항 (Security Tips)
- **도메인 검증**: 실제 운영 환경에서는 `webViewClient`나 `navigationDelegate`를 통해 허용된 도메인에서만 브릿지가 동작하도록 제한하는 것이 안전합니다.
- **데이터 난독화**: 민감한 정보는 브릿지를 통해 평문으로 전달하지 않도록 주의하세요.

---
*더 상세한 구현 코드는 프로젝트 내 `HybridWebViewScreen` 파일을 참고하세요.*
