# NapSsp App Bridge Sample

NapSsp(Nasmedia AdMixer SSP) SDK를 WebView 하이브리드 방식으로 연동하는 Android/iOS 샘플 앱입니다.

## 구조

```
android/     - Android 샘플 앱 (Kotlin + Compose)
docs/        - 가이드 문서
examples/    - 포맷별 코드 예시
```

## 빠른 시작

### 먼저 이해할 것
- 데모 모드: 설정 없이 화면 흐름과 WebView 브리지를 먼저 확인하는 모드
- 실 SDK 모드: `Configure Keys`에 실제 MEDIA_KEY / AD_UNIT_ID를 넣고 광고 응답까지 확인하는 모드
- 권장 순서: 데모 모드 확인 → Configure Keys 입력 → 배너/네이티브 확인 → HybridWebView 확인

### Android

```bash
cd android
# Gradle 빌드 (JDK 11 이상 필요)
./gradlew assembleDebug

# 벤더 SDK 경로를 켜서 빌드
./gradlew assembleDebug -PvendorSdkEnabled=true
```

### iOS

```bash
cd ios
# Xcode 또는 xcodebuild로 빌드
xcodebuild -scheme NapSspIOSSample -destination 'generic/platform=iOS Simulator' build
```

### 문서 요약

| 파일 | 내용 |
|---|---|
| `docs/quickstart.md` | 빠른 시작 — 초보자용 단계별 안내 (5분) |
| `docs/guide.md` | 메인 가이드 — 전체 흐름과 상세 예시 |
| `docs/hybrid-webview.md` | WebView ↔ Native 브릿지 상세 |
| `docs/nap-ssp-android-sdk-native.md` | Android SDK 공식 연동 가이드 |

## 핵심 포인트 (요약)

- SDK 초기화: AdMixer.getInstance().initialize(context, MEDIA_KEY, ArrayList(...))
- 포맷별 호출: AdView / NativeAdView / VideoAdView / RewardInterstitialVideoAd / InterstitialAd / InterstitialVideoAd
- 벤더 SDK 전환: `-PvendorSdkEnabled=true` 또는 `NAPSSP_VENDOR_SDK_ENABLED=true`
- 이벤트 수신: 콜백에서 loaded/displayed/clicked 등의 이벤트를 받아 웹에 전달

## 먼저 읽을 것 (초보자 빠른 시작)

1. `docs/quickstart.md` — 바로 따라하기(5분)
2. `docs/guide.md` — 전체 흐름 및 상세
3. `docs/hybrid-webview.md` — WebView 브리지 규격
4. `docs/nap-ssp-android-sdk-native.md` — Android 공식 연동 가이드
5. `docs/nap-ssp-ios-sdk-native.md` — iOS 공식 연동 가이드

## 설정 방법

하드코딩 수정 없이 앱에서 바로 테스트할 수 있습니다.

- Android: 앱 실행 후 `Configure Keys` 버튼에서 MEDIA_KEY / AdUnit ID 입력
- iOS: 우상단 `Configure Keys` 버튼에서 MEDIA_KEY / AdUnit ID 입력
- 입력하지 않으면 샘플 기본값으로 동작합니다.

참고:
- Android는 `AppConfig` + `NapSspConfig`를 통해 저장된 키를 우선 사용합니다.
- iOS는 `UserDefaults` + `NapSspConfig`를 통해 저장된 키를 우선 사용합니다.
- iOS 샘플은 현재 재현 가능한 빌드를 위해 `ios/Vendor/` 아래의 로컬 xcframework를 사용합니다.
- 즉, 이 샘플의 iOS 빌드는 원격 SPM fetch 상태에 덜 의존하도록 고정되어 있습니다.

## 문의

- 이메일: nap_adx@nasmedia.co.kr
- 공식 가이드: `docs/nap-ssp-android-sdk-native.md`

