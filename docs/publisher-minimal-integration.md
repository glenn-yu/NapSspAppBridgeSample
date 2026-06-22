# Publisher App Minimum Integration Guide / 매체 앱 최소 연동 가이드

[English](#english) | [한국어](#한국어)

---

## English

This guide isolates the **minimum required files and configuration settings** needed for integration into publisher applications, removing dependencies on sample UI and debugging resources.

Since the full sample application includes demo workflows, configuration panels, and complex layouts, it is significantly larger than what is needed for production. Use this guide to package only the necessary resources for your integration.

### 1. Requirements Assessment

Refer to the table below to determine which files are required depending on your targeted integration scope.

| Question | Required Scope |
|---|---|
| Do you render ads directly inside native view layouts? | SDK configuration + SDK integration wrapper |
| Do you request ads from JavaScript inside a WebView? | Hybrid Bridge configuration |
| Do you support Native Ad formats? | Android XML layout / iOS XIB file |
| Do you support Interstitial or Rewarded ads? | Interstitial / Rewarded modules |
| Do you require Mediation support? | Mediation SDK dependencies and Adapter registration |

The simplest implementation consists of: **SDK dependencies + Permission/Info.plist configurations + Config settings + SDK Integration modules**.

---

### 2. Android Minimum Configuration

#### 2.1 Required & Recommended Gradle Dependencies

Extract only the production dependencies from `android/app/build.gradle.kts` to your project.

Core SDK:
```kotlin
implementation("io.github.nasmedia-tech:admixer-ssp:1.0.23")
implementation("com.google.android.gms:play-services-ads-identifier:18.3.0")
```

Add these for Mediation support:
```kotlin
implementation("io.github.nasmedia-tech:admixer-admanager:1.0.14")
implementation("io.github.nasmedia-tech:admixer-adfit:1.0.11")
implementation("io.github.nasmedia-tech:admixer-pangle:1.0.11")
implementation("com.pangle.global:pag-sdk:8.0.0.4")
implementation("io.github.nasmedia-tech:admixer-applovin:1.0.8")
implementation("io.github.nasmedia-tech:admixer-unity:1.0.6")
```
> [!NOTE]
> If you are not utilizing third-party mediation networks, you can omit all mediation dependencies and adapter registrations.

#### 2.2 Manifest Settings

Default permissions from the sample:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="com.google.android.gms.permission.AD_ID" />
```

Optional permissions (audit these based on your active features and app store policies):
```xml
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

If utilizing Google Mobile Ads or its mediation networks, configure your application ID:
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="YOUR_GOOGLE_MOBILE_ADS_APP_ID" />
```

#### 2.3 Core Kotlin Files

Core native integration files:
```text
android/app/src/main/java/.../bridge/NapSspConfig.kt
android/app/src/main/java/.../bridge/NapSspSdkIntegration.kt
```

Role descriptions:

| File | Role |
|---|---|
| `NapSspConfig.kt` | Manages active MediaKeys and AdUnitIDs |
| `NapSspSdkIntegration.kt` | Handles SDK initializations, ad loads/shows, lifecycles, and callbacks |

> [!TIP]
> The sample's `AppConfig.kt` is a utility class for run-time key input during demo testing. For production apps, replace it with `BuildConfig` parameters, CI build pipeline secrets, or a remote server configuration API.

#### 2.4 Layout Resources for Native Ads

If you integrate Native Ads, import the following layouts:
```text
android/app/src/main/res/layout/admixer_item_320x480.xml
android/app/src/main/res/layout/admixer_item_300x250.xml
android/app/src/main/res/layout/admixer_item_320x100.xml
android/app/src/main/res/layout/admixer_item_320x50.xml
```
*If you only support Banner, Video, Interstitial, or Rewarded formats, these layouts are not required.*

#### 2.5 Hybrid WebView Additional Files

Required only if you request ads using JavaScript inside a WebView:
```text
android/app/src/main/java/.../HybridWebViewScreen.kt
android/app/src/main/assets/index.html   # Reference HTML file
```

---

### 3. iOS Minimum Configuration

#### 3.1 SDK Package Configurations

Core package specifications based on the sample's `ios/project.yml`:
```yaml
AdMixer
AdMixerMediation
AdMixerMediationGAM
AdMixerMediationAdFit
```
Include only the mediation network adapters that you intend to use.

#### 3.2 Info.plist Settings

Map these entries into your active target's Info.plist or Build Settings:
```text
NSUserTrackingUsageDescription
GADApplicationIdentifier
```
Provide appropriate compliance texts and your production Google/Mediation application IDs.

#### 3.3 Core Swift Files

Core native integration files:
```text
ios/Sources/NapSspIOSSample/NapSspConfig.swift
ios/Sources/NapSspIOSSample/Bridge/NapSspSdkIntegration.swift
```

Add these conditionally based on your supported formats:
```text
ios/Sources/NapSspIOSSample/Bridge/InterstitialModule.swift   # For Interstitial Banner Ads
ios/Sources/NapSspIOSSample/Bridge/RewardedModule.swift       # For Rewarded Video Ads
ios/Sources/NapSspIOSSample/Bridge/NapSspAdEventBridge.swift  # For Local Event Routing
```

Role descriptions:

| File | Role |
|---|---|
| `NapSspConfig.swift` | Manages active MediaKeys and AdUnitIDs |
| `NapSspSdkIntegration.swift` | Handles SDK initializations, ad loads/shows, and delegate callbacks |
| `InterstitialModule.swift` | Coordinates loading and rendering of interstitial overlays |
| `RewardedModule.swift` | Coordinates loading and rendering of rewarded overlays |

#### 3.4 Native Ad Layouts (XIB)

If you support Native Ads, import this XIB layout:
```text
ios/Sources/NapSspIOSSample/AMMNativeAdView.xib
```

#### 3.5 Hybrid WebView Additional Files

Required only if you request ads using JavaScript inside a WebView:
```text
ios/Sources/NapSspIOSSample/HybridWebViewScreen.swift
ios/Sources/NapSspIOSSample/index.html   # Reference HTML file
```

---

### 4. Files Safe to Exclude (Sample UI Only)

The following files are only used for demo testing and should be excluded from your production target:

#### Android
* `AdDemoScreen.kt`, `FormatDetailScreen.kt`, `MainActivity.kt`
* `SampleDialogs.kt`, `SampleFormat.kt`, `SampleUiState.kt`, `SampleViewModel.kt`
* `IntentBridgeReceiver.kt`, `HybridEventBridge.kt`, `AdEventLogger.kt`

#### iOS
* `AdDemoScreen.swift`, `ContentView.swift`
* `SampleFormat.swift`, `SampleSDKMode.swift`, `SampleState.swift`, `SampleViewModel.swift`
* `SdkHooks.swift`, `AdEventLogger.swift`, `HybridEventBridge.swift`

---

### 5. Recommended Delivery Packages

#### Option A: Native Ads Only
* **Docs**: `docs/publisher-minimal-integration.md`, `docs/nap-ssp-android-sdk-native.md`, `docs/nap-ssp-ios-sdk-native.md`
* **Android**: `bridge/NapSspConfig.kt`, `bridge/NapSspSdkIntegration.kt` + XML layouts.
* **iOS**: `NapSspConfig.swift`, `Bridge/NapSspSdkIntegration.swift` + XIB/modules.

#### Option B: Hybrid WebView Integration
* **Docs**: `docs/public-hybrid-bridge-guide.md`, `docs/publisher-minimal-integration.md`
* **Android**: `HybridWebViewScreen.kt` + `bridge/*` + XML layouts.
* **iOS**: `HybridWebViewScreen.swift` + `Bridge/*` + XIB.
* **Web**: Bridge request/response specifications (or sample `index.html`).

#### Option C: Full Sample (PoC / Technical Review)
* **Docs**: The entire `docs/` set + `quickstart.md`
* Deliver the full repository when reviewers need to run the interactive demo app end-to-end.

---

### 6. Pre-Production Checklist
- [ ] Apply production `MEDIA_KEY` and `AD_UNIT_ID` configurations.
- [ ] Substitute default Google Mobile Ads App IDs with active production credentials.
- [ ] Clean up unused third-party mediation dependencies.
- [ ] Audit and remove unused application permissions.
- [ ] Review Android `usesCleartextTraffic` configurations.
- [ ] Set appropriate logging levels for production builds.
- [ ] Make sure that Android layouts and iOS XIBs are linked to the build targets.
- [ ] For Hybrid WebView, verify that the action/format contracts match identically across Android and iOS platforms.

---

### 7. Summary
To lower the entry barrier for publishers, avoid sending the full repository immediately.
1. Provide **Option A** (with this minimal guide) for native implementations.
2. Provide **Option B** (with the public bridge guide) for hybrid layouts.
3. Provide the **full repository** (Option C) for PoC and technical reviews.

---

## 한국어

이 문서는 샘플 앱 전체가 아니라 **일반 매체 앱에 실제로 필요한 최소 파일과 설정**만 분리해서 전달하기 위한 가이드입니다.

샘플 앱은 데모 화면, 키 입력 UI, 하이브리드 WebView 예제까지 포함하므로 실제 연동에 필요한 코드보다 큽니다. 매체사에 전달할 때는 아래 항목만 추려서 안내하는 것을 권장합니다.

### 1. 먼저 결정할 것

연동 범위에 따라 필요한 파일이 달라집니다.

| 질문 | 필요 여부 |
|---|---|
| Native 앱 화면에 광고를 직접 붙이나요? | SDK 설정 + SDK 연동 래퍼 필요 |
| WebView 안의 JS에서 광고를 요청하나요? | Hybrid Bridge 추가 필요 |
| Native 광고 포맷을 사용하나요? | Android layout / iOS XIB 필요 |
| 전면/보상형 광고를 사용하나요? | Interstitial / Reward 모듈 필요 |
| 미디에이션을 사용하나요? | 미디에이션 dependency와 adapter 등록 필요 |

가장 작은 구성은 **SDK dependency + 권한/Info.plist + Config + SDK Integration**입니다.

---

### 2. Android 최소 구성

#### 2.1 필수/권장 Gradle 의존성

`android/app/build.gradle.kts`의 dependency 중 실제 매체 앱에 필요한 항목만 가져갑니다.

기본 SDK:
```kotlin
implementation("io.github.nasmedia-tech:admixer-ssp:1.0.23")
implementation("com.google.android.gms:play-services-ads-identifier:18.3.0")
```

미디에이션을 쓰는 경우 추가:
```kotlin
implementation("io.github.nasmedia-tech:admixer-admanager:1.0.14")
implementation("io.github.nasmedia-tech:admixer-adfit:1.0.11")
implementation("io.github.nasmedia-tech:admixer-pangle:1.0.11")
implementation("com.pangle.global:pag-sdk:8.0.0.4")
implementation("io.github.nasmedia-tech:admixer-applovin:1.0.8")
implementation("io.github.nasmedia-tech:admixer-unity:1.0.6")
```
> [!NOTE]
> 미디에이션을 사용하지 않는 매체 앱이라면 미디에이션 dependency와 adapter 등록 코드는 제거할 수 있습니다.

#### 2.2 Manifest 설정

샘플 기준 기본 권한:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="com.google.android.gms.permission.AD_ID" />
```

샘플에는 아래 권한도 포함되어 있지만, 운영 앱에서는 SDK 가이드와 앱 정책에 맞춰 필요 여부를 검토합니다.
```xml
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

Google Mobile Ads 또는 관련 미디에이션을 쓰는 경우 앱 ID도 운영 값으로 교체합니다.
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="YOUR_GOOGLE_MOBILE_ADS_APP_ID" />
```

#### 2.3 가져갈 핵심 Kotlin 파일

Native 광고 연동에 필요한 핵심 파일:
```text
android/app/src/main/java/.../bridge/NapSspConfig.kt
android/app/src/main/java/.../bridge/NapSspSdkIntegration.kt
```

역할:

| 파일 | 역할 |
|---|---|
| `NapSspConfig.kt` | MediaKey, AdUnitID 관리 |
| `NapSspSdkIntegration.kt` | SDK initialize, 광고 load/show, lifecycle, callback 처리 |

> [!TIP]
> 샘플의 `AppConfig.kt`는 앱 화면에서 키를 바꿔 넣기 위한 데모 편의 기능입니다. 실제 매체 앱에서는 `BuildConfig` / `local.properties` / CI secret으로 주입하거나, 서버 설정값으로 내려받는 등의 운영 방식에 맞는 구조로 대체하는 것을 권장합니다.

#### 2.4 Native 광고 사용 시 필요한 Android resource

Native 광고를 쓰면 아래 layout 리소스가 필요합니다.
```text
android/app/src/main/res/layout/admixer_item_320x480.xml
android/app/src/main/res/layout/admixer_item_300x250.xml
android/app/src/main/res/layout/admixer_item_320x100.xml
android/app/src/main/res/layout/admixer_item_320x50.xml
```
*Native 광고를 쓰지 않고 배너/비디오/전면/보상형만 쓴다면 이 layout들은 필수가 아닙니다.*

#### 2.5 Hybrid WebView 사용 시 추가 파일

WebView 안의 JavaScript에서 광고를 요청하는 구조라면 추가로 필요합니다.
```text
android/app/src/main/java/.../HybridWebViewScreen.kt
android/app/src/main/assets/index.html   # 샘플용 HTML 파일
```

---

### 3. iOS 최소 구성

#### 3.1 SDK package 설정

샘플의 `ios/project.yml` 기준 핵심 package는 아래입니다.
```yaml
AdMixer
AdMixerMediation
AdMixerMediationGAM
AdMixerMediationAdFit
```
미디에이션 네트워크를 더 쓰는 경우 해당 adapter package를 추가합니다. 쓰지 않는 adapter는 제외해도 됩니다.

#### 3.2 Info.plist 설정

ATT, IDFA, Google Mobile Ads 또는 미디에이션 사용 여부에 따라 기존 Info.plist 또는 Build Settings에 관련 항목을 운영용 문구와 키로 적용합니다.
```text
NSUserTrackingUsageDescription
GADApplicationIdentifier
```

#### 3.3 가져갈 핵심 Swift 파일

Native 광고 연동 핵심:
```text
ios/Sources/NapSspIOSSample/NapSspConfig.swift
ios/Sources/NapSspIOSSample/Bridge/NapSspSdkIntegration.swift
```

포맷별 추가 파일:
```text
ios/Sources/NapSspIOSSample/Bridge/InterstitialModule.swift   # 전면 광고 사용 시
ios/Sources/NapSspIOSSample/Bridge/RewardedModule.swift       # 보상형 광고 사용 시
ios/Sources/NapSspIOSSample/Bridge/NapSspAdEventBridge.swift  # 이벤트 브릿지/내부 이벤트 전달 사용 시
```

역할:

| 파일 | 역할 |
|---|---|
| `NapSspConfig.swift` | MediaKey, AdUnitID 관리 |
| `NapSspSdkIntegration.swift` | SDK initialize, 광고 load/show, delegate callback 처리 |
| `InterstitialModule.swift` | 전면 배너/팝업 로드와 표시 보조 |
| `RewardedModule.swift` | 보상형 광고 로드와 표시 보조 |

#### 3.4 Native 광고 사용 시 필요한 XIB

Native 광고를 쓰면 아래 XIB가 필요합니다.
```text
ios/Sources/NapSspIOSSample/AMMNativeAdView.xib
```

#### 3.5 Hybrid WebView 사용 시 추가 파일

WebView 안의 JavaScript에서 광고를 요청하는 구조라면 추가로 필요합니다.
```text
ios/Sources/NapSspIOSSample/HybridWebViewScreen.swift
ios/Sources/NapSspIOSSample/index.html   # 샘플용. 실제 앱에서는 매체 웹 코드로 대체 가능
```

---

### 4. 샘플 전용이라 보통 빼도 되는 파일

#### Android
* `AdDemoScreen.kt`, `FormatDetailScreen.kt`, `MainActivity.kt`
* `SampleDialogs.kt`, `SampleFormat.kt`, `SampleUiState.kt`, `SampleViewModel.kt`
* `IntentBridgeReceiver.kt`, `HybridEventBridge.kt`, `AdEventLogger.kt`

#### iOS
* `AdDemoScreen.swift`, `ContentView.swift`
* `SampleFormat.swift`, `SampleSDKMode.swift`, `SampleState.swift`, `SampleViewModel.swift`
* `SdkHooks.swift`, `AdEventLogger.swift`, `HybridEventBridge.swift`

---

### 5. 전달 패키지 권장안

#### A안. Native 광고만 연동하는 매체사
* **전달 문서**: `docs/publisher-minimal-integration.md`, `docs/nap-ssp-android-sdk-native.md`, `docs/nap-ssp-ios-sdk-native.md`
* **Android**: `bridge/NapSspConfig.kt`, `bridge/NapSspSdkIntegration.kt`, 필요한 layout
* **iOS**: `NapSspConfig.swift`, `Bridge/NapSspSdkIntegration.swift`, 필요한 XIB/모듈

#### B안. Hybrid WebView까지 연동하는 매체사
* **전달 문서**: `docs/public-hybrid-bridge-guide.md`, `docs/publisher-minimal-integration.md`
* **Android**: `HybridWebViewScreen.kt` + `bridge/*` + 필요한 layout
* **iOS**: `HybridWebViewScreen.swift` + `Bridge/*` + 필요한 XIB
* **JS**: bridge request/response 규격 또는 샘플 index.html

#### C안. 전체 샘플 (PoC/기술 검토용)
* **전달 문서**: `docs/` 문서 전체 + `quickstart.md`
* 데모 앱을 직접 실행해보는 PoC/레퍼런스 검토가 필요할 때 저장소 전체를 전달합니다.

---

### 6. 운영 반영 전 체크리스트
- [ ] 운영 `MEDIA_KEY`와 `AD_UNIT_ID`를 적용했는가?
- [ ] 테스트용 Google Mobile Ads App ID를 운영 값으로 교체했는가?
- [ ] 사용하지 않는 미디에이션 dependency를 제거했는가?
- [ ] 사용하지 않는 권한을 제거했는가?
- [ ] Android `usesCleartextTraffic` 운영 정책을 확인했는가?
- [ ] Debug log level을 운영 정책에 맞게 조정했는가?
- [ ] Native 광고를 쓰는 경우 layout/XIB가 앱 target에 포함되어 있는가?
- [ ] Hybrid WebView 사용 시 JS request/response contract가 Android/iOS에서 동일한가?

---

### 7. 정리

매체사에 샘플앱 전체를 그대로 전달하면 구조가 커 보여서 진입 장벽이 생길 수 있습니다.

권장 방식은 다음과 같습니다.
1. **일반 매체사**에는 이 최소 연동 가이드와 필요한 파일 목록만 전달합니다.
2. **Hybrid WebView 매체사**에는 공개 브릿지 가이드를 함께 전달합니다.
3. **PoC/기술 검토**에는 전체 샘플앱과 문서를 전달합니다.
