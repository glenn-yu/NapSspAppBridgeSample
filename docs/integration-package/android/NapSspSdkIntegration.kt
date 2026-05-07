package com.yourapp // TODO: 실제 패키지명으로 변경

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.bytedance.sdk.openadsdk.api.init.PAGConfig
import com.bytedance.sdk.openadsdk.api.init.PAGSdk
import com.nasmedia.admixerssp.ads.AdEvent
import com.nasmedia.admixerssp.ads.AdInfo
import com.nasmedia.admixerssp.ads.AdListener
import com.nasmedia.admixerssp.ads.AdView
import com.nasmedia.admixerssp.ads.InterstitialAd
import com.nasmedia.admixerssp.ads.InterstitialVideoAd
import com.nasmedia.admixerssp.ads.NativeAdView
import com.nasmedia.admixerssp.ads.PopupInterstitialAdOption
import com.nasmedia.admixerssp.ads.RewardInterstitialVideoAd
import com.nasmedia.admixerssp.ads.VideoAdView
import com.nasmedia.admixerssp.common.AdMixer
import com.nasmedia.admixerssp.common.AdMixerLog
import com.nasmedia.admixerssp.common.nativeads.NativeAdViewBinder

/**
 * NapSSP SDK 연동 엔진
 *
 * 광고 생성 / 생명주기 / 이벤트 콜백을 일괄 관리합니다.
 * 사용법:
 *   1. onAdEventCallback을 설정해 광고 이벤트를 수신합니다.
 *   2. initialize(context)를 호출해 SDK를 초기화합니다.
 *   3. bannerView / nativeView / videoView 등으로 광고 View를 요청합니다.
 *   4. 화면 종료 시 clearAllAds()를 호출합니다.
 */
object NapSspSdkIntegration {

    /** SDK 광고 이벤트 콜백. (event, format, detail) 형태로 수신합니다. */
    var onAdEventCallback: ((event: String, format: String, detail: String) -> Unit)? = null

    private var isSdkInitialized = false
    private val activeAds = mutableMapOf<String, Any>()

    // ── 초기화 ────────────────────────────────────────────────────────────

    @Synchronized
    fun initialize(context: Context) {
        if (isSdkInitialized) return

        runCatching {
            AdMixerLog.setLogLevel(AdMixerLog.LogLevel.DEBUG) // 운영: ERROR 또는 NONE 권장
            AdMixer.getInstance().initialize(
                context,
                NapSspConfig.MEDIA_KEY,
                ArrayList(NapSspConfig.AD_UNIT_IDS.values.toList())
            )

            // 사용하는 미디에이션 어댑터만 등록 (불필요한 것은 제거)
            AdMixer.registerAdapter(AdMixer.ADAPTER_ADMANAGER)
            AdMixer.registerAdapter(AdMixer.ADAPTER_ADFIT)
            AdMixer.registerAdapter(AdMixer.ADAPTER_PANGLE)
            AdMixer.registerAdapter(AdMixer.ADAPTER_APPLOVIN)
            AdMixer.registerAdapter(AdMixer.ADAPTER_UNITY)

            // Pangle 미디에이션 사용 시 초기화 (사용하지 않으면 이 블록 삭제)
            val pagConfig = PAGConfig.Builder()
                .appId(NapSspConfig.PANGLE_APP_ID)
                .debugLog(true)
                .supportMultiProcess(false)
                .build()
            PAGSdk.init(context, pagConfig, object : PAGSdk.PAGInitCallback {
                override fun success() { Log.i("NapSsp", "Pangle init success") }
                override fun fail(code: Int, msg: String) { Log.e("NapSsp", "Pangle init fail: $code") }
            })

            isSdkInitialized = true
            onAdEventCallback?.invoke("loaded", "initialize", NapSspConfig.MEDIA_KEY)
        }.onFailure {
            onAdEventCallback?.invoke("failed", "initialize", it.message ?: "sdk init failed")
        }
    }

    // ── 배너 광고 ─────────────────────────────────────────────────────────

