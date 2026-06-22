# Quickstart — NapSsp AppBridge Sample / 빠른 시작

[English](#english) | [한국어](#한국어)

---

## English

The shortest path to “app opens, keys are configured, ads load.”

### 0) What you need
- Git
- Android: JDK 17+ (JDK 21 recommended) + Android Studio or just the Gradle wrapper
- iOS: macOS + Xcode 15.3+
- Optional: real `MEDIA_KEY` / `AD_UNIT_ID` values

### 1) Clone
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
  ./gradlew assembleDebug -PvendorSdkEnabled=true   # optional vendor path
  ```
* **Windows PowerShell**:
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

### 3) Run iOS (macOS only)
```bash
cd ios
DEVELOPER_DIR="/Applications/Xcode.app/Contents/Developer" xcodebuild -scheme NapSspIOSSample -destination 'generic/platform=iOS Simulator' build
```
If you prefer Xcode:
```bash
open ios
```
After the app launches:
1. Tap **Configure Keys**.
2. Verify the sample defaults or enter real values.
3. Try **Banner**, **Native**, **Rewarded**, then **Interstitial**.

### 4) Troubleshooting
- `Unable to locate a Java Runtime` ➡️ Install JDK 17 or higher and set `JAVA_HOME`.
- `./gradlew: not found` or a missing wrapper error ➡️ Run commands from `android/`, not repo root.
- `xcodebuild: command not found` ➡️ Install Xcode and accept the license.
- Ads do not load ➡️ Confirm the `MEDIA_KEY` / `AD_UNIT_ID` values and that the vendor SDK path is enabled when needed.
- Windows cannot build iOS ➡️ Use a Mac for the iOS target.

### 5) Sanity check order
- Demo mode / sample defaults
- Banner
- Native
- Rewarded / Interstitial
- HybridWebView bridge

---

## 한국어

"앱을 켜고, 키를 설정하고, 광고를 로드하기" 위한 가장 빠른 방법입니다.

### 0) 사전 준비사항
- Git
- Android: JDK 17 이상 (JDK 21 권장) + Android Studio 또는 Gradle 래퍼
- iOS: macOS + Xcode 15.3 이상
- 선택사항: 실제 검증을 위한 `MEDIA_KEY` 및 `AD_UNIT_ID` 설정값

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
  ./gradlew assembleDebug -PvendorSdkEnabled=true   # 벤더 SDK 포함 옵션
  ```
* **Windows PowerShell**:
  ```powershell
  cd android
  .\gradlew.bat assembleDebug
  .\gradlew.bat assembleDebug -PvendorSdkEnabled=true   # 벤더 SDK 포함 옵션
  ```

앱 실행 후 검증 방법:
1. **Configure Keys** 버튼을 탭합니다.
2. 기본값을 그대로 유지하거나 실제 발급받은 `MEDIA_KEY`와 ad unit ID를 입력합니다.
3. **Banner** 광고를 먼저 로드해본 뒤 **Native** 광고를 확인합니다.
4. 기본 광고들이 정상 로드되면 **HybridWebView**를 테스트합니다.

### 3) iOS 실행 (macOS 전용)
```bash
cd ios
DEVELOPER_DIR="/Applications/Xcode.app/Contents/Developer" xcodebuild -scheme NapSspIOSSample -destination 'generic/platform=iOS Simulator' build
```
Xcode로 열기:
```bash
open ios
```
앱 실행 후 검증 방법:
1. **Configure Keys** 버튼을 탭합니다.
2. 샘플 기본 키 값을 확인하거나 실제 발급받은 키 값을 입력합니다.
3. **Banner**, **Native**, **Rewarded**, **Interstitial** 순서로 정상 동작하는지 테스트합니다.

### 4) 문제 해결 (Troubleshooting)
- `Unable to locate a Java Runtime` ➡️ JDK 17 이상을 설치하고 `JAVA_HOME` 환경 변수를 알맞게 설정해 주세요.
- `./gradlew: not found` 또는 래퍼 누락 에러 ➡️ 리포지토리 루트가 아닌 `android/` 폴더 안에서 래퍼 스크립트를 실행했는지 확인하세요.
- `xcodebuild: command not found` ➡️ Xcode를 설치하고 CLI 도구 및 라이선스 동의를 완료해 주세요.
- 광고가 로드되지 않음 ➡️ `MEDIA_KEY` / `AD_UNIT_ID` 값이 맞는지, 그리고 필요한 경우 벤더 SDK 설정 옵션이 켜져 있는지 확인하세요.
- Windows에서 iOS 빌드가 안 됨 ➡️ iOS 타겟 빌드는 Mac 기기가 필요합니다.

### 5) 단계별 정상 연동 확인 순서
- 데모 모드 / 기본 샘플 키
- 배너 광고 (Banner)
- 네이티브 광고 (Native)
- 리워드 / 전면 배너 (Rewarded / Interstitial)
- 하이브리드 웹뷰 브릿지 (HybridWebView bridge)
