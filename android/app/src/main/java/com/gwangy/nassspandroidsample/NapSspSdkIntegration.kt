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

    // [최종 해결책] 기존 광고를 완전히 파괴하고 제거 (사용자 제안 GONE 추가)
    private fun destroyAndRemoveAd(format: String) {
        activeAds[format]?.let { ad ->
            // 1. 가시성 제거 (SDK 렌더링 중단 유도)
            if (ad is View) {
                ad.visibility = View.GONE
            }
            
            // 2. 파괴 호출 (리스너 강제 해제)
            when (ad) {
                is AdView -> ad.onDestroy()
                is NativeAdView -> ad.onDestroy()
                is VideoAdView -> ad.onDestroy()
                is InterstitialAd -> ad.stopInterstitial()
                is InterstitialVideoAd -> ad.stopInterstitialVideoAd()
                is RewardInterstitialVideoAd -> ad.stopRewardVideoAd()
            }

            // 3. 부모 레이아웃에서 제거
            if (ad is View) {
                (ad.parent as? ViewGroup)?.removeView(ad)
            }
        }
        // 4. 참조 제거
        activeAds.remove(format)
    }

    fun initialize(context: Context) {
        if (isSdkInitialized) return
        AdMixerLog.setLogLevel(AdMixerLog.LogLevel.DEBUG)
        AdMixer.getInstance().initialize(context, NapSspConfig.MEDIA_KEY, ArrayList(NapSspConfig.AD_UNIT_IDS.values.toList()))
        isSdkInitialized = true
        notifyEvent("loaded", "initialize", NapSspConfig.MEDIA_KEY)
    }

    fun bannerView(context: Context): View? {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["banner_320x100"] ?: return null
        val format = "banner"
        
        destroyAndRemoveAd(format) // 즉시 파괴 및 null화

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

    fun nativeView(context: Context): View? {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["native"] ?: return null
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

    fun videoView(context: Context): View? {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["outstream_video"] ?: return null
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

    fun rewardVideoView(context: Context) {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["reward_video"] ?: return
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

    fun interstitialVideoView(context: Context) {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["interstitial_320x480"] ?: return
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
