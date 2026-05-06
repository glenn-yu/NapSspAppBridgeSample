# 내부 개발자용 NapSsp AppBridge 가이드

이 문서는 저장소 유지보수자와 내부 개발자를 위한 상세 가이드입니다. 공개용 설명보다 구현 파일, 검증 절차, 리스크 항목을 더 구체적으로 다룹니다.

## 1. 문서/코드 분리 방향

현재 저장소는 **실행 가능한 통합 샘플 앱**입니다. 따라서 데모 UI, 테스트 자동화, 키 입력 화면, WebView 브릿지, Native SDK 호출 예제가 함께 들어 있습니다.

외부 전달 시에는 전체 샘플앱을 그대로 주기보다 아래처럼 분리하는 것이 좋습니다.

- 일반 매체사: `publisher-minimal-integration.md` 중심으로 최소 파일만 안내
- Hybrid WebView 매체사: `public-hybrid-bridge-guide.md` + 최소 연동 파일 안내
- 내부 개발/QA: 이 문서와 `bridge-validation-report.md`, Maestro 테스트까지 포함
- PoC/레퍼런스 검토: 저장소 전체와 `quickstart.md` 제공

전달 범위 선택 기준은 `docs/delivery-packages.md`에 정리되어 있습니다.

## 2. 현재 구현 파일

### 공통 Web UI

- Android: `android/app/src/main/assets/index.html`
- iOS: `ios/Sources/NapSspIOSSample/index.html`

주의: 두 HTML은 역할은 같지만 완전히 같은 파일은 아닙니다. JS API를 변경할 때는 양쪽 파일을 동시에 확인해야 합니다.

### Android

- 브릿지/UI: `android/app/src/main/java/com/gwangy/nassspandroidsample/HybridWebViewScreen.kt`
- SDK 연동: `android/app/src/main/java/com/gwangy/nassspandroidsample/bridge/NapSspSdkIntegration.kt`
- 설정: `android/app/src/main/java/com/gwangy/nassspandroidsample/bridge/NapSspConfig.kt`
- 런타임 설정 저장: `android/app/src/main/java/com/gwangy/nassspandroidsample/AppConfig.kt`
- 테스트용 intent receiver: `android/app/src/main/java/com/gwangy/nassspandroidsample/IntentBridgeReceiver.kt`
- Manifest: `android/app/src/main/AndroidManifest.xml`

### iOS

- 브릿지/UI: `ios/Sources/NapSspIOSSample/HybridWebViewScreen.swift`
- SDK 연동: `ios/Sources/NapSspIOSSample/Bridge/NapSspSdkIntegration.swift`
- 설정: `ios/Sources/NapSspIOSSample/NapSspConfig.swift`
- Reward 모듈: `ios/Sources/NapSspIOSSample/Bridge/RewardedModule.swift`
- Interstitial 모듈: `ios/Sources/NapSspIOSSample/Bridge/InterstitialModule.swift`
- XcodeGen 설정: `ios/project.yml`

## 3. Bridge contract

### Request

```json
{
  "action": "init | loadAd | clearAds",
  "params": {
    "format": "banner | native | video | rewardVideo | interstitialVideo | interstitialBanner",
    "adUnitId": "optional string"
  }
}
```

### Response

```json
{
  "action": "init | loadAd | clearAds | event | error",
  "status": "success | error",
  "data": "message"
}
```

### 지원 포맷

브릿지 계층에서 지원 포맷을 먼저 검증합니다.

- `banner`
- `native`
- `video`
- `rewardVideo`
- `interstitialVideo`
- `interstitialBanner`

지원하지 않는 포맷은 SDK로 넘기지 않고 `loadAd/error`로 응답해야 합니다.

## 4. Android 상세 흐름

1. `HybridWebViewScreen`이 WebView를 생성합니다.
2. `NapSspHybridBridge`를 `NapSspBridge` 이름으로 JS에 주입합니다.
3. JS가 `window.NapSspBridge.postMessage(json)`을 호출합니다.
4. `postMessage`가 JSON을 파싱하고 action/format을 검증합니다.
5. `init`은 `NapSspSdkIntegration.initialize(context)`를 호출합니다.
6. `loadAd`는 Compose coroutine을 통해 기존 광고를 해제하고 새 광고 View를 요청합니다.
7. View형 광고는 Compose 하단 `AndroidView(FrameLayout)`에 붙입니다.
8. 전체 화면 광고는 SDK show API를 호출하고 하단 View는 만들지 않습니다.
9. SDK 이벤트는 `onAdEventCallback`을 통해 JS `window.onNapSspMessage(...)`로 전달합니다.

### Android 생명주기

`LifecycleEventObserver`가 다음을 호출합니다.

- `ON_RESUME` → `NapSspSdkIntegration.resumeAll()`
- `ON_PAUSE` → `NapSspSdkIntegration.pauseAll()`
- `ON_DESTROY` → `NapSspSdkIntegration.clearAllAds()`

광고 교체 전에도 `clearAllAds()` 또는 format별 destroy가 선행됩니다.

## 5. iOS 상세 흐름

