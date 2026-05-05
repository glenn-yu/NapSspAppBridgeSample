# Bridge 검증 리포트

검증 일시: 2026-05-05 KST

## 1. 환경 확인

### JDK

Homebrew 설치 상태:

- `openjdk`
- `openjdk@17`

macOS 기본 Java 탐색은 JDK를 찾지 못했습니다.

```text
/usr/libexec/java_home -V
→ Unable to locate a Java Runtime
```

빌드에는 다음 경로를 명시해 사용했습니다.

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME="$HOME/Library/Android/sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
```

확인된 Java 버전:

```text
openjdk version "17.0.19" 2026-04-21
OpenJDK Runtime Environment Homebrew (build 17.0.19+0)
```

### Maestro

설치:

```bash
brew tap mobile-dev-inc/tap
brew install mobile-dev-inc/tap/maestro
```

확인된 버전:

```text
maestro 2.5.1
```

### Android device

사용 AVD:

```text
Pixel_6_API_34
```

실행 상태:

```text
emulator-5554 device
```

## 2. 적용한 Bridge 안정성 보완

### Android

파일: `android/app/src/main/java/com/gwangy/nassspandroidsample/HybridWebViewScreen.kt`

- 지원 format allowlist 추가
- 알 수 없는 format은 SDK에 전달하지 않고 `loadAd/error`로 응답
- `loadAd` 요청 수신 직후 `Accepted <format>` ACK 반환
- Native → JS 응답 시 `JSONObject.quote(...)`로 JS string literal escaping 처리

### iOS

파일: `ios/Sources/NapSspIOSSample/HybridWebViewScreen.swift`

- 지원 format allowlist 추가
- 잘못된 JSON에 `error/error` 응답 추가
- 알 수 없는 format은 SDK에 전달하지 않고 `loadAd/error`로 응답
- root view controller 미확보 시 `loadAd/error` 응답
- Native → JS 응답 시 `JSONSerialization`으로 JS string literal escaping 처리

## 3. Android 빌드 검증

실행 명령:

```bash
cd android
./gradlew :app:assembleDebug --no-daemon
```

결과:

```text
BUILD SUCCESSFUL
38 actionable tasks
```

경고:

- `LocalLifecycleOwner` deprecated
- `WebSettings.databaseEnabled` deprecated

위 경고는 브릿지 동작 실패 원인은 아닙니다.

## 4. Android Maestro 시나리오

파일:

- `maestro/android-bridge-smoke.yaml`

검증 항목:

1. 앱 실행
2. WebView 샘플 화면 노출 확인
3. `Initialize SDK` 클릭
4. `INIT SUCCESS` 로그 확인
5. `Load Banner` 클릭
6. `LOADAD SUCCESS: Accepted banner` 로그 확인
7. `Clear All Ads & Releases` 클릭
8. `CLEARADS SUCCESS` 로그 확인

실행 명령:

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
maestro test maestro/android-bridge-smoke.yaml
```

결과:

```text
Flow android-bridge-smoke
Launch app "com.gwangy.nassspandroidsample" with clear state... COMPLETED
Assert that "NapSsp Hybrid Sample" is visible... COMPLETED
Tap on "Initialize SDK"... COMPLETED
Scrolling DOWN until ".*INIT SUCCESS.*" is visible... COMPLETED
Scrolling UP until "Load Banner.*" is visible... COMPLETED
Tap on "Load Banner.*"... COMPLETED
Scrolling DOWN until ".*LOADAD SUCCESS.*Accepted banner.*" is visible... COMPLETED
Scrolling DOWN until "Clear All Ads & Releases" is visible... COMPLETED
Tap on "Clear All Ads & Releases"... COMPLETED
Scrolling DOWN until ".*CLEARADS SUCCESS.*" is visible... COMPLETED
```

Maestro 결과: 통과

## 5. iOS 빌드 검증

### 원인 확인

처음 `xcodebuild`가 SPM dependency resolve 단계에서 장시간 멈춘 원인은 SwiftPM이 GitHub 인증 정보를 읽기 위해 macOS Keychain 조회(`SecItemCopyMatching`)에 머문 것이었습니다.

다음 옵션으로 Keychain 인증 공급자를 우회하자 실제 dependency 오류가 확인되었습니다.

```bash
-packageAuthorizationProvider netrc
```

확인된 실제 오류:

```text
failed downloading 'https://github.com/Nasmedia-Tech/iOS-AdMixerDownload/raw/main/AdMixerMediationAdFit1.0.8.xcframework.zip': badResponseStatusCode(404)
failed downloading 'https://github.com/Nasmedia-Tech/iOS-AdMixerDownload/raw/master/AdMixerSDK_iOS_v1.6.1.zip': badResponseStatusCode(404)
```

GitHub repository content 확인 결과 해당 파일명은 존재하지 않았고, 사용 가능한 파일명으로 교체했습니다.

변경 파일:

