package com.gwangy.nassspandroidsample

import android.app.Activity
import android.content.Context
import com.nasmedia.admixerssp.ads.AMMRewardVideo
import com.nasmedia.admixerssp.ads.AMMRewardVideoLoadCallback
import com.nasmedia.admixerssp.ads.AdError
import com.nasmedia.admixerssp.ads.AdInfo
import com.nasmedia.admixerssp.ads.FullScreenContentCallback
import com.nasmedia.admixerssp.ads.OnUserEarnedRewardListener

class RewardedAdModule(
    private val context: Context,
    private val emitEvent: (eventName: String, payload: Map<String, Any?>) -> Unit = { _, _ -> },
) {
    private var rewardedAd: AMMRewardVideo? = null
    private var adUnitId: String? = null

    fun create(adUnitId: String): Boolean {
        if (!VendorSdkBridgeSupport.isEnabled()) return false

        close()
        this.adUnitId = adUnitId

        emitEvent("created", mapOf("adUnitId" to adUnitId, "format" to "rewarded"))
        return true
    }

    fun load(): Boolean {
        if (!VendorSdkBridgeSupport.isEnabled()) return false
        val currentAdUnitId = adUnitId ?: return false

        return runCatching {
            val adInfo = AdInfo.Builder(currentAdUnitId)
                .build()

            AMMRewardVideo.loadAd(context, adInfo, object : AMMRewardVideoLoadCallback() {
                override fun onSuccessLoadReward(adapterName: String, ad: AMMRewardVideo) {
                    rewardedAd = ad
                    emitEvent(
                        "loaded",
                        mapOf(
                            "adUnitId" to currentAdUnitId,
                            "format" to "rewarded",
                            "adapterName" to adapterName
                        )
                    )

                    ad.setFullScreenContentCallback(object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            emitEvent("displayed", mapOf("adUnitId" to currentAdUnitId, "format" to "rewarded"))
                        }
                        override fun onAdClicked() {
                            emitEvent("clicked", mapOf("adUnitId" to currentAdUnitId, "format" to "rewarded"))
                        }
                        override fun onAdCompleted() {
                            emitEvent("completed", mapOf("adUnitId" to currentAdUnitId, "format" to "rewarded"))
                        }
                        override fun onAdDismissedFullScreenContent() {
                            emitEvent("closed", mapOf("adUnitId" to currentAdUnitId, "format" to "rewarded"))
                            close()
                        }
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            emitEvent(
                                "failed",
                                mapOf(
                                    "adUnitId" to currentAdUnitId,
                                    "format" to "rewarded",
                                    "errorMessage" to "${adError.code} / ${adError.message}"
                                )
                            )
                            close()
                        }
                    })
                }

                override fun onFailLoadReward(errorCode: Int, errorMsg: String?) {
                    emitEvent(
                        "failed",
                        mapOf(
                            "adUnitId" to currentAdUnitId,
                            "format" to "rewarded",
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
        val ad = rewardedAd ?: return false
        val activity = context as? Activity ?: return false
        return runCatching {
            ad.show(activity, object : OnUserEarnedRewardListener {
                override fun onUserEarnedReward() {
                    emitEvent("rewarded", mapOf("adUnitId" to (adUnitId ?: ""), "format" to "rewarded"))
                }
            })
            true
        }.getOrDefault(false)
    }

    fun close(): Boolean {
        val ad = rewardedAd
        if (ad != null) {
            ad.stop()
            rewardedAd = null
            return true
        }
        return false
    }
}
