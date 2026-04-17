package com.gwangy.nassspandroidsample.bridge

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.bytedance.sdk.openadsdk.api.init.PAGConfig
import com.bytedance.sdk.openadsdk.api.init.PAGSdk
import com.gwangy.nassspandroidsample.AdEventLogger
import com.gwangy.nassspandroidsample.AppConfig
import com.gwangy.nassspandroidsample.R
import com.nasmedia.admixerssp.ads.*
import com.nasmedia.admixerssp.common.AdMixer
import com.nasmedia.admixerssp.common.AdMixerLog
import com.nasmedia.admixerssp.common.nativeads.NativeAdViewBinder
import kotlin.random.Random

/**
 * NapSSP 광고 SDK 연동 엔진
 * 광고의 생성, 파괴, 생명주기 관리 및 Already Exist 오류 방지 로직이 포함되어 있습니다.
 */
object NapSspSdkIntegration {

    var onAdEventCallback: ((event: String, format: String, detail: String) -> Unit)? = null
    
    private var isSdkInitialized = false
    private val activeAds = mutableMapOf<String, Any>()

    private fun notifyEvent(event: String, format: String, id: String) {
        when (event) {
            "loaded"    -> AdEventLogger.loaded(format, id)
            "displayed" -> AdEventLogger.displayed(format, id)
            "clicked"   -> AdEventLogger.clicked(format, id)
        }
        onAdEventCallback?.invoke(event, format, id)
    }

    private fun destroyAndRemoveAd(format: String) {
        activeAds[format]?.let { ad ->
            if (ad is View) { ad.visibility = View.GONE }
            when (ad) {
                is AdView -> { ad.onPause(); ad.onDestroy() }
                is NativeAdView -> { ad.onPause(); ad.onDestroy() }
                is VideoAdView -> { ad.onPause(); ad.onDestroy() }
                is InterstitialAd -> ad.stopInterstitial()
                is InterstitialVideoAd -> ad.stopInterstitialVideoAd()
                is RewardInterstitialVideoAd -> ad.stopRewardVideoAd()
            }
            if (ad is View) { (ad.parent as? ViewGroup)?.removeView(ad) }
        }
        activeAds.remove(format)
    }

    @Synchronized
    fun initialize(context: Context) {
        if (isSdkInitialized) return

        val mediaKey = AppConfig.getMediaKey(context) ?: NapSspConfig.MEDIA_KEY
        val adUnitIds = NapSspConfig.AD_UNIT_IDS

        AdEventLogger.request("initialize", mediaKey)
        runCatching {
            AdMixerLog.setLogLevel(AdMixerLog.LogLevel.DEBUG)
            AdMixer.getInstance().initialize(
                context,
                mediaKey,
                ArrayList(adUnitIds.values.toList())
            )
            
            // 가이드에 명시된 미디에이션 어댑터 전수 등록
            AdMixer.registerAdapter(AdMixer.ADAPTER_ADMANAGER)
            AdMixer.registerAdapter(AdMixer.ADAPTER_ADFIT)
            AdMixer.registerAdapter(AdMixer.ADAPTER_PANGLE)
            AdMixer.registerAdapter(AdMixer.ADAPTER_APPLOVIN)
            AdMixer.registerAdapter(AdMixer.ADAPTER_UNITY)

            // Pangle 초기화 (Pangle 사용 시 필수)
            val pAGInitConfig = PAGConfig.Builder()
                .appId("8245842") // 테스트용 발급 앱 ID (필요 시 수정)
                .debugLog(true)
                .supportMultiProcess(false)
                .build()
            PAGSdk.init(context, pAGInitConfig, object : PAGSdk.PAGInitCallback {
                override fun success() {
                    Log.i("Pangle", "pangle init success")
                }
                override fun fail(code: Int, msg: String) {
                    Log.i("Pangle", "pangle init fail: $code")
                }
            })

            isSdkInitialized = true
            notifyEvent("loaded", "initialize", mediaKey)
        }.onFailure {
            val reason = it.message ?: "sdk init failed"
            AdEventLogger.failed("initialize", mediaKey, reason)
            onAdEventCallback?.invoke("failed", "initialize", reason)
        }
    }

