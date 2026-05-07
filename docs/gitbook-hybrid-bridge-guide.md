# NapSSP App-WebView 브릿지 연동 가이드

> 이 가이드는 WebView 기반 하이브리드 앱에서 NapSSP 광고 SDK를 연동하는 방법을 처음부터 설명합니다.
> Android / iOS 모두 다룹니다.

---

## 목차

1. [이 가이드가 필요한 경우](#1-이-가이드가-필요한-경우)
2. [구조 한눈에 보기](#2-구조-한눈에-보기)
3. [시작 전 준비물](#3-시작-전-준비물)
4. [Android 연동](#4-android-연동)
   - 4-1. SDK 설치
   - 4-2. 권한 설정
   - 4-3. SDK 초기화 코드
   - 4-4. 브릿지 + WebView 화면 구성
5. [iOS 연동](#5-ios-연동)
   - 5-1. SDK 설치
   - 5-2. Info.plist 설정
   - 5-3. SDK 초기화 코드
   - 5-4. 브릿지 + WebView 화면 구성
6. [JavaScript 연동 (공통)](#6-javascript-연동-공통)
7. [광고 포맷별 사용법](#7-광고-포맷별-사용법)
8. [이벤트 처리](#8-이벤트-처리)
9. [자주 묻는 질문 & 문제 해결](#9-자주-묻는-질문--문제-해결)
10. [운영 전 체크리스트](#10-운영-전-체크리스트)

---

## 1. 이 가이드가 필요한 경우

다음 중 하나라면 이 가이드가 맞습니다.

- 앱 안의 **WebView** 화면에서 광고를 보여주고 싶다
- 광고 요청은 **JavaScript**에서 하고, 실제 광고 SDK 호출은 **Native(Android/iOS)** 에서 처리하고 싶다
- Android와 iOS 모두 **동일한 JS 코드**로 광고를 제어하고 싶다

> **순수 Native 앱**에서 광고를 붙이는 경우는 이 가이드 대신 `nap-ssp-android-sdk-native.md` / `nap-ssp-ios-sdk-native.md`를 참고하세요.

---

## 2. 구조 한눈에 보기

```
┌─────────────────────────────────────────┐
│              WebView (HTML/JS)           │
│                                          │
│  callNative('loadAd', {format:'banner'}) │
│            │                             │
│            ▼                             │
│  window.NapSspBridge.postMessage(...)    │  ← Android
│  window.webkit.messageHandlers...        │  ← iOS
└──────────────┬──────────────────────────┘
               │  JSON 메시지
               ▼
┌─────────────────────────────────────────┐
│           Native Bridge Layer            │
│  • JSON 파싱 & 포맷 검증                  │
│  • 즉시 ACK 응답 → JS                    │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│         NapSSP / AdMixer SDK             │
│  • 광고 로드 / 노출 / 클릭 / 종료 이벤트   │
└──────────────┬──────────────────────────┘
               │  SDK 이벤트 콜백
               ▼
┌─────────────────────────────────────────┐
│         Native Bridge Layer              │
│  window.onNapSspMessage(responseJson)   │  ← JS 콜백 호출
└─────────────────────────────────────────┘
```

> **중요:** `loadAd`에 대한 즉시 응답(`Accepted banner`)은 **"광고 로드 성공"이 아닙니다**.
> 브릿지가 요청을 정상 수신했다는 ACK입니다.
> 실제 광고 로드 성공/실패는 SDK 콜백을 통해 **`event` action**으로 별도로 전달됩니다.

---

## 3. 시작 전 준비물

| 항목 | 설명 |
|---|---|
| `MEDIA_KEY` | Nasmedia에서 발급받은 매체 키. 예: `10771` |
| `AD_UNIT_ID` | 광고 단위 ID. 포맷마다 다릅니다. 예: `104704` |
| Android 개발 환경 | JDK 17, Android Studio, `minSdk 24` 이상 |
| iOS 개발 환경 | macOS, Xcode 15.3 이상, `iOS 14.0` 이상 |

> 아직 키가 없다면 샘플 기본값(`MEDIA_KEY: 10771`)으로 먼저 동작을 확인할 수 있습니다.

---

## 4. Android 연동

### 4-1. SDK 설치

**`android/settings.gradle.kts`** — Maven 저장소 추가

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://devrepo.kakao.com/nexus/content/groups/public/")    // AdFit
        maven(url = "https://artifact.bytedance.com/repository/pangle/")         // Pangle
    }
}
```

**`android/app/build.gradle.kts`** — 의존성 추가

```kotlin
dependencies {
    // NapSSP 기본 SDK (필수)
    implementation("io.github.nasmedia-tech:admixer-ssp:1.0.23")
    implementation("com.google.android.gms:play-services-ads-identifier:18.3.0")

    // 미디에이션 어댑터 (사용하는 네트워크만 선택)
    implementation("io.github.nasmedia-tech:admixer-admanager:1.0.15_delta")  // Google Ad Manager
    implementation("io.github.nasmedia-tech:admixer-adfit:1.0.11")            // Kakao AdFit
    implementation("io.github.nasmedia-tech:admixer-pangle:1.0.11")           // Pangle
    implementation("com.pangle.global:pag-sdk:8.0.0.4")
    implementation("io.github.nasmedia-tech:admixer-applovin:1.0.8")          // AppLovin
    implementation("io.github.nasmedia-tech:admixer-unity:1.0.6")             // Unity Ads
}
```

> 미디에이션을 쓰지 않는다면 `admixer-ssp`와 `play-services-ads-identifier`만 추가해도 됩니다.

---

### 4-2. 권한 설정

**`AndroidManifest.xml`**

```xml
<!-- 필수 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="com.google.android.gms.permission.AD_ID" />

<!-- Google Mobile Ads 사용 시 (운영 ID로 교체) -->
<application>
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="YOUR_GOOGLE_MOBILE_ADS_APP_ID" />
</application>
```

---

### 4-3. SDK 초기화 코드

두 파일을 프로젝트에 추가합니다.

**`NapSspConfig.kt`** — 키 설정

```kotlin
package com.yourapp.bridge

object NapSspConfig {
    // 실제 키로 교체하세요. 소스 코드에 직접 커밋하지 말고 BuildConfig나 서버 설정으로 주입하세요.
    const val MEDIA_KEY = "10771"

    const val PANGLE_APP_ID = "YOUR_PANGLE_APP_ID"

    val AD_UNIT_IDS = mapOf(
        "banner_320x100"       to "104704",
        "native"               to "104588",
        "outstream_video"      to "104589",
        "reward_video"         to "103722",
        "interstitial_320x480" to "104702",
        "interstitial_320x480_f" to "104703",
    )
}
```

**`NapSspSdkIntegration.kt`** — SDK 연동 엔진

```kotlin
package com.yourapp.bridge

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.bytedance.sdk.openadsdk.api.init.PAGConfig
import com.bytedance.sdk.openadsdk.api.init.PAGSdk
import com.nasmedia.admixerssp.ads.*
import com.nasmedia.admixerssp.common.AdMixer
import com.nasmedia.admixerssp.common.AdMixerLog
import com.nasmedia.admixerssp.common.nativeads.NativeAdViewBinder

object NapSspSdkIntegration {

    var onAdEventCallback: ((event: String, format: String, detail: String) -> Unit)? = null

    private var isSdkInitialized = false
    private val activeAds = mutableMapOf<String, Any>()

    // ─────────────────────────────────────────
    // 초기화
    // ─────────────────────────────────────────

    @Synchronized
    fun initialize(context: Context) {
        if (isSdkInitialized) return

        AdMixerLog.setLogLevel(AdMixerLog.LogLevel.DEBUG)
        AdMixer.getInstance().initialize(
            context,
            NapSspConfig.MEDIA_KEY,
            ArrayList(NapSspConfig.AD_UNIT_IDS.values.toList())
        )

        // 사용하는 미디에이션 어댑터만 등록
        AdMixer.registerAdapter(AdMixer.ADAPTER_ADMANAGER)
        AdMixer.registerAdapter(AdMixer.ADAPTER_ADFIT)
        AdMixer.registerAdapter(AdMixer.ADAPTER_PANGLE)
        AdMixer.registerAdapter(AdMixer.ADAPTER_APPLOVIN)
        AdMixer.registerAdapter(AdMixer.ADAPTER_UNITY)

        // Pangle 초기화 (Pangle 미디에이션 사용 시)
        val pagConfig = PAGConfig.Builder()
            .appId(NapSspConfig.PANGLE_APP_ID)
            .debugLog(true)
            .supportMultiProcess(false)
            .build()
        PAGSdk.init(context, pagConfig, object : PAGSdk.PAGInitCallback {
            override fun success() { Log.i("Pangle", "init success") }
            override fun fail(code: Int, msg: String) { Log.e("Pangle", "init fail $code") }
        })

        isSdkInitialized = true
        onAdEventCallback?.invoke("loaded", "initialize", NapSspConfig.MEDIA_KEY)
    }

    // ─────────────────────────────────────────
    // 배너 광고
    // ─────────────────────────────────────────

    @Synchronized
    fun bannerView(context: Context, customAdUnitId: String? = null): View? {
        val adUnitId = customAdUnitId ?: NapSspConfig.AD_UNIT_IDS["banner_320x100"] ?: return null
        destroyAndRemoveAd("banner")
        return runCatching {
            val adView = AdView(context)
            adView.setAdInfo(AdInfo.Builder(adUnitId).setIsUseMediation(true).build())
            adView.setAlwaysShowAdView(true)
            adView.setAdViewListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) {
                    onAdEventCallback?.invoke("loaded", "banner", adUnitId)
                    adView.showAd()
                }
                override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", "banner", "[$errorCode] $errorMsg")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    if (event == AdEvent.DISPLAYED) onAdEventCallback?.invoke("displayed", "banner", adUnitId)
                    if (event == AdEvent.CLICK) onAdEventCallback?.invoke("clicked", "banner", adUnitId)
                }
            })
            activeAds["banner"] = adView
            adView.loadAd()
            adView
        }.getOrNull()
    }

    // ─────────────────────────────────────────
    // 네이티브 광고 (layout 파일 필요)
    // ─────────────────────────────────────────

    @Synchronized
    fun nativeView(context: Context, customAdUnitId: String? = null): View? {
        val adUnitId = customAdUnitId ?: NapSspConfig.AD_UNIT_IDS["native"] ?: return null
        destroyAndRemoveAd("native")
        return runCatching {
            val nativeView = NativeAdView(context)
            val layoutId = R.layout.admixer_item_320x480   // 프로젝트 layout ID에 맞게 수정
            val adViewIds = mapOf(
                "nativeLayout" to layoutId,
                "iv_icon" to R.id.iv_icon,
                "tv_title" to R.id.tv_title,
                "tv_adv" to R.id.tv_adv,
                "tv_desc" to R.id.tv_desc,
                "iv_main" to R.id.iv_main,
                "btn_cta" to R.id.btn_cta,
            )
            val adInfo = AdInfo.Builder(adUnitId)
                .setIsUseMediation(true)
                .apply {
                    setViewIds(AdMixer.ADAPTER_ADFIT, adViewIds)
                    setViewIds(AdMixer.ADAPTER_PANGLE, adViewIds)
                    setViewIds(AdMixer.ADAPTER_ADMANAGER, adViewIds)
                }.build()
            val viewBinder = NativeAdViewBinder.Builder(layoutId)
                .setIconImageId(R.id.iv_icon).setTitleId(R.id.tv_title)
                .setAdvertiserId(R.id.tv_adv).setDescriptionId(R.id.tv_desc)
                .setMainViewId(R.id.iv_main).setCtaId(R.id.btn_cta).build()
            nativeView.setAdInfo(adInfo)
            nativeView.setViewBinder(viewBinder)
            nativeView.setAdViewListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) = onAdEventCallback?.invoke("loaded", "native", adUnitId) ?: Unit
                override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", "native", "[$errorCode] $errorMsg")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    if (event == AdEvent.DISPLAYED) onAdEventCallback?.invoke("displayed", "native", adUnitId)
                    if (event == AdEvent.CLICK) onAdEventCallback?.invoke("clicked", "native", adUnitId)
                }
            })
            activeAds["native"] = nativeView
            nativeView.loadNativeAd()
            nativeView
        }.getOrNull()
    }

    // ─────────────────────────────────────────
    // 아웃스트림 비디오
    // ─────────────────────────────────────────

    @Synchronized
    fun videoView(context: Context, customAdUnitId: String? = null): View? {
        val adUnitId = customAdUnitId ?: NapSspConfig.AD_UNIT_IDS["outstream_video"] ?: return null
        destroyAndRemoveAd("video")
        return runCatching {
            val videoView = VideoAdView(context)
            videoView.setAdInfo(AdInfo.Builder(adUnitId).setIsUseMediation(true).isRetry(false).build())
            videoView.setAdViewListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) = onAdEventCallback?.invoke("loaded", "video", adUnitId) ?: Unit
                override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", "video", "[$errorCode] $errorMsg")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    when (event) {
                        AdEvent.DISPLAYED  -> onAdEventCallback?.invoke("displayed", "video", adUnitId)
                        AdEvent.CLICK      -> onAdEventCallback?.invoke("clicked", "video", adUnitId)
                        AdEvent.SKIPPED    -> onAdEventCallback?.invoke("skipped", "video", adUnitId)
                        AdEvent.COMPLETION -> onAdEventCallback?.invoke("completed", "video", adUnitId)
                        else -> {}
                    }
                }
            })
            activeAds["video"] = videoView
            videoView.loadAd()
            videoView
        }.getOrNull()
    }

    // ─────────────────────────────────────────
    // 보상형 광고 (전체화면, View 반환 없음)
    // ─────────────────────────────────────────

    @Synchronized
    fun rewardVideoView(context: Context, customAdUnitId: String? = null) {
        val adUnitId = customAdUnitId ?: NapSspConfig.AD_UNIT_IDS["reward_video"] ?: return
        destroyAndRemoveAd("rewardVideo")
        runCatching {
            val rewardAd = RewardInterstitialVideoAd(context)
            rewardAd.setAdInfo(
                AdInfo.Builder(adUnitId).setMute(true).setIsUseMediation(true).build()
            )
            rewardAd.setListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) {
                    onAdEventCallback?.invoke("loaded", "rewardVideo", adUnitId)
                    rewardAd.showRewardVideoAd()
                }
                override fun onFailedToReceiveAd(v: Any?, a: String?, e: Int, m: String?) {
                    onAdEventCallback?.invoke("failed", "rewardVideo", "[$e] $m")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    when (event) {
                        AdEvent.DISPLAYED    -> onAdEventCallback?.invoke("displayed", "rewardVideo", adUnitId)
                        AdEvent.CLICK        -> onAdEventCallback?.invoke("clicked", "rewardVideo", adUnitId)
                        AdEvent.EARNEDREWARD -> onAdEventCallback?.invoke("rewarded", "rewardVideo", adUnitId)
                        AdEvent.CLOSE        -> onAdEventCallback?.invoke("closed", "rewardVideo", adUnitId)
                        else -> {}
                    }
                }
            })
            activeAds["rewardVideo"] = rewardAd
            rewardAd.loadRewardVideoAd()
        }
    }

    // ─────────────────────────────────────────
    // 전면 비디오 광고 (전체화면)
    // ─────────────────────────────────────────

    @Synchronized
    fun interstitialVideoView(context: Context, customAdUnitId: String? = null) {
        val adUnitId = customAdUnitId ?: NapSspConfig.AD_UNIT_IDS["interstitial_320x480"] ?: return
        destroyAndRemoveAd("interstitialVideo")
        runCatching {
            val interstitialAd = InterstitialVideoAd(context)
            interstitialAd.setAdInfo(
                AdInfo.Builder(adUnitId).interstitialTimeout(0).setIsUseMediation(true).build()
            )
            interstitialAd.setListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) {
                    onAdEventCallback?.invoke("loaded", "interstitialVideo", adUnitId)
                    interstitialAd.showInterstitialVideoAd()
                }
                override fun onFailedToReceiveAd(v: Any?, a: String?, e: Int, m: String?) {
                    onAdEventCallback?.invoke("failed", "interstitialVideo", "[$e] $m")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    when (event) {
                        AdEvent.DISPLAYED  -> onAdEventCallback?.invoke("displayed", "interstitialVideo", adUnitId)
                        AdEvent.CLICK      -> onAdEventCallback?.invoke("clicked", "interstitialVideo", adUnitId)
                        AdEvent.CLOSE      -> onAdEventCallback?.invoke("closed", "interstitialVideo", adUnitId)
                        else -> {}
                    }
                }
            })
            activeAds["interstitialVideo"] = interstitialAd
            interstitialAd.loadInterstitialVideoAd()
        }
    }

    // ─────────────────────────────────────────
    // 전면 배너/팝업 광고 (전체화면)
    // ─────────────────────────────────────────

    @Synchronized
    fun interstitialBannerView(context: Context, customAdUnitId: String? = null) {
        val adUnitId = customAdUnitId ?: NapSspConfig.AD_UNIT_IDS["interstitial_320x480_f"] ?: return
        destroyAndRemoveAd("interstitialBanner")
        runCatching {
            val interstitialAd = InterstitialAd(context)
            val adConfig = PopupInterstitialAdOption().apply {
                setDisableBackKey(false)
                setButtonLeft("광고종료", "#234234")
                setCountDown(0, 5)
            }
            interstitialAd.setAdInfo(
                AdInfo.Builder(adUnitId)
                    .isUseBackgroundAlpha(true)
                    .popupAdOption(adConfig)
                    .interstitialAdType(AdInfo.InterstitialAdType.Popup)
                    .setIsUseMediation(true)
                    .build()
            )
            interstitialAd.setAdListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) {
                    onAdEventCallback?.invoke("loaded", "interstitialBanner", adUnitId)
                    interstitialAd.showInterstitial()
                }
                override fun onFailedToReceiveAd(v: Any?, a: String?, e: Int, m: String?) {
                    onAdEventCallback?.invoke("failed", "interstitialBanner", "[$e] $m")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    when (event) {
                        AdEvent.DISPLAYED -> onAdEventCallback?.invoke("displayed", "interstitialBanner", adUnitId)
                        AdEvent.CLICK     -> onAdEventCallback?.invoke("clicked", "interstitialBanner", adUnitId)
                        AdEvent.CLOSE     -> onAdEventCallback?.invoke("closed", "interstitialBanner", adUnitId)
                        else -> {}
                    }
                }
            })
            activeAds["interstitialBanner"] = interstitialAd
            interstitialAd.startInterstitial()
        }
    }

    // ─────────────────────────────────────────
    // 생명주기 관리
    // ─────────────────────────────────────────

    fun clearAllAds() {
        activeAds.keys.toList().forEach { destroyAndRemoveAd(it) }
    }

    fun resumeAll() {
        activeAds.values.forEach {
            if (it is AdView) it.onResume()
            else if (it is NativeAdView) it.onResume()
            else if (it is VideoAdView) it.onResume()
        }
    }

    fun pauseAll() {
        activeAds.values.forEach {
            if (it is AdView) it.onPause()
            else if (it is NativeAdView) it.onPause()
            else if (it is VideoAdView) it.onPause()
        }
    }

    private fun destroyAndRemoveAd(format: String) {
        activeAds[format]?.let { ad ->
            if (ad is View) ad.visibility = View.GONE
            when (ad) {
                is AdView -> { ad.onPause(); ad.onDestroy() }
                is NativeAdView -> { ad.onPause(); ad.onDestroy() }
                is VideoAdView -> { ad.onPause(); ad.onDestroy() }
                is InterstitialAd -> ad.stopInterstitial()
                is InterstitialVideoAd -> ad.stopInterstitialVideoAd()
                is RewardInterstitialVideoAd -> ad.stopRewardVideoAd()
            }
            if (ad is View) (ad.parent as? ViewGroup)?.removeView(ad)
        }
        activeAds.remove(format)
    }
}
```

---

### 4-4. 브릿지 + WebView 화면 구성

**`HybridWebViewScreen.kt`** — WebView와 광고를 함께 보여주는 화면

```kotlin
package com.yourapp

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nasmedia.admixerssp.ads.AdView
import com.yourapp.bridge.NapSspSdkIntegration
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

// ─────────────────────────────────────────────────────────────────────────
// JS ↔ Native 브릿지
// ─────────────────────────────────────────────────────────────────────────

class NapSspHybridBridge(
    private val webView: WebView,
    private val onAdRequest: (String, String?) -> Unit
) {
    private var lastActionTime = 0L

    private val supportedFormats = setOf(
        "banner", "native", "video",
        "rewardVideo", "interstitialVideo", "interstitialBanner"
    )

    @JavascriptInterface
    fun postMessage(jsonString: String) {
        // 0.5초 이내 중복 요청 무시
        val now = System.currentTimeMillis()
        if (now - lastActionTime < 500) return
        lastActionTime = now

        try {
            val req = JSONObject(jsonString)
            val action = req.optString("action")
            val params = req.optJSONObject("params") ?: JSONObject()

            when (action) {
                "init" -> {
                    NapSspSdkIntegration.initialize(webView.context)
                    sendResponse("init", "success", "SDK Initialized")
                }
                "loadAd" -> {
                    val format = params.optString("format")
                    val adUnitId = params.optString("adUnitId").takeIf { it.isNotEmpty() }
                    if (format !in supportedFormats) {
                        sendResponse("loadAd", "error", "Unsupported format: $format")
                        return
                    }
                    webView.post { onAdRequest(format, adUnitId) }
                    sendResponse("loadAd", "success", "Accepted $format")
                }
                "clearAds" -> {
                    webView.post {
                        onAdRequest("clear", null)
                        NapSspSdkIntegration.clearAllAds()
                        sendResponse("clearAds", "success", "All ads cleared")
                    }
                }
                else -> sendResponse(action, "error", "Unknown action")
            }
        } catch (e: Exception) {
            sendResponse("error", "error", e.message ?: "Invalid JSON")
        }
    }

    fun sendResponse(action: String, status: String, data: Any) {
        val response = JSONObject().apply {
            put("action", action)
            put("status", status)
            put("data", data)
        }
        // JS로 안전하게 전달하기 위해 escape 처리
        val escaped = JSONObject.quote(response.toString())
        webView.post {
            webView.evaluateJavascript(
                "window.onNapSspMessage && window.onNapSspMessage($escaped)", null
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// Compose 화면
// ─────────────────────────────────────────────────────────────────────────

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HybridWebViewScreen(modifier: Modifier = Modifier) {
    var currentAdView by remember { mutableStateOf<View?>(null) }
    var adHeight by remember { mutableStateOf(0.dp) }
    var adSessionId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var isRequestingAd by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // 화면 생명주기와 SDK 생명주기 연결
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME  -> NapSspSdkIntegration.resumeAll()
                Lifecycle.Event.ON_PAUSE   -> NapSspSdkIntegration.pauseAll()
                Lifecycle.Event.ON_DESTROY -> NapSspSdkIntegration.clearAllAds()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            NapSspSdkIntegration.clearAllAds()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // WebView 영역
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { ctx ->
                val webView = WebView(ctx)
                val bridge = NapSspHybridBridge(webView) { format, customAdUnitId ->
                    if (isRequestingAd) return@NapSspHybridBridge
                    isRequestingAd = true

                    coroutineScope.launch {
                        try {
                            if (format == "clear") {
                                currentAdView = null; adHeight = 0.dp
                                NapSspSdkIntegration.clearAllAds()
                                return@launch
                            }
                            currentAdView = null
                            NapSspSdkIntegration.clearAllAds()
                            delay(200)
                            adSessionId = UUID.randomUUID().toString()

                            val adView = when (format) {
                                "banner"             -> { adHeight = 100.dp; NapSspSdkIntegration.bannerView(context, customAdUnitId) }
                                "native"             -> { adHeight = 400.dp; NapSspSdkIntegration.nativeView(context, customAdUnitId) }
                                "video"              -> { adHeight = 250.dp; NapSspSdkIntegration.videoView(context, customAdUnitId) }
                                "rewardVideo"        -> { adHeight = 0.dp; NapSspSdkIntegration.rewardVideoView(context, customAdUnitId); null }
                                "interstitialVideo"  -> { adHeight = 0.dp; NapSspSdkIntegration.interstitialVideoView(context, customAdUnitId); null }
                                "interstitialBanner" -> { adHeight = 0.dp; NapSspSdkIntegration.interstitialBannerView(context, customAdUnitId); null }
                                else -> { adHeight = 0.dp; null }
                            }
                            if (isActive) currentAdView = adView
                        } finally {
                            isRequestingAd = false
                        }
                    }
                }

                // SDK 이벤트 → JS 콜백 연결
                NapSspSdkIntegration.onAdEventCallback = { event, format, detail ->
                    bridge.sendResponse("event", "success", "[$format] $event: $detail")
                }

                webView.apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    addJavascriptInterface(bridge, "NapSspBridge")
                    // 실제 앱에서는 서버 URL이나 assets 파일을 로드합니다
                    loadUrl("file:///android_asset/index.html")
                }
            }
        )

        // 광고 View 영역 (배너/네이티브/비디오)
        key(adSessionId) {
            if (currentAdView != null && adHeight > 0.dp) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(adHeight),
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            currentAdView?.let { adView ->
                                (adView.parent as? ViewGroup)?.removeView(adView)
                                adView.layoutParams = FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT
                                )
                                addView(adView)
                                if (adView is AdView) adView.onResume()
                            }
                        }
                    }
                )
            }
        }
    }
}
```

---

## 5. iOS 연동

### 5-1. SDK 설치

iOS는 Swift Package Manager(SPM)로 설치합니다.

**Xcode → File → Add Package Dependencies** 에서 아래 URL을 추가하거나,
`Package.swift`에 직접 추가합니다.

```swift
// Package.swift 예시
dependencies: [
    .package(url: "https://github.com/Nasmedia-Tech/admixer-ios-spm", from: "1.1.6"),
    .package(url: "https://github.com/Nasmedia-Tech/admixer-ios-mediation-spm", from: "2.3.3"),
    // 미디에이션 어댑터 (사용하는 네트워크만 선택)
    .package(url: "https://github.com/Nasmedia-Tech/admixer-ios-mediation-gam-spm", from: "1.0.8"),
    .package(url: "https://github.com/Nasmedia-Tech/admixer-ios-mediation-adfit-spm", from: "1.0.7"),
],
```

> 패키지 URL은 Nasmedia에서 발급받은 정확한 주소를 사용하세요.
> 내부 벤더 패키지를 사용하는 경우 `ios/Vendor/Packages/` 경로를 그대로 사용합니다.

---

### 5-2. Info.plist 설정

```xml
<!-- ATT 동의 문구 (IDFA 수집 시 필수) -->
<key>NSUserTrackingUsageDescription</key>
<string>맞춤형 광고 제공을 위해 기기 식별자를 사용합니다.</string>

