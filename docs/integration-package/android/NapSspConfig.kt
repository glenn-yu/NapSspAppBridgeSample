package com.yourapp // TODO: 실제 패키지명으로 변경

/**
 * NapSSP 광고 키 설정
 *
 * MEDIA_KEY와 AD_UNIT_IDS를 실제 발급받은 값으로 교체하세요.
 * 운영 앱에서는 소스에 직접 커밋하지 말고 BuildConfig / CI secret / 서버 설정으로 주입하는 것을 권장합니다.
 */
object NapSspConfig {

    // ── 필수: 실제 키로 교체 ──────────────────────────────────────────────
    const val MEDIA_KEY = "10771"           // TODO: 실제 MEDIA_KEY로 교체

    val AD_UNIT_IDS = mapOf(
        "banner_320x100"         to "104704",   // TODO: 실제 AD_UNIT_ID로 교체
        "native"                 to "104588",
        "outstream_video"        to "104589",
        "reward_video"           to "103722",
        "interstitial_320x480"   to "104702",
        "interstitial_320x480_f" to "104703",
    )
    // ──────────────────────────────────────────────────────────────────────

    // Pangle 미디에이션 사용 시 실제 앱 ID로 교체
    const val PANGLE_APP_ID = "YOUR_PANGLE_APP_ID"
}
