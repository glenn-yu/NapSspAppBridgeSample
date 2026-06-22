package com.gwangy.nassspandroidsample.bridge

import android.app.Activity
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
                is AMMBannerView -> { ad.onPause(); ad.stop() }
                is AMMNativeAdView -> { ad.onPause(); ad.stop() }
                is AMMVideoView -> { ad.onPause(); ad.stop() }
                is AMMInterstitial -> ad.stop()
                is AMMVideoInterstitial -> ad.stop()
                is AMMRewardVideo -> ad.stop()
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
            
            // Mediation adapter is automatically registered when the dependency is present.

            // Pangle 초기화 (Pangle 사용 시 필수)
            val pAGInitConfig = PAGConfig.Builder()
                .appId(NapSspConfig.PANGLE_APP_ID)
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
    fun bannerView(context: Context, customAdUnitId: String? = null): View? {
        val adUnitId = customAdUnitId ?: AppConfig.getAdUnit(context, "banner_320x100") ?: NapSspConfig.AD_UNIT_IDS["banner_320x100"] ?: return null
        val format = "banner"
        destroyAndRemoveAd(format)
        return runCatching {
            val adView = AMMBannerView(context)
            adView.setAdInfo(AdInfo.Builder(adUnitId).build())
            
            // [주의] AdListener는 AMMBannerView가 WeakReference로 보유하므로 GC 방지를 위해 별도 보관을 하거나 SDK 스펙에 따라 관리해야 하지만,
            // activeAds 맵에 adView를 보관하고 있으므로 adListener 익명 객체는 뷰 인스턴스가 존재하는 동안 유지됩니다.
            adView.setAdViewListener(object : AdListener() {
                override fun onReceivedAd(adapterName: String, adView: Any) {
                    notifyEvent("loaded", format, adUnitId)
                }
                override fun onFailedToReceiveAd(adView: Any?, adapterName: String, errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$errorCode] $errorMsg")
                }
                override fun onAdDisplayed() {
                    notifyEvent("displayed", format, adUnitId)
                }
                override fun onAdClicked() {
                    notifyEvent("clicked", format, adUnitId)
                }
            })
            activeAds[format] = adView
            adView.loadAd()
            adView
        }.getOrNull()
    }

    @Synchronized
    fun nativeView(context: Context, customAdUnitId: String? = null): View? {
        val adUnitId = customAdUnitId ?: AppConfig.getAdUnit(context, "native") ?: NapSspConfig.AD_UNIT_IDS["native"] ?: return null
        val format = "native"
        destroyAndRemoveAd(format)
        val layouts = listOf(R.layout.admixer_item_320x480, R.layout.admixer_item_300x250, R.layout.admixer_item_320x100, R.layout.admixer_item_320x50)
        val selectedLayout = layouts[Random.nextInt(layouts.size)]
        return runCatching {
            val nativeAdView = AMMNativeAdView(context)
            
            // Configure native ad view binder.
            val viewBinder = NativeAdViewBinder.Builder(selectedLayout)
                .setIconImageId(R.id.nap_mx_iv_icon)
                .setTitleId(R.id.nap_mx_tv_title)
                .setAdvertiserId(R.id.nap_mx_tv_adv)
                .setDescriptionId(R.id.nap_mx_tv_desc)
                .setMainViewId(R.id.nap_mx_iv_main)
                .setCtaId(R.id.nap_mx_btn_cta)
                .build()
            
            val adInfo = AdInfo.Builder(adUnitId)
                .setAdViewBinder(viewBinder)
                .build()
            
            nativeAdView.setAdInfo(adInfo)
            nativeAdView.setAdViewListener(object : AdListener() {
                override fun onReceivedAd(adapterName: String, adView: Any) {
                    notifyEvent("loaded", format, adUnitId)
                }
                override fun onFailedToReceiveAd(adView: Any?, adapterName: String, errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$errorCode] $errorMsg")
                }
                override fun onAdDisplayed() {
                    notifyEvent("displayed", format, adUnitId)
                }
                override fun onAdClicked() {
                    notifyEvent("clicked", format, adUnitId)
                }
            })
            activeAds[format] = nativeAdView
            nativeAdView.loadNativeAd()
            nativeAdView
        }.getOrNull()
    }

    @Synchronized
    fun videoView(context: Context, customAdUnitId: String? = null): View? {
        val adUnitId = customAdUnitId ?: AppConfig.getAdUnit(context, "outstream_video") ?: NapSspConfig.AD_UNIT_IDS["outstream_video"] ?: return null
        val format = "video"
        destroyAndRemoveAd(format)
        return runCatching {
            val videoView = AMMVideoView(context)
            videoView.setAdInfo(AdInfo.Builder(adUnitId).build())
            videoView.setAdViewListener(object : AdListener() {
                override fun onReceivedAd(adapterName: String, adView: Any) {
                    notifyEvent("loaded", format, adUnitId)
                }
                override fun onFailedToReceiveAd(adView: Any?, adapterName: String, errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$errorCode] $errorMsg")
                }
                override fun onAdDisplayed() {
                    notifyEvent("displayed", format, adUnitId)
                }
                override fun onAdClicked() {
                    notifyEvent("clicked", format, adUnitId)
                }
                override fun onAdCompleted() {
                    notifyEvent("completed", format, adUnitId)
                }
                override fun onAdSkipped() {
                    notifyEvent("skipped", format, adUnitId)
                }
            })
            activeAds[format] = videoView
            videoView.loadAd()
            videoView
        }.getOrNull()
    }

    @Synchronized
    fun rewardVideoView(context: Context, customAdUnitId: String? = null) {
        val adUnitId = customAdUnitId ?: AppConfig.getAdUnit(context, "reward_video") ?: NapSspConfig.AD_UNIT_IDS["reward_video"] ?: return
        val format = "rewardVideo"
        destroyAndRemoveAd(format)
        runCatching {
            val params = mapOf(
                "useid" to "nas",
                "name" to "hdragon",
                "phone" to "010-1111-1111"
            )
            val adInfo = AdInfo.Builder(adUnitId)
                .setCustomParams(params)
                .setMute(true)
                .build()

            // Load Reward Video Ad
            AMMRewardVideo.loadAd(context, adInfo, object : AMMRewardVideoLoadCallback() {
                override fun onSuccessLoadReward(adapterName: String, ad: AMMRewardVideo) {
                    notifyEvent("loaded", format, adUnitId)
                    activeAds[format] = ad
                    ad.setFullScreenContentCallback(object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            notifyEvent("displayed", format, adUnitId)
                        }
                        override fun onAdClicked() {
                            notifyEvent("clicked", format, adUnitId)
                        }
                        override fun onAdCompleted() {
                            notifyEvent("completed", format, adUnitId)
                        }
                        override fun onAdDismissedFullScreenContent() {
                            notifyEvent("closed", format, adUnitId)
                            destroyAndRemoveAd(format)
                        }
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            onAdEventCallback?.invoke("failed", format, "${adError.code} / ${adError.message}")
                            destroyAndRemoveAd(format)
                        }
                    })
                    val activity = context as? Activity ?: return
                    ad.show(activity, object : OnUserEarnedRewardListener {
                        override fun onUserEarnedReward() {
                            onAdEventCallback?.invoke("rewarded", format, adUnitId)
                        }
                    })
                }

                override fun onFailLoadReward(errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$errorCode] $errorMsg")
                }
            })
        }
    }

    @Synchronized
    fun interstitialVideoView(context: Context, customAdUnitId: String? = null) {
        val adUnitId = customAdUnitId ?: AppConfig.getAdUnit(context, "interstitial_320x480") ?: NapSspConfig.AD_UNIT_IDS["interstitial_320x480"] ?: return
        val format = "interstitialVideo"
        destroyAndRemoveAd(format)
        runCatching {
            val adInfo = AdInfo.Builder(adUnitId)
                .build()

            // Load Video Interstitial Ad
            AMMVideoInterstitial.loadAd(context, adInfo, object : AMMVideoInterstitialLoadCallback() {
                override fun onSuccessLoadVideoInterstitial(adapterName: String, ad: AMMVideoInterstitial) {
                    notifyEvent("loaded", format, adUnitId)
                    activeAds[format] = ad
                    ad.setFullScreenContentCallback(object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            notifyEvent("displayed", format, adUnitId)
                        }
                        override fun onAdClicked() {
                            notifyEvent("clicked", format, adUnitId)
                        }
                        override fun onAdCompleted() {
                            notifyEvent("completed", format, adUnitId)
                        }
                        override fun onAdDismissedFullScreenContent() {
                            notifyEvent("closed", format, adUnitId)
                            destroyAndRemoveAd(format)
                        }
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            onAdEventCallback?.invoke("failed", format, "${adError.code} / ${adError.message}")
                            destroyAndRemoveAd(format)
                        }
                    })
                    val activity = context as? Activity ?: return
                    ad.showAd(activity)
                }

                override fun onFailLoadVideoInterstitial(errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$errorCode] $errorMsg")
                }
            })
        }
    }

    @Synchronized
    fun interstitialBannerView(context: Context, customAdUnitId: String? = null) {
        val adUnitId = customAdUnitId ?: AppConfig.getAdUnit(context, "interstitial_320x480_f") ?: NapSspConfig.AD_UNIT_IDS["interstitial_320x480_f"] ?: return
        val format = "interstitialBanner"
        destroyAndRemoveAd(format)
        runCatching {
            val adInfo = AdInfo.Builder(adUnitId).build()
            // Load Interstitial Banner Ad
            AMMInterstitial.loadAd(context, adInfo, object : AMMInterstitialLoadCallback() {
                override fun onSuccessLoadInterstitial(adapterName: String, ad: AMMInterstitial) {
                    notifyEvent("loaded", format, adUnitId)
                    activeAds[format] = ad
                    ad.setFullScreenContentCallback(object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            notifyEvent("displayed", format, adUnitId)
                        }
                        override fun onAdClicked() {
                            notifyEvent("clicked", format, adUnitId)
                        }
                        override fun onAdDismissedFullScreenContent() {
                            notifyEvent("closed", format, adUnitId)
                            destroyAndRemoveAd(format)
                        }
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            onAdEventCallback?.invoke("failed", format, "${adError.code} / ${adError.message}")
                            destroyAndRemoveAd(format)
                        }
                    })
                    val activity = context as? Activity ?: return
                    ad.showAd(activity)
                }

                override fun onFailLoadInterstitial(errorCode: Int, errorMsg: String?) {
                    onAdEventCallback?.invoke("failed", format, "[$errorCode] $errorMsg")
                }
            })
        }
    }

    fun clearAllAds() {
        activeAds.keys.toList().forEach { destroyAndRemoveAd(it) }
    }

    fun resumeAll() {
        activeAds.values.forEach { ad ->
            if (ad is AMMBannerView) ad.onResume()
            else if (ad is AMMNativeAdView) ad.onResume()
            else if (ad is AMMVideoView) ad.onResume()
        }
    }

    fun pauseAll() {
        activeAds.values.forEach { ad ->
            if (ad is AMMBannerView) ad.onPause()
            else if (ad is AMMNativeAdView) ad.onPause()
            else if (ad is AMMVideoView) ad.onPause()
        }
    }
}