<!-- Google Mobile Ads 사용 시 (운영 ID로 교체) -->
<key>GADApplicationIdentifier</key>
<string>YOUR_GOOGLE_MOBILE_ADS_APP_ID</string>
```

---

### 5-3. SDK 초기화 코드

**`NapSspConfig.swift`** — 키 설정

```swift
import Foundation

enum NapSspConfig {
    // 실제 키로 교체하세요. xcconfig 또는 서버 설정으로 주입하는 것을 권장합니다.
    static let mediaKey: Int = 10771

    static let adUnitIDs: [String: Int] = [
        "banner_320x100":        104704,
        "native":                104588,
        "outstream_video":       104589,
        "reward_video":          103722,
        "interstitial_320x480":  104702,
        "interstitial_320x480_f": 104703,
    ]

    static func adUnitID(_ key: String) -> Int {
        adUnitIDs[key] ?? 0
    }
}
```

**`NapSspSdkIntegration.swift`** — SDK 연동 엔진

```swift
import Foundation
import UIKit
import AdMixer
import AdMixerMediation

class NapSspSdkIntegration: NSObject {
    static let shared = NapSspSdkIntegration()

    var onAdEventCallback: ((String, String, String) -> Void)?
    private var activeAds: [String: Any] = [:]

    // ─────────────────────────────────────────
    // 초기화
    // ─────────────────────────────────────────

