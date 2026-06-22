# Nap SSP Hybrid WebView Bridge Integration Guide / 하이브리드 웹뷰 브릿지 연동 가이드

[English](#english) | [한국어](#한국어)

---

## English

This guide describes how to bridge and communicate with the Nap SSP Native SDK using JavaScript (Html/JS) within a hybrid WebView application.

### 1. Overview

In hybrid applications, the JavaScript layer in the WebView requests ad loads from the native layer. The native SDK then instantiates the ad views and renders them on top of the WebView or within designated layout containers.

* **Communication Method**: JSON-based Message Passing
* **Bridge Interface Name**: `NapSspBridge`
* **JS ➡️ Native**: `postMessage(jsonString)`
* **Native ➡️ JS**: `onNapSspMessage(jsonString)` (Callback)

---

### 2. JavaScript Interface Specifications

#### 2.1 Request Formats (JS ➡️ Native)

Use the following JSON payloads when requesting actions from JavaScript:

```javascript
// 1. Initialize SDK (Recommended once on page load)
window.NapSspBridge.postMessage(JSON.stringify({ action: "init" }));

// 2. Load Ad
const request = {
  action: "loadAd",
  requestId: "uuid-or-client-id", // Optional: echoed back on the response and on subsequent events
  params: {
    format: "banner", // banner, native, video, rewardVideo, interstitialVideo, interstitialBanner
    adUnitId: "YOUR_ADUNIT_ID" // Optional custom ad unit ID
  }
};
window.NapSspBridge.postMessage(JSON.stringify(request));

// 3. Clear all ads & release memory
window.NapSspBridge.postMessage(JSON.stringify({ action: "clearAds" }));
```

> **`requestId` (optional, all actions)** — If you attach a `requestId` to any request, the native bridge echoes it back on the matching response **and** on the SDK lifecycle events that follow that `loadAd` (`event` action). This lets the web layer correlate an asynchronous `loaded`/`clicked`/`closed` event with the request that triggered it. Because only one ad request is active at a time, events carry the `requestId` of the most recent `loadAd` until the next `loadAd` or `clearAds`. The field is omitted from responses when the request did not include it.

#### 2.2 Callback Receiver (Native ➡️ JS)

Native events and SDK status reports are routed back to the global `onNapSspMessage` JavaScript handler.

```javascript
window.onNapSspMessage = function(responseStr) {
  const response = JSON.parse(responseStr);
  
  switch(response.action) {
    case "init":     // Initialization result
    case "loadAd":   // Verification result of load request
    case "clearAds": // Tear-down result
    case "event":    // SDK lifecycle event callback (Critical)
      console.log(`[${response.action}] ${response.status} (${response.requestId}):`, response.data);
      break;
    case "busy":     // Request was throttled (see 4.1) — same requestId you sent
      console.warn("Throttled:", response.requestId);
      break;
    case "error":
      console.error("Bridge Error:", response.data);
      break;
  }
};
```

Every response object may carry an optional `requestId` field — present only when the originating request supplied one — alongside `action`, `status`, and `data`.

##### Event Payload Structure (`response.action == "event"`)
These represent active ad lifecycle callbacks forwarded by the native SDK.
* `data`: Formatted string (e.g., `"[banner] loaded: AD_UNIT_ID"`).
* `requestId`: The `requestId` of the most recent `loadAd`, so events can be mapped back to their request.

---

### 3. Supported Ad Formats & Specifications

| Format | Description | Render Location |
| :--- | :--- | :--- |
| `banner` | 320x50, 320x100 Banner | Injected at layout bottom or target container |
| `native` | Native Asset Ad | Injected at layout bottom or target container |
| `video` | Outstream Video | Injected at layout bottom or target container |
| `rewardVideo` | Rewarded Video | Fullscreen Overlay |
| `interstitialVideo` | Interstitial Video | Fullscreen Overlay |
| `interstitialBanner` | Interstitial Banner (Popup) | Fullscreen Overlay |

---

### 4. Platform-Specific Notes

#### 4.1 Throttling
* The native bridge implements a **500ms** rate limit for duplicate actions. We recommend disabling active UI buttons on the web during this interval.
* A request that arrives inside this window is **not** processed; the bridge immediately replies with a `busy` response (`action: "busy"`, `status: "busy"`) carrying your original `requestId`, so the web layer can tell which request was dropped and retry.

#### 4.2 User-Agent Additions
* The suffix `NapSspHybridBridge` is automatically appended to the default WebView User-Agent string. (E.g., `... Chrome/123.0.0.0 Mobile Safari/537.36 NapSspHybridBridge`).

---

### 5. Sample Codes

For complete reference layouts, check these files inside the repository:
* **HTML/JS**: `android/app/src/main/assets/index.html` (Android asset container)
* **Android Native**: `android/.../HybridWebViewScreen.kt`
* **iOS Native**: `ios/.../HybridWebViewScreen.swift`

---

## 한국어

이 문서는 하이브리드 앱의 WebView(Html/JS) 환경에서 Nap SSP Native SDK를 호출하기 위한 브릿지 연동 방법을 설명합니다.

### 1. 개요

하이브리드 앱에서는 웹뷰의 JS가 네이티브 영역에 광고 로드를 요청하고, 네이티브 SDK가 광고 뷰를 생성하여 웹뷰 위 또는 지정된 영역에 표시합니다.

- **통신 방식**: JSON 기반 메시지 패싱
- **Bridge Name**: `NapSspBridge`
- **JS -> Native**: `postMessage(jsonString)` 호출
- **Native -> JS**: `onNapSspMessage(jsonString)` 콜백 수신

---

### 2. JavaScript 인터페이스 명세

#### 2.1 광고 호출 및 제어 (Request)

웹에서 네이티브로 작업을 요청할 때는 아래의 JSON 구조를 사용합니다.

```javascript
// 1. SDK 초기화 (앱 시작 시 또는 페이지 로드 시 1회 권장)
window.NapSspBridge.postMessage(JSON.stringify({ action: "init" }));

// 2. 광고 로드
const request = {
  action: "loadAd",
  requestId: "uuid-or-client-id", // (옵션) 응답과 이후 이벤트에 그대로 echo됩니다
  params: {
    format: "banner", // banner, native, video, rewardVideo, interstitialVideo, interstitialBanner
    adUnitId: "YOUR_ADUNIT_ID" // (옵션)
  }
};
window.NapSspBridge.postMessage(JSON.stringify(request));

// 3. 모든 광고 제거 및 리소스 해제
window.NapSspBridge.postMessage(JSON.stringify({ action: "clearAds" }));
```

> **`requestId` (옵션, 모든 action 공통)** — 요청에 `requestId`를 넣으면, 네이티브 브릿지가 해당 응답은 물론 그 `loadAd` 이후 발생하는 SDK 라이프사이클 이벤트(`event` action)에도 같은 값을 echo합니다. 덕분에 웹에서 비동기로 도착하는 `loaded`/`clicked`/`closed` 이벤트를 어떤 요청에서 비롯됐는지 짝지을 수 있습니다. 한 번에 하나의 광고 요청만 활성이므로, 이벤트에는 가장 최근 `loadAd`의 `requestId`가 다음 `loadAd` 또는 `clearAds` 전까지 실립니다. 요청에 `requestId`가 없으면 응답에서도 해당 필드는 생략됩니다.

#### 2.2 결과 및 이벤트 수신 (Response Callback)

네이티브 작업 결과 및 광고 상태 변경 이벤트는 웹의 전역 함수 `onNapSspMessage`로 전달됩니다.

```javascript
window.onNapSspMessage = function(responseStr) {
  const response = JSON.parse(responseStr);
  
  switch(response.action) {
    case "init":     // 초기화 결과
    case "loadAd":   // 로드 요청 수락 여부
    case "clearAds": // 제거 완료 결과
    case "event":    // 광고 상태 이벤트 (중요)
      console.log(`[${response.action}] ${response.status} (${response.requestId}):`, response.data);
      break;
    case "busy":     // throttle로 거절된 요청 (4.1 참고) — 보낸 requestId 그대로
      console.warn("Throttled:", response.requestId);
      break;
    case "error":
      console.error("Bridge Error:", response.data);
      break;
  }
};
```

모든 응답 객체는 `action`, `status`, `data`와 함께 옵션 필드 `requestId`를 포함할 수 있습니다(요청에 `requestId`가 있었을 때만).

##### Event 데이터 형식 (response.action == "event")
네이티브 SDK로부터 전달되는 광고 라이프사이클 이벤트입니다.
- `data`: `"[banner] loaded: AD_UNIT_ID"` 와 같은 문자열 형식 (플랫폼 통합)
- `requestId`: 가장 최근 `loadAd`의 `requestId`. 이벤트를 요청과 매핑하는 데 사용합니다.

---

### 3. 지원 광고 포맷 및 파라미터

| Format | 설명 | 표시 방식 |
| :--- | :--- | :--- |
| `banner` | 320x50, 320x100 배너 | 레이아웃 하단 또는 지정 영역 삽입 |
| `native` | 네이티브 광고 (에셋형) | 레이아웃 하단 또는 지정 영역 삽입 |
| `video` | 아웃스트림 동영상 | 레이아웃 하단 또는 지정 영역 삽입 |
| `rewardVideo` | 리워드형 동영상 | 전면(Full-screen) 노출 |
| `interstitialVideo` | 전면 동영상 | 전면(Full-screen) 노출 |
| `interstitialBanner` | 전면 배너 (팝업형) | 전면(Full-screen) 노출 |

---

### 4. 플랫폼별 특이사항

#### 4.1 중복 호출 방지 (Throttling)
- 네이티브 브릿지에서 약 **0.5초(500ms)** 간격으로 동일 액션에 대한 중복 호출을 방지하고 있습니다. JS에서도 버튼 비활성화 등의 처리를 권장합니다.
- 이 창 안에 들어온 요청은 처리되지 **않고**, 브릿지가 보낸 `requestId`를 담아 `busy` 응답(`action: "busy"`, `status: "busy"`)을 즉시 돌려줍니다. 웹에서는 어떤 요청이 버려졌는지 알고 재시도할 수 있습니다.

#### 4.2 사용자 에이전트 (User-Agent)
- 하이브리드 웹뷰의 User-Agent 끝에 `NapSspHybridBridge` 접미사가 자동으로 추가됩니다. (예: `... Chrome/123.0.0.0 Mobile Safari/537.36 NapSspHybridBridge`)

---

### 5. 샘플 코드

상세한 구현 예시는 프로젝트 내 다음 파일을 참조하세요:
- **HTML/JS**: `android/app/src/main/assets/index.html`
- **Android**: `android/.../HybridWebViewScreen.kt`
- **iOS**: `ios/.../HybridWebViewScreen.swift`