- `ios/Vendor/Packages/AdMixer/Package.swift`
  - `AdMixerSDK_iOS_v1.6.1.zip` → `AdMixer1.1.6.xcframework.zip`
  - checksum: `60a7896d90e2d1ccab9f1b051ebeb42c8bb53c7838a460fc3b5c8b0c867652de`
- `ios/Vendor/Packages/AdMixerMediationAdFit/Package.swift`
  - `AdMixerMediationAdFit1.0.8.xcframework.zip` → `AdMixerMediationAdFit1.0.7.xcframework.zip`
  - checksum: `800d663320d261ef22f646cd221741c9a1ea3f0ecb0f79c31708f3a2e08e4c14`

### 추가 컴파일/런타임 오류 수정

SPM resolve 후 Swift 컴파일 단계에서 다음 오류가 확인되어 수정했습니다.

```text
instance member 'onAdEventCallback' cannot be used on type 'NapSspSdkIntegration'
value of type 'WKWebView' has no member 'evaluateJavascript'
```

수정 파일:

- `ios/Sources/NapSspIOSSample/HybridWebViewScreen.swift`
  - `NapSspSdkIntegration.onAdEventCallback` → `NapSspSdkIntegration.shared.onAdEventCallback`
  - `evaluateJavascript` → `evaluateJavaScript(..., completionHandler: nil)`

최초 iOS Maestro 실행 중 `Initialize SDK` 탭 직후 다음 런타임 크래시가 확인되어 수정했습니다.

```text
NSInvalidArgumentException: +[NSJSONSerialization dataWithJSONObject:options:error:]: Invalid top-level type in JSON write
```

원인과 수정:

- 원인: Native → JS 응답용 JS string literal을 만들 때 top-level `String`을 `JSONSerialization`에 넘기면서 `.fragmentsAllowed` 옵션이 없었습니다.
- 수정: `JSONSerialization.data(withJSONObject: jsonStr, options: [.fragmentsAllowed])` 적용

### 검증 명령

SPM resolve:

```bash
cd ios
xcodebuild -resolvePackageDependencies   -project NapSspIOSSample.xcodeproj   -scheme NapSspIOSSample   -clonedSourcePackagesDirPath .spm-cache   -packageAuthorizationProvider netrc   -verbose
```

결과:

```text
resolved source packages: GoogleMobileAds, AdMixerMediationAdFit, AdFit, AdMixer, AdMixerMediationGAM, AdMixerMediation, GoogleUserMessagingPlatform
```

Simulator 빌드:

```bash
cd ios
xcodebuild   -project NapSspIOSSample.xcodeproj   -scheme NapSspIOSSample   -destination 'platform=iOS Simulator,id=F5390915-AD8B-47EC-9C54-4B892FFDF011'   -derivedDataPath .derivedData   -clonedSourcePackagesDirPath .spm-cache   -packageAuthorizationProvider netrc   build
```

결과:

```text
** BUILD SUCCEEDED **
```

설치 대상:

```text
iPhone 17 Pro - iOS 26.4 - F5390915-AD8B-47EC-9C54-4B892FFDF011
```

경고:

- `AMMNativeAdView.xib`: deployment target보다 오래된 버전 기준으로 설정되어 기능 제한 가능 경고
- AppIntents metadata extraction skipped 경고

위 경고는 iOS 빌드 실패 원인은 아닙니다.

## 6. iOS Maestro 시나리오

파일:

- `maestro/ios-bridge-smoke.yaml`

검증 항목:

1. 앱 실행
2. iOS WebView 샘플 화면 노출 확인
3. `Initialize SDK` 클릭
4. `INIT SUCCESS` 로그 확인
5. `Load Banner` 클릭
6. `LOADAD SUCCESS: Accepted banner` 로그 확인
7. `Clear All Ads & Releases` 클릭
8. `CLEARADS SUCCESS` 로그 확인