    static func initializeSdk() {
        let adUnitIDs = Set(NapSspConfig.adUnitIDs.values.filter { $0 > 0 })
        AMMediation.shared.initialize(mediaKey: NapSspConfig.mediaKey, adunitID: adUnitIDs)
        shared.onAdEventCallback?("loaded", "initialize", String(NapSspConfig.mediaKey))
    }

    // ─────────────────────────────────────────
    // 배너 광고
    // ─────────────────────────────────────────

    static func banner(rootVC: UIViewController, customAdUnitId: Int? = nil) -> UIView? {
        let adUnitId = customAdUnitId ?? NapSspConfig.adUnitID("banner_320x100")
        shared.destroyAndRemoveAd(format: "banner")
        let bannerView = AMMBannerView(rootViewController: rootVC)
        bannerView.delegate = shared
        bannerView.adUnitID = adUnitId
        shared.activeAds["banner"] = bannerView
        bannerView.load()
        return bannerView
    }

    // ─────────────────────────────────────────
    // 네이티브 광고 (AMMNativeAdView.xib 필요)
    // ─────────────────────────────────────────

    static func native(rootVC: UIViewController, customAdUnitId: Int? = nil) -> UIView? {
        let adUnitId = customAdUnitId ?? NapSspConfig.adUnitID("native")
        shared.destroyAndRemoveAd(format: "native")
        let nativeAdView = Bundle.main.loadNibNamed("AMMNativeAdView", owner: nil)?.first as? AMMNativeAdView
        let nativeView = AMMNativeAdViewContainer(rootViewController: rootVC)
        nativeView.nativeAdView = nativeAdView
        nativeView.delegate = shared
        nativeView.adUnitID = adUnitId
        shared.activeAds["native"] = nativeView
        nativeView.load()
        return nativeView
    }

