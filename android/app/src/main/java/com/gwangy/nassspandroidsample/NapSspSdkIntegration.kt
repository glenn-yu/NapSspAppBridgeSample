package com.gwangy.nassspandroidsample

import android.content.Context
import android.view.View
import com.nasmedia.admixerssp.common.AdMixer
import com.nasmedia.admixerssp.ads.*
import com.nasmedia.admixerssp.listener.*

object NapSspSdkIntegration {
    
    // Bridge callback to notify WebView
    var onAdEventCallback: ((String, String, String) -> Unit)? = null

    private fun notifyEvent(event: String, format: String, id: String) {
        when(event) {
            "loaded" -> AdEventLogger.loaded(format, id)
            "displayed" -> AdEventLogger.displayed(format, id)
            "clicked" -> AdEventLogger.clicked(format, id)
            "failed" -> {} // Handled separately with reason
        }
        onAdEventCallback?.invoke(event, format, id)
    }

    fun initialize(context: Context) {
        AdEventLogger.request("initialize", NapSspConfig.MEDIA_KEY)
        runCatching {
            // AdMixer initialization
            AdMixer.getInstance().initialize(
                context, 
                NapSspConfig.MEDIA_KEY, 
                NapSspConfig.AD_UNIT_IDS.values.toList()
            )
            notifyEvent("loaded", "initialize", NapSspConfig.MEDIA_KEY)
        }.onFailure {
            val reason = it.message ?: "sdk init failed"
            AdEventLogger.failed("initialize", NapSspConfig.MEDIA_KEY, reason)
            onAdEventCallback?.invoke("failed", "initialize", reason)
        }
    }

    fun bannerView(context: Context): View? {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["banner_320x100"] ?: return null
        val format = "banner"
        AdEventLogger.request(format, adUnitId)
        
        return runCatching {
            val adInfo = AdInfo.Builder(adUnitId)
                .setIsUseMediation(true)
                .build()
            val adView = AdView(context)
            adView.setAdInfo(adInfo)
            
            adView.setAdListener(object : AdListener {
                override fun onAdLoaded() = notifyEvent("loaded", format, adUnitId)
                override fun onAdReceived(p0: String?) = notifyEvent("displayed", format, adUnitId)
                override fun onAdClicked() = notifyEvent("clicked", format, adUnitId)
                override fun onAdFailedToLoad(error: AdError) {
                    AdEventLogger.failed(format, adUnitId, error.toString())
                    onAdEventCallback?.invoke("failed", format, error.toString())
                }
                override fun onAdClosed() {}
                override fun onAdLeftApplication() {}
            })
            
            adView.loadAd()
            adView
        }.getOrElse {
            AdEventLogger.failed(format, adUnitId, it.message ?: "banner setup failed")
            null
        }
    }

    fun nativeView(context: Context): View? {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["native"] ?: return null
        val format = "native"
        AdEventLogger.request(format, adUnitId)
        
        return runCatching {
            val adInfo = AdInfo.Builder(adUnitId)
                .setIsUseMediation(true)
                .build()
            val nativeView = NativeAdView(context)
            
            nativeView.setAdListener(object : NativeAdListener {
                override fun onAdLoaded() = notifyEvent("loaded", format, adUnitId)
                override fun onAdReceived(p0: String?) = notifyEvent("displayed", format, adUnitId)
                override fun onAdClicked() = notifyEvent("clicked", format, adUnitId)
                override fun onAdFailedToLoad(error: AdError) {
                    AdEventLogger.failed(format, adUnitId, error.toString())
                    onAdEventCallback?.invoke("failed", format, error.toString())
                }
            })

            nativeView.setAdInfo(adInfo, context)
            nativeView.loadNativeAd()
            nativeView
        }.getOrElse {
            AdEventLogger.failed(format, adUnitId, it.message ?: "native setup failed")
            null
        }
    }

    fun videoView(context: Context): View? {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["outstream_video"] ?: return null
        val format = "video"
        AdEventLogger.request(format, adUnitId)
        
        return runCatching {
            val adInfo = AdInfo.Builder(adUnitId)
                .setIsUseMediation(true)
                .build()
            val videoView = VideoAdView(context)
            
            videoView.setAdListener(object : VideoAdListener {
                override fun onAdLoaded() = notifyEvent("loaded", format, adUnitId)
                override fun onAdReceived(p0: String?) = notifyEvent("displayed", format, adUnitId)
                override fun onAdClicked() = notifyEvent("clicked", format, adUnitId)
                override fun onAdFailedToLoad(error: AdError) {
                    AdEventLogger.failed(format, adUnitId, error.toString())
                    onAdEventCallback?.invoke("failed", format, error.toString())
                }
                override fun onAdStarted() {}
                override fun onAdCompleted() {}
            })

            videoView.setAdInfo(adInfo, context)
            videoView.loadAd()
            videoView
        }.getOrElse {
            AdEventLogger.failed(format, adUnitId, it.message ?: "video setup failed")
            null
        }
    }

    fun rewardVideoView(context: Context): View? {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["reward_video"] ?: return null
        val format = "rewardVideo"
        AdEventLogger.request(format, adUnitId)
        
        return runCatching {
            val adInfo = AdInfo.Builder(adUnitId)
                .setIsUseMediation(true)
                .build()
            val rewardAd = RewardInterstitialVideoAd(context)
            
            rewardAd.setAdListener(object : RewardVideoAdListener {
                override fun onAdLoaded() = notifyEvent("loaded", format, adUnitId)
                override fun onAdReceived(p0: String?) = notifyEvent("displayed", format, adUnitId)
                override fun onAdClicked() = notifyEvent("clicked", format, adUnitId)
                override fun onAdFailedToLoad(error: AdError) {
                    AdEventLogger.failed(format, adUnitId, error.toString())
                    onAdEventCallback?.invoke("failed", format, error.toString())
                }
                override fun onAdReward(p0: String?, p1: Int) {
                    onAdEventCallback?.invoke("rewarded", format, "$p0:$p1")
                }
                override fun onAdClosed() {}
                override fun onAdCompleted() {}
                override fun onAdStarted() {}
            })

            rewardAd.setAdInfo(adInfo, context)
            rewardAd.loadRewardVideoAd()
            null
        }.getOrElse {
            AdEventLogger.failed(format, adUnitId, it.message ?: "reward setup failed")
            null
        }
    }

    fun interstitialVideoView(context: Context): View? {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["interstitial_320x480"] ?: return null
        val format = "interstitialVideo"
        AdEventLogger.request(format, adUnitId)
        
        return runCatching {
            val adInfo = AdInfo.Builder(adUnitId)
                .setIsUseMediation(true)
                .build()
            val interstitialAd = InterstitialVideoAd(context)
            
            interstitialAd.setAdListener(object : InterstitialVideoAdListener {
                override fun onAdLoaded() = notifyEvent("loaded", format, adUnitId)
                override fun onAdReceived(p0: String?) = notifyEvent("displayed", format, adUnitId)
                override fun onAdClicked() = notifyEvent("clicked", format, adUnitId)
                override fun onAdFailedToLoad(error: AdError) {
                    AdEventLogger.failed(format, adUnitId, error.toString())
                    onAdEventCallback?.invoke("failed", format, error.toString())
                }
                override fun onAdClosed() {}
                override fun onAdCompleted() {}
                override fun onAdStarted() {}
            })

            interstitialAd.setAdInfo(adInfo, context)
            interstitialAd.loadInterstitialVideoAd()
            null
        }.getOrElse {
            AdEventLogger.failed(format, adUnitId, it.message ?: "interstitial setup failed")
            null
        }
    }
}
