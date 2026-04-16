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

## 3. 웹(HTML/JS) 구현 예시 (Full Snippet)

웹 개발자가 자신의 HTML 파일에 적용할 수 있는 표준 브릿지 호출 코드입니다.

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>NapSsp Bridge Sample</title>
  <script>
    // 1. 네이티브 명령 전달 함수 (Android/iOS 통합)
    function callNative(message) {
        if (window.NapSspBridge) {
            // Android 브릿지
            window.NapSspBridge.postMessage(message);
        } else if (window.webkit && window.webkit.messageHandlers.NapSspBridge) {
            // iOS 브릿지
            window.webkit.messageHandlers.NapSspBridge.postMessage(message);
        } else {
            console.error("Native Bridge not found");
        }
    }

    // 2. 네이티브로부터 응답을 받는 전역 콜백 함수
    window.__napSspAck = function(response) {
        document.getElementById('status').innerText = response;
        if (response.includes("clicked")) {
            console.log("광고 클릭됨!");
        }
    };
  </script>
</head>
<body>
  <h2>Nap SSP 하이브리드 광고 테스트</h2>
  <div id="status" style="padding:10px; background:#eee;">준비됨</div>

  <!-- 명령 전달 버튼들 -->
  <button onclick="callNative('init')">SDK 초기화</button>
  <button onclick="callNative('loadBanner')">배너 광고 로드</button>
  <button onclick="callNative('loadRewardVideo')">리워드 비디오</button>
</body>
</html>
```

---

## 4. 포맷별 호출 명령어 상세 가이드

JavaScript에서 네이티브 광고를 호출할 때 사용하는 명령어(`message`) 리스트입니다.

| 광고 포맷 | JS 명령어 (Message) | 설명 |
| :--- | :--- | :--- |
| **배너 (Banner)** | `loadBanner` | 하단/상단에 고정되는 320x100 등 표준 배너 호출 |
| **네이티브 (Native)** | `loadNative` | 앱 UI에 자연스럽게 녹아드는 맞춤형 광고 호출 |
| **동영상 (Video)** | `loadVideo` | 피드 중간 등에서 재생되는 아웃스트림 비디오 호출 |
| **리워드 (Reward)** | `loadRewardVideo` | 사용자가 시청 시 보상을 지급하는 전면 비디오 호출 |
| **전면 (Interstitial)** | `loadInterstitialVideo` | 화면 전체를 덮는 전면형 동영상 광고 호출 |

### 💡 포맷별 구현 팁

#### 1. 배너 & 네이티브
- 호출 즉시 네이티브 영역에 광고 뷰가 생성됩니다.
- 성공 시 `SDK Event: loaded` 콜백이 웹으로 전달됩니다.

#### 2. 리워드 비디오 (Reward Video)
- 시청 완료 시 웹으로 `rewarded` 이벤트가 전달됩니다.
- **예시 콜백**: `SDK Event: rewarded | Format: rewardVideo | Detail: success`
- 웹에서는 이 콜백을 받았을 때 포인트 지급 등의 로직을 처리하면 됩니다.

#### 3. 전면 광고 (Interstitial)
- 호출 시 화면 전체가 광고로 덮이므로, 웹뷰의 인터랙션이 잠시 중단될 수 있음을 고려해야 합니다.

---

## 4. 전체 메시지 시나리오

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
