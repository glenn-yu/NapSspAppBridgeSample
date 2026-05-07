package com.gwangy.nassspandroidsample.bridge

import com.gwangy.nassspandroidsample.BuildConfig

/**
 * NapSSP 하이브리드 광고 설정 파일
 * 보안을 위해 실제 키 값은 local.properties에서 관리하며, BuildConfig를 통해 주입됩니다.
 */
object NapSspConfig {
    val MEDIA_KEY: String = BuildConfig.NAP_MEDIA_KEY

    const val PANGLE_APP_ID = "8245842"

    val AD_UNIT_IDS = mapOf(
        "banner_320x100" to BuildConfig.NAP_ADUNIT_BANNER,
        // 나머지 ID들도 BuildConfig에 추가하여 관리 가능합니다.
        "interstitial_320x480_f" to "104703",
        "interstitial_320x480"   to "104702",
        "banner_320x50"         to "104701",
        "instream_video"        to "104591",
        "outstream_video"       to "104589",
        "native"                to "104588",
        "reward_video"          to "103722"
    )
}
