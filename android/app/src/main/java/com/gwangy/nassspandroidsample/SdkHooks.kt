package com.gwangy.nassspandroidsample

object SdkHooks {
    fun describe(format: SampleFormat): String = when (format) {
        SampleFormat.Banner -> banner()
        SampleFormat.Native -> native()
        SampleFormat.Video -> video()
        SampleFormat.RewardVideo -> rewardVideo()
        SampleFormat.InterstitialVideo -> interstitialVideo()
        SampleFormat.HybridWebView -> hybridWebView()
    }

    fun banner(): String = "Banner SDK hook ready"
    fun native(): String = "Native SDK hook ready"
    fun video(): String = "Video SDK hook ready"
    fun rewardVideo(): String = "Reward video SDK hook ready"
    fun interstitialVideo(): String = "Interstitial video SDK hook ready"
    fun hybridWebView(): String = "WebView hybrid bridge ready"

    fun hybridStatus(): String = "NapSsp hybrid bridge ready"
}