    // ─────────────────────────────────────────
    // 아웃스트림 비디오
    // ─────────────────────────────────────────

    static func video(rootVC: UIViewController, customAdUnitId: Int? = nil) -> UIView? {
        let adUnitId = customAdUnitId ?? NapSspConfig.adUnitID("outstream_video")
        shared.destroyAndRemoveAd(format: "video")
        let videoView = AMMVideoView(rootViewController: rootVC)
        videoView.delegate = shared
        videoView.adUnitID = adUnitId
        shared.activeAds["video"] = videoView
        videoView.load()
        return videoView
    }

    // ─────────────────────────────────────────
    // 보상형 광고 (전체화면)
    // ─────────────────────────────────────────

    static func rewardVideo(rootVC: UIViewController, customAdUnitId: Int? = nil) {
        let adUnitId = customAdUnitId ?? NapSspConfig.adUnitID("reward_video")
        shared.destroyAndRemoveAd(format: "rewardVideo")
        AMMRewardVideo.load(adUnitID: adUnitId) { reward, error in
            if let reward = reward {
                reward.delegate = shared
                shared.activeAds["rewardVideo"] = reward
                shared.onAdEventCallback?("loaded", "rewardVideo", String(adUnitId))
                reward.show(rootViewController: rootVC)
            } else {
                shared.onAdEventCallback?("failed", "rewardVideo", error?.localizedDescription ?? "load failed")
            }
        }
    }

    // ─────────────────────────────────────────
    // 전면 비디오 광고 (전체화면)
    // ─────────────────────────────────────────

    static func interstitialVideo(rootVC: UIViewController, customAdUnitId: Int? = nil) {
        let adUnitId = customAdUnitId ?? NapSspConfig.adUnitID("interstitial_320x480")
        shared.destroyAndRemoveAd(format: "interstitialVideo")
        AMMVideoInterstitial.load(adUnitID: adUnitId) { interstitial, error in
            if let interstitial = interstitial {
                interstitial.delegate = shared
                shared.activeAds["interstitialVideo"] = interstitial
                shared.onAdEventCallback?("loaded", "interstitialVideo", String(adUnitId))
                interstitial.show(rootViewController: rootVC)
            } else {
                shared.onAdEventCallback?("failed", "interstitialVideo", error?.localizedDescription ?? "load failed")
            }
        }
    }

    // ─────────────────────────────────────────
    // 전면 배너/팝업 광고 (전체화면)
    // ─────────────────────────────────────────

    static func interstitialBanner(rootVC: UIViewController, customAdUnitId: Int? = nil) {
        let adUnitId = customAdUnitId ?? NapSspConfig.adUnitID("interstitial_320x480_f")
        shared.destroyAndRemoveAd(format: "interstitialBanner")
        AMMInterstitial.load(adUnitID: adUnitId) { interstitial, error in
            if let interstitial = interstitial {
                interstitial.delegate = shared
                shared.activeAds["interstitialBanner"] = interstitial
                shared.onAdEventCallback?("loaded", "interstitialBanner", String(adUnitId))
                interstitial.show(rootViewController: rootVC)
            } else {
                shared.onAdEventCallback?("failed", "interstitialBanner", error?.localizedDescription ?? "load failed")
            }
        }
    }

    // ─────────────────────────────────────────
    // 생명주기 관리
    // ─────────────────────────────────────────

    static func clearAllAds() {
        Array(shared.activeAds.keys).forEach { shared.destroyAndRemoveAd(format: $0) }
    }

    private func destroyAndRemoveAd(format: String) {
        if let ad = activeAds[format] {
            if let v = ad as? UIView { v.isHidden = true }
            if let b = ad as? AMMBannerView { b.stop() }
            else if let n = ad as? AMMNativeAdViewContainer { n.stop() }
            else if let v = ad as? AMMVideoView { v.stop() }
            else if let i = ad as? AMMInterstitial { i.stop() }
            else if let r = ad as? AMMRewardVideo { r.stop() }
            else if let iv = ad as? AMMVideoInterstitial { iv.stop() }
            if let v = ad as? UIView { v.removeFromSuperview() }
        }
        activeAds.removeValue(forKey: format)
    }
}

// ─────────────────────────────────────────
// SDK Delegate 구현
// ─────────────────────────────────────────

