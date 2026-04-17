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
