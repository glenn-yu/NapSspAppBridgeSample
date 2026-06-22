# 🚀 NapSSP Hybrid Ad Integration Guide (Web to Native) / 하이브리드 광고 통합 풀스택 가이드

[English](#english) | [한국어](#한국어)

---

## English

This guide covers how hybrid app developers can integrate native ads in just a few minutes using the provided **`bridge` package**.

### Summary (30-Second Quickstart)
1. Run the app and enter the `MEDIA_KEY` and ad keys in `Configure Keys`.
2. Verify basic behavior with a Banner or Native ad first.
3. Next, initialize (`init`) inside the `HybridWebView` and tap an ad trigger button to verify the bridge.
4. If no custom keys are entered, the app will run with sample defaults.
5. The iOS sample is currently configured to build reliably using local xcframeworks located in `ios/Vendor/`.

### 1. Integration Workflow
Hybrid ads run in the order of **Web (Request) ➡️ Bridge (Forward) ➡️ Native (Execution)**.
1. **Web**: Calls `callNative('loadAd', {format: 'banner'})`
2. **Bridge**: Receives the command from the web and forwards it to the native engine.
3. **Native**: **[Encapsulated]** Destroys any existing ad object, creates a new ad object, and calls `loadAd()`.
4. **SDK**: Renders the ad within the native layout upon successful load.
5. **Callback**: Reports success or failure back to the web's `window.onNapSspMessage()` handler.

### 2. Copy & Paste Integration
The core integration logic is encapsulated in this sample project for partners to quickly copy and run.

#### ① Copy the Native Engine
* **Android**: Copy the entire `app/src/main/java/.../bridge/` directory into your project.
* **iOS**: Copy the entire `ios/Sources/.../Bridge/` directory into your project.

#### ② Security & Configurations
We recommend not hardcoding sensitive ad keys in the source code.
* **Android**: Define keys in your project root's `local.properties` file and access them via `BuildConfig`.
  ```properties
  napssp.media_key=10771
  napssp.adunit_banner=104704
  ```
* **iOS**: We recommend defining and managing environment variables using `.xcconfig` files.

### 3. Web-Side (HTML/JavaScript) Implementation
Add the following standard communication scripts to your web page:
```javascript
// ① Send an ad request to native
function callNative(action, params = {}) {
    const message = JSON.stringify({ action, params });
    if (window.NapSspBridge) {
        window.NapSspBridge.postMessage(message); // Android
    } else if (window.webkit?.messageHandlers?.NapSspBridge) {
        window.webkit.messageHandlers.NapSspBridge.postMessage(message); // iOS
    }
}

// ② Receive results from native
window.onNapSspMessage = function(responseStr) {
    const res = JSON.parse(responseStr);
    console.log(`Status: ${res.status}, Data: ${res.data}`);
};
```

### 4. Ad Destruction Sequence
The provided `bridge` module implements a robust teardown sequence to prevent "Already Exist" issues.
* **Android**: `GONE -> onPause -> onDestroy -> removeView -> null`
* **iOS**: `isHidden -> stop -> removeFromSuperview -> nil`

### 5. UI Optimization: UUID Session Management
When refreshing ads in a WebView, assign a **Unique ID (UUID)** to force the UI framework (Compose/SwiftUI) to completely redraw the view without leaving residual frames.
* **Android**: `key(UUID.randomUUID().toString()) { AndroidView(...) }`
* **iOS**: `AdViewRepresentable(adView: view).id(UUID())`

This logic temporarily bypasses the framework's view recycling behavior, preventing layout glitches or listener errors when replacing ad objects.

### 6. Official Documentation (Reference)
For detailed SDK specifications and the latest native integration guides, refer to:
* **Android SDK Official Guide**: [https://napmx.github.io/#/android/](https://napmx.github.io/#/android/)
* **iOS SDK Official Guide**: [https://napmx.github.io/#/ios/](https://napmx.github.io/#/ios/)

---

## 한국어

이 문서는 하이브리드 앱 개발자가 제공된 **`bridge` 패키지**를 사용하여 단 몇 분 만에 네이티브 광고를 연동하는 방법을 다룹니다.

### 초보자용 30초 요약
1. 앱을 실행하고 `Configure Keys`에서 MEDIA_KEY와 광고 키를 입력합니다.
2. 먼저 배너 또는 네이티브 광고로 기본 동작을 확인합니다.
3. 그 다음 `HybridWebView`에서 `init` 후 광고 버튼을 눌러 브리지를 확인합니다.
4. 입력한 키가 없으면 샘플 기본값으로 동작합니다.
5. iOS 샘플은 현재 `ios/Vendor/` 아래 로컬 xcframework 기준으로 빌드 재현성을 확보한 상태입니다.

### 1. 연동 흐름 한눈에 보기 (Workflow)
하이브리드 광고는 **웹(요청) ➡️ 브릿지(전달) ➡️ 네이티브(실행)** 순으로 동작합니다.
1. **Web**: `callNative('loadAd', {format: 'banner'})` 호출
2. **Bridge**: 웹의 명령을 수신하여 네이티브 엔진으로 배달
3. **Native**: **[캡슐화]** 기존 광고 파괴 후 새 광고 객체 생성 및 `loadAd()` 호출
4. **SDK**: 광고 로드 성공 시 네이티브 영역에 광고 노출
5. **Callback**: 웹의 `onNapSspMessage()`로 성공/실패 보고

### 2. 빠른 연동 방법 (Copy & Paste)
본 샘플 프로젝트는 파트너사가 즉시 복사해서 사용할 수 있도록 핵심 로직을 캡슐화해 두었습니다.

#### ① 네이티브 엔진 복사
* **Android**: `app/src/main/java/.../bridge/` 폴더 전체를 본인 프로젝트로 복사하세요.
* **iOS**: `ios/Sources/.../Bridge/` 폴더 전체를 본인 프로젝트로 복사하세요.

#### ② 보안 및 키 설정 (Configuration)
민감한 광고 키 값들은 소스 코드에 하드코딩하지 않는 것을 권장합니다.
* **Android**: 프로젝트 루트의 `local.properties`에 키를 정의하고 `BuildConfig`를 통해 접근하세요.
  ```properties
  napssp.media_key=10771
  napssp.adunit_banner=104704
  ```
* **iOS**: `.xcconfig` 파일에 환경 변수를 정의하여 관리하는 것을 권장합니다.

### 3. Web Side (HTML/JavaScript) 구현
웹 페이지에 아래 표준 통신 스크립트를 추가하세요.
```javascript
// ① 네이티브에 광고 요청 보내기
function callNative(action, params = {}) {
    const message = JSON.stringify({ action, params });
    if (window.NapSspBridge) {
        window.NapSspBridge.postMessage(message); // Android
    } else if (window.webkit?.messageHandlers?.NapSspBridge) {
        window.webkit.messageHandlers.NapSspBridge.postMessage(message); // iOS
    }
}

// ② 네이티브로부터 결과 받기
window.onNapSspMessage = function(responseStr) {
    const res = JSON.parse(responseStr);
    console.log(`상태: ${res.status}, 데이터: ${res.data}`);
};
```

### 4. 플랫폼별 정석 파괴 시퀀스 (이미 캡슐화됨)
제공된 `bridge` 폴더 내에는 `Already Exist` 오류를 방지하기 위한 정석 시퀀스가 이미 구현되어 있습니다.
* **Android**: `GONE -> onPause -> onDestroy -> removeView -> null`
* **iOS**: `isHidden -> stop -> removeFromSuperview -> nil`

### 5. UI 최적화: 고유 세션 관리 (UUID의 의미)
웹뷰에서 광고를 새로 고칠 때, UI 프레임워크(Compose/SwiftUI)가 이전 광고의 잔상을 남기지 않고 **화면을 완전히 새로 그리게 강제**하려면 **고유 ID(UUID)**를 부여해야 합니다.
* **Android**: `key(UUID.randomUUID().toString()) { AndroidView(...) }`
* **iOS**: `AdViewRepresentable(adView: view).id(UUID())`

이 로직은 프레임워크의 뷰 재사용(Recycle) 기능을 일시적으로 차단하여, 광고 객체 교체 시 발생할 수 있는 레이아웃 꼬임이나 리스너 오류를 완벽하게 방지합니다.

### 6. 공식 기술 지원 문서 (Reference)
더 상세한 SDK 사양이나 최신 버전의 네이티브 연동 가이드는 아래 공식 문서를 참조하세요.
* **Android SDK 공식 가이드**: [https://napmx.github.io/#/android/](https://napmx.github.io/#/android/)
* **iOS SDK 공식 가이드**: [https://napmx.github.io/#/ios/](https://napmx.github.io/#/ios/)
