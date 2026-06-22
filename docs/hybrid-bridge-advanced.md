# Hybrid Bridge Advanced Integration & Troubleshooting Guide / 고급 연동 및 트러블슈팅 가이드

[English](#english) | [한국어](#한국어)

---

## English

This guide covers advanced integration topics and troubleshooting tips when deploying the Nap SSP Hybrid Bridge in production environments.

### 1. Layout Optimization by Ad Format

In hybrid applications, native ad views are placed on top of or below the WebView. Below are the recommended native container configurations for each format:

| Format | Recommended Height | Features |
| :--- | :--- | :--- |
| **Banner** | 100dp / 100pt | Height capable of accommodating both 320x50 and 320x100 configurations |
| **Native** | 350~450dp | Flexible depending on asset density; ensure sufficient spacing |
| **Video** | 250dp | 16:9 ratio is recommended for outstream video rendering |
| **Full-Screen** | 0 (None) | Interstitial overlays do not occupy native UI layout spaces |

---

### 2. Debugging and Logging

If you encounter bridge communication issues, verify logs using the following tools:

#### 2.1 Web Side (Chrome/Safari Inspector)
* Attach the device web inspector to verify if `onNapSspMessage` callbacks are being dispatched.
* If `REQ: ...` entries do not appear, audit your JavaScript `callNative` implementations.

#### 2.2 Native Side (Logcat / Xcode Console)
* **Android**: Filter for the `NapSspHybridBridge` tag in Android Studio `Logcat`.
* **iOS**: Check the Xcode Console for `[HybridBridge]` or active SDK logs.

---

### 3. Platform Detection and Resolution

To manage platform-specific layouts from a single, unified web codebase, implement a detection logic:

```javascript
const userAgent = navigator.userAgent;
const isNapSspBridge = userAgent.includes("NapSspHybridBridge");

function getPlatform() {
    if (window.NapSspBridge) return "android";
    if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.NapSspBridge) return "ios";
    return "web";
}
```

---

### 4. Troubleshooting FAQ

#### Q. I sent an ad load request but received no response.
* **A1**: Verify that the SDK initialization (`init`) action has been invoked beforehand.
* **A2**: Check device native logs for `Invalid AdUnitId` exceptions.

#### Q. The bridge does not function on iOS only.
* **A1**: Verify that you are calling `window.webkit.messageHandlers.NapSspBridge.postMessage` precisely. (iOS uses a different handler syntax than Android).
* **A2**: Verify that HTTP connections are allowed in your `Info.plist`'s `NSAppTransportSecurity` mapping.

#### Q. The ad view covers my Web UI elements.
* **A**: Adjust the Z-Index or native view hierarchy layouts. Refer to the sample application's `HybridWebViewScreen` implementation, which allocates a distinct, separate container at the bottom.

---

### 5. Security Recommendations

* Restrict incoming `postMessage` handlers to process data only from trusted, verified web domains.
* Before launching in production, disable detailed logging (e.g., call `AdMixerLog.setLogLevel(None)`) to prevent diagnostic logs from capturing sensitive system variables.

---

## 한국어

이 문서는 Nap SSP 하이브리드 브릿지를 실제 서비스 환경에 적용할 때 고려해야 할 심화 주제와 문제 해결 방법을 다룹니다.

### 1. 광고 포맷별 레이아웃 최적화

하이브리드 앱에서는 네이티브 광고 뷰가 웹뷰 위에 얹혀지거나 하단에 삽입됩니다. 각 포맷별로 권장되는 네이티브 영역(Container) 설정은 다음과 같습니다.

| 포맷 | 권장 높이 | 특징 |
| :--- | :--- | :--- |
| **Banner** | 100dp / 100pt | 320x50, 320x100 모두 대응 가능한 높이 권장 |
| **Native** | 350~450dp | 에셋 구성에 따라 유동적이며, 충분한 공간 확보 필요 |
| **Video** | 250dp | 아웃스트림 동영상 재생을 위해 16:9 비율 권장 |
| **Full-Screen** | 0 (전면) | 전면 광고는 네이티브 레이아웃 공간을 차지하지 않음 |

---

### 2. 디버깅 및 로그 확인

브릿지 통신이 원활하지 않을 경우 다음 방법으로 로그를 확인하십시오.

#### 2.1 Web 영역 (Chrome/Safari Inspector)
- 웹뷰를 PC와 연결하여 `onNapSspMessage` 콜백이 수신되는지 콘솔 로그를 확인합니다.
- `REQ: ...` 로그가 찍히지 않는다면 JS의 `callNative` 함수 호출부를 점검하십시오.

#### 2.2 Native 영역 (Logcat / Xcode Console)
- **Android**: `Logcat`에서 `NapSspHybridBridge` 태그를 필터링하십시오.
- **iOS**: Xcode 콘솔에서 `[HybridBridge]` 또는 SDK 로그를 확인하십시오.

---

### 3. 플랫폼 판별 및 대응

웹 코드 한 벌로 Android와 iOS를 동시에 대응하려면 아래와 같은 판별 로직을 권장합니다.

```javascript
const userAgent = navigator.userAgent;
const isNapSspBridge = userAgent.includes("NapSspHybridBridge");

function getPlatform() {
    if (window.NapSspBridge) return "android";
    if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.NapSspBridge) return "ios";
    return "web";
}
```

---

### 4. 트러블슈팅 (FAQ)

#### Q. 광고 로드 요청을 보냈는데 응답이 없어요.
- **A1**: SDK 초기화(`init`) 액션을 먼저 호출했는지 확인하십시오.
- **A2**: 네이티브 로그에서 `Invalid AdUnitId` 에러가 발생하는지 확인하십시오.

#### Q. iOS에서만 브릿지가 작동하지 않아요.
- **A1**: `window.webkit.messageHandlers.NapSspBridge.postMessage` 형식을 정확히 사용했는지 확인하십시오. (Android와 호출 방식이 다릅니다.)
- **A2**: `Info.plist`에 `NSAppTransportSecurity` (HTTP 허용) 설정이 누락되지 않았는지 확인하십시오.

#### Q. 광고 뷰가 웹 UI를 가려요.
- **A**: 네이티브 영역의 Z-Index 또는 레이아웃 계층을 조정해야 합니다. 샘플 앱의 `HybridWebViewScreen` 구현 방식(웹뷰 하단에 별도 영역 할당)을 참고하십시오.

---

### 5. 보안 권장사항

- `postMessage`로 전달되는 JSON 데이터는 신뢰할 수 있는 도메인에서만 발송되도록 웹 영역에서 제한을 두는 것이 좋습니다.
- 프로덕션 배포 시에는 `AdMixerLog.setLogLevel(None)`을 호출하여 민감한 정보가 로그에 남지 않도록 하십시오.