실행 명령:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
maestro test --platform ios --udid F5390915-AD8B-47EC-9C54-4B892FFDF011 maestro/ios-bridge-smoke.yaml
```

결과:

```text
Flow ios-bridge-smoke
Launch app "com.nasmedia.NapSspIOSSample" with clear state... COMPLETED
Assert that "NapSsp iOS Hybrid" is visible... COMPLETED
Tap on "Initialize SDK"... COMPLETED
Scrolling DOWN until ".*INIT SUCCESS.*" is visible... COMPLETED
Scrolling UP until "Load Banner.*" is visible... COMPLETED
Tap on "Load Banner.*"... COMPLETED
Scrolling DOWN until ".*LOADAD SUCCESS.*Accepted banner.*" is visible... COMPLETED
Scrolling DOWN until "Clear All Ads & Releases" is visible... COMPLETED
Tap on "Clear All Ads & Releases"... COMPLETED
Scrolling DOWN until ".*CLEARADS SUCCESS.*" is visible... COMPLETED
```

Maestro 결과: 통과

## 7. Bridge 오류 확인

Android Maestro smoke 테스트 중 다음 브릿지 실패 문구는 발생하지 않았습니다.

- `Bridge not found`
- `Invalid JSON`
- `Unsupported format`
- 앱 프로세스 `FATAL EXCEPTION`

Android Logcat에는 Google Play services 구버전 경고와 에뮬레이터/시스템 로그가 있었지만, 테스트 대상 앱의 브릿지 crash로 확인된 항목은 없었습니다.

iOS는 최초 Maestro 실행에서 Native → JS escaping 구현 문제로 `NSInvalidArgumentException` 크래시가 확인되었습니다. `.fragmentsAllowed` 수정 후 재빌드/재설치하고 동일 Maestro 시나리오를 재실행했으며 통과했습니다.

수정 후 최근 iOS 로그에서 다음 브릿지 실패 문구 또는 앱 크래시는 재현되지 않았습니다.

- `Bridge not found`
- `Invalid JSON`
- `Unsupported format`
- `NSInvalidArgumentException`
- `SIGABRT`

## 8. 결론

Android와 iOS 모두 WebView ↔ Native bridge가 다음 수준까지 검증되었습니다.

- JS → Native `init` 요청 정상 처리
- JS → Native `loadAd` 요청 정상 수신 및 ACK 반환
- JS → Native `clearAds` 요청 정상 처리
- Native → JS 응답 문자열 escaping 보완
- Maestro UI smoke 테스트 통과
- 수정 후 bridge not found / JSON parsing / unsupported format / app fatal crash 미재현

## 9. 전체 포맷 Maestro 추가 검증

요청에 따라 기본 banner smoke 외에 전체 지원 포맷을 추가로 실행했습니다.

대상 포맷:

- `banner`
- `native`
- `video`
- `rewardVideo`
- `interstitialVideo`
- `interstitialBanner`

추가 테스트 파일:

- `maestro/android-all-formats.yaml`
- `maestro/ios-all-formats.yaml`

### Android 전체 포맷 결과

실행 명령:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
maestro test maestro/android-all-formats.yaml
```

결과:

```text
Flow android-all-formats
Load Banner -> LOADAD SUCCESS Accepted banner ... COMPLETED
Load Native Ad -> LOADAD SUCCESS Accepted native ... COMPLETED
Load Outstream Video -> LOADAD SUCCESS Accepted video ... COMPLETED
Show Reward Video -> screenshot captured after native full-screen ad presentation ... COMPLETED
Show Interstitial Video -> LOADAD SUCCESS Accepted interstitialVideo ... COMPLETED
Show Interstitial Banner -> screenshot captured after native full-screen ad presentation ... COMPLETED
Process exited with code 0
```

확인 사항:

- `banner`, `native`, `video`, `interstitialVideo`는 WebView 로그에서 `LOADAD SUCCESS: Accepted <format>` ACK를 확인했습니다.
- `rewardVideo`, `interstitialBanner`는 전체 화면 광고가 WebView를 덮어 ACK 로그가 UI에 노출되지 않아, Maestro에서 탭 후 full-screen ad presentation까지 진행하고 screenshot을 캡처했습니다.
- Android 로그 확인 결과 테스트 앱의 `FATAL EXCEPTION`, `Bridge not found`, `Invalid JSON`, `Unsupported format`은 확인되지 않았습니다.
- `interstitialVideo`는 bridge ACK는 정상이며, SDK 로그에 `This adUnit ID(104702) is not support interstitial`가 출력되었습니다. 이는 현재 테스트 ad unit 설정 관련 SDK 응답으로, bridge 라우팅 실패나 앱 크래시는 아닙니다.

### iOS 전체 포맷 결과

실행 명령:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
maestro test --platform ios --udid F5390915-AD8B-47EC-9C54-4B892FFDF011 maestro/ios-all-formats.yaml
```

결과:

```text
Flow ios-all-formats
Load Banner -> LOADAD SUCCESS Accepted banner ... COMPLETED
Load Native Ad -> LOADAD SUCCESS Accepted native ... COMPLETED
Load Outstream Video -> LOADAD SUCCESS Accepted video ... COMPLETED
Show Reward Video -> LOADAD SUCCESS Accepted rewardVideo ... COMPLETED
Show Interstitial Video -> LOADAD SUCCESS Accepted interstitialVideo ... COMPLETED
Show Interstitial Banner -> LOADAD SUCCESS Accepted interstitialBanner ... COMPLETED
Process exited with code 0
```

확인 사항:

- iOS는 6개 포맷 모두 WebView 로그에서 `LOADAD SUCCESS: Accepted <format>` ACK를 확인했습니다.
- 최근 iOS 로그 확인 결과 `NSInvalidArgumentException`, `SIGABRT`, `Bridge not found`, `Invalid JSON`, `Unsupported format`은 확인되지 않았습니다.
