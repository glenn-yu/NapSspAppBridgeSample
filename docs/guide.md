# 🚀 NapSSP 하이브리드 광고 통합 풀스택 가이드 (Web to Native)

이 문서는 하이브리드 앱 개발자가 웹(JS)에서 광고를 요청하고, 네이티브(Android/iOS)에서 SDK를 실행하여 광고를 노출하는 **전체 연동 과정**을 다룹니다.

---

## 1. 연동 흐름 한눈에 보기 (Workflow)

하이브리드 광고는 **웹(요청) → 브릿지(전달) → 네이티브(실행)** 순으로 동작합니다.

1.  **Web**: 사용자가 버튼 클릭 → `callNative('loadAd', {format: 'banner'})` 호출
2.  **Bridge**: 네이티브가 웹의 JSON 메시지를 수신하여 파싱
3.  **Native**: 기존 광고 파괴 후 새 광고 객체 생성 및 `loadAd()` 호출
4.  **SDK**: 광고 로드 성공 시 네이티브 영역에 광고 노출
5.  **Callback**: 네이티브가 웹의 `onNapSspMessage()`를 호출하여 성공/실패 보고

---

## 2. Web Side (HTML/JavaScript) 구현

웹 페이지(또는 `index.html`)에 아래 코드를 추가하여 네이티브와 통신할 준비를 합니다.

### 2.1 네이티브 호출 및 응답 수신
```javascript
// [중복 호출 방지용 플래그]
let isRequesting = false;

// ① 네이티브에 광고 요청 보내기
function callNative(action, params = {}) {
    if (isRequesting) return;
    isRequesting = true;
    setTimeout(() => { isRequesting = false; }, 1000); // 1초 데바운스

    const message = JSON.stringify({ action, params });
    
    if (window.NapSspBridge) {
        // Android 호출
        window.NapSspBridge.postMessage(message);
    } else if (window.webkit?.messageHandlers?.NapSspBridge) {
        // iOS 호출
        window.webkit.messageHandlers.NapSspBridge.postMessage(message);
    }
}

// ② 네이티브로부터 결과 받기 (전역 함수)
window.onNapSspMessage = function(responseStr) {
    const res = JSON.parse(responseStr);
    console.log(`액션: ${res.action}, 상태: ${res.status}, 데이터: ${res.data}`);
};
```

---

## 3. Bridge Side (Native Connection)

웹의 메시지를 받아서 네이티브 엔진(`NapSspSdkIntegration`)으로 연결해주는 다리 역할을 합니다.

### 3.1 Android (Kotlin/Compose)
```kotlin
class NapSspHybridBridge(private val webView: WebView, val onAdLoaded: (View?) -> Unit) {
    @JavascriptInterface
    fun postMessage(jsonString: String) {
        val request = JSONObject(jsonString)
        val action = request.getString("action")
        if (action == "loadAd") {
            val format = request.getJSONObject("params").getString("format")
            webView.post { 
                val adView = NapSspSdkIntegration.loadAd(format) 
                onAdLoaded(adView) // UI 업데이트
            }
        }
    }
}
```

### 3.2 iOS (Swift/SwiftUI)
```swift
final class NapSspHybridBridge: NSObject, WKScriptMessageHandler {
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        guard let body = message.body as? String else { return }
        // JSON 파싱 후 NapSspSdkIntegration.shared.loadAd(format) 호출
    }
}
```

---

## 4. Native Side (SDK Integration - 4개 언어 지원)

가장 중요한 **"Already Exist"** 오류 방지를 위해 아래 정석 시퀀스를 각 언어별로 구현합니다.

### 4.1 Android (Kotlin & Java 공통 정석 시퀀스)
**순서: GONE → onPause → onDestroy → removeView → null**

*   **Kotlin**: `ad.visibility = View.GONE; ad.onPause(); ad.onDestroy(); activeAds.remove(format)`
*   **Java**: `ad.setVisibility(View.GONE); ad.onPause(); ad.onDestroy(); activeAds.remove(format);`

### 4.2 iOS (Swift & Obj-C 공통 정석 시퀀스)
**순서: isHidden → stop → removeFromSuperview → nil**

*   **Swift**: `ad.isHidden = true; ad.stop(); ad.removeFromSuperview(); activeAds.removeValue(forKey: format)`
*   **Obj-C**: `[ad setHidden:YES]; [ad stop]; [ad removeFromSuperview]; [activeAds removeObjectForKey:format];`

---

## 5. 어댑터(Adapter) 및 미디에이션 설정

각 광고 네트워크별 필수 설정값입니다.

| 네트워크 | Android 설정 | iOS 설정 | 비고 |
| :--- | :--- | :--- | :--- |
| **Google** | `APPLICATION_ID` (Manifest) | `GADApplicationIdentifier` (plist) | 필수 |
| **Adfit** | `registerAdapter(ADAPTER_ADFIT)` | AdMixerMediationAdFit 설치 | 필수 |
| **Pangle** | `PAGSdk.init()` 별도 호출 | `pagConfig.appID` 설정 | 필수 |
| **AppLovin**| SDK Key (Manifest) | `AppLovinSdkKey` (plist) | 필수 |

---

## 6. UI 최적화: 고유 세션 관리

웹뷰에서 광고를 계속 새로 고칠 때, UI 프레임워크가 이전 광고를 완전히 지우고 새 광고를 그리게 하려면 **고유 ID**를 부여해야 합니다.

*   **Android**: `key(UUID.randomUUID().toString()) { AndroidView(...) }`
*   **iOS**: `AdViewRepresentable(adView: view).id(UUID())`

---

## 6. 공식 기술 지원 문서 (Reference)

더 상세한 SDK 사양이나 최신 버전의 네이티브 연동 가이드는 아래 공식 문서를 참조하세요.

*   **Android SDK 공식 가이드**: [https://nasmob.atlassian.net/wiki/x/HwAPLQ](https://nasmob.atlassian.net/wiki/x/HwAPLQ)
*   **iOS SDK 공식 가이드**: [https://nasmob.atlassian.net/wiki/x/4QFiL](https://nasmob.atlassian.net/wiki/x/4QFiL)

---
*Nasmedia Technical Support (2026-04-16 Updated)*
