package com.gwangy.nassspandroidsample

import com.nasmedia.admixerssp.ads.*

internal object VendorSdkBridgeSupport {
    fun isEnabled(): Boolean = BuildConfig.VENDOR_SDK_ENABLED

    fun buildAdInfo(adUnitId: String, interstitialType: String? = null): Any {
        return AdInfo.Builder(adUnitId)
            .build()
    }

    fun createAdListenerProxy(onEvent: (eventName: String, payload: Map<String, Any?>) -> Unit): Any {
        return object : AdListener() {
            override fun onReceivedAd(adapterName: String, adView: Any) {
                onEvent("loaded", mapOf("adapterName" to adapterName))
            }

            override fun onFailedToReceiveAd(adView: Any?, adapterName: String, errorCode: Int, errorMsg: String?) {
                onEvent(
                    "failed",
                    mapOf(
                        "errorCode" to errorCode,
                        "errorMessage" to (errorMsg ?: ""),
                        "adapterName" to adapterName
                    )
                )
            }

            override fun onAdDisplayed() {
                onEvent("displayed", mapOf())
            }

            override fun onAdClicked() {
                onEvent("clicked", mapOf())
            }
        }
    }

    fun setListener(target: Any, listener: Any): Boolean {
        val adListener = listener as? AdListener ?: return false
        return when (target) {
            is AMMBannerView -> { target.setAdViewListener(adListener); true }
            is AMMNativeAdView -> { target.setAdViewListener(adListener); true }
            is AMMVideoView -> { target.setAdViewListener(adListener); true }
            else -> false
        }
    }

    fun setAdInfo(target: Any, adInfo: Any): Boolean {
        val info = adInfo as? AdInfo ?: return false
        return when (target) {
            is AMMBannerView -> { target.setAdInfo(info); true }
            is AMMNativeAdView -> { target.setAdInfo(info); true }
            is AMMVideoView -> { target.setAdInfo(info); true }
            else -> false
        }
    }

    fun invokeMethod(target: Any, vararg methodNames: String): Boolean {
        methodNames.forEach { methodName ->
            when (methodName) {
                "loadAd" -> {
                    when (target) {
                        is AMMBannerView -> { target.loadAd(); return true }
                        is AMMVideoView -> { target.loadAd(); return true }
                    }
                }
                "loadNativeAd" -> {
                    if (target is AMMNativeAdView) { target.loadNativeAd(); return true }
                }
            }
        }
        return false
    }

    fun stopAndClear(target: Any, vararg methodNames: String): Boolean {
        var stopped = false
        methodNames.forEach { methodName ->
            when (methodName) {
                "stop" -> {
                    when (target) {
                        is AMMBannerView -> { target.stop(); stopped = true }
                        is AMMNativeAdView -> { target.stop(); stopped = true }
                        is AMMVideoView -> { target.stop(); stopped = true }
                        is AMMInterstitial -> { target.stop(); stopped = true }
                        is AMMVideoInterstitial -> { target.stop(); stopped = true }
                        is AMMRewardVideo -> { target.stop(); stopped = true }
                    }
                }
            }
        }
        runCatching {
            when (target) {
                is AMMBannerView -> target.setAdViewListener(null)
                is AMMNativeAdView -> target.setAdViewListener(null)
                is AMMVideoView -> target.setAdViewListener(null)
            }
        }
        return stopped
    }
}
