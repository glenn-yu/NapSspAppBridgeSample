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

    fun banner(): String = "배너 광고를 붙일 준비가 됨"
    fun native(): String = "네이티브 광고를 붙일 준비가 됨"
    fun video(): String = "동영상 광고를 붙일 준비가 됨"
    fun rewardVideo(): String = "리워드 동영상 광고를 붙일 준비가 됨"
    fun interstitialVideo(): String = "전면 동영상 광고를 붙일 준비가 됨"
    fun hybridWebView(): String = "웹뷰에서 네이티브 브리지를 사용할 준비가 됨"

    fun execute(format: SampleFormat, context: android.content.Context): String = when (format) {
        SampleFormat.Banner -> { NapSspSdkIntegration.bannerView(context); "배너 광고 실행" }
        SampleFormat.Native -> { NapSspSdkIntegration.nativeView(context); "네이티브 광고 실행" }
        SampleFormat.Video -> { NapSspSdkIntegration.videoView(context); "동영상 광고 실행" }
        SampleFormat.RewardVideo -> { NapSspSdkIntegration.rewardVideoView(context); "리워드 동영상 광고 실행" }
        SampleFormat.InterstitialVideo -> { NapSspSdkIntegration.interstitialVideoView(context); "전면 동영상 광고 실행" }
        SampleFormat.HybridWebView -> { "웹뷰 브리지 실행" }
    }

    fun hybridStatus(): String = "NapSsp hybrid bridge ready"
}
