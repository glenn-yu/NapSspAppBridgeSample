# NapSsp Hybrid App Bridge 통합 개발 가이드 (AOS & iOS)

이 문서는 Nap SSP Native SDK를 하이브리드 앱(WebView)에 통합하기 위한 **3대 핵심 요소**와 **정석 구현 방법**을 다룹니다.

---

## 1. 하이브리드 통합의 3대 핵심 요소 (The 3 Pillars)

성공적인 광고 연동을 위해 아래 3가지 파일(또는 로직)이 반드시 쌍을 이뤄야 합니다.

### ① 웹 기둥 (Web Side - JavaScript)
*   **역할**: 사용자 액션(버튼 클릭)을 감지하여 네이티브에 JSON 명령을 전달하고 결과를 수신합니다.
*   **핵심**: 중복 호출 방지(Debounce) 로직이 포함되어야 합니다.
*   **참조 파일**: `index.html`

### ② 브릿지 기둥 (Native Glue - Bridge Screen)
*   **역할**: 웹뷰와 네이티브 엔진 사이의 통로입니다. 웹의 JSON을 파싱하여 엔진을 호출합니다.
*   **핵심**: 광고 갱신 시 UI를 강제로 리프레시하는 **세션 관리(`key`/`id`)**가 핵심입니다.
*   **참조 파일**: `HybridWebViewScreen.kt` (AOS), `HybridWebViewScreen.swift` (iOS)

### ③ SDK 엔진 기둥 (Native Engine - SDK Integration)
*   **역할**: 실제 광고 SDK를 호출하고, 광고 객체의 **생명주기(Lifecycle)를 엄격하게 관리**합니다.
*   **핵심**: `Already Exist` 오류를 막는 **정석 파괴 시퀀스**가 이 레이어에 구현됩니다.
*   **참조 파일**: `NapSspSdkIntegration.kt` (AOS), `NapSspSdkIntegration.swift` (iOS)

---

## 2. [가장 중요] 플랫폼별 정석 파괴 시퀀스

동일한 ID로 광고를 다시 부를 때 발생하는 오류를 방지하기 위해, 새로운 광고를 만들기 전 반드시 기존 객체를 아래 순서대로 처리해야 합니다.

### 2.1 Android (Kotlin) 시퀀스
1.  **가시성 제거**: `banner.visibility = View.GONE` (엔진 중단 신호)
2.  **일시 정지**: `banner.onPause()` (리소스 정리 시작)
3.  **파괴**: `banner.onDestroy()` (리스너 강제 해제)
4.  **부모 제거**: `parent.removeView(banner)` (레이아웃 탈착)
5.  **참조 제거**: `banner = null` (메모리 해제)

### 2.2 iOS (Swift) 시퀀스
1.  **가시성 제거**: `view.isHidden = true`
2.  **중단**: `banner.stop()` (리스너 해제 및 리소스 정리)
3.  **부모 제거**: `view.removeFromSuperview()` (UI 계층 탈착)
4.  **참조 제거**: `banner = nil` (메모리 해제)

---

## 3. 하이브리드 통신 규격 (Standard JSON Bridge)

### 3.1 Web → Native (Request)
```javascript
const request = {
    action: "loadAd",
    params: { format: "banner", adUnitId: "104704" }
};
// Android
window.NapSspBridge.postMessage(JSON.stringify(request));
// iOS
window.webkit.messageHandlers.NapSspBridge.postMessage(JSON.stringify(request));
```

---

## 4. UI 갱신 및 관리 (HybridWebView)

### 4.1 UI 강제 리프레시 (Key Strategy)
동일한 영역에 광고를 계속 새로 그리기 위해 고유 세션 ID를 사용합니다.

*   **Android (Compose)**: 
    ```kotlin
    key(adSessionId) { 
        AndroidView(factory = { context -> FrameLayout(context).apply { addView(adView) } }) 
    }
    ```
*   **iOS (SwiftUI)**: 
    ```swift
    AdViewRepresentable(adView: view).id(adViewId)
    ```

---

## 5. 자주 발생하는 오류 해결

| 오류 현상 | 원인 및 해결책 |
| :--- | :--- |
| **Already Exist** | 위 섹션 2의 **정석 파괴 시퀀스**를 준수했는지 확인하세요. 특히 `onDestroy()`/`stop()` 호출 후 `Map`에서 확실히 제거해야 합니다. |
| **중복 호출 (Double Firing)** | 네이티브 브릿지에서 타임스탬프 기반 락(Lock)을 걸거나, JS에서 `isProcessing` 플래그를 사용하세요. |
| **빈 화면 (AOS)** | `AndroidManifest.xml`에 `usesCleartextTraffic="true"` 설정을 확인하세요. |

---
*Nasmedia Technical Support (2026-04-16 Updated)*
