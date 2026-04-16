# Nap SSP Hybrid WebView Bridge 연동 가이드

이 문서는 하이브리드 앱의 WebView(Html/JS) 환경에서 Nap SSP Native SDK를 호출하기 위한 브릿지 연동 방법을 설명합니다.

---

## 1. 개요

하이브리드 앱에서는 웹뷰의 JS가 네이티브 영역에 광고 로드를 요청하고, 네이티브 SDK가 광고 뷰를 생성하여 웹뷰 위 또는 지정된 영역에 표시합니다.

- **통신 방식**: JSON 기반 메시지 패싱
- **Bridge Name**: `NapSspBridge`
- **JS -> Native**: `postMessage(jsonString)` 호출
- **Native -> JS**: `onNapSspMessage(jsonString)` 콜백 수신

---

## 2. JavaScript 인터페이스 명세

### 2.1 광고 호출 (Request)

웹에서 네이티브로 광고를 요청할 때는 아래의 JSON 구조를 사용합니다.

```javascript
const request = {
  action: "loadAd",
  params: {
    format: "banner", // banner, native, video, rewardVideo, interstitialVideo
    adUnitId: "YOUR_ADUNIT_ID" // (옵션) 네이티브에서 기본값 사용 가능
  }
};

// Android 호출
window.NapSspBridge.postMessage(JSON.stringify(request));

// iOS 호출
window.webkit.messageHandlers.NapSspBridge.postMessage(JSON.stringify(request));
```

### 2.2 결과 수신 (Response Callback)

네이티브 작업 결과는 웹의 전역 함수 `onNapSspMessage`로 전달됩니다.

```javascript
window.onNapSspMessage = function(responseStr) {
  const response = JSON.parse(responseStr);
  console.log("Action:", response.action);
  console.log("Status:", response.status); // success, error
  console.log("Data:", response.data);
};
```

---

## 3. 플랫폼별 구현 가이드

### 3.1 Android (Kotlin)

1. **JavascriptInterface 정의**: `postMessage` 메서드를 가진 클래스를 생성합니다.
2. **Bridge 등록**: `webView.addJavascriptInterface(bridge, "NapSspBridge")` 호출.
3. **JSON 파싱**: `JSONObject`를 사용하여 요청을 처리합니다.

```kotlin
class NapSspHybridBridge(private val webView: WebView) {
    @JavascriptInterface
    fun postMessage(jsonString: String) {
        val request = JSONObject(jsonString)
        if (request.getString("action") == "loadAd") {
            // SDK 호출 로직
        }
    }
}
```

### 3.2 iOS (Swift)

1. **WKScriptMessageHandler 구현**: `userContentController`에서 메시지를 수신합니다.
2. **Bridge 등록**: `WKUserContentController.add(handler, name: "NapSspBridge")` 호출.
3. **JSON 파싱**: `JSONDecoder`를 사용하여 요청을 처리합니다.

```swift
final class NapSspHybridBridge: NSObject, WKScriptMessageHandler {
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        if message.name == "NapSspBridge", let body = message.body as? String {
            // JSON 파싱 및 SDK 호출
        }
    }
}
```

---

## 4. 지원 광고 포맷 및 파라미터

| Format | 설명 | 표시 방식 |
| :--- | :--- | :--- |
| `banner` | 320x50, 320x100 배너 | 지정된 Native Area에 삽입 |
| `native` | 네이티브 광고 (에셋형) | 지정된 Native Area에 삽입 |
| `video` | 아웃스트림 동영상 | 지정된 Native Area에 삽입 |
| `rewardVideo` | 리워드형 동영상 | 전면(Full-screen) 노출 |
| `interstitialVideo` | 전면 동영상 | 전면(Full-screen) 노출 |

---

## 5. 샘플 코드

상세한 구현 예시는 프로젝트 내 다음 파일을 참조하세요:
- **HTML**: `examples/hybrid-sample.html`
- **Android**: `android/.../HybridWebViewScreen.kt`
- **iOS**: `ios/.../HybridWebViewScreen.swift`
