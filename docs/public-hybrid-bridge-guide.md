# NapSsp AppBridge 공개 연동 가이드

이 문서는 외부 개발자가 NapSsp WebView ↔ Native 브릿지 샘플을 이해하고 Android/iOS 앱에 동일한 구조를 적용할 수 있도록 정리한 공개용 가이드입니다.

## 1. 개요

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

## 2. 지원 플랫폼

- Android: Kotlin + Jetpack Compose + Android WebView
- iOS: SwiftUI + WKWebView

## 3. 지원 광고 포맷

| format | 설명 | 화면 배치 |
|---|---|---|
| `banner` | 320x100 배너 | WebView 하단 Native View 영역 |
| `native` | 네이티브 광고 | WebView 하단 Native View 영역 |
| `video` | 아웃스트림 비디오 | WebView 하단 Native View 영역 |
| `rewardVideo` | 리워드 비디오 | 전체 화면 |
| `interstitialVideo` | 전면 비디오 | 전체 화면 |
| `interstitialBanner` | 전면 배너/팝업 | 전체 화면 |

## 4. JavaScript 요청 규격

WebView에서 Native로 전달하는 메시지는 JSON 문자열입니다.

### SDK 초기화

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

### 광고 요청

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

### 커스텀 Ad Unit ID 사용

```js
callNative('loadAd', {
  format: 'banner',
  adUnitId: 'YOUR_AD_UNIT_ID'
})
```

> **플랫폼별 adUnitId 타입 주의**
>
> JS에서 전달하는 `adUnitId`는 항상 **문자열**입니다. 플랫폼 Native 계층에서 각각 다음과 같이 처리합니다.
>
> - **Android**: 문자열 그대로 사용합니다.
> - **iOS**: `Int`로 변환해 SDK에 전달합니다. 숫자가 아닌 값이 전달되면 기본 설정값이 사용됩니다.

### 광고 해제

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

## 5. Native 응답 규격

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

## 6. Android 연동 요약

Android는 `WebView.addJavascriptInterface`로 `NapSspBridge` 객체를 주입합니다.

```kotlin
addJavascriptInterface(bridge, "NapSspBridge")
```

JS에서는 다음 방식으로 호출합니다.

```js
window.NapSspBridge.postMessage(JSON.stringify({ action, params }))
```

Native 계층은 다음을 담당합니다.

- JSON 파싱
- 지원 포맷 검증
- SDK 초기화
- 광고 객체 생성 및 기존 광고 해제
- 광고 생명주기 `resume`, `pause`, `destroy` 전달
- SDK 이벤트를 JS 콜백으로 반환

## 7. iOS 연동 요약

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

## 8. 실제 매체 앱에 필요한 최소 파일

샘플 앱은 데모 UI, 테스트 화면, 키 입력 화면, QA용 코드까지 포함하므로 실제 매체 앱에 필요한 코드보다 큽니다. 외부 매체사에 전달할 때는 전체 샘플앱보다 최소 연동 파일을 먼저 안내하는 것을 권장합니다.

자세한 분리 기준은 `docs/publisher-minimal-integration.md`와 `docs/delivery-packages.md`를 참고합니다.

### Android

Hybrid WebView 연동에 필요한 핵심 파일:

```text
android/app/src/main/java/.../HybridWebViewScreen.kt
android/app/src/main/java/.../bridge/NapSspConfig.kt
android/app/src/main/java/.../bridge/NapSspSdkIntegration.kt
android/app/src/main/assets/index.html   # 샘플 참고용. 실제 앱에서는 매체 웹 코드로 대체 가능
```

함께 필요한 설정:

```text
android/app/build.gradle.kts dependency
android/app/src/main/AndroidManifest.xml 권한/meta-data
android/app/src/main/res/layout/admixer_item_*.xml   # Native 광고 사용 시만
```

일반 매체 앱에서는 `AdDemoScreen`, `SampleViewModel`, `IntentBridgeReceiver` 같은 샘플/테스트용 파일은 보통 필요하지 않습니다.

### iOS

Hybrid WebView 연동에 필요한 핵심 파일:

```text
ios/Sources/NapSspIOSSample/HybridWebViewScreen.swift
ios/Sources/NapSspIOSSample/NapSspConfig.swift
ios/Sources/NapSspIOSSample/Bridge/NapSspSdkIntegration.swift
ios/Sources/NapSspIOSSample/index.html   # 샘플 참고용. 실제 앱에서는 매체 웹 코드로 대체 가능
```

포맷에 따라 추가:

```text
ios/Sources/NapSspIOSSample/Bridge/InterstitialModule.swift   # 전면 광고 사용 시
ios/Sources/NapSspIOSSample/Bridge/RewardedModule.swift       # 보상형 광고 사용 시
ios/Sources/NapSspIOSSample/AMMNativeAdView.xib               # Native 광고 사용 시
```

일반 매체 앱에서는 `AdDemoScreen`, `SampleState`, `SampleViewModel`, `ContentView` 같은 샘플 UI 파일은 보통 필요하지 않습니다.

## 9. 광고 설정값

샘플 앱은 기본 테스트 값을 포함합니다. 실제 매체 연동 시에는 앱 화면의 `Configure Keys` 또는 플랫폼별 설정 파일을 통해 실제 `MEDIA_KEY`와 `AD_UNIT_ID`를 적용합니다.

권장 방식:

- 소스 코드에 운영 키를 직접 커밋하지 않습니다.
- Android는 `local.properties`, `BuildConfig`, CI secret, 원격 설정 등을 통해 주입합니다.
- iOS는 `xcconfig`, build setting, 원격 설정 등을 통해 관리합니다.

## 10. 검증 순서

1. 앱 실행
2. `Initialize SDK` 클릭
3. `INIT SUCCESS` 로그 확인
4. `Load Banner` 클릭
5. `LOADAD SUCCESS` 또는 SDK 이벤트 로그 확인
6. `Clear All Ads & Releases` 클릭
7. `CLEARADS SUCCESS` 로그 확인
8. Native 로그에서 bridge parsing/error 예외가 없는지 확인

## 11. 운영 반영 전 체크리스트

- [ ] 실제 `MEDIA_KEY` / `AD_UNIT_ID` 적용
- [ ] 테스트용 앱 ID 제거 또는 운영용 ID 교체
- [ ] 불필요한 Android 권한 제거 검토
- [ ] `usesCleartextTraffic` 운영 정책 검토
- [ ] Debug 로그 레벨 운영 정책 검토
- [ ] 외부 입력 JSON 검증 유지
- [ ] 광고 객체 해제 로직 유지
- [ ] Android/iOS 양쪽에서 동일한 action/format/event 규격 유지
