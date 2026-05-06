# 매체 앱 최소 연동 가이드

이 문서는 샘플 앱 전체가 아니라 **일반 매체 앱에 실제로 필요한 최소 파일과 설정**만 분리해서 전달하기 위한 가이드입니다.

샘플 앱은 데모 화면, 테스트 자동화, 키 입력 UI, 하이브리드 WebView 예제까지 포함하므로 실제 연동에 필요한 코드보다 큽니다. 매체사에 전달할 때는 아래 항목만 추려서 안내하는 것을 권장합니다.

## 1. 먼저 결정할 것

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

## 2. Android 최소 구성

### 2.1 필수/권장 Gradle 의존성

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

> 미디에이션을 사용하지 않는 매체 앱이라면 미디에이션 dependency와 adapter 등록 코드는 제거할 수 있습니다.

### 2.2 Manifest 설정

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

### 2.3 가져갈 핵심 Kotlin 파일

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

샘플의 `AppConfig.kt`는 앱 화면에서 키를 바꿔 넣기 위한 데모 편의 기능입니다. 실제 매체 앱에서는 다음 중 하나로 대체하는 것이 좋습니다.

- `BuildConfig` / `local.properties` / CI secret으로 주입
- 서버 설정값으로 내려받기
- 앱 내부 운영 config 모듈에 통합

### 2.4 Native 광고 사용 시 필요한 Android resource

Native 광고를 쓰면 아래 layout 리소스가 필요합니다.

```text
android/app/src/main/res/layout/admixer_item_320x480.xml
android/app/src/main/res/layout/admixer_item_300x250.xml
android/app/src/main/res/layout/admixer_item_320x100.xml
android/app/src/main/res/layout/admixer_item_320x50.xml
```

Native 광고를 쓰지 않고 배너/비디오/전면/보상형만 쓴다면 이 layout들은 필수가 아닙니다.

### 2.5 Hybrid WebView 사용 시 추가 파일

WebView 안의 JavaScript에서 광고를 요청하는 구조라면 추가로 필요합니다.

```text
android/app/src/main/java/.../HybridWebViewScreen.kt
android/app/src/main/assets/index.html   # 샘플용. 실제 앱에서는 매체 웹 코드로 대체 가능
```

실제 매체 앱에서는 `index.html` 전체를 가져가기보다, JS bridge 호출 규격만 기존 웹 페이지에 반영하는 편이 좋습니다.

---

## 3. iOS 최소 구성

### 3.1 SDK package 설정

샘플의 `ios/project.yml` 기준 핵심 package는 아래입니다.

```yaml
AdMixer
AdMixerMediation
AdMixerMediationGAM
AdMixerMediationAdFit
```

미디에이션 네트워크를 더 쓰는 경우 해당 adapter package를 추가합니다. 쓰지 않는 adapter는 제외해도 됩니다.

### 3.2 Info.plist 설정

샘플은 `project.yml`에서 Info.plist 항목을 생성합니다. 실제 앱에서는 기존 Info.plist 또는 build setting에 반영합니다.

주요 항목:

```text
NSUserTrackingUsageDescription
GADApplicationIdentifier
```

ATT, IDFA, Google Mobile Ads 또는 미디에이션 사용 여부에 따라 운영 값과 문구를 적용합니다.

### 3.3 가져갈 핵심 Swift 파일

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

### 3.4 Native 광고 사용 시 필요한 XIB

Native 광고를 쓰면 아래 XIB가 필요합니다.

```text
ios/Sources/NapSspIOSSample/AMMNativeAdView.xib
```

Native 광고를 쓰지 않으면 필수가 아닙니다.

### 3.5 Hybrid WebView 사용 시 추가 파일

WebView 안의 JavaScript에서 광고를 요청하는 구조라면 추가로 필요합니다.

```text
ios/Sources/NapSspIOSSample/HybridWebViewScreen.swift
ios/Sources/NapSspIOSSample/index.html   # 샘플용. 실제 앱에서는 매체 웹 코드로 대체 가능
```

