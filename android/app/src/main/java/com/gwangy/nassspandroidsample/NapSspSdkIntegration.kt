package com.gwangy.nassspandroidsample

import android.content.Context
import android.view.View

object NapSspSdkIntegration {
    fun initialize(context: Context) {
        AdEventLogger.request("initialize", NapSspConfig.MEDIA_KEY)
        runCatching {
            val adMixerClass = Class.forName("com.nasmedia.admixer.core.AdMixer")
            val getInstance = adMixerClass.getMethod("getInstance")
            val instance = getInstance.invoke(null)
            adMixerClass.getMethod("initialize", Context::class.java, String::class.java, List::class.java)
                .invoke(instance, context, NapSspConfig.MEDIA_KEY, NapSspConfig.AD_UNIT_IDS.values.toList())
            AdEventLogger.loaded("initialize", NapSspConfig.MEDIA_KEY)
        }.onFailure {
            AdEventLogger.failed("initialize", NapSspConfig.MEDIA_KEY, it.message ?: "sdk init failed")
        }
    }

    fun bannerView(context: Context): View? {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["banner_320x100"] ?: return null
        AdEventLogger.request("banner", adUnitId)
        return runCatching {
            val adInfoClass = Class.forName("com.nasmedia.admixer.ads.AdInfo")
            val adViewClass = Class.forName("com.nasmedia.admixer.ads.AdView")
            val builder = adInfoClass.getMethod("builder", String::class.java).invoke(null, adUnitId)
            builder.javaClass.getMethod("setIsUseMediation", Boolean::class.javaPrimitiveType).invoke(builder, true)
            val adInfo = builder.javaClass.getMethod("build").invoke(builder)
            val adView = adViewClass.getConstructor(Context::class.java).newInstance(context)
            adViewClass.getMethod("setAdInfo", adInfoClass).invoke(adView, adInfo)
            adViewClass.getMethod("loadAd").invoke(adView)
            AdEventLogger.loaded("banner", adUnitId)
            AdEventLogger.displayed("banner", adUnitId)
            adView as View
        }.getOrElse {
            AdEventLogger.failed("banner", adUnitId, it.message ?: "banner failed")
            null
        }
    }

    fun nativeView(context: Context): View? {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["native"] ?: return null
        AdEventLogger.request("native", adUnitId)
        return runCatching {
            val adInfoClass = Class.forName("com.nasmedia.admixer.ads.AdInfo")
            val nativeClass = Class.forName("com.nasmedia.admixer.ads.NativeAdView")
            val builder = adInfoClass.getMethod("builder", String::class.java).invoke(null, adUnitId)
            builder.javaClass.getMethod("setIsUseMediation", Boolean::class.javaPrimitiveType).invoke(builder, true)
            val adInfo = builder.javaClass.getMethod("build").invoke(builder)
            val nativeView = nativeClass.getConstructor(Context::class.java).newInstance(context)
            nativeClass.getMethod("setAdInfo", adInfoClass, Context::class.java).invoke(nativeView, adInfo, context)
            nativeClass.getMethod("loadNativeAd").invoke(nativeView)
            AdEventLogger.loaded("native", adUnitId)
            AdEventLogger.displayed("native", adUnitId)
            nativeView as View
        }.getOrElse {
            AdEventLogger.failed("native", adUnitId, it.message ?: "native failed")
            null
        }
    }

    fun videoView(context: Context): View? {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["outstream_video"] ?: return null
        AdEventLogger.request("video", adUnitId)
        return runCatching {
            val adInfoClass = Class.forName("com.nasmedia.admixer.ads.AdInfo")
            val videoClass = Class.forName("com.nasmedia.admixer.ads.VideoAdView")
            val builder = adInfoClass.getMethod("builder", String::class.java).invoke(null, adUnitId)
            builder.javaClass.getMethod("setIsUseMediation", Boolean::class.javaPrimitiveType).invoke(builder, true)
            val adInfo = builder.javaClass.getMethod("build").invoke(builder)
            val videoView = videoClass.getConstructor(Context::class.java).newInstance(context)
            videoClass.getMethod("setAdInfo", adInfoClass, Context::class.java).invoke(videoView, adInfo, context)
            videoClass.getMethod("loadAd").invoke(videoView)
            AdEventLogger.loaded("video", adUnitId)
            AdEventLogger.displayed("video", adUnitId)
            videoView as View
        }.getOrElse {
            AdEventLogger.failed("video", adUnitId, it.message ?: "video failed")
            null
        }
    }

    fun rewardVideoView(context: Context): View? {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["reward_video"] ?: return null
        AdEventLogger.request("rewardVideo", adUnitId)
        return runCatching {
            val adInfoClass = Class.forName("com.nasmedia.admixer.ads.AdInfo")
            val rewardClass = Class.forName("com.nasmedia.admixer.ads.RewardInterstitialVideoAd")
            val builder = adInfoClass.getMethod("builder", String::class.java).invoke(null, adUnitId)
            builder.javaClass.getMethod("setIsUseMediation", Boolean::class.javaPrimitiveType).invoke(builder, true)
            val adInfo = builder.javaClass.getMethod("build").invoke(builder)
            val rewardView = rewardClass.getConstructor(Context::class.java).newInstance(context)
            rewardClass.getMethod("setAdInfo", adInfoClass, Context::class.java).invoke(rewardView, adInfo, context)
            rewardClass.getMethod("loadRewardVideoAd").invoke(rewardView)
            AdEventLogger.loaded("rewardVideo", adUnitId)
            AdEventLogger.displayed("rewardVideo", adUnitId)
            rewardView as View
        }.getOrElse {
            AdEventLogger.failed("rewardVideo", adUnitId, it.message ?: "reward failed")
            null
        }
    }

    fun interstitialVideoView(context: Context): View? {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["interstitial_320x480"] ?: return null
        AdEventLogger.request("interstitialVideo", adUnitId)
        return runCatching {
            val adInfoClass = Class.forName("com.nasmedia.admixer.ads.AdInfo")
            val interstitialClass = Class.forName("com.nasmedia.admixer.ads.InterstitialVideoAd")
            val builder = adInfoClass.getMethod("builder", String::class.java).invoke(null, adUnitId)
            builder.javaClass.getMethod("setIsUseMediation", Boolean::class.javaPrimitiveType).invoke(builder, true)
            val adInfo = builder.javaClass.getMethod("build").invoke(builder)
            val interstitialView = interstitialClass.getConstructor(Context::class.java).newInstance(context)
            interstitialClass.getMethod("setAdInfo", adInfoClass, Context::class.java).invoke(interstitialView, adInfo, context)
            interstitialClass.getMethod("loadInterstitialVideoAd").invoke(interstitialView)
            AdEventLogger.loaded("interstitialVideo", adUnitId)
            AdEventLogger.displayed("interstitialVideo", adUnitId)
            interstitialView as View
        }.getOrElse {
            AdEventLogger.failed("interstitialVideo", adUnitId, it.message ?: "interstitial failed")
            null
        }
    }
}