extension NapSspSdkIntegration: AMMBannerViewDelegate, AMMNativeDelegate,
    AMMVideoViewDelegate, AMMInterstitialDelegate, AMMRewardVideoDelegate, AMMVideoInterstitialDelegate {

    func onSuccessBanner() {
        if let v = activeAds["banner"] as? AMMBannerView {
            onAdEventCallback?("loaded", "banner", String(v.adUnitID))
        }
    }
    func onFailBanner() { onAdEventCallback?("failed", "banner", "load failed") }
    func onTapBanner() {
        if let v = activeAds["banner"] as? AMMBannerView {
            onAdEventCallback?("clicked", "banner", String(v.adUnitID))
        }
    }

    func onSuccessNative() {
        if let v = activeAds["native"] as? AMMNativeAdViewContainer {
            onAdEventCallback?("loaded", "native", String(v.adUnitID))
        }
    }
    func onFailNative() { onAdEventCallback?("failed", "native", "load failed") }
    func onTapNative() {
        if let v = activeAds["native"] as? AMMNativeAdViewContainer {
            onAdEventCallback?("clicked", "native", String(v.adUnitID))
        }
    }

    func onSuccessVideo() {
        if let v = activeAds["video"] as? AMMVideoView {
            onAdEventCallback?("loaded", "video", String(v.adUnitID))
        }
    }
    func onFailVideo() { onAdEventCallback?("failed", "video", "load failed") }
    func onTapVideoViewMore() {
        if let v = activeAds["video"] as? AMMVideoView {
            onAdEventCallback?("clicked", "video", String(v.adUnitID))
        }
    }
    func onSkipVideo() { onAdEventCallback?("skipped", "video", "") }
    func onCompleteVideo() { onAdEventCallback?("completed", "video", "") }

    func onSuccessShowReward() { onAdEventCallback?("displayed", "rewardVideo", "") }
    func onFailShowReward(error: (any Error)?) { onAdEventCallback?("failed", "rewardVideo", error?.localizedDescription ?? "") }
    func onTapRewardVideo() { onAdEventCallback?("clicked", "rewardVideo", "") }
    func onCloseRewardVideo() { onAdEventCallback?("closed", "rewardVideo", "") }
    func onRewardVideoComplete() { onAdEventCallback?("completed", "rewardVideo", "") }
    func onRewardVideoEarned() { onAdEventCallback?("rewarded", "rewardVideo", "success") }

    func onSuccessShowInterstitial() { onAdEventCallback?("displayed", "interstitialBanner", "") }
    func onFailShowInterstitial(error: (any Error)?) { onAdEventCallback?("failed", "interstitialBanner", error?.localizedDescription ?? "") }
    func onTapInterstitial() { onAdEventCallback?("clicked", "interstitialBanner", "") }
    func onCloseInterstitial() { onAdEventCallback?("closed", "interstitialBanner", "") }

    func onSuccessShowVideoInterstitial() { onAdEventCallback?("displayed", "interstitialVideo", "") }
    func onFailShowVideoInterstitial(error: (any Error)?) { onAdEventCallback?("failed", "interstitialVideo", error?.localizedDescription ?? "") }
    func onCloseVideoInterstitial() { onAdEventCallback?("closed", "interstitialVideo", "") }
    func onTapVideoInterstitialViewMore() { onAdEventCallback?("clicked", "interstitialVideo", "") }
    func onCompleteVideoInterstitial() { onAdEventCallback?("completed", "interstitialVideo", "") }
}
```

---

### 5-4. 브릿지 + WebView 화면 구성

**`HybridWebViewScreen.swift`**

```swift
#if os(iOS) && canImport(UIKit)
import SwiftUI
import WebKit

// ─────────────────────────────────────────
// 메시지 구조체
// ─────────────────────────────────────────

struct HybridRequest: Codable {
    let action: String
    let params: [String: String]?
}

// ─────────────────────────────────────────
// JS ↔ Native 브릿지
// ─────────────────────────────────────────

final class NapSspHybridBridge: NSObject, WKScriptMessageHandler {
    weak var webView: WKWebView?
    var onAdLoaded: ((UIView?, CGFloat) -> Void)?

    private var lastActionTime: Date = .distantPast
    private let supportedFormats: Set<String> = [
        "banner", "native", "video",
        "rewardVideo", "interstitialVideo", "interstitialBanner"
    ]

    override init() {
        super.init()
        NapSspSdkIntegration.shared.onAdEventCallback = { [weak self] event, format, detail in
            self?.sendResponse(action: "event", status: "success", data: "[\(format)] \(event): \(detail)")
        }
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        // 0.5초 이내 중복 요청 무시
        let now = Date()
        guard now.timeIntervalSince(lastActionTime) >= 0.5 else { return }
        lastActionTime = now

        guard let body = message.body as? String,
              let data = body.data(using: .utf8),
              let request = try? JSONDecoder().decode(HybridRequest.self, from: data) else {
            sendResponse(action: "error", status: "error", data: "Invalid JSON")
            return
        }

        switch request.action {
        case "init":
            NapSspSdkIntegration.initializeSdk()
            sendResponse(action: "init", status: "success", data: "SDK Initialized")

        case "loadAd":
            guard let format = request.params?["format"], supportedFormats.contains(format) else {
                sendResponse(action: "loadAd", status: "error",
                             data: "Unsupported format: \(request.params?["format"] ?? "")")
                return
            }
            let adUnitId = request.params?["adUnitId"]
            handleLoadAd(format: format, adUnitId: adUnitId)

        case "clearAds":
            DispatchQueue.main.async {
                self.onAdLoaded?(nil, 0)
                NapSspSdkIntegration.clearAllAds()
                self.sendResponse(action: "clearAds", status: "success", data: "All ads cleared")
            }

        default:
            sendResponse(action: request.action, status: "error", data: "Unknown action")
        }
    }

    private func handleLoadAd(format: String, adUnitId: String? = nil) {
        let customId = adUnitId.flatMap { Int($0) }

        DispatchQueue.main.async {
            guard let rootVC = UIApplication.shared.connectedScenes
                .compactMap({ $0 as? UIWindowScene })
                .flatMap({ $0.windows })
                .first(where: { $0.isKeyWindow })?.rootViewController else {
                self.sendResponse(action: "loadAd", status: "error", data: "Root VC not found")
                return
            }

            var view: UIView? = nil
            var height: CGFloat = 0

            switch format {
            case "banner":             view = NapSspSdkIntegration.banner(rootVC: rootVC, customAdUnitId: customId);             height = 100
            case "native":             view = NapSspSdkIntegration.native(rootVC: rootVC, customAdUnitId: customId);             height = 400
            case "video":              view = NapSspSdkIntegration.video(rootVC: rootVC, customAdUnitId: customId);              height = 250
            case "rewardVideo":        NapSspSdkIntegration.rewardVideo(rootVC: rootVC, customAdUnitId: customId)
            case "interstitialVideo":  NapSspSdkIntegration.interstitialVideo(rootVC: rootVC, customAdUnitId: customId)
            case "interstitialBanner": NapSspSdkIntegration.interstitialBanner(rootVC: rootVC, customAdUnitId: customId)
            default: break
            }

            self.onAdLoaded?(view, height)
            self.sendResponse(action: "loadAd", status: "success", data: "Accepted \(format)")
        }
    }

    func sendResponse(action: String, status: String, data: String) {
        let responseDict: [String: Any] = ["action": action, "status": status, "data": data]
        guard let jsonData = try? JSONSerialization.data(withJSONObject: responseDict),
              let jsonStr = String(data: jsonData, encoding: .utf8),
              let jsArgData = try? JSONSerialization.data(withJSONObject: jsonStr, options: [.fragmentsAllowed]),
              let jsArg = String(data: jsArgData, encoding: .utf8) else { return }
        DispatchQueue.main.async {
            self.webView?.evaluateJavaScript(
                "window.onNapSspMessage && window.onNapSspMessage(\(jsArg))", completionHandler: nil
            )
        }
    }
}

// WKWebView retain cycle 방지 프록시
final class LeakAvoider: NSObject, WKScriptMessageHandler {
    weak var delegate: WKScriptMessageHandler?
    init(_ delegate: WKScriptMessageHandler) { self.delegate = delegate; super.init() }
    func userContentController(_ ucc: WKUserContentController, didReceive message: WKScriptMessage) {
        delegate?.userContentController(ucc, didReceive: message)
    }
}

