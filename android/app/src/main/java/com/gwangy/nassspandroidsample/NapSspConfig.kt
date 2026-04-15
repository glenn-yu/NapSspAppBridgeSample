package com.gwangy.nassspandroidsample

object NapSspConfig {
    const val MEDIA_KEY = "10771"
    val AD_UNIT_IDS = mapOf(
        "banner_320x100" to "104704",
        "interstitial_320x480_f" to "104703",
        "interstitial_320x480" to "104702",
        "banner_320x50" to "104701",
        "instream_video" to "104591",
        "outstream_video" to "104589",
        "native" to "104588",
        "banner_300x250_f" to "103862",
        "reward_video" to "103722",
        "banner_300x250" to "103485",
    )

    val MEDITATION_HINTS = listOf(
        "AdManager" to "io.github.nasmedia-tech:admixer-admanager:1.0.14",
        "AdFit" to "io.github.nasmedia-tech:admixer-adfit:1.0.10",
        "Pangle" to "io.github.nasmedia-tech:admixer-pangle:1.0.10",
        "AppLovin" to "io.github.nasmedia-tech:admixer-applovin:1.0.8",
        "Unity" to "io.github.nasmedia-tech:admixer-unity:1.0.6",
    )
}