    @Synchronized
    fun bannerView(context: Context, customAdUnitId: String? = null): View? {
        val adUnitId = customAdUnitId ?: NapSspConfig.AD_UNIT_IDS["banner_320x100"] ?: return null
        val format = "banner"
        destroyAndRemoveAd(format)
        return runCatching {
            val adView = AdView(context)
            adView.setAdInfo(AdInfo.Builder(adUnitId).setIsUseMediation(true).build())
            adView.setAlwaysShowAdView(true)
            adView.setAdViewListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) {
                    onAdEventCallback?.invoke("loaded", format, adUnitId)
                    adView.showAd()
                }
                override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$errorCode] $errorMsg")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    if (event == AdEvent.DISPLAYED) onAdEventCallback?.invoke("displayed", format, adUnitId)
                    if (event == AdEvent.CLICK) onAdEventCallback?.invoke("clicked", format, adUnitId)
                }
            })
            activeAds[format] = adView
            adView.loadAd()
            adView
        }.getOrNull()
    }

    // ── 네이티브 광고 ─────────────────────────────────────────────────────
    // layout XML과 view ID가 필요합니다. 프로젝트의 실제 값으로 교체하세요.
    // TODO: layoutId, iv_icon, tv_title, tv_adv, tv_desc, iv_main, btn_cta → 실제 R.layout.*, R.id.*

    @Synchronized
    fun nativeView(context: Context, customAdUnitId: String? = null): View? {
        val adUnitId = customAdUnitId ?: NapSspConfig.AD_UNIT_IDS["native"] ?: return null
        val format = "native"
        destroyAndRemoveAd(format)
        return runCatching {
            val nativeView = NativeAdView(context)

            // TODO: 아래 layoutId와 R.id.* 값을 실제 프로젝트 리소스로 교체하세요
            val layoutId = R.layout.admixer_item_320x480
            val adViewIds = mutableMapOf<String, Int>().apply {
                put("nativeLayout", layoutId)
                listOf("iv_icon", "tv_title", "tv_adv", "tv_desc", "iv_main", "btn_cta").forEach { name ->
                    val id = context.resources.getIdentifier(name, "id", context.packageName)
                    if (id != 0) put(name, id)
                }
            }

            val adInfo = AdInfo.Builder(adUnitId).setIsUseMediation(true).apply {
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
                override fun onReceivedAd(adapterName: String?, view: Any?) {
                    onAdEventCallback?.invoke("loaded", format, adUnitId)
                }
                override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$errorCode] $errorMsg")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    if (event == AdEvent.DISPLAYED) onAdEventCallback?.invoke("displayed", format, adUnitId)
                    if (event == AdEvent.CLICK) onAdEventCallback?.invoke("clicked", format, adUnitId)
                }
            })
            activeAds[format] = nativeView
            nativeView.loadNativeAd()
            nativeView
        }.getOrNull()
    }

    // ── 아웃스트림 비디오 ─────────────────────────────────────────────────

    @Synchronized
    fun videoView(context: Context, customAdUnitId: String? = null): View? {
        val adUnitId = customAdUnitId ?: NapSspConfig.AD_UNIT_IDS["outstream_video"] ?: return null
        val format = "video"
        destroyAndRemoveAd(format)
        return runCatching {
            val videoView = VideoAdView(context)
            videoView.setAdInfo(AdInfo.Builder(adUnitId).setIsUseMediation(true).isRetry(false).build())
            videoView.setAdViewListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) {
                    onAdEventCallback?.invoke("loaded", format, adUnitId)
                }
                override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$errorCode] $errorMsg")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    when (event) {
                        AdEvent.DISPLAYED  -> onAdEventCallback?.invoke("displayed", format, adUnitId)
                        AdEvent.CLICK      -> onAdEventCallback?.invoke("clicked", format, adUnitId)
                        AdEvent.SKIPPED    -> onAdEventCallback?.invoke("skipped", format, adUnitId)
                        AdEvent.COMPLETION -> onAdEventCallback?.invoke("completed", format, adUnitId)
                        else -> {}
                    }
                }
            })
            activeAds[format] = videoView
            videoView.loadAd()
            videoView
        }.getOrNull()
    }

    // ── 보상형 비디오 (전체화면) ──────────────────────────────────────────

    @Synchronized
    fun rewardVideoView(context: Context, customAdUnitId: String? = null) {
        val adUnitId = customAdUnitId ?: NapSspConfig.AD_UNIT_IDS["reward_video"] ?: return
        val format = "rewardVideo"
        destroyAndRemoveAd(format)
        runCatching {
            val rewardAd = RewardInterstitialVideoAd(context)
            rewardAd.setAdInfo(
                AdInfo.Builder(adUnitId)
                    .setCustomParams(mapOf("useid" to "YOUR_USER_ID")) // TODO: 실제 유저 식별값으로 교체
                    .setMute(true)
                    .setIsUseMediation(true)
                    .build()
            )
            rewardAd.setListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) {
                    onAdEventCallback?.invoke("loaded", format, adUnitId)
                    rewardAd.showRewardVideoAd()
                }
                override fun onFailedToReceiveAd(v: Any?, a: String?, e: Int, m: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$e] $m")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    when (event) {
                        AdEvent.DISPLAYED    -> onAdEventCallback?.invoke("displayed", format, adUnitId)
                        AdEvent.CLICK        -> onAdEventCallback?.invoke("clicked", format, adUnitId)
                        AdEvent.EARNEDREWARD -> onAdEventCallback?.invoke("rewarded", format, adUnitId)
                        AdEvent.CLOSE        -> onAdEventCallback?.invoke("closed", format, adUnitId)
                        AdEvent.SKIPPED      -> onAdEventCallback?.invoke("skipped", format, adUnitId)
                        AdEvent.COMPLETION   -> onAdEventCallback?.invoke("completed", format, adUnitId)
                        else -> {}
                    }
                }
            })
            activeAds[format] = rewardAd
            rewardAd.loadRewardVideoAd()
        }
    }

    // ── 전면 비디오 (전체화면) ────────────────────────────────────────────

    @Synchronized
    fun interstitialVideoView(context: Context, customAdUnitId: String? = null) {
        val adUnitId = customAdUnitId ?: NapSspConfig.AD_UNIT_IDS["interstitial_320x480"] ?: return
        val format = "interstitialVideo"
        destroyAndRemoveAd(format)
        runCatching {
            val interstitialAd = InterstitialVideoAd(context)
            interstitialAd.setAdInfo(
                AdInfo.Builder(adUnitId)
                    .interstitialTimeout(0)
                    .maxRetryCountInSlot(-1)
                    .setIsUseMediation(true)
                    .build()
            )
            interstitialAd.setListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) {
                    onAdEventCallback?.invoke("loaded", format, adUnitId)
                    interstitialAd.showInterstitialVideoAd()
                }
                override fun onFailedToReceiveAd(v: Any?, a: String?, e: Int, m: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$e] $m")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    when (event) {
                        AdEvent.DISPLAYED  -> onAdEventCallback?.invoke("displayed", format, adUnitId)
                        AdEvent.CLICK      -> onAdEventCallback?.invoke("clicked", format, adUnitId)
                        AdEvent.CLOSE      -> onAdEventCallback?.invoke("closed", format, adUnitId)
                        AdEvent.SKIPPED    -> onAdEventCallback?.invoke("skipped", format, adUnitId)
                        AdEvent.COMPLETION -> onAdEventCallback?.invoke("completed", format, adUnitId)
                        else -> {}
                    }
                }
            })
            activeAds[format] = interstitialAd
            interstitialAd.loadInterstitialVideoAd()
        }
    }

    // ── 전면 배너/팝업 (전체화면) ─────────────────────────────────────────

    @Synchronized
    fun interstitialBannerView(context: Context, customAdUnitId: String? = null) {
        val adUnitId = customAdUnitId ?: NapSspConfig.AD_UNIT_IDS["interstitial_320x480_f"] ?: return
        val format = "interstitialBanner"
        destroyAndRemoveAd(format)
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
                    onAdEventCallback?.invoke("loaded", format, adUnitId)
                    interstitialAd.showInterstitial()
                }
                override fun onFailedToReceiveAd(v: Any?, a: String?, e: Int, m: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$e] $m")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    when (event) {
                        AdEvent.DISPLAYED   -> onAdEventCallback?.invoke("displayed", format, adUnitId)
                        AdEvent.CLICK,
                        AdEvent.LEFT_CLICK,
                        AdEvent.RIGHT_CLICK -> onAdEventCallback?.invoke("clicked", format, adUnitId)
                        AdEvent.CLOSE       -> onAdEventCallback?.invoke("closed", format, adUnitId)
                        else -> {}
                    }
                }
            })
            activeAds[format] = interstitialAd
            interstitialAd.startInterstitial()
        }
    }

    // ── 생명주기 ──────────────────────────────────────────────────────────

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
                is AdView                    -> { ad.onPause(); ad.onDestroy() }
                is NativeAdView              -> { ad.onPause(); ad.onDestroy() }
                is VideoAdView               -> { ad.onPause(); ad.onDestroy() }
                is InterstitialAd            -> ad.stopInterstitial()
                is InterstitialVideoAd       -> ad.stopInterstitialVideoAd()
                is RewardInterstitialVideoAd -> ad.stopRewardVideoAd()
            }
            if (ad is View) (ad.parent as? ViewGroup)?.removeView(ad)
        }
        activeAds.remove(format)
    }
}