// ─────────────────────────────────────────
// SwiftUI 화면
// ─────────────────────────────────────────

struct WebViewContainer: UIViewRepresentable {
    let bridge: NapSspHybridBridge
    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        config.userContentController.add(LeakAvoider(bridge), name: "NapSspBridge")
        config.allowsInlineMediaPlayback = true
        config.mediaTypesRequiringUserActionForPlayback = []
        let webView = WKWebView(frame: .zero, configuration: config)
        bridge.webView = webView
        // 실제 앱에서는 서버 URL이나 Bundle 파일을 로드합니다
        if let url = Bundle.main.url(forResource: "index", withExtension: "html") {
            webView.loadFileURL(url, allowingReadAccessTo: url.deletingLastPathComponent())
        }
        return webView
    }
    func updateUIView(_ uiView: WKWebView, context: Context) {}
}

struct HybridWebViewScreen: View {
    @State private var adView: UIView? = nil
    @State private var adHeight: CGFloat = 0
    @State private var adViewId = UUID()
    private let bridge = NapSspHybridBridge()

    var body: some View {
        VStack(spacing: 0) {
            WebViewContainer(bridge: bridge)
                .onAppear {
                    bridge.onAdLoaded = { view, height in
                        self.adView = view
                        self.adHeight = height
                        self.adViewId = UUID()
                        // 타입별 높이 보정
                        if view is AMMBannerView { self.adHeight = 100 }
                        else if view is AMMNativeAdViewContainer { self.adHeight = 350 }
                        else if view is AMMVideoView { self.adHeight = 250 }
                        else { self.adHeight = 0 }
                    }
                }

            if let view = adView, adHeight > 0 {
                Divider()
                AdViewRepresentable(adView: view)
                    .id(adViewId)
                    .frame(maxWidth: .infinity)
                    .frame(height: adHeight)
            }
        }
        .onDisappear { NapSspSdkIntegration.clearAllAds() }
    }
}

struct AdViewRepresentable: UIViewRepresentable {
    let adView: UIView
    func makeUIView(context: Context) -> UIView {
        adView.setContentCompressionResistancePriority(.required, for: .vertical)
        return adView
    }
    func updateUIView(_ uiView: UIView, context: Context) {}
}
#endif
```

---

## 6. JavaScript 연동 (공통)

Android / iOS 모두 동일한 JS 코드를 사용합니다.

### JS → Native 요청 함수

```javascript
function callNative(action, params = {}) {
    const request = JSON.stringify({ action, params });

    if (window.NapSspBridge?.postMessage) {
        // Android
        window.NapSspBridge.postMessage(request);
    } else if (window.webkit?.messageHandlers?.NapSspBridge) {
        // iOS
        window.webkit.messageHandlers.NapSspBridge.postMessage(request);
    } else {
        console.warn('NapSspBridge를 찾을 수 없습니다. 브라우저에서 실행 중이거나 브릿지가 아직 준비되지 않았습니다.');
    }
}
```

### Native → JS 응답 수신 함수

```javascript
window.onNapSspMessage = function(responseStr) {
    const response = JSON.parse(responseStr);
    // response.action : "init" | "loadAd" | "clearAds" | "event" | "error"
    // response.status : "success" | "error"
    // response.data   : 메시지 문자열

    console.log('[NapSsp]', response.action, response.status, response.data);

    if (response.action === 'event') {
        // SDK 이벤트 처리 (loaded, displayed, clicked, rewarded, closed 등)
        handleAdEvent(response.data);
    }
};

function handleAdEvent(data) {
    // 예: "[banner] loaded: 104704"
    console.log('Ad Event:', data);
}
```

### 완성된 HTML 예시

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>NapSSP Hybrid Sample</title>
    <style>
        body { font-family: -apple-system, sans-serif; padding: 20px; background: #f5f5f5; }
        button {
            display: block; width: 100%; padding: 14px; margin: 8px 0;
            background: #1a73e8; color: white; border: none; border-radius: 8px;
            font-size: 16px; cursor: pointer;
        }
        button:active { background: #1557b0; }
        .log { background: #1e1e1e; color: #4ec94e; padding: 12px; border-radius: 8px;
               font-family: monospace; font-size: 12px; min-height: 120px;
               margin-top: 16px; overflow-y: auto; }
    </style>
</head>
<body>
    <h2>NapSSP Hybrid</h2>

    <button onclick="callNative('init')">1. SDK 초기화</button>
    <button onclick="callNative('loadAd', {format: 'banner'})">배너 광고</button>
    <button onclick="callNative('loadAd', {format: 'native'})">네이티브 광고</button>
    <button onclick="callNative('loadAd', {format: 'video'})">아웃스트림 비디오</button>
    <button onclick="callNative('loadAd', {format: 'rewardVideo'})">보상형 광고</button>
    <button onclick="callNative('loadAd', {format: 'interstitialVideo'})">전면 비디오</button>
    <button onclick="callNative('loadAd', {format: 'interstitialBanner'})">전면 배너</button>
    <button onclick="callNative('clearAds')" style="background:#666">광고 해제</button>

    <div class="log" id="log">브릿지 연결 대기 중...</div>

    <script>
        function log(msg) {
            const el = document.getElementById('log');
            const time = new Date().toLocaleTimeString();
            el.innerHTML += `<div>[${time}] ${msg}</div>`;
            el.scrollTop = el.scrollHeight;
        }

        function callNative(action, params = {}) {
            const request = JSON.stringify({ action, params });
            log(`→ ${action} ${params.format || ''}`);
            try {
                if (window.NapSspBridge?.postMessage) {
                    window.NapSspBridge.postMessage(request);
                } else if (window.webkit?.messageHandlers?.NapSspBridge) {
                    window.webkit.messageHandlers.NapSspBridge.postMessage(request);
                } else {
                    log('<span style="color:red">브릿지를 찾을 수 없습니다</span>');
                }
            } catch (e) {
                log('<span style="color:red">오류: ' + e.message + '</span>');
            }
        }

        window.onNapSspMessage = function(responseStr) {
            const res = JSON.parse(responseStr);
            const color = res.status === 'success' ? '#4ec94e' : '#f44336';
            log(`<span style="color:${color}">← ${res.action} [${res.status}] ${res.data}</span>`);
        };

        log('준비 완료. SDK 초기화 버튼을 먼저 누르세요.');
    </script>
</body>
</html>
```

---

## 7. 광고 포맷별 사용법

### JS에서 사용할 수 있는 모든 포맷

| format 값 | 광고 종류 | 표시 방식 | 화면 높이 |
|---|---|---|---|
| `banner` | 배너 (320×100) | WebView 하단 Native 영역 | 100dp / pt |
| `native` | 네이티브 | WebView 하단 Native 영역 | 350~400dp / pt |
| `video` | 아웃스트림 비디오 | WebView 하단 Native 영역 | 250dp / pt |
| `rewardVideo` | 보상형 비디오 | 전체화면 (SDK 자동 표시) | — |
| `interstitialVideo` | 전면 비디오 | 전체화면 (SDK 자동 표시) | — |
| `interstitialBanner` | 전면 배너/팝업 | 전체화면 (SDK 자동 표시) | — |