    @Synchronized
    fun bannerView(context: Context): View? {
        val adUnitId = AppConfig.getAdUnit(context, "banner_320x100") ?: NapSspConfig.AD_UNIT_IDS["banner_320x100"] ?: return null
        val format = "banner"
        destroyAndRemoveAd(format)
        return runCatching {
            val adView = AdView(context)
            adView.setAdInfo(AdInfo.Builder(adUnitId).setIsUseMediation(true).build())
            adView.setAlwaysShowAdView(true)
            adView.setAdViewListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) {
                    notifyEvent("loaded", format, adUnitId)
                    adView.showAd()
                }
                override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$errorCode] $errorMsg")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    if (event == AdEvent.DISPLAYED) notifyEvent("displayed", format, adUnitId)
                    if (event == AdEvent.CLICK) notifyEvent("clicked", format, adUnitId)
                }
            })
            activeAds[format] = adView
            adView.loadAd()
            adView
        }.getOrNull()
    }

    @Synchronized
    fun nativeView(context: Context): View? {
        val adUnitId = AppConfig.getAdUnit(context, "native") ?: NapSspConfig.AD_UNIT_IDS["native"] ?: return null
        val format = "native"
        destroyAndRemoveAd(format)
        val layouts = listOf(R.layout.admixer_item_320x480, R.layout.admixer_item_300x250, R.layout.admixer_item_320x100, R.layout.admixer_item_320x50)
        val selectedLayout = layouts[Random.nextInt(layouts.size)]
        return runCatching {
            val nativeView = NativeAdView(context)
            val adInfoBuilder = AdInfo.Builder(adUnitId).setIsUseMediation(true)
            
            // 레이아웃 파일에 존재하는 ID만 동적으로 찾아서 매핑 (크래시 방지)
            val adViewIds = mutableMapOf<String, Int>()
            adViewIds["nativeLayout"] = layoutId // AdFit 어댑터가 요구하는 레이아웃 키 추가
            val viewFields = listOf("iv_icon", "tv_title", "tv_adv", "tv_desc", "iv_main", "btn_cta")
            viewFields.forEach { name ->
                val id = context.resources.getIdentifier(name, "id", context.packageName)
                if (id != 0) adViewIds[name] = id
            }
            
            adInfoBuilder.setViewIds(AdMixer.ADAPTER_ADFIT, adViewIds)
            adInfoBuilder.setViewIds(AdMixer.ADAPTER_PANGLE, adViewIds)
            adInfoBuilder.setViewIds(AdMixer.ADAPTER_ADMANAGER, adViewIds)
            val adInfo = adInfoBuilder.build()

            val viewBinder = NativeAdViewBinder.Builder(layoutId)
                .setIconImageId(R.id.iv_icon).setTitleId(R.id.tv_title)
                .setAdvertiserId(R.id.tv_adv).setDescriptionId(R.id.tv_desc)
                .setMainViewId(R.id.iv_main).setCtaId(R.id.btn_cta).build()
            
            nativeView.setAdInfo(adInfo)
            nativeView.setViewBinder(viewBinder)
            nativeView.setAdViewListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) = notifyEvent("loaded", format, adUnitId)
                override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$errorCode] $errorMsg")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    if (event == AdEvent.DISPLAYED) notifyEvent("displayed", format, adUnitId)
                    if (event == AdEvent.CLICK) notifyEvent("clicked", format, adUnitId)
                }
            })
            activeAds[format] = nativeView
            nativeView.loadNativeAd()
            nativeView
        }.getOrNull()
    }

    @Synchronized
    fun videoView(context: Context): View? {
        val adUnitId = AppConfig.getAdUnit(context, "outstream_video") ?: NapSspConfig.AD_UNIT_IDS["outstream_video"] ?: return null
        val format = "video"
        destroyAndRemoveAd(format)
        return runCatching {
            val videoView = VideoAdView(context)
            videoView.setAdInfo(AdInfo.Builder(adUnitId).setIsUseMediation(true).isRetry(false).build())
            videoView.setAdViewListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) = notifyEvent("loaded", format, adUnitId)
                override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$errorCode] $errorMsg")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    when (event) {
                        AdEvent.DISPLAYED  -> notifyEvent("displayed", format, adUnitId)
                        AdEvent.CLICK      -> notifyEvent("clicked", format, adUnitId)
                        AdEvent.SKIPPED    -> notifyEvent("skipped", format, adUnitId)
                        AdEvent.COMPLETION -> notifyEvent("completed", format, adUnitId)
                        else -> {}
                    }
                }
            })
            activeAds[format] = videoView
            videoView.loadAd()
            videoView
        }.getOrNull()
    }

    @Synchronized
    fun rewardVideoView(context: Context) {
        val adUnitId = AppConfig.getAdUnit(context, "reward_video") ?: NapSspConfig.AD_UNIT_IDS["reward_video"] ?: return
        val format = "rewardVideo"
        destroyAndRemoveAd(format)
        runCatching {
            val rewardAd = RewardInterstitialVideoAd(context)
            val params = mapOf(
                "useid" to "nas",
                "name" to "hdragon",
                "phone" to "010-1111-1111"
            )
            rewardAd.setAdInfo(
                AdInfo.Builder(adUnitId)
                    .setCustomParams(params)
                    .setMute(true)
                    .setIsUseMediation(true)
                    .build()
            )
            rewardAd.setListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) {
                    notifyEvent("loaded", format, adUnitId)
                    rewardAd.showRewardVideoAd()
                }
                override fun onFailedToReceiveAd(v: Any?, a: String?, e: Int, m: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$e] $m")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    when (event) {
                        AdEvent.DISPLAYED    -> notifyEvent("displayed", format, adUnitId)
                        AdEvent.CLICK        -> notifyEvent("clicked", format, adUnitId)
                        AdEvent.EARNEDREWARD -> onAdEventCallback?.invoke("rewarded", format, adUnitId)
                        AdEvent.CLOSE        -> notifyEvent("closed", format, adUnitId)
                        AdEvent.SKIPPED      -> notifyEvent("skipped", format, adUnitId)
                        AdEvent.COMPLETION   -> notifyEvent("completed", format, adUnitId)
                        else -> {}
                    }
                }
            })
            activeAds[format] = rewardAd
            rewardAd.loadRewardVideoAd()
        }
    }

    @Synchronized
    fun interstitialVideoView(context: Context) {
        val adUnitId = AppConfig.getAdUnit(context, "interstitial_320x480") ?: NapSspConfig.AD_UNIT_IDS["interstitial_320x480"] ?: return
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
                    notifyEvent("loaded", format, adUnitId)
                    interstitialAd.showInterstitialVideoAd()
                }
                override fun onFailedToReceiveAd(v: Any?, a: String?, e: Int, m: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$e] $m")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    when (event) {
                        AdEvent.DISPLAYED  -> notifyEvent("displayed", format, adUnitId)
                        AdEvent.CLICK      -> notifyEvent("clicked", format, adUnitId)
                        AdEvent.CLOSE      -> notifyEvent("closed", format, adUnitId)
                        AdEvent.SKIPPED    -> notifyEvent("skipped", format, adUnitId)
                        AdEvent.COMPLETION -> notifyEvent("completed", format, adUnitId)
                        else -> {}
                    }
                }
            })
            activeAds[format] = interstitialAd
            interstitialAd.loadInterstitialVideoAd()
        }
    }

    @Synchronized
    fun interstitialBannerView(context: Context) {
        val adUnitId = AppConfig.getAdUnit(context, "interstitial_320x480_f") ?: NapSspConfig.AD_UNIT_IDS["interstitial_320x480_f"] ?: return
        val format = "interstitialBanner"
        destroyAndRemoveAd(format)
        runCatching {
            val interstitialAd = InterstitialAd(context)
            val adConfig = PopupInterstitialAdOption().apply {
                setDisableBackKey(false)
                setButtonLeft("광고종료", "#234234")
                setCountDown(0, 5) // gauge 타입, 5초
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
                    notifyEvent("loaded", format, adUnitId)
                    interstitialAd.showInterstitial()
                }
                override fun onFailedToReceiveAd(v: Any?, a: String?, e: Int, m: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$e] $m")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    when (event) {
                        AdEvent.DISPLAYED   -> notifyEvent("displayed", format, adUnitId)
                        AdEvent.CLICK       -> notifyEvent("clicked", format, adUnitId)
                        AdEvent.CLOSE       -> notifyEvent("closed", format, adUnitId)
                        AdEvent.LEFT_CLICK  -> notifyEvent("clicked", format, adUnitId)
                        AdEvent.RIGHT_CLICK -> notifyEvent("clicked", format, adUnitId)
                        else -> {}
                    }
                }
            })
            activeAds[format] = interstitialAd
            interstitialAd.startInterstitial()
        }
    }

    fun clearAllAds() {
        activeAds.keys.toList().forEach { destroyAndRemoveAd(it) }
    }

    fun resumeAll() {
        activeAds.values.forEach { ad ->
            if (ad is AdView) ad.onResume()
            else if (ad is NativeAdView) ad.onResume()
            else if (ad is VideoAdView) ad.onResume()
        }
    }

    fun pauseAll() {
        activeAds.values.forEach { ad ->
            if (ad is AdView) ad.onPause()
            else if (ad is NativeAdView) ad.onPause()
            else if (ad is VideoAdView) ad.onPause()
        }
    }
}