실제 매체 앱에서는 `index.html` 전체보다 JS bridge 요청/응답 규격만 전달하는 것이 좋습니다.

---

## 4. 샘플 전용이라 보통 빼도 되는 파일

### Android

```text
AdDemoScreen.kt
FormatDetailScreen.kt
MainActivity.kt
SampleDialogs.kt
SampleFormat.kt
SampleUiState.kt
SampleViewModel.kt
IntentBridgeReceiver.kt
HybridEventBridge.kt
AdEventLogger.kt
```

`AdEventLogger.kt`는 디버깅에 유용하지만 운영 필수는 아닙니다.

### iOS

```text
AdDemoScreen.swift
ContentView.swift
SampleFormat.swift
SampleSDKMode.swift
SampleState.swift
SampleViewModel.swift
SdkHooks.swift
AdEventLogger.swift
HybridEventBridge.swift
```

`App.swift`, `AppDelegate.swift`는 실제 앱의 기존 진입점에 SDK 초기화 코드를 통합하면 됩니다.

---

## 5. 전달 패키지 권장안

### A안. Native 광고만 연동하는 매체사

전달 문서:

```text
docs/publisher-minimal-integration.md
docs/nap-ssp-android-sdk-native.md
docs/nap-ssp-ios-sdk-native.md
```

전달 코드 범위:

```text
Android: bridge/NapSspConfig.kt, bridge/NapSspSdkIntegration.kt, 필요한 layout
iOS: NapSspConfig.swift, Bridge/NapSspSdkIntegration.swift, 필요한 XIB/모듈
```

### B안. Hybrid WebView까지 연동하는 매체사

전달 문서:

```text
docs/public-hybrid-bridge-guide.md
docs/publisher-minimal-integration.md
```

전달 코드 범위:

```text
Android: HybridWebViewScreen.kt + bridge/* + 필요한 layout
iOS: HybridWebViewScreen.swift + Bridge/* + 필요한 XIB
 JS: bridge request/response 규격 또는 샘플 index.html
```

### C안. 내부 유지보수/QA용

전달 문서:

```text
docs/internal-bridge-developer-guide.md
docs/bridge-validation-report.md
docs/mediation.md
```

샘플앱 전체 구조와 테스트 자동화까지 필요할 때만 이 묶음을 사용합니다.

---

## 6. 운영 반영 전 체크리스트

- [ ] 운영 `MEDIA_KEY`와 `AD_UNIT_ID`를 적용했는가?
- [ ] 테스트용 Google Mobile Ads App ID를 운영 값으로 교체했는가?
- [ ] 사용하지 않는 미디에이션 dependency를 제거했는가?
- [ ] 사용하지 않는 권한을 제거했는가?
- [ ] Android `usesCleartextTraffic` 운영 정책을 확인했는가?
- [ ] Debug log level을 운영 정책에 맞게 조정했는가?
- [ ] Native 광고를 쓰는 경우 layout/XIB가 앱 target에 포함되어 있는가?
- [ ] 전면/보상형 광고는 실제 표시할 View를 앱 화면에 붙이지 않는다는 점을 반영했는가?
- [ ] Hybrid WebView 사용 시 JS request/response contract가 Android/iOS에서 동일한가?

---

## 7. 정리

매체사에 샘플앱 전체를 그대로 전달하면 구조가 커 보여서 진입 장벽이 생길 수 있습니다.

권장 방식은 다음과 같습니다.

1. **일반 매체사**에는 이 최소 연동 가이드와 필요한 파일 목록만 전달합니다.
2. **Hybrid WebView 매체사**에는 공개 브릿지 가이드를 함께 전달합니다.
3. **내부 개발/QA**에는 전체 샘플앱과 내부 개발자 가이드를 전달합니다.

즉, 샘플앱은 레퍼런스 구현으로 유지하고 외부 전달은 `최소 연동 패키지` 중심으로 분리하는 편이 가장 좋습니다.
