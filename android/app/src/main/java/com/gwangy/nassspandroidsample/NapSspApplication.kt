package com.gwangy.nassspandroidsample

import android.app.Application

class NapSspApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContextHolder.appContext = applicationContext
        NapSspInitializer.initialize()
    }
}
