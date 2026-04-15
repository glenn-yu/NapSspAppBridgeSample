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

    fun banner(context: Context): String {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["banner_320x100"] ?: return "missing banner id"
        AdEventLogger.request("banner", adUnitId)
        AdEventLogger.loaded("banner", adUnitId)
        AdEventLogger.displayed("banner", adUnitId)
        return "banner ready: $adUnitId"
    }

    fun native(context: Context): String {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["native"] ?: return "missing native id"
        AdEventLogger.request("native", adUnitId)
        AdEventLogger.loaded("native", adUnitId)
        AdEventLogger.displayed("native", adUnitId)
        return "native ready: $adUnitId"
    }

    fun video(context: Context): String {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["outstream_video"] ?: return "missing video id"
        AdEventLogger.request("video", adUnitId)
        AdEventLogger.loaded("video", adUnitId)
        AdEventLogger.displayed("video", adUnitId)
        return "video ready: $adUnitId"
    }

    fun rewardVideo(context: Context): String {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["reward_video"] ?: return "missing reward id"
        AdEventLogger.request("rewardVideo", adUnitId)
        AdEventLogger.loaded("rewardVideo", adUnitId)
        AdEventLogger.displayed("rewardVideo", adUnitId)
        return "reward ready: $adUnitId"
    }

    fun interstitialVideo(context: Context): String {
        val adUnitId = NapSspConfig.AD_UNIT_IDS["interstitial_320x480"] ?: return "missing interstitial id"
        AdEventLogger.request("interstitialVideo", adUnitId)
        AdEventLogger.loaded("interstitialVideo", adUnitId)
        AdEventLogger.displayed("interstitialVideo", adUnitId)
        return "interstitial ready: $adUnitId"
    }
}
