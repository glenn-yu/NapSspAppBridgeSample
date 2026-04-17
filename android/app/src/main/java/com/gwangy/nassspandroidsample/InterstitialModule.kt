package com.gwangy.nassspandroidsample

import android.content.Context

class InterstitialModule(
    private val context: Context,
    private val emitEvent: (eventName: String, payload: Map<String, Any?>) -> Unit = { _, _ -> },
) {
    private var interstitialAd: Any? = null
    private var adUnitId: String? = null

    fun create(adUnitId: String): Boolean {
        if (!VendorSdkBridgeSupport.isEnabled()) return false

        close()
        this.adUnitId = adUnitId

        return runCatching {
            val ad = Class.forName("com.nasmedia.admixerssp.ads.InterstitialAd")
                .getConstructor(Context::class.java)
                .newInstance(context)
            val adInfo = VendorSdkBridgeSupport.buildAdInfo(
                adUnitId = adUnitId,
                interstitialType = "Basic",
            )
            val listener = VendorSdkBridgeSupport.createAdListenerProxy { eventName, payload ->
                emitEvent(
                    eventName,
                    payload + mapOf(
                        "adUnitId" to adUnitId,
                        "format" to "interstitial",
                    ),
                )
            }

            VendorSdkBridgeSupport.setAdInfo(ad, adInfo)
            VendorSdkBridgeSupport.setListener(ad, listener)
            interstitialAd = ad
            emitEvent("created", mapOf("adUnitId" to adUnitId, "format" to "interstitial"))
            true
        }.getOrElse {
            emitEvent(
                "failed",
                mapOf(
                    "adUnitId" to adUnitId,
                    "format" to "interstitial",
                    "errorMessage" to (it.message ?: "Unable to create interstitial ad"),
                ),
            )
            false
        }
    }

    fun load(): Boolean {
        val ad = ensureCreated() ?: return false
        return runCatching {
            VendorSdkBridgeSupport.invokeMethod(ad, "loadInterstitial", "startInterstitial")
        }.getOrDefault(false)
    }

    fun show(): Boolean {
        val ad = interstitialAd ?: return false
        return runCatching {
            VendorSdkBridgeSupport.invokeMethod(ad, "showInterstitial")
        }.getOrDefault(false)
    }

    fun close(): Boolean {
        val ad = interstitialAd ?: return false
        val stopped = runCatching {
            VendorSdkBridgeSupport.stopAndClear(ad, "stopInterstitial")
        }.getOrDefault(false)
        interstitialAd = null
        return stopped
    }

    private fun ensureCreated(): Any? {
        val existing = interstitialAd
        if (existing != null) return existing
        val currentAdUnitId = adUnitId ?: return null
        return if (create(currentAdUnitId)) interstitialAd else null
    }
}
