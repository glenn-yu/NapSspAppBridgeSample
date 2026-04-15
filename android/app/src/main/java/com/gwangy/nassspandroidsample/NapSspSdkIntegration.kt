package com.gwangy.nassspandroidsample

import android.content.Context

object NapSspSdkIntegration {
    fun initialize(context: Context) {
        // nap ssp SDK init placeholder based on guide API
        // AdMixer.registerAdapter(AdMixer.ADAPTER_ADMANAGER)
        // AdMixer.registerAdapter(AdMixer.ADAPTER_ADFIT)
        // AdMixer.registerAdapter(AdMixer.ADAPTER_PANGLE)
        // AdMixer.getInstance().initialize(context, NapSspConfig.MEDIA_KEY, NapSspConfig.AD_UNIT_IDS.values.toList())
        AdEventLogger.request("initialize", NapSspConfig.MEDIA_KEY)
        println("NapSsp Android SDK initialize hook ready")
    }

    fun banner(context: Context) {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["banner_320x100"] ?: return
        AdEventLogger.request("banner", adUnitId)
        // AdInfo.Builder(adUnitId).setIsUseMediation(true).build()
        // AdView(context).apply { setAdInfo(adInfo); loadAd() }
        AdEventLogger.loaded("banner", adUnitId)
        AdEventLogger.displayed("banner", adUnitId)
    }

    fun native(context: Context) {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["native"] ?: return
        AdEventLogger.request("native", adUnitId)
        // NativeAdView(context).apply { setAdInfo(adInfo, context); loadNativeAd() }
        AdEventLogger.loaded("native", adUnitId)
        AdEventLogger.displayed("native", adUnitId)
    }

    fun video(context: Context) {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["outstream_video"] ?: return
        AdEventLogger.request("video", adUnitId)
        // VideoAdView(context).apply { setAdInfo(adInfo, context); loadAd() }
        AdEventLogger.loaded("video", adUnitId)
        AdEventLogger.displayed("video", adUnitId)
    }

    fun rewardVideo(context: Context) {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["reward_video"] ?: return
        AdEventLogger.request("rewardVideo", adUnitId)
        // RewardInterstitialVideoAd(context).apply { setAdInfo(adInfo, context); loadRewardVideoAd() }
        AdEventLogger.loaded("rewardVideo", adUnitId)
        AdEventLogger.displayed("rewardVideo", adUnitId)
    }

    fun interstitialVideo(context: Context) {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["interstitial_320x480"] ?: return
        AdEventLogger.request("interstitialVideo", adUnitId)
        // InterstitialVideoAd(context).apply { setAdInfo(adInfo, context); loadInterstitialVideoAd() }
        AdEventLogger.loaded("interstitialVideo", adUnitId)
        AdEventLogger.displayed("interstitialVideo", adUnitId)
    }
}
