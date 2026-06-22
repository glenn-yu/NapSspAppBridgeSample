# NapSsp AppBridge Public Integration Guide / 공개 연동 가이드

[English](#english) | [한국어](#한국어)

---

## English

This guide is designed for external publishers to understand the NapSsp WebView ↔ Native Bridge sample structure and apply the same architecture to their Android/iOS applications.

### 1. Overview

The NapSsp AppBridge allows JavaScript UI inside a WebView to interact with native Ad SDKs securely by exchanging JSON messages, rather than calling native SDKs directly.

```text
WebView HTML/JS
  └─ JSON message
      └─ Native Bridge
          └─ NapSsp / AdMixer SDK
              └─ Ad Load/Show/Click/Close Events
                  └─ Native Bridge
                      └─ window.onNapSspMessage(...)
```

Using this architecture, the hybrid web layer maintains a unified JavaScript API, while the native Android and iOS layers independently and safely manage SDK initialization and the ad lifecycle.

### 2. Supported Platforms
* **Android**: Kotlin + Jetpack Compose + Android WebView
* **iOS**: SwiftUI + WKWebView

### 3. Supported Ad Formats

| Format | Description | Screen Placement |
|---|---|---|
| `banner` | 320x100 Banner | Native View container at the bottom of the WebView |
| `native` | Native Ad | Native View container at the bottom of the WebView |
| `video` | Outstream Video | Native View container at the bottom of the WebView |
| `rewardVideo` | Rewarded Video | Fullscreen Overlay |
| `interstitialVideo` | Interstitial Video | Fullscreen Overlay |
| `interstitialBanner` | Interstitial Banner/Popup | Fullscreen Overlay |

### 4. JavaScript Request Specification

Messages passed from the WebView to Native are stringified JSON objects.

#### SDK Initialization
```javascript
callNative('init')
```
JSON Payload:
```json
{
  "action": "init",
  "params": {}
}
```

#### Ad Request
```javascript
callNative('loadAd', { format: 'banner' })
```
JSON Payload:
```json
{
  "action": "loadAd",
  "params": {
    "format": "banner"
  }
}
```

#### Custom Ad Unit ID
```javascript
callNative('loadAd', {
  format: 'banner',
  adUnitId: 'YOUR_AD_UNIT_ID'
})
```
> [!IMPORTANT]
> **Platform Types for adUnitId**
>
> The `adUnitId` passed from JS is always a **String**. The native layers process it as follows:
> - **Android**: Uses the string as-is.
> - **iOS**: Converts the string to an `Int`. If a non-numeric string is provided, it falls back to the default configuration.

#### Clear Ads
```javascript
callNative('clearAds')
```
JSON Payload:
```json
{
  "action": "clearAds",
  "params": {}
}
```

### 5. Native Response Specification

Native calls the `window.onNapSspMessage(responseString)` JavaScript function. The argument `responseString` is a stringified JSON object.

```json
{
  "action": "loadAd",
  "status": "success",
  "data": "Accepted banner"
}
```

Field Descriptions:

| Field | Description |
|---|---|
| `action` | Action name. E.g., `init`, `loadAd`, `clearAds`, `event` |
| `status` | `success` or `error` |
| `data` | Result message or detailed event data |

Ad SDK events are dispatched in this format:
```json
{
  "action": "event",
  "status": "success",
  "data": "[banner] loaded: 104704"
}
```

Error response example:
```json
{
  "action": "loadAd",
  "status": "error",
  "data": "Unsupported format: unknownFormat"
}
```

Ad load failure event example:
```json
{
  "action": "event",
  "status": "success",
  "data": "[banner] failed: [-1] No fill"
}
```

### 6. Android Integration Summary

Android registers the `NapSspBridge` object using `WebView.addJavascriptInterface`:
```kotlin
addJavascriptInterface(bridge, "NapSspBridge")
```

JavaScript calls native as follows:
```javascript
window.NapSspBridge.postMessage(JSON.stringify({ action, params }))
```

The Native layer is responsible for:
* JSON parsing and validation.
* Supported format verification.
* SDK initialization.
* Managing ad object lifecycle (destruction of old ads, creation of new ones).
* Passing lifecycle events (`resume`, `pause`, `destroy`).
* Forwarding SDK callbacks to JS.

### 7. iOS Integration Summary

iOS registers the `NapSspBridge` handler using `WKScriptMessageHandler`:
```swift
config.userContentController.add(LeakAvoider(bridge), name: "NapSspBridge")
```

JavaScript calls native as follows:
```javascript
window.webkit.messageHandlers.NapSspBridge.postMessage(
  JSON.stringify({ action, params })
)
```

The Native layer handles JSON parsing, format validation, SDK invocation, and callback routing just like Android.

### 8. Minimum Files Required for Publisher Apps

Since the sample app includes testing interfaces and debugging resources, it is larger than a standard production layout. We recommend referencing only the minimum core files when integrating.

Refer to `docs/publisher-minimal-integration.md` and `docs/delivery-packages.md` for complete breakdown lists.

#### Android Core Files
```text
android/app/src/main/java/.../HybridWebViewScreen.kt
android/app/src/main/java/.../bridge/NapSspConfig.kt
android/app/src/main/java/.../bridge/NapSspSdkIntegration.kt
android/app/src/main/assets/index.html   # Reference HTML file.
```

Required Configurations:
```text
android/app/build.gradle.kts dependency
android/app/src/main/AndroidManifest.xml permissions & meta-data
android/app/src/main/res/layout/admixer_item_*.xml   # Only for Native Ad layouts
```

#### iOS Core Files
```text
ios/Sources/NapSspIOSSample/HybridWebViewScreen.swift
ios/Sources/NapSspIOSSample/NapSspConfig.swift
ios/Sources/NapSspIOSSample/Bridge/NapSspSdkIntegration.swift
ios/Sources/NapSspIOSSample/index.html   # Reference HTML file.
```

Add conditionally based on the formats you support:
```text
ios/Sources/NapSspIOSSample/Bridge/InterstitialModule.swift   # For Interstitial Ads
ios/Sources/NapSspIOSSample/Bridge/RewardedModule.swift       # For Rewarded Ads
ios/Sources/NapSspIOSSample/AMMNativeAdView.xib               # For Native Ads
```

### 9. Ad Configurations

The sample app contains default test values. For production integration, configure your active `MEDIA_KEY` and `AD_UNIT_ID` in the settings screen or configurations.

* **Android**: Define credentials in your local `local.properties` file, CI secrets, or remote configurations. Do not commit keys to public git source control.
* **iOS**: Configure credentials via `.xcconfig` files, build settings, or remote database configs.

### 10. Verification Steps
1. Run the app.
2. Tap **Initialize SDK**.
3. Confirm `INIT SUCCESS` log.
4. Tap **Load Banner**.
5. Confirm `LOADAD SUCCESS` or corresponding SDK events.
6. Tap **Clear All Ads & Releases**.
7. Confirm `CLEARADS SUCCESS` log.
8. Verify there are no bridge parsing exceptions or warnings in the device console.

### 11. Pre-Production Checklist
- [ ] Apply production `MEDIA_KEY` and `AD_UNIT_ID`.
- [ ] Replace test App IDs with production credentials.
- [ ] Audit Android permissions to remove unnecessary requests.
- [ ] Review `usesCleartextTraffic` network policies.
- [ ] Review Console log level policies.
- [ ] Maintain input JSON parsing validation.
- [ ] Maintain explicit ad object release/destruction procedures.
- [ ] Maintain consistent action/format/event JSON signatures across both platforms.

---

## 한국어

이 문서는 외부 개발자가 NapSsp WebView ↔ Native 브릿지 샘플을 이해하고 Android/iOS 앱에 동일한 구조를 적용할 수 있도록 정리한 공개용 가이드입니다.

### 1. 개요

NapSsp AppBridge는 WebView 안의 JavaScript UI에서 Native 광고 SDK를 직접 호출하지 않고, 약속된 JSON 메시지를 Native 브릿지로 전달하는 구조입니다.

```text
WebView HTML/JS
  └─ JSON message
      └─ Native Bridge
          └─ NapSsp / AdMixer SDK
              └─ 광고 로드·노출·클릭·종료 이벤트
                  └─ Native Bridge
                      └─ window.onNapSspMessage(...)
```

이 구조를 사용하면 하이브리드 화면은 동일한 JS API를 유지하고, Android/iOS 각각의 Native 계층에서 SDK 초기화와 광고 객체 생명주기를 안전하게 관리할 수 있습니다.

### 2. 지원 플랫폼
- Android: Kotlin + Jetpack Compose + Android WebView
- iOS: SwiftUI + WKWebView

### 3. 지원 광고 포맷

| format | 설명 | 화면 배치 |
|---|---|---|
| `banner` | 320x100 배너 | WebView 하단 Native View 영역 |
| `native` | 네이티브 광고 | WebView 하단 Native View 영역 |
| `video` | 아웃스트림 비디오 | WebView 하단 Native View 영역 |
| `rewardVideo` | 리워드 비디오 | 전체 화면 |
| `interstitialVideo` | 전면 비디오 | 전체 화면 |
| `interstitialBanner` | 전면 배너/팝업 | 전체 화면 |

### 4. JavaScript 요청 규격

WebView에서 Native로 전달하는 메시지는 JSON 문자열입니다.

#### SDK 초기화
```js
callNative('init')
```
전달 메시지:
```json
{
  "action": "init",
  "params": {}
}
```

#### 광고 요청
```js
callNative('loadAd', { format: 'banner' })
```
전달 메시지:
```json
{
  "action": "loadAd",
  "params": {
    "format": "banner"
  }
}
```

#### 커스텀 Ad Unit ID 사용
```js
callNative('loadAd', {
  format: 'banner',
  adUnitId: 'YOUR_AD_UNIT_ID'
})
```
> [!IMPORTANT]
> **플랫폼별 adUnitId 타입 주의**
>
> JS에서 전달하는 `adUnitId`는 항상 **문자열**입니다. 플랫폼 Native 계층에서 각각 다음과 같이 처리합니다.
> - **Android**: 문자열 그대로 사용합니다.
> - **iOS**: `Int`로 변환해 SDK에 전달합니다. 숫자가 아닌 값이 전달되면 기본 설정값이 사용됩니다.

#### 광고 해제
```js
callNative('clearAds')
```
전달 메시지:
```json
{
  "action": "clearAds",
  "params": {}
}
```

### 5. Native 응답 규격

Native는 JS의 `window.onNapSspMessage(responseString)` 함수를 호출합니다. `responseString`은 JSON 문자열입니다.

```json
{
  "action": "loadAd",
  "status": "success",
  "data": "Accepted banner"
}
```

필드 설명:

| 필드 | 설명 |
|---|---|
| `action` | 처리한 액션 이름. 예: `init`, `loadAd`, `clearAds`, `event` |
| `status` | `success` 또는 `error` |
| `data` | 결과 메시지 또는 이벤트 상세 |

광고 SDK 이벤트는 다음 형태로 전달됩니다.
```json
{
  "action": "event",
  "status": "success",
  "data": "[banner] loaded: 104704"
}
```

에러 응답 예시:
```json
{
  "action": "loadAd",
  "status": "error",
  "data": "Unsupported format: unknownFormat"
}
```

SDK 광고 로드 실패 이벤트 예시:
```json
{
  "action": "event",
  "status": "success",
  "data": "[banner] failed: [-1] No fill"
}
```

### 6. Android 연동 요약

Android는 `WebView.addJavascriptInterface`로 `NapSspBridge` 객체를 주입합니다.
```kotlin
addJavascriptInterface(bridge, "NapSspBridge")
```

JS에서는 다음 방식으로 호출합니다.
```js
window.NapSspBridge.postMessage(JSON.stringify({ action, params }))
```

Native 계층은 다음을 담당합니다.
- JSON 파싱 및 유효성 검증
- 지원 포맷 검증
- SDK 초기화
- 광고 객체 생성 및 기존 광고 해제
- 광고 생명주기 `resume`, `pause`, `destroy` 전달
- SDK 이벤트를 JS 콜백으로 반환

### 7. iOS 연동 요약

iOS는 `WKScriptMessageHandler`로 `NapSspBridge` message handler를 등록합니다.
```swift
config.userContentController.add(LeakAvoider(bridge), name: "NapSspBridge")
```

JS에서는 다음 방식으로 호출합니다.
```js
window.webkit.messageHandlers.NapSspBridge.postMessage(
  JSON.stringify({ action, params })
)
```

Native 계층은 Android와 동일하게 JSON 파싱, 포맷 검증, SDK 호출, 이벤트 반환을 담당합니다.

### 8. 실제 매체 앱에 필요한 최소 파일

샘플 앱은 데모 UI, 테스트 화면, 키 입력 화면, QA용 코드까지 포함하므로 실제 매체 앱에 필요한 코드보다 큽니다. 외부 매체사에 전달할 때는 전체 샘플앱보다 최소 연동 파일을 먼저 안내하는 것을 권장합니다.

자세한 분리 기준은 `docs/publisher-minimal-integration.md`와 `docs/delivery-packages.md`를 참고합니다.

#### Android 핵심 파일
```text
android/app/src/main/java/.../HybridWebViewScreen.kt
android/app/src/main/java/.../bridge/NapSspConfig.kt
android/app/src/main/java/.../bridge/NapSspSdkIntegration.kt
android/app/src/main/assets/index.html   # 샘플 참고용 HTML 파일
```

함께 필요한 설정:
```text
android/app/build.gradle.kts dependency
android/app/src/main/AndroidManifest.xml 권한/meta-data
android/app/src/main/res/layout/admixer_item_*.xml   # Native 광고 사용 시만
```

#### iOS 핵심 파일
```text
ios/Sources/NapSspIOSSample/HybridWebViewScreen.swift
ios/Sources/NapSspIOSSample/NapSspConfig.swift
ios/Sources/NapSspIOSSample/Bridge/NapSspSdkIntegration.swift
ios/Sources/NapSspIOSSample/index.html   # 샘플 참고용 HTML 파일
```

포맷에 따라 추가:
```text
ios/Sources/NapSspIOSSample/Bridge/InterstitialModule.swift   # 전면 광고 사용 시
ios/Sources/NapSspIOSSample/Bridge/RewardedModule.swift       # 보상형 광고 사용 시
ios/Sources/NapSspIOSSample/AMMNativeAdView.xib               # Native 광고 사용 시
```

### 9. 광고 설정값

샘플 앱은 기본 테스트 값을 포함합니다. 실제 매체 연동 시에는 앱 화면의 `Configure Keys` 또는 플랫폼별 설정 파일을 통해 실제 `MEDIA_KEY`와 `AD_UNIT_ID`를 적용합니다.

* **Android**: `local.properties`, `BuildConfig`, CI secret, 원격 설정 등을 통해 주입합니다. 운영 키 값을 코드에 직접 하드코딩하여 커밋하지 않도록 주의합니다.
* **iOS**: `xcconfig`, build setting, 원격 설정 등을 통해 관리합니다.

### 10. 검증 순서
1. 앱 실행
2. `Initialize SDK` 클릭
3. `INIT SUCCESS` 로그 확인
4. `Load Banner` 클릭
5. `LOADAD SUCCESS` 또는 SDK 이벤트 로그 확인
6. `Clear All Ads & Releases` 클릭
7. `CLEARADS SUCCESS` 로그 확인
8. Native 로그에서 bridge parsing/error 예외가 없는지 확인

### 11. 운영 반영 전 체크리스트
- [ ] 실제 `MEDIA_KEY` / `AD_UNIT_ID` 적용
- [ ] 테스트용 앱 ID 제거 또는 운영용 ID 교체
- [ ] 불필요한 Android 권한 제거 검토
- [ ] `usesCleartextTraffic` 운영 정책 검토
- [ ] Debug 로그 레벨 운영 정책 검토
- [ ] 외부 입력 JSON 검증 유지
- [ ] 광고 객체 해제 로직 유지
- [ ] Android/iOS 양쪽에서 동일한 action/format/event 규격 유지
