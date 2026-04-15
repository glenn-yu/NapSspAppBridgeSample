package com.glennyu.nassspandroidsample

object SdkHooks {
    fun describe(format: SampleFormat): String = when (format) {
        SampleFormat.Banner -> "Banner SDK hook here"
        SampleFormat.Native -> "Native SDK hook here"
        SampleFormat.Video -> "Video SDK hook here"
        SampleFormat.RewardVideo -> "Reward video SDK hook here"
        SampleFormat.InterstitialVideo -> "Interstitial video SDK hook here"
        SampleFormat.HybridWebView -> "WebView hybrid bridge hook here"
    }

    fun hybridStatus(): String = "NapSsp hybrid bridge ready"
}
