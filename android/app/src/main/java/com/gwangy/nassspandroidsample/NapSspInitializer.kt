package com.gwangy.nassspandroidsample

import com.gwangy.nassspandroidsample.bridge.NapSspConfig
import com.gwangy.nassspandroidsample.bridge.NapSspSdkIntegration

object NapSspInitializer {
    fun initialize() {
        val context = AppContextHolder.appContext ?: return
        NapSspSdkIntegration.initialize(context)
        println("NapSsp Android init mediaKey=${NapSspConfig.MEDIA_KEY} adUnitIds=${NapSspConfig.AD_UNIT_IDS.values.joinToString(",")}")
    }
}
