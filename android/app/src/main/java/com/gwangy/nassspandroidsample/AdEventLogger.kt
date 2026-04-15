package com.gwangy.nassspandroidsample

object AdEventLogger {
    fun request(format: String, id: String) = println("NapSsp request format=$format id=$id")
    fun loaded(format: String, id: String) = println("NapSsp loaded format=$format id=$id")
    fun displayed(format: String, id: String) = println("NapSsp displayed format=$format id=$id")
    fun clicked(format: String, id: String) = println("NapSsp clicked format=$format id=$id")
    fun failed(format: String, id: String, reason: String) = println("NapSsp failed format=$format id=$id reason=$reason")
}
