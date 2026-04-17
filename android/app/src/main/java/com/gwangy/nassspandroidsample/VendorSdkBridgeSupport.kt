package com.gwangy.nassspandroidsample

import java.lang.reflect.Method
import java.lang.reflect.Proxy

internal object VendorSdkBridgeSupport {
    fun isEnabled(): Boolean = BuildConfig.VENDOR_SDK_ENABLED

    fun buildAdInfo(adUnitId: String, interstitialType: String? = null): Any {
        val builderClass = Class.forName("com.nasmedia.admixerssp.ads.AdInfo\$Builder")
        val builder = builderClass.getConstructor(String::class.java).newInstance(adUnitId)

        invokeIfPresent(builder, "setIsUseMediation", true)

        if (interstitialType != null) {
            val typeClass = Class.forName("com.nasmedia.admixerssp.ads.AdInfo\$InterstitialAdType")
            val enumValue = enumValueOf(typeClass, interstitialType)
            invokeIfPresent(builder, "interstitialAdType", enumValue)
        }

        return invoke(builder, "build") ?: error("Unable to build AdInfo")
    }

    fun createAdListenerProxy(onEvent: (eventName: String, payload: Map<String, Any?>) -> Unit): Any {
        val listenerClass = Class.forName("com.nasmedia.admixerssp.ads.AdListener")
        return Proxy.newProxyInstance(
            listenerClass.classLoader,
            arrayOf(listenerClass),
        ) { _, method, args ->
            when (method.name) {
                "onReceivedAd" -> {
                    onEvent("loaded", mapOf())
                    null
                }
                "onFailedToReceiveAd" -> {
                    val errorCode = args?.getOrNull(2) as? Int
                    val errorMsg = args?.getOrNull(3)?.toString().orEmpty()
                    onEvent(
                        "failed",
                        mapOf(
                            "errorCode" to errorCode,
                            "errorMessage" to errorMsg,
                        ),
                    )
                    null
                }
                "onEventAd" -> {
                    val eventName = args?.getOrNull(1)?.toString().orEmpty()
                    val mappedEvent = when (eventName.uppercase()) {
                        "DISPLAYED" -> "displayed"
                        "CLICK" -> "clicked"
                        "CLOSE" -> "closed"
                        "EARNEDREWARD" -> "rewarded"
                        "COMPLETION" -> "completed"
                        "SKIPPED" -> "skipped"
                        else -> eventName.lowercase()
                    }
                    onEvent(mappedEvent, mapOf("rawEvent" to eventName))
                    null
                }
                else -> defaultReturn(method)
            }
        }
    }

    fun setListener(target: Any, listener: Any): Boolean =
        invokeIfPresent(target, "setAdViewListener", listener) ||
            invokeIfPresent(target, "setAdListener", listener) ||
            invokeIfPresent(target, "setListener", listener)

    fun setAdInfo(target: Any, adInfo: Any): Boolean = invokeIfPresent(target, "setAdInfo", adInfo)

    fun invokeMethod(target: Any, vararg methodNames: String): Boolean {
        methodNames.forEach { methodName ->
            if (invokeIfPresent(target, methodName)) return true
        }
        return false
    }

    fun stopAndClear(target: Any, vararg methodNames: String): Boolean {
        val stopped = invokeMethod(target, *methodNames)
        runCatching {
            invokeIfPresent(target, "setListener", null)
            invokeIfPresent(target, "setAdListener", null)
            invokeIfPresent(target, "setAdViewListener", null)
        }
        return stopped
    }

    private fun enumValueOf(enumClass: Class<*>, enumName: String): Any {
        @Suppress("UNCHECKED_CAST")
        val clazz = enumClass as Class<out Enum<*>>
        return java.lang.Enum.valueOf(clazz, enumName)
    }

    private fun invokeIfPresent(target: Any, methodName: String, vararg args: Any?): Boolean =
        runCatching {
            invoke(target, methodName, *args)
            true
        }.getOrDefault(false)

    private fun invoke(target: Any, methodName: String, vararg args: Any?): Any? {
        val methods = target.javaClass.methods.filter { it.name == methodName && it.parameterTypes.size == args.size }
        val method = methods.firstOrNull { candidate ->
            candidate.parameterTypes.zip(args).all { (paramType, arg) ->
                arg == null || paramType.isAssignableFrom(arg.javaClass) || isPrimitiveMatch(paramType, arg.javaClass)
            }
        } ?: error("Method $methodName not found on ${target.javaClass.name}")
        return method.invoke(target, *args)
    }

    private fun isPrimitiveMatch(paramType: Class<*>, argType: Class<*>): Boolean =
        (paramType == java.lang.Integer.TYPE && argType == Int::class.javaObjectType) ||
            (paramType == java.lang.Boolean.TYPE && argType == Boolean::class.javaObjectType) ||
            (paramType == java.lang.Long.TYPE && argType == Long::class.javaObjectType) ||
            (paramType == java.lang.Float.TYPE && argType == Float::class.javaObjectType) ||
            (paramType == java.lang.Double.TYPE && argType == Double::class.javaObjectType)

    private fun defaultReturn(method: Method): Any? = when (method.returnType) {
        java.lang.Boolean.TYPE -> false
        java.lang.Integer.TYPE -> 0
        java.lang.Long.TYPE -> 0L
        java.lang.Float.TYPE -> 0f
        java.lang.Double.TYPE -> 0.0
        java.lang.Void.TYPE -> null
        else -> null
    }
}
