package com.gwangy.nassspandroidsample

object NapSspInitializer {
    fun initialize() {
        val context = AppContextHolder.appContext ?: return
        NapSspSdkIntegration.initialize(context)
        val adUnitIds = NapSspConfig.adUnitIds(context)
        println("NapSsp Android init mediaKey=${NapSspConfig.mediaKey(context)} adUnitIds=${adUnitIds.values.joinToString(",")}")
        println("NapSsp Android mediation hints=${NapSspConfig.MEDITATION_HINTS.joinToString { "${it.first}:${it.second}" }}")
    }
}
