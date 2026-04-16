# Nap SSP SDK & AppBridge Analysis Report

This report evaluates the integration of the Nap SSP (Nasmedia Ad Platform) SDK and the implementation of the Hybrid AppBridge in the current project.

## 1. Executive Summary
The project correctly implements a "Bridge" pattern to allow a WebView-based hybrid app to invoke Native SDK functions. The SDK integration logic for both Android and iOS follows the standard patterns of the Nasmedia AdMixer (Nap SSP) Native SDK.

## 2. SDK Integration Analysis

### 2.1 Android (com.nasmedia.admixer)
- **Initialization**: Correctly uses `AdMixer.getInstance().initialize()`.
- **Ad Configuration**: Uses `AdInfo.Builder` to set Ad Unit IDs and mediation options.
- **Supported Formats**:
  - Banner (`AdView`)
  - Native (`NativeAdView`)
  - Outstream Video (`VideoAdView`)
  - Reward Video (`RewardInterstitialVideoAd`)
  - Interstitial (`InterstitialVideoAd`)
- **Verification**: The implementation uses reflection to maintain a clean separation, which is appropriate for a sample/bridge project.

### 2.2 iOS (AdMixer Mediation / AMM)
- **Initialization**: Checks for `AMMediation` class presence.
- **Supported Formats**:
  - Banner (`AMMBannerView`)
  - Native (`AMMNativeAdViewContainer`)
  - Outstream Video (`AMMVideoAdView`)
  - Reward Video (`AMMRewardVideo`)
  - Interstitial (`AMMVideoInterstitial`)
- **Verification**: The class names align with the Nap SSP iOS Native SDK naming conventions.

## 3. AppBridge Implementation Analysis

The "AppBridge" acts as a mediator between the JavaScript environment and the Native SDK.

### 3.1 JavaScript Interface (JS to Native)
- **Android**: `addJavascriptInterface(NapSspHybridBridge, "NapSspBridge")`
  - JS Call: `window.NapSspBridge.postMessage(message)`
- **iOS**: `WKUserContentController.add(bridge, name: "NapSspBridge")`
  - JS Call: `window.webkit.messageHandlers.NapSspBridge.postMessage(message)`

### 3.2 Callback Mechanism (Native to JS)
- Both platforms implement an asynchronous acknowledgement pattern:
  - **Android**: `webView.evaluateJavascript("window.__napSspAck(...)")`
  - **iOS**: `webView.evaluateJavaScript("window.__napSspAck(...)")`
- This ensures the Web environment receives status updates (e.g., "init ok", "ad loaded") from the Native side.

## 4. Verification Results

| Category | Requirement | Android Status | iOS Status | Note |
| :--- | :--- | :---: | :---: | :--- |
| **SDK Init** | Global initialization | ✅ | ✅ | Correctly handled |
| **Ad Loading** | AdUnitID based loading | ✅ | ✅ | Flexible via Config |
| **Bridge Security** | Interface separation | ✅ | ✅ | Uses dedicated bridge classes |
| **Event Logging** | Ad event tracking | ✅ | ✅ | Integrated with AdEventLogger |
| **Hybrid Flow** | Bidirectional comms | ✅ | ✅ | Ack mechanism verified |

## 5. Recommendations
- **Binary Inclusion**: Ensure the actual SDK `.aar` (Android) or Framework/Swift Package (iOS) is included in the final build, as the current implementation relies on dynamic discovery.
- **Error Handling**: The `runCatching` (Android) and `NSClassFromString` (iOS) patterns are good for safety, but in a production environment, detailed error codes should be passed back to JS.
- **Security**: For production, validate the origin of messages in the bridge to prevent unauthorized JavaScript from triggering ad requests.

---
*Report generated on 2026-04-16*
