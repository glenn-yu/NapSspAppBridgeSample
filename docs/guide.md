# NapSsp Hybrid App Bridge 통합 개발 가이드 (AOS & iOS)

이 문서는 Nap SSP Native SDK를 하이브리드 앱(WebView)에 통합하여, 웹의 JavaScript에서 네이티브 광고를 호출하고 제어하는 모든 과정을 다룹니다. 

---

## 1. 하이브리드 통신 규격 (Standard JSON Bridge)

Android와 iOS는 동일한 JSON 규약을 사용하여 통신합니다. 웹 개발자는 플랫폼 구분 없이 아래 규격을 사용하면 됩니다.

### 1.1 Web → Native (Request)
웹에서 네이티브 기능을 호출할 때 사용하는 JSON 구조입니다.

```javascript
const request = {
    action: "loadAd", // loadAd, init, clearAds
    params: { 
        format: "banner", // banner, native, video, rewardVideo, interstitialVideo, interstitialBanner
        adUnitId: "104704" // (선택) 특정 ID 지정 시
    }
};

// Android 호출
if (window.NapSspBridge) {
    window.NapSspBridge.postMessage(JSON.stringify(request));
}
// iOS 호출
if (window.webkit?.messageHandlers?.NapSspBridge) {
    window.webkit.messageHandlers.NapSspBridge.postMessage(JSON.stringify(request));
}
```

### 1.2 Native → Web (Response Callback)
네이티브 작업 결과나 광고 이벤트(노출, 클릭 등)는 웹의 전역 함수로 전달됩니다.

```javascript
window.onNapSspMessage = function(responseStr) {
    const res = JSON.parse(responseStr);
    console.log(`[${res.action}] ${res.status.toUpperCase()}:`, res.data);
};
```

---

## 2. Android (Kotlin / Compose) 구현

### 2.1 필수 설정
*   **build.gradle.kts**: `admixer-ssp`, `play-services-ads-identifier`, `androidx.appcompat` 추가.
*   **AndroidManifest.xml**: `INTERNET`, `AD_ID` 권한 및 `usesCleartextTraffic="true"` 필수.
*   **Theme**: `Theme.AppCompat.DayNight.NoActionBar` 사용 권한 필수.

### 2.2 SDK 통합 코드 (NapSspSdkIntegration.kt)
**"파괴 후 재생성"** 전략을 사용하여 `Already Exist` 오류를 방지하고 갱신 성능을 확보합니다.

```kotlin
object NapSspSdkIntegration {
    private val activeAds = mutableMapOf<String, Any>()

    private fun destroyAd(format: String) {
        activeAds[format]?.let { ad ->
            if (ad is View) (ad.parent as? ViewGroup)?.removeView(ad)
            when (ad) {
                is AdView -> ad.onDestroy()
                is NativeAdView -> ad.onDestroy()
                is VideoAdView -> ad.onDestroy()
                is InterstitialAd -> ad.stopInterstitial()
                is InterstitialVideoAd -> ad.stopInterstitialVideoAd()
                is RewardInterstitialVideoAd -> ad.stopRewardVideoAd()
            }
        }
        activeAds.remove(format)
    }

    fun bannerView(context: Context): View? {
        destroyAd("banner") // 1. 기존 객체 완전 소멸 및 null화
        
        val adView = AdView(context) // 2. 새 인스턴스 생성
        adView.setAdInfo(AdInfo.Builder("104704").setIsUseMediation(true).build())
        adView.setAlwaysShowAdView(true)
        adView.setAdViewListener(object : AdListener {
            override fun onReceivedAd(adapterName: String?, view: Any?) {
                adView.showAd() // 3. 수신 즉시 노출 (배너 필수)
            }
            // ... onEventAd에서 CLICK, DISPLAYED 로그 처리
        })
        activeAds["banner"] = adView
        adView.loadAd()
        return adView
    }
}
```

---

## 3. iOS (Swift / SwiftUI) 구현

### 3.1 SDK 통합 코드 (NapSspSdkIntegration.swift)
iOS도 Android와 대칭되는 **"Stop & Recreate"** 전략을 사용합니다.

