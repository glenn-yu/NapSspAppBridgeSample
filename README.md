# NapSsp App Bridge Sample

[English](#english) | [한국어](#한국어)

---

## English

This is an Android/iOS sample application demonstrating how to integrate the NapSsp (KT Nasmedia AdMixer SSP) SDK v2.0.0 using both WebView hybrid bridge and native methods.

### 0) Prerequisites
- Git
- Android: JDK 17+ (JDK 21 recommended) + Android Studio or Gradle wrapper
- iOS: macOS + Xcode 15.3+
- Optional: Real `MEDIA_KEY` and `AD_UNIT_ID` values for validation

### 1) Clone the Repository
* **macOS / Linux**:
  ```bash
  git clone https://github.com/glenn-yu/NapSspAppBridgeSample.git
  cd NapSspAppBridgeSample
  ```
* **Windows PowerShell**:
  ```powershell
  git clone https://github.com/glenn-yu/NapSspAppBridgeSample.git
  cd NapSspAppBridgeSample
  ```

### 2) Run Android
* **macOS / Linux**:
  ```bash
  cd android
  ./gradlew assembleDebug
  ./gradlew assembleDebug -PvendorSdkEnabled=true   # Optional vendor SDK path
  ```
* **Windows PowerShell**:
  ```powershell
  cd android
  .\gradlew.bat assembleDebug
  .\gradlew.bat assembleDebug -PvendorSdkEnabled=true   # Optional vendor SDK path
  ```

Once the app launches:
1. Tap **Configure Keys**.
2. Keep the sample defaults or paste your real `MEDIA_KEY` / ad unit ID values.
3. Try **Banner** first, then **Native**.
4. Try **HybridWebView** after verifying the basic flow.

### 3) Run iOS (macOS only)
```bash
cd ios
DEVELOPER_DIR="/Applications/Xcode.app/Contents/Developer" xcodebuild -scheme NapSspIOSSample -destination 'generic/platform=iOS Simulator' build
```
If using Xcode:
```bash
open ios
```
Once the app launches:
1. Tap **Configure Keys**.
2. Verify the sample defaults or enter real values.
3. Try **Banner**, **Native**, **Rewarded**, then **Interstitial**.

### 4) Project Structure
```
android/     - Android Sample App (Kotlin + Compose)
ios/         - iOS Sample App (SwiftUI + local xcframeworks)
docs/        - Quickstart, release notes, and integration guides
examples/    - Format-specific code snippets
```

### 5) Guide Documents Summary

| File | Description |
|---|---|
| `docs/quickstart.md` | Single-page quickstart for beginners |
| `docs/guide.md` | Integration flow and detailed examples |
| `docs/hybrid-webview.md` | WebView ↔ Native bridge JSON specifications |
| `docs/public-hybrid-bridge-guide.md` | Public bridge integration guide for publishers |
| `docs/nap-ssp-android-sdk-native.md` | Android official Native SDK integration guide |
| `docs/nap-ssp-ios-sdk-native.md` | iOS official Native SDK integration guide |

### 6) Key Highlights
* **SDK Initialization**: 
  - Android: `AdMixer.getInstance().initialize(...)` (no need to call `registerAdapter()` in v2)
  - iOS: `AMMediation.shared.initialize(...)`
* **Format Calls**: Banner, Native, Rewarded, Interstitial, and Video.
* **Event Handlers**: Forwards `loaded`, `displayed`, `clicked`, `rewarded`, and `closed` callbacks to the web layer or UI.

### 7) Troubleshooting
* `Unable to locate a Java Runtime` ➡️ Install JDK 17+ and set the `JAVA_HOME` environment variable.
* `./gradlew: not found` ➡️ Run the command inside the `android/` directory.
* `xcodebuild: command not found` ➡️ Install Xcode and accept the Xcode license.
* **Ads do not load** ➡️ Double-check your `MEDIA_KEY` and `AD_UNIT_ID` values. Make sure the vendor path is active if required.

### 8) Contact & Support
- Email: nap_mx@nasmedia.co.kr
- Official Guide: [https://napmx.github.io](https://napmx.github.io)

---

## 한국어

이 샘플 앱은 WebView 하이브리드 브릿지 및 네이티브 연동 방식으로 NapSsp (KT나스미디어 AdMixer SSP) SDK v2.0.0을 연동하는 Android 및 iOS 데모 프로젝트입니다.

### 0) 사전 준비사항
- Git
- Android: JDK 17+ (JDK 21 권장) + Android Studio 또는 Gradle 래퍼
- iOS: macOS + Xcode 15.3+
- 선택사항: 실제 검증을 위한 `MEDIA_KEY` 및 `AD_UNIT_ID` 발급값

### 1) 저장소 클론
* **macOS / Linux**:
  ```bash
  git clone https://github.com/glenn-yu/NapSspAppBridgeSample.git
  cd NapSspAppBridgeSample
  ```
* **Windows PowerShell**:
  ```powershell
  git clone https://github.com/glenn-yu/NapSspAppBridgeSample.git
  cd NapSspAppBridgeSample
  ```

### 2) Android 실행
* **macOS / Linux**:
  ```bash
  cd android
  ./gradlew assembleDebug
  ./gradlew assembleDebug -PvendorSdkEnabled=true   # 벤더 SDK를 포함하는 빌드 옵션
  ```
* **Windows PowerShell**:
  ```powershell
  cd android
  .\gradlew.bat assembleDebug
  .\gradlew.bat assembleDebug -PvendorSdkEnabled=true   # 벤더 SDK를 포함하는 빌드 옵션
  ```

앱 실행 후 검증 방법:
1. **Configure Keys** 버튼을 누릅니다.
2. 기본값을 유지하거나 발급받은 실제 `MEDIA_KEY` 및 ad unit ID를 입력합니다.
3. **Banner** 광고를 먼저 테스트한 뒤 **Native** 광고를 테스트합니다.
4. 기본 연동이 정상 작동하면 **HybridWebView**를 테스트합니다.

### 3) iOS 실행 (macOS 전용)
```bash
cd ios
DEVELOPER_DIR="/Applications/Xcode.app/Contents/Developer" xcodebuild -scheme NapSspIOSSample -destination 'generic/platform=iOS Simulator' build
```
Xcode로 프로젝트를 직접 열 경우:
```bash
open ios
```
앱 실행 후 검증 방법:
1. **Configure Keys** 버튼을 누릅니다.
2. 기본 샘플 키 값을 확인하거나 실제 발급받은 값을 입력합니다.
3. **Banner**, **Native**, **Rewarded**, **Interstitial** 순서로 확인합니다.

### 4) 프로젝트 구조
```
android/     - Android 샘플 앱 (Kotlin + Compose)
ios/         - iOS 샘플 앱 (SwiftUI + 로컬 xcframeworks)
docs/        - 빠른 시작, 릴리즈 노트, 연동 가이드 문서 등
examples/    - 광고 포맷별 소스 코드 예시
```

### 5) 문서 요약

| 파일명 | 내용 |
|---|---|
| `docs/quickstart.md` | 초보자를 위한 빠른 시작 가이드 |
| `docs/guide.md` | 상세 통합 가이드 및 연동 예시 |
| `docs/hybrid-webview.md` | WebView ↔ Native 브릿지 JSON 명세 |
| `docs/public-hybrid-bridge-guide.md` | 퍼블리셔용 WebView ↔ Native 브릿지 공개 규격 가이드 |
| `docs/nap-ssp-android-sdk-native.md` | Android 공식 Native SDK 연동 가이드 |
| `docs/nap-ssp-ios-sdk-native.md` | iOS 공식 Native SDK 연동 가이드 |

### 6) 핵심 기술 요약
* **SDK 초기화**: 
  - Android: `AdMixer.getInstance().initialize(...)` (v2 SDK부터는 registerAdapter 호출이 필요 없음)
  - iOS: `AMMediation.shared.initialize(...)`
* **지원 포맷**: 배너, 네이티브, 리워드 동영상, 전면 배너, 아웃스트림 비디오 등
* **이벤트 처리**: `loaded`, `displayed`, `clicked`, `rewarded`, `closed` 등의 생명주기 이벤트를 웹뷰 브릿지 혹은 네이티브 UI로 정확하게 콜백.

### 7) 문제 해결 (Troubleshooting)
* `Unable to locate a Java Runtime` ➡️ JDK 17 이상을 설치하고 `JAVA_HOME` 환경 변수를 올바르게 설정하세요.
* `./gradlew: not found` ➡️ 반드시 `android/` 디렉토리 내부에서 래퍼 스크립트를 실행해 주세요.
* `xcodebuild: command not found` ➡️ Xcode를 설치하고 라이선스 동의를 완료하세요.
* **광고가 노출되지 않음** ➡️ `MEDIA_KEY`와 `AD_UNIT_ID` 설정값 및 벤더 SDK 설정 옵션이 켜져 있는지 확인하세요.

### 8) 문의 및 공식 지원
- 이메일: nap_mx@nasmedia.co.kr
- 공식 가이드: [https://napmx.github.io](https://napmx.github.io)
