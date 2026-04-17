package com.gwangy.nassspandroidsample.bridge

import android.content.Context
import android.view.View
import android.view.ViewGroup
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

        val mediaKey = NapSspConfig.mediaKey(context)
        val adUnitIds = NapSspConfig.adUnitIds(context)

        AdEventLogger.request("initialize", mediaKey)
        runCatching {
            AdMixerLog.setLogLevel(AdMixerLog.LogLevel.DEBUG)
            AdMixer.getInstance().initialize(
                context,
                mediaKey,
                ArrayList(adUnitIds.values.toList())
            )
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
<<<<<<< HEAD:android/app/src/main/java/com/gwangy/nassspandroidsample/bridge/NapSspSdkIntegration.kt
        val adUnitId = AppConfig.getAdUnit(context, "banner_320x100") ?: NapSspConfig.AD_UNIT_IDS["banner_320x100"] ?: return null
=======
        val adUnitId = NapSspConfig.adUnitIds(context)["banner_320x100"] ?: return null
>>>>>>> 9e7a30c (fix: make sample beginner-friendly config flow):android/app/src/main/java/com/gwangy/nassspandroidsample/NapSspSdkIntegration.kt
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
<<<<<<< HEAD:android/app/src/main/java/com/gwangy/nassspandroidsample/bridge/NapSspSdkIntegration.kt
        val adUnitId = AppConfig.getAdUnit(context, "native") ?: NapSspConfig.AD_UNIT_IDS["native"] ?: return null
=======
        val adUnitId = NapSspConfig.adUnitIds(context)["native"] ?: return null
>>>>>>> 9e7a30c (fix: make sample beginner-friendly config flow):android/app/src/main/java/com/gwangy/nassspandroidsample/NapSspSdkIntegration.kt
        val format = "native"
        destroyAndRemoveAd(format)
        val layouts = listOf(R.layout.admixer_item_320x480, R.layout.admixer_item_300x250, R.layout.admixer_item_320x100, R.layout.admixer_item_320x50)
        val selectedLayout = layouts[Random.nextInt(layouts.size)]
        return runCatching {
            val nativeView = NativeAdView(context)
            val adInfo = AdInfo.Builder(adUnitId).setIsUseMediation(true).build()
            val viewBinder = NativeAdViewBinder.Builder(selectedLayout)
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
<<<<<<< HEAD:android/app/src/main/java/com/gwangy/nassspandroidsample/bridge/NapSspSdkIntegration.kt
        val adUnitId = AppConfig.getAdUnit(context, "outstream_video") ?: NapSspConfig.AD_UNIT_IDS["outstream_video"] ?: return null
=======
        val adUnitId = NapSspConfig.adUnitIds(context)["outstream_video"] ?: return null
>>>>>>> 9e7a30c (fix: make sample beginner-friendly config flow):android/app/src/main/java/com/gwangy/nassspandroidsample/NapSspSdkIntegration.kt
        val format = "video"
        destroyAndRemoveAd(format)
        return runCatching {
            val videoView = VideoAdView(context)
            videoView.setAdInfo(AdInfo.Builder(adUnitId).setIsUseMediation(true).build())
            videoView.setAdViewListener(object : AdListener {
                override fun onReceivedAd(adapterName: String?, view: Any?) = notifyEvent("loaded", format, adUnitId)
                override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$errorCode] $errorMsg")
                }
                override fun onEventAd(view: Any?, event: AdEvent?) {
                    if (event == AdEvent.DISPLAYED) notifyEvent("displayed", format, adUnitId)
                    if (event == AdEvent.CLICK) notifyEvent("clicked", format, adUnitId)
                }
            })
            activeAds[format] = videoView
            videoView.loadAd()
            videoView
        }.getOrNull()
    }

    @Synchronized
    fun rewardVideoView(context: Context) {
<<<<<<< HEAD:android/app/src/main/java/com/gwangy/nassspandroidsample/bridge/NapSspSdkIntegration.kt
        val adUnitId = AppConfig.getAdUnit(context, "reward_video") ?: NapSspConfig.AD_UNIT_IDS["reward_video"] ?: return
=======
        val adUnitId = NapSspConfig.adUnitIds(context)["reward_video"] ?: return
>>>>>>> 9e7a30c (fix: make sample beginner-friendly config flow):android/app/src/main/java/com/gwangy/nassspandroidsample/NapSspSdkIntegration.kt
        val format = "rewardVideo"
        destroyAndRemoveAd(format)
        runCatching {
            val rewardAd = RewardInterstitialVideoAd(context)
            rewardAd.setAdInfo(AdInfo.Builder(adUnitId).setIsUseMediation(true).build())
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
                        AdEvent.DISPLAYED -> notifyEvent("displayed", format, adUnitId)
                        AdEvent.CLICK -> notifyEvent("clicked", format, adUnitId)
                        AdEvent.EARNEDREWARD -> onAdEventCallback?.invoke("rewarded", format, adUnitId)
                        AdEvent.CLOSE -> notifyEvent("closed", format, adUnitId)
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
<<<<<<< HEAD:android/app/src/main/java/com/gwangy/nassspandroidsample/bridge/NapSspSdkIntegration.kt
        val adUnitId = AppConfig.getAdUnit(context, "interstitial_320x480") ?: NapSspConfig.AD_UNIT_IDS["interstitial_320x480"] ?: return
=======
        val adUnitId = NapSspConfig.adUnitIds(context)["interstitial_320x480"] ?: return
>>>>>>> 9e7a30c (fix: make sample beginner-friendly config flow):android/app/src/main/java/com/gwangy/nassspandroidsample/NapSspSdkIntegration.kt
        val format = "interstitialVideo"
        destroyAndRemoveAd(format)
        runCatching {
            val interstitialAd = InterstitialVideoAd(context)
            interstitialAd.setAdInfo(AdInfo.Builder(adUnitId).setIsUseMediation(true).build())
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
                        AdEvent.DISPLAYED -> notifyEvent("displayed", format, adUnitId)
                        AdEvent.CLICK -> notifyEvent("clicked", format, adUnitId)
                        AdEvent.CLOSE -> notifyEvent("closed", format, adUnitId)
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
<<<<<<< HEAD:android/app/src/main/java/com/gwangy/nassspandroidsample/bridge/NapSspSdkIntegration.kt
        val adUnitId = AppConfig.getAdUnit(context, "interstitial_320x480_f") ?: NapSspConfig.AD_UNIT_IDS["interstitial_320x480_f"] ?: return
=======
        val adUnitId = NapSspConfig.adUnitIds(context)["interstitial_320x480_f"] ?: return
>>>>>>> 9e7a30c (fix: make sample beginner-friendly config flow):android/app/src/main/java/com/gwangy/nassspandroidsample/NapSspSdkIntegration.kt
        val format = "interstitialBanner"
        destroyAndRemoveAd(format)
        runCatching {
            val interstitialAd = InterstitialAd(context)
            interstitialAd.setAdInfo(AdInfo.Builder(adUnitId).interstitialAdType(AdInfo.InterstitialAdType.Basic).build())
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
                        AdEvent.DISPLAYED -> notifyEvent("displayed", format, adUnitId)
                        AdEvent.CLICK -> notifyEvent("clicked", format, adUnitId)
                        AdEvent.CLOSE -> notifyEvent("closed", format, adUnitId)
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
