package com.gwangy.nassspandroidsample

import android.app.Activity
import android.content.Context
import com.nasmedia.admixerssp.ads.AMMInterstitial
import com.nasmedia.admixerssp.ads.AMMInterstitialLoadCallback
import com.nasmedia.admixerssp.ads.AdError
import com.nasmedia.admixerssp.ads.AdInfo
import com.nasmedia.admixerssp.ads.FullScreenContentCallback

class InterstitialModule(
    private val context: Context,
    private val emitEvent: (eventName: String, payload: Map<String, Any?>) -> Unit = { _, _ -> },
) {
    private var interstitialAd: AMMInterstitial? = null
    private var adUnitId: String? = null

    fun create(adUnitId: String): Boolean {
        if (!VendorSdkBridgeSupport.isEnabled()) return false

        close()
        this.adUnitId = adUnitId

        emitEvent("created", mapOf("adUnitId" to adUnitId, "format" to "interstitial"))
        return true
    }

    fun load(): Boolean {
        if (!VendorSdkBridgeSupport.isEnabled()) return false
        val currentAdUnitId = adUnitId ?: return false

        return runCatching {
            val adInfo = AdInfo.Builder(currentAdUnitId)
                .build()

            AMMInterstitial.loadAd(context, adInfo, object : AMMInterstitialLoadCallback() {
                override fun onSuccessLoadInterstitial(adapterName: String, ad: AMMInterstitial) {
                    interstitialAd = ad
                    emitEvent(
                        "loaded",
                        mapOf(
                            "adUnitId" to currentAdUnitId,
                            "format" to "interstitial",
                            "adapterName" to adapterName
                        )
                    )

                    ad.setFullScreenContentCallback(object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            emitEvent("displayed", mapOf("adUnitId" to currentAdUnitId, "format" to "interstitial"))
                        }
                        override fun onAdClicked() {
                            emitEvent("clicked", mapOf("adUnitId" to currentAdUnitId, "format" to "interstitial"))
                        }
                        override fun onAdDismissedFullScreenContent() {
                            emitEvent("closed", mapOf("adUnitId" to currentAdUnitId, "format" to "interstitial"))
                            close()
                        }
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            emitEvent(
                                "failed",
                                mapOf(
                                    "adUnitId" to currentAdUnitId,
                                    "format" to "interstitial",
                                    "errorMessage" to "${adError.code} / ${adError.message}"
                                )
                            )
                            close()
                        }
                    })
                }

                override fun onFailLoadInterstitial(errorCode: Int, errorMsg: String?) {
                    emitEvent(
                        "failed",
                        mapOf(
                            "adUnitId" to currentAdUnitId,
                            "format" to "interstitial",
                            "errorMessage" to "[$errorCode] $errorMsg"
                        )
                    )
                    close()
                }
            })
            true
        }.getOrDefault(false)
    }

    fun show(): Boolean {
        val ad = interstitialAd ?: return false
        val activity = context as? Activity ?: return false
        return runCatching {
            ad.showAd(activity)
            true
        }.getOrDefault(false)
    }

    fun close(): Boolean {
        val ad = interstitialAd
        if (ad != null) {
            ad.stop()
            interstitialAd = null
            return true
        }
        return false
    }
}
