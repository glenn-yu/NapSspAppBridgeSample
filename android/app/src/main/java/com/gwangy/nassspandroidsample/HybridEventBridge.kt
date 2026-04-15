package com.gwangy.nassspandroidsample

object HybridEventBridge {
    fun logRequest(message: String) = AdEventLogger.request("hybrid", message)
    fun logLoaded(message: String) = AdEventLogger.loaded("hybrid", message)
    fun logDisplayed(message: String) = AdEventLogger.displayed("hybrid", message)
    fun logClicked(message: String) = AdEventLogger.clicked("hybrid", message)
    fun logFailed(message: String, reason: String) = AdEventLogger.failed("hybrid", message, reason)
}
