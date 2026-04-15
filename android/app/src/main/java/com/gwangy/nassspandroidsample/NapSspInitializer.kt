package com.gwangy.nassspandroidsample

object NapSspInitializer {
    private const val MEDIA_KEY = "10771"
    private val AD_UNIT_IDS = listOf(
        "104704", // 320x100
        "104703", // 320x480 (F)
        "104702", // 320x480
        "104701", // 320x50
        "104591", // instream Video
        "104589", // outstream video
        "104588", // native
        "103862", // 300x250 (F)
        "103722", // reward video
        "103485"  // 300x250
    )

    fun initialize() {
        // 테스트값 적용됨
        // 실제 nap ssp Android SDK 초기화 코드
        // 예: mediation 설정, adapter 등록, consent 처리, ad unit 준비
        println("NapSsp Android init mediaKey=$MEDIA_KEY adUnitIds=${AD_UNIT_IDS.joinToString(",")}")
    }
}