```swift
class NapSspSdkIntegration: NSObject {
    static let shared = NapSspSdkIntegration()
    private var activeAds: [String: Any] = [:]

    private func destroyAd(format: String) {
        if let ad = activeAds[format] {
            if let b = ad as? AMMBannerView { b.stop() }
            else if let n = ad as? AMMNativeAdViewContainer { n.stop() }
            else if let i = ad as? AMMInterstitial { i.stop() }
            (ad as? UIView)?.removeFromSuperview()
        }
        activeAds.removeValue(forKey: format)
    }

    static func banner(rootVC: UIViewController) -> UIView? {
        shared.destroyAd(format: "banner") // 1. 기존 파괴
        
        let bannerView = AMMBannerView(rootViewController: rootVC) // 2. 신규 생성
        bannerView.adUnitId = "104707"
        bannerView.delegate = shared
        shared.activeAds["banner"] = bannerView
        bannerView.loadAd()
        return bannerView
    }
}
```

---

## 4. UI 갱신 및 관리 (HybridWebView)

### 4.1 UI 강제 리프레시 (Key Strategy)
동일한 영역에 광고를 계속 새로 그리기 위해 고유 세션 ID를 사용합니다.

*   **Android (Compose)**: `key(adSessionId) { AndroidView(...) }`
*   **iOS (SwiftUI)**: `AdViewRepresentable(view).id(adViewId)`

---

## 5. 핵심 관리 로직 (Best Practices)

### 5.1 생명주기(Lifecycle) 연동
앱이 백그라운드로 전환될 때 광고 활동을 중단하고, 화면이 닫힐 때 자원을 모두 해제해야 합니다.
*   **Android**: `LifecycleEventObserver`를 사용해 `ON_PAUSE`, `ON_RESUME`을 SDK에 전달합니다. (가이드 필수)
*   **iOS**: SwiftUI의 `.onDisappear` 시점에 모든 광고 인스턴스의 `stop()`을 호출합니다.

### 5.2 광고 뷰 갱신 및 리스너 관리
동일한 `AdUnit ID`로 광고를 계속 호출할 때 발생하는 `Already Exist` 오류를 방지하기 위한 두 가지 전략입니다.

#### [전략 1] 광고 뷰를 새로 생성하는 경우 (가장 확실한 방법)
기존 광고 뷰를 버리고 새 광고를 요청할 때입니다. 아래의 순서를 반드시 지켜야 SDK 내부 리스너가 정상적으로 정리됩니다.
1.  **`destroy()` 호출**: 기존 뷰의 `onDestroy()` (Android) 또는 `stop()` (iOS)을 호출하여 리스너 점유를 강제 해제합니다.
2.  **부모 뷰에서 제거**: 레이아웃에서 기존 뷰를 완전히 떼어냅니다 (`removeView`).
3.  **새 객체 생성 및 로드**: 그 후에 새로운 인스턴스를 만들어 추가하고 `loadAd()`를 호출합니다.

#### [전략 2] 기존 광고 뷰를 레이아웃에서 뗐다가 다시 붙이는 경우
인스턴스를 유지하면서 재호출하고 싶을 때 유용한 팁입니다.
*   SDK의 광고 뷰는 레이아웃에서 제거되는 시점(`onDetachedFromWindow`)에 내부적으로 리스너를 제거(`stopLoad`)하도록 설계되어 있습니다.
*   따라서 `parent.removeView(myAdView)`를 수행한 후, 다시 `addView()` 하고 `loadAd()`를 하면 중복 에러 없이 로드됩니다.

---

## 6. 자주 발생하는 오류 해결

| 오류 현상 | 원인 및 해결책 |
| :--- | :--- |
| **Already Exist** | 동일 인스턴스 재사용 시 반드시 `stopLoad()`를 호출하여 이전 리스너를 제거한 후 `loadAd()`를 하세요. 신규 생성 시에는 이전 객체의 `onDestroy()` 호출과 `Map`에서의 제거가 필수입니다. |
| **빈 화면 (AOS)** | `usesCleartextTraffic="true"`가 누락되어 HTTP 소재가 차단되었을 수 있습니다. |
| **클릭 무반응** | 광고 뷰가 `MATCH_PARENT`로 설정되어 실제 클릭 영역을 확보했는지 확인하세요. |
| **두 번씩 호출됨** | JS 버튼 클릭 시 `setTimeout` 등을 이용해 데바운스 처리를 하세요. |

---
*Nasmedia Technical Support (2026-04-16 Updated)*