1. `HybridWebViewScreen`이 `WKWebView`를 생성합니다.
2. `WKUserContentController`에 `NapSspBridge` handler를 등록합니다.
3. 순환 참조 방지를 위해 `LeakAvoider` proxy를 사용합니다.
4. JS가 `window.webkit.messageHandlers.NapSspBridge.postMessage(json)`을 호출합니다.
5. `userContentController(_:didReceive:)`가 JSON을 디코딩하고 action/format을 검증합니다.
6. `init`은 `NapSspSdkIntegration.initializeSdk()`를 호출합니다.
7. View형 광고는 SwiftUI 하단 `AdViewRepresentable`에 붙입니다.
8. 전체 화면 광고는 SDK show API를 호출하고 하단 View는 만들지 않습니다.
9. SDK 이벤트는 `onAdEventCallback`과 `NapSspAdEventBridge`를 통해 전달됩니다.

## 6. 오류 방지 기준

### JSON escaping

Native → JS 응답은 JSON 문자열을 JS string literal로 전달합니다.

- Android: `JSONObject.quote(response.toString())` 사용
- iOS: `JSONSerialization.data(withJSONObject: jsonStr, options: [.fragmentsAllowed])`로 JS 인자 문자열 생성

직접 `'...'` 문자열 결합만 사용하면 작은따옴표, 개행, 역슬래시 포함 데이터에서 JS syntax error가 날 수 있습니다.

### 즉시 ACK

`loadAd` 요청은 광고 네트워크 응답을 기다리지 않고 `Accepted <format>` ACK를 반환합니다. 이 ACK는 브릿지 수신/파싱/라우팅 성공을 검증하기 위한 값입니다.

광고 SDK 로드 성공/실패는 별도의 `event` 응답으로 전달됩니다.

### 포맷 검증

브릿지는 지원하지 않는 `format`을 SDK 계층으로 넘기지 않습니다. 이 기준은 테스트 자동화에서 bridge contract를 확인하는 핵심 조건입니다.

## 7. 빌드 환경

### Android

현재 확인된 로컬 환경:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME="$HOME/Library/Android/sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"

cd android
./gradlew :app:assembleDebug --no-daemon
```

주의: Homebrew로 `openjdk@17`이 설치되어 있어도 macOS `/usr/libexec/java_home`에 등록되어 있지 않으면 `java` 실행이 실패할 수 있습니다. 위처럼 `JAVA_HOME`과 `PATH`를 명시하면 빌드가 가능합니다.

### iOS

```bash
cd ios
xcodebuild -scheme NapSspIOSSample -destination 'generic/platform=iOS Simulator' -derivedDataPath .derivedData build
```

SPM binary dependency resolve가 네트워크 상태에 영향을 받습니다.

## 8. Maestro 테스트

테스트 파일:

- `maestro/android-bridge-smoke.yaml`
- `maestro/ios-bridge-smoke.yaml`
- `maestro/android-all-formats.yaml`
- `maestro/ios-all-formats.yaml`

Android 실행 전제:

- Android emulator 또는 실제 디바이스가 `adb devices`에 online 상태로 표시되어야 합니다.
- Debug APK가 빌드되어 있어야 합니다.
- 앱 ID: `com.gwangy.nassspandroidsample`

Android 실행:

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
maestro test maestro/android-bridge-smoke.yaml
```

iOS 실행 전제:

- iOS Simulator가 booted 상태여야 합니다.
- Simulator용 앱이 빌드/설치되어 있어야 합니다.
- 앱 ID: `com.nasmedia.NapSspIOSSample`
- Maestro 실행 시 `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`을 명시합니다.

iOS 실행:

```bash
maestro test --platform ios --udid <SIMULATOR_UDID> maestro/ios-bridge-smoke.yaml
```

검증 항목:

- WebView bridge ready 문구 노출
- `Initialize SDK` 요청 후 `INIT SUCCESS` 로그 노출
- `Load Banner` 요청 후 `LOADAD SUCCESS` ACK 노출
- `Clear All Ads & Releases` 요청 후 `CLEARADS SUCCESS` 로그 노출
- 전체 포맷 테스트에서 `banner`, `native`, `video`, `rewardVideo`, `interstitialVideo`, `interstitialBanner` 요청 실행
- bridge 관련 crash 또는 JS bridge not found 오류 미노출

## 9. 운영 전 리스크 정리

아래 항목은 샘플/테스트 편의를 위해 남아 있습니다. 운영 앱 반영 시 정책 검토가 필요합니다.

- Android `READ_PHONE_STATE` 권한
- Android `usesCleartextTraffic="true"`
- Android exported `IntentBridgeReceiver`
- Android SDK log level `DEBUG`
- 테스트용 Google Mobile Ads App ID
- Reward custom params 샘플값
- Android/iOS HTML 중복 관리

## 10. 변경 시 체크리스트

- [ ] Android/iOS 양쪽 bridge action이 동일한가?
- [ ] Android/iOS 양쪽 format set이 동일한가?
- [ ] Native → JS 응답 escaping이 안전한가?
- [ ] `loadAd` ACK와 SDK event가 분리되어 있는가?
- [ ] 기존 광고 destroy 후 새 광고를 생성하는가?
- [ ] Compose/SwiftUI 컨테이너에서 기존 parent 제거 후 View를 붙이는가?
- [ ] Maestro smoke 테스트를 업데이트했는가?
- [ ] 공개 문서와 내부 문서의 민감 정보 범위가 분리되어 있는가?
