package com.gwangy.nassspandroidsample

import android.content.Context

class RewardedAdModule(
    private val context: Context,
    private val emitEvent: (eventName: String, payload: Map<String, Any?>) -> Unit = { _, _ -> },
) {
    private var rewardedAd: Any? = null
    private var adUnitId: String? = null

    fun create(adUnitId: String): Boolean {
        if (!VendorSdkBridgeSupport.isEnabled()) return false

        close()
        this.adUnitId = adUnitId

        return runCatching {
            val ad = Class.forName("com.nasmedia.admixerssp.ads.RewardInterstitialVideoAd")
                .getConstructor(Context::class.java)
                .newInstance(context)
            val adInfo = VendorSdkBridgeSupport.buildAdInfo(adUnitId)
            val listener = VendorSdkBridgeSupport.createAdListenerProxy { eventName, payload ->
                emitEvent(
                    eventName,
                    payload + mapOf(
                        "adUnitId" to adUnitId,
                        "format" to "rewarded",
                    ),
                )
            }

            VendorSdkBridgeSupport.setAdInfo(ad, adInfo)
            VendorSdkBridgeSupport.setListener(ad, listener)
            rewardedAd = ad
            emitEvent("created", mapOf("adUnitId" to adUnitId, "format" to "rewarded"))
            true
        }.getOrElse {
            emitEvent(
                "failed",
                mapOf(
                    "adUnitId" to adUnitId,
                    "format" to "rewarded",
                    "errorMessage" to (it.message ?: "Unable to create rewarded ad"),
                ),
            )
            false
        }
    }

    fun load(): Boolean {
        val ad = ensureCreated() ?: return false
        return runCatching {
            VendorSdkBridgeSupport.invokeMethod(ad, "loadRewardVideoAd")
        }.getOrDefault(false)
    }

    fun show(): Boolean {
        val ad = rewardedAd ?: return false
        return runCatching {
            VendorSdkBridgeSupport.invokeMethod(ad, "showRewardVideoAd")
        }.getOrDefault(false)
    }

    fun close(): Boolean {
        val ad = rewardedAd ?: return false
        val stopped = runCatching {
            VendorSdkBridgeSupport.stopAndClear(ad, "stopRewardVideoAd")
        }.getOrDefault(false)
        rewardedAd = null
        return stopped
    }

    private fun ensureCreated(): Any? {
        val existing = rewardedAd
        if (existing != null) return existing
        val currentAdUnitId = adUnitId ?: return null
        return if (create(currentAdUnitId)) rewardedAd else null
    }
}
