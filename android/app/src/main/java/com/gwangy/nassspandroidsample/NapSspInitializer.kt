package com.gwangy.nassspandroidsample

object NapSspInitializer {
    fun initialize() {
        // 테스트값 적용됨
        // 실제 nap ssp Android SDK 초기화 코드
        // 예: mediation 설정, adapter 등록, consent 처리, ad unit 준비
        println("NapSsp Android init mediaKey=${NapSspConfig.MEDIA_KEY} adUnitIds=${NapSspConfig.AD_UNIT_IDS.values.joinToString(",")}")
        println("NapSsp Android mediation hints=${NapSspConfig.MEDITATION_HINTS.joinToString { "${it.first}:${it.second}" }}")
    }
}