### 커스텀 Ad Unit ID 전달

기본 설정값 대신 특정 광고 단위 ID를 직접 지정하려면 `adUnitId`를 함께 전달합니다.

```javascript
// 기본 ID 사용
callNative('loadAd', { format: 'banner' })

// 커스텀 ID 사용
callNative('loadAd', { format: 'banner', adUnitId: '104704' })
```

> iOS에서는 `adUnitId`를 `Int`로 변환해 SDK에 전달합니다. 숫자가 아닌 값이 전달되면 기본 설정값이 사용됩니다.

---

## 8. 이벤트 처리

Native에서 JS로 전달하는 이벤트는 모두 `window.onNapSspMessage`로 수신합니다.

### 이벤트 종류

| event | 설명 |
|---|---|
| `loaded` | 광고 로드 성공 |
| `displayed` | 광고 노출 |
| `clicked` | 광고 클릭 |
| `rewarded` | 보상 지급 (rewardVideo 전용) |
| `completed` | 광고 재생 완료 |
| `skipped` | 광고 건너뜀 |
| `closed` | 전면/보상 광고 닫힘 |
| `failed` | 광고 로드/표시 실패 |

### 이벤트 처리 예시

```javascript
window.onNapSspMessage = function(responseStr) {
    const res = JSON.parse(responseStr);

    if (res.action === 'event' && res.status === 'success') {
        // res.data 형식: "[format] event: detail"
        // 예: "[banner] loaded: 104704"
        // 예: "[rewardVideo] rewarded: 103722"
        // 예: "[banner] failed: [-1] No fill"

        const [formatPart, eventPart] = res.data.split('] ');
        const format = formatPart.replace('[', '');
        const [event] = eventPart.split(': ');

        switch (event) {
            case 'loaded':
                console.log(`${format} 광고 로드 성공`);
                break;
            case 'rewarded':
                // 리워드 지급 처리
                grantReward();
                break;
            case 'closed':
                // 전면 광고 닫힘 처리
                break;
            case 'failed':
                console.warn(`${format} 광고 로드 실패:`, res.data);
                break;
        }
    }
};
```

---

## 9. 자주 묻는 질문 & 문제 해결

### Q. 브릿지를 찾을 수 없다는 오류가 납니다

```
브릿지를 찾을 수 없습니다
```

**원인과 해결:**

| 원인 | 해결 |
|---|---|
| 일반 브라우저에서 테스트 중 | 반드시 앱 내 WebView에서 확인하세요 |
| Android: `addJavascriptInterface`가 호출되지 않음 | WebView 설정 코드에서 `addJavascriptInterface(bridge, "NapSspBridge")` 확인 |
| iOS: `userContentController.add`가 호출되지 않음 | `config.userContentController.add(LeakAvoider(bridge), name: "NapSspBridge")` 확인 |
| WebView가 완전히 로드되기 전에 JS를 호출함 | `onPageFinished` 또는 `webView.navigationDelegate`에서 로드 완료 후 호출 |

---

### Q. `loadAd` 응답은 success인데 광고가 보이지 않습니다

`loadAd` 즉시 응답은 **브릿지 수신 확인(ACK)** 입니다. 실제 광고 표시는 SDK 응답을 기다려야 합니다.

`window.onNapSspMessage`에서 `action === "event"` 이고 `data`에 `loaded` 또는 `failed`가 들어오는지 확인하세요.

```javascript
// 이 응답은 ACK (브릿지가 요청을 받았다는 의미)
{ "action": "loadAd", "status": "success", "data": "Accepted banner" }

// 이 응답이 실제 광고 로드 성공
{ "action": "event", "status": "success", "data": "[banner] loaded: 104704" }

// 이 응답이 실제 광고 로드 실패
{ "action": "event", "status": "success", "data": "[banner] failed: [-1] No fill" }
```

---

### Q. 광고가 로드됐지만 화면에 안 보입니다 (Android)

- `bannerView` / `nativeView` / `videoView`는 반환된 `View`를 Compose 화면에 붙여야 합니다.
- `HybridWebViewScreen.kt`에서 `currentAdView`가 null이 아니고 `adHeight > 0.dp`인지 확인하세요.
- `rewardVideo` / `interstitialVideo` / `interstitialBanner`는 SDK가 자동으로 전체화면을 띄우므로 별도 View 처리가 필요 없습니다.

---

### Q. iOS에서 광고가 올바른 크기로 표시되지 않습니다

`AdViewRepresentable`에서 `setContentCompressionResistancePriority(.required, for: .vertical)`를 설정했는지 확인하세요.

또한 `onAdLoaded` 클로저에서 타입별 높이를 명시적으로 지정하는 부분을 확인하세요.

```swift
if view is AMMBannerView { self.adHeight = 100 }
else if view is AMMNativeAdViewContainer { self.adHeight = 350 }
else if view is AMMVideoView { self.adHeight = 250 }
```

---

### Q. Android에서 빌드 오류가 납니다

```
Unable to locate a Java Runtime
```

JDK 17이 설치되어 있고 `JAVA_HOME`이 설정되어 있는지 확인하세요.

```bash
export JAVA_HOME=/path/to/jdk17
./gradlew assembleDebug
```

---

### Q. 앱을 백그라운드로 보냈다가 돌아오면 광고 오류가 납니다

생명주기 연결이 누락된 경우입니다. `HybridWebViewScreen`의 `DisposableEffect`에서 `ON_RESUME` / `ON_PAUSE` 이벤트가 `NapSspSdkIntegration.resumeAll()` / `pauseAll()`에 연결되어 있는지 확인하세요.

---

## 10. 운영 전 체크리스트

연동 완료 후 운영 배포 전에 아래 항목을 확인하세요.

**키 설정**
- [ ] `MEDIA_KEY`를 실제 운영 값으로 교체했다
- [ ] `AD_UNIT_ID`를 포맷별 실제 운영 값으로 교체했다
- [ ] Google Mobile Ads App ID를 운영 값으로 교체했다
- [ ] 키를 소스 코드에 직접 커밋하지 않았다 (`BuildConfig`, `xcconfig`, 서버 설정 등 활용)

**Android**
- [ ] 불필요한 권한(READ_PHONE_STATE 등)을 앱 정책에 맞게 검토했다
- [ ] `usesCleartextTraffic` 설정을 운영 정책에 맞게 검토했다
- [ ] SDK 로그 레벨을 운영 환경에 맞게 조정했다 (`AdMixerLog.LogLevel.ERROR` 등)

**iOS**
- [ ] `NSUserTrackingUsageDescription` 문구를 앱에 맞게 작성했다
- [ ] ATT(App Tracking Transparency) 요청 타이밍을 앱 흐름에 맞게 구현했다

**브릿지**
- [ ] JS `callNative` 호출 전에 반드시 `init`을 먼저 호출하는지 확인했다
- [ ] 광고 해제(`clearAds`) 호출로 이전 광고를 정리하는 흐름이 있다
- [ ] `window.onNapSspMessage`에서 `failed` 이벤트를 처리하고 있다
- [ ] Android / iOS 양쪽에서 동일한 `format` 값을 사용하고 있다

---

## 문의

이메일: nap_adx@nasmedia.co.kr
