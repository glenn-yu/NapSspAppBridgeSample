package com.gwangy.nassspandroidsample

object NapSspInitializer {
    fun initialize() {
        NapSspSdkIntegration.initialize(AppContextHolder.appContext ?: return)
        println("NapSsp Android init mediaKey=${NapSspConfig.MEDIA_KEY} adUnitIds=${NapSspConfig.AD_UNIT_IDS.values.joinToString(",")}")
        println("NapSsp Android mediation hints=${NapSspConfig.MEDITATION_HINTS.joinToString { "${it.first}:${it.second}" }}")
    }
}
