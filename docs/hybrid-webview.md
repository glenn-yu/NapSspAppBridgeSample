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

### 2.1 광고 호출 및 제어 (Request)

웹에서 네이티브로 작업을 요청할 때는 아래의 JSON 구조를 사용합니다.

```javascript
// 1. SDK 초기화 (앱 시작 시 또는 페이지 로드 시 1회 권장)
window.NapSspBridge.postMessage(JSON.stringify({ action: "init" }));

// 2. 광고 로드
const request = {
  action: "loadAd",
  params: {
    format: "banner", // banner, native, video, rewardVideo, interstitialVideo, interstitialBanner
    adUnitId: "YOUR_ADUNIT_ID" // (옵션)
  }
};
window.NapSspBridge.postMessage(JSON.stringify(request));

// 3. 모든 광고 제거 및 리소스 해제
window.NapSspBridge.postMessage(JSON.stringify({ action: "clearAds" }));
```

### 2.2 결과 및 이벤트 수신 (Response Callback)

네이티브 작업 결과 및 광고 상태 변경 이벤트는 웹의 전역 함수 `onNapSspMessage`로 전달됩니다.

```javascript
window.onNapSspMessage = function(responseStr) {
  const response = JSON.parse(responseStr);
  
  switch(response.action) {
    case "init":     // 초기화 결과
    case "loadAd":   // 로드 요청 수락 여부
    case "clearAds": // 제거 완료 결과
    case "event":    // 광고 상태 이벤트 (중요)
      console.log(`[${response.action}] ${response.status}:`, response.data);
      break;
    case "error":
      console.error("Bridge Error:", response.data);
      break;
  }
};
```

#### Event 데이터 형식 (response.action == "event")
네이티브 SDK로부터 전달되는 광고 라이프사이클 이벤트입니다.
- `data`: `"[banner] loaded: AD_UNIT_ID"` 와 같은 문자열 형식 (플랫폼 통합)

---

## 3. 지원 광고 포맷 및 파라미터

| Format | 설명 | 표시 방식 |
| :--- | :--- | :--- |
| `banner` | 320x50, 320x100 배너 | 레이아웃 하단 또는 지정 영역 삽입 |
| `native` | 네이티브 광고 (에셋형) | 레이아웃 하단 또는 지정 영역 삽입 |
| `video` | 아웃스트림 동영상 | 레이아웃 하단 또는 지정 영역 삽입 |
| `rewardVideo` | 리워드형 동영상 | 전면(Full-screen) 노출 |
| `interstitialVideo` | 전면 동영상 | 전면(Full-screen) 노출 |
| `interstitialBanner` | 전면 배너 (팝업형) | 전면(Full-screen) 노출 |

---

## 4. 플랫폼별 특이사항

### 4.1 중복 호출 방지 (Throttling)
- 네이티브 브릿지에서 약 **0.5초(500ms)** 간격으로 동일 액션에 대한 중복 호출을 방지하고 있습니다. JS에서도 버튼 비활성화 등의 처리를 권장합니다.

### 4.2 사용자 에이전트 (User-Agent)
- 하이브리드 웹뷰의 User-Agent 끝에 `NapSspHybridBridge` 접미사가 자동으로 추가됩니다. (예: `... Chrome/123.0.0.0 Mobile Safari/537.36 NapSspHybridBridge`)

---

## 5. 샘플 코드

상세한 구현 예시는 프로젝트 내 다음 파일을 참조하세요:
- **HTML/JS**: `android/app/src/main/assets/index.html`
- **Android**: `android/.../HybridWebViewScreen.kt`
- **iOS**: `ios/.../HybridWebViewScreen.swift`
