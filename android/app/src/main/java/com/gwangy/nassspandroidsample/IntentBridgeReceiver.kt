package com.gwangy.nassspandroidsample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gwangy.nassspandroidsample.bridge.NapSspSdkIntegration

class IntentBridgeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val ctx = context ?: return
        val action = intent?.getStringExtra("action") ?: return
        when (action) {
            "init" -> {
                NapSspSdkIntegration.initialize(ctx)
                println("IntentBridge: init called")
            }
            "loadAd" -> {
                val format = intent.getStringExtra("format") ?: ""
                println("IntentBridge: loadAd format=$format")
                when (format) {
                    "banner" -> NapSspSdkIntegration.bannerView(ctx)
                    "native" -> NapSspSdkIntegration.nativeView(ctx)
                    "video" -> NapSspSdkIntegration.videoView(ctx)
                    "rewardVideo" -> NapSspSdkIntegration.rewardVideoView(ctx)
                    "interstitialVideo" -> NapSspSdkIntegration.interstitialVideoView(ctx)
                    "interstitialBanner" -> NapSspSdkIntegration.interstitialBannerView(ctx)
                    else -> println("IntentBridge: unknown format $format")
                }
            }
            "clear" -> {
                NapSspSdkIntegration.clearAllAds()
                println("IntentBridge: clear called")
            }
            else -> println("IntentBridge: unknown action $action")
        }
    }
}
