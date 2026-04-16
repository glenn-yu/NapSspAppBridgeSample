package com.gwangy.nassspandroidsample

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.nasmedia.admixerssp.ads.*
import com.nasmedia.admixerssp.common.AdMixer
import com.nasmedia.admixerssp.common.AdMixerLog
import com.nasmedia.admixerssp.common.nativeads.NativeAdViewBinder

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

    // Destroy and remove existing ad view/object for the format
    private fun destroyAndRemoveAd(format: String) {
        activeAds.remove(format)?.let { ad ->
            if (ad is View) {
                (ad.parent as? ViewGroup)?.removeView(ad)
            }
            when (ad) {
                is AdView -> runCatching { ad.onDestroy() }
                is NativeAdView -> runCatching { ad.onDestroy() }
                is VideoAdView -> runCatching { ad.onDestroy() }
                is InterstitialAd -> runCatching { ad.stopInterstitial() }
                is InterstitialVideoAd -> runCatching { ad.stopInterstitialVideoAd() }
                is RewardInterstitialVideoAd -> runCatching { ad.stopRewardVideoAd() }
                else -> {}
            }
        }
    }

    // Initialize with optional runtime override via AppConfig
    fun initialize(context: Context) {
        if (isSdkInitialized) return
        val mediaKey = AppConfig.getMediaKey(context) ?: NapSspConfig.MEDIA_KEY
        AdEventLogger.request("initialize", mediaKey)
        runCatching {
            AdMixerLog.setLogLevel(AdMixerLog.LogLevel.DEBUG)
            AdMixer.getInstance().initialize(context, mediaKey, NapSspConfig.AD_UNIT_IDS.values.toList())
            isSdkInitialized = true
            notifyEvent("loaded", "initialize", mediaKey)
        }.onFailure {
            val reason = it.message ?: "sdk init failed"
            AdEventLogger.failed("initialize", mediaKey, reason)
            onAdEventCallback?.invoke("failed", "initialize", reason)
        }
    }

    // Banner
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

    // Native
    fun nativeView(context: Context): View? {
        val adUnitId = AppConfig.getAdUnit(context, "native") ?: NapSspConfig.AD_UNIT_IDS["native"] ?: return null
        val format = "native"
        destroyAndRemoveAd(format)
        return runCatching {
            val nativeView = NativeAdView(context)
            val adInfo = AdInfo.Builder(adUnitId).setIsUseMediation(true).build()
            val viewBinder = NativeAdViewBinder.Builder(R.layout.admixer_item_320x480)
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

    // Video
    fun videoView(context: Context): View? {
        val adUnitId = AppConfig.getAdUnit(context, "outstream_video") ?: NapSspConfig.AD_UNIT_IDS["outstream_video"] ?: return null
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

    // Reward
    fun rewardVideoView(context: Context): View? {
        val adUnitId = AppConfig.getAdUnit(context, "reward_video") ?: NapSspConfig.AD_UNIT_IDS["reward_video"] ?: return null
        val format = "rewardVideo"
        destroyAndRemoveAd(format)
        return runCatching {
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
        }.getOrNull()
    }

    // Interstitial video
    fun interstitialVideoView(context: Context): View? {
        val adUnitId = AppConfig.getAdUnit(context, "interstitial_320x480") ?: NapSspConfig.AD_UNIT_IDS["interstitial_320x480"] ?: return null
        val format = "interstitialVideo"
        destroyAndRemoveAd(format)
        return runCatching {
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
        }.getOrNull()
    }

    // Interstitial banner
    fun interstitialBannerView(context: Context) {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["interstitial_320x480_f"] ?: return
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
        }.getOrNull()
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
    fun rewardVideoView(context: Context): View? {
        val adUnitId = AppConfig.getAdUnit(context, "reward_video") ?: NapSspConfig.AD_UNIT_IDS["reward_video"] ?: return null
>>>>>>> 2e3c92b (feat(android): add runtime AppConfig and use overrides for media key/ad unit ids)
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

<<<<<<< HEAD
    fun interstitialVideoView(context: Context) {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["interstitial_320x480"] ?: return
=======
    fun interstitialVideoView(context: Context): View? {
        val adUnitId = AppConfig.getAdUnit(context, "interstitial_320x480") ?: NapSspConfig.AD_UNIT_IDS["interstitial_320x480"] ?: return null
>>>>>>> 2e3c92b (feat(android): add runtime AppConfig and use overrides for media key/ad unit ids)
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

    fun interstitialBannerView(context: Context) {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["interstitial_320x480_f"] ?: return
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
