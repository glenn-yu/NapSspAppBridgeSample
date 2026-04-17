# NapSsp App Bridge Sample

The shortest path to get the sample app running is below. If you are a beginner, follow the Quickstart first.

---

<!-- Quickstart (inlined from docs/quickstart.md) -->

# Quickstart — NapSsp AppBridge Sample

The shortest path to “app opens, keys are configured, ads load.”

## 0) What you need
- Git
- Android: JDK 17 + Android Studio or just the Gradle wrapper
- iOS: macOS + Xcode 15.3+
- Optional: real `MEDIA_KEY` / `AD_UNIT_ID` values

## 1) Clone

### macOS / Linux
```bash
git clone https://github.com/glenn-yu/NapSspAppBridgeSample.git
cd NapSspAppBridgeSample
```

### Windows PowerShell
```powershell
git clone https://github.com/glenn-yu/NapSspAppBridgeSample.git
cd NapSspAppBridgeSample
```

## 2) Run Android

### macOS / Linux
```bash
cd android
./gradlew assembleDebug
./gradlew assembleDebug -PvendorSdkEnabled=true   # optional vendor path
```

### Windows PowerShell
```powershell
cd android
.\gradlew.bat assembleDebug
.\gradlew.bat assembleDebug -PvendorSdkEnabled=true   # optional vendor path
```

After the app launches:
1. Tap **Configure Keys**.
2. Keep the sample defaults or paste your real `MEDIA_KEY` / ad unit ID values.
3. Try **Banner** first, then **Native**.
4. Use **HybridWebView** only after the basic flow works.

## 3) Run iOS (macOS only)

```bash
cd ios
xcodebuild -scheme NapSspIOSSample -destination 'generic/platform=iOS Simulator' build
```

If you prefer Xcode:
```bash
open ios
```

After the app launches:
1. Tap **Configure Keys**.
2. Verify the sample defaults or enter real values.
3. Try **Banner**, **Native**, **Rewarded**, then **Interstitial**.

## 4) Troubleshooting
- `Unable to locate a Java Runtime` → install JDK 17 and set `JAVA_HOME`.
- `./gradlew: not found` or a missing wrapper error → run commands from `android/`, not repo root.
- `xcodebuild: command not found` → install Xcode and accept the license.
- Ads do not load → confirm the `MEDIA_KEY` / `AD_UNIT_ID` values and that the vendor SDK path is enabled when needed.
- Windows cannot build iOS → use a Mac for the iOS target.

## 5) Sanity check order
- Demo mode / sample defaults
- Banner
- Native
- Rewarded / Interstitial
- HybridWebView bridge

---

NapSsp(Nasmedia AdMixer SSP) SDK를 WebView 하이브리드 방식과 네이티브 방식으로 연동하는 Android/iOS 샘플 앱입니다.

## 구조

```
android/     - Android 샘플 앱 (Kotlin + Compose)
ios/         - iOS 샘플 앱 (SwiftUI + vendored xcframeworks)
docs/        - 빠른 시작, LFS 계획, 릴리즈 노트, 상세 가이드
examples/    - 포맷별 코드 예시
```

## 빠른 시작

- 초보자는 `docs/quickstart.md`부터 보세요.
- Android는 `android/`에서 `./gradlew assembleDebug`를 실행합니다.
- iOS는 macOS에서 `ios/` 폴더를 열고 `xcodebuild` 또는 Xcode로 빌드합니다.
- 샘플 화면에는 `Use vendored / Use remote SPM` 토글이 있고, 현재 빌드가 어떤 소스를 쓰는지 표시합니다.
- `docs/ios-xcframeworks.md`에 xcframework 관리 요약을 정리해 두었습니다.
- 벤더 SDK가 필요한 Android 경로는 `-PvendorSdkEnabled=true` 또는 `NAPSSP_VENDOR_SDK_ENABLED=true`로 켭니다.

## 문서 요약

| 파일 | 내용 |
|---|---|
| `docs/quickstart.md` | 초보자용 한 화면 빠른 시작 |
| `docs/guide.md` | 전체 흐름과 상세 예시 |
| `docs/hybrid-webview.md` | WebView ↔ Native 브릿지 규격 |
| `docs/ios-xcframeworks.md` | iOS xcframework 관리 / Git LFS / artifact 호스팅 요약 |
| `docs/ios-vendor-lfs-migration-plan.md` | `ios/Vendor/` Git LFS 이전 계획 |
| `docs/release-notes-draft.md` | 다음 릴리즈용 초안 |
| `docs/nap-ssp-android-sdk-native.md` | Android 공식 연동 가이드 |
| `docs/nap-ssp-ios-sdk-native.md` | iOS 공식 연동 가이드 |

## 핵심 포인트

- SDK 초기화: `AdMixer.getInstance().initialize(...)` / iOS `AMMediation.shared.initialize(...)`
- 포맷별 호출: banner / native / rewarded / interstitial / video
- 이벤트 수신: loaded / displayed / clicked / rewarded / closed 콜백을 웹 또는 UI로 전달
- iOS 샘플은 현재 `ios/Vendor/` 아래의 로컬 xcframework를 사용해 재현 빌드를 우선합니다.

## 설정 방법

하드코딩 수정 없이 앱에서 바로 테스트할 수 있습니다。

- Android: 앱 실행 후 `Configure Keys` 버튼에서 MEDIA_KEY / Ad Unit ID 입력
- iOS: 우상단 `Configure Keys` 버튼에서 MEDIA_KEY / Ad Unit ID 입력
- 입력하지 않으면 샘플 기본값으로 동작합니다。

## 문의

- 이메일: nap_adx@nasmedia.co.kr
- 공식 가이드: `docs/nap-ssp-android-sdk-native.md`
