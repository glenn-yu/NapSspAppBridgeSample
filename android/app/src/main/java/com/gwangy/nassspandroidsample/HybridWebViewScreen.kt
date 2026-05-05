package com.gwangy.nassspandroidsample

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.gwangy.nassspandroidsample.bridge.NapSspConfig
import com.gwangy.nassspandroidsample.bridge.NapSspSdkIntegration
import com.nasmedia.admixerssp.ads.AdView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

class NapSspHybridBridge(
    private val webView: WebView,
    private val onAdRequest: (String, String?) -> Unit
) {
    private var lastActionTime = 0L

    private val supportedFormats = setOf(
        "banner",
        "native",
        "video",
        "rewardVideo",
        "interstitialVideo",
        "interstitialBanner"
    )

    @JavascriptInterface
    fun postMessage(jsonString: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastActionTime < 500) return // JS와 동일하게 0.5초로 조정
        lastActionTime = currentTime

        try {
            val request = JSONObject(jsonString)
            val action = request.optString("action")
            val params = request.optJSONObject("params") ?: JSONObject()

            when (action) {
                "init" -> {
                    NapSspSdkIntegration.initialize(webView.context)
                    sendResponse("init", "success", "SDK Initialized")
                }
                "loadAd" -> {
                    val format = params.optString("format")
                    val adUnitId = params.optString("adUnitId").takeIf { it.isNotEmpty() }
                    if (format !in supportedFormats) {
                        sendResponse("loadAd", "error", "Unsupported format: $format")
                        return
                    }
                    webView.post { onAdRequest(format, adUnitId) }
                    sendResponse("loadAd", "success", "Accepted $format")
                }
                "clearAds" -> {
                    webView.post {
                        onAdRequest("clear", null)
                        NapSspSdkIntegration.clearAllAds()
                        sendResponse("clearAds", "success", "All ads cleared")
                    }
                }
                else -> sendResponse(action, "error", "Unknown action")
            }
        } catch (e: Exception) {
            sendResponse("error", "error", e.message ?: "Invalid JSON")
        }
    }

    fun sendResponse(action: String, status: String, data: Any) {
        val response = JSONObject().apply {
            put("action", action)
            put("status", status)
            put("data", data)
        }
        val responseLiteral = JSONObject.quote(response.toString())
        webView.post {
            webView.evaluateJavascript("window.onNapSspMessage && window.onNapSspMessage($responseLiteral)", null)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HybridWebViewScreen(
    modifier: Modifier = Modifier,
) {
    var currentAdView by remember { mutableStateOf<View?>(null) }
    var adHeight by remember { mutableStateOf(0.dp) }
    var adSessionId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var isRequestingAd by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = androidx.compose.ui.platform.LocalContext.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> NapSspSdkIntegration.resumeAll()
                Lifecycle.Event.ON_PAUSE -> NapSspSdkIntegration.pauseAll()
                Lifecycle.Event.ON_DESTROY -> NapSspSdkIntegration.clearAllAds()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            NapSspSdkIntegration.clearAllAds()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { factoryContext ->
                val webView = WebView(factoryContext)
                val bridge = NapSspHybridBridge(webView) { format, customAdUnitId ->
                    if (isRequestingAd) return@NapSspHybridBridge
                    isRequestingAd = true

                    coroutineScope.launch {
                        try {
                            if (format == "clear") {
                                currentAdView = null
                                adHeight = 0.dp
                                NapSspSdkIntegration.clearAllAds()
                                return@launch
                            }

                            currentAdView = null 
                            NapSspSdkIntegration.clearAllAds()
                            delay(200)

                            adSessionId = UUID.randomUUID().toString()
                            val adView = when (format) {
                                "banner" -> { adHeight = 100.dp; NapSspSdkIntegration.bannerView(context, customAdUnitId) }
                                "native" -> { adHeight = 400.dp; NapSspSdkIntegration.nativeView(context, customAdUnitId) }
                                "video" -> { adHeight = 250.dp; NapSspSdkIntegration.videoView(context, customAdUnitId) }
                                "rewardVideo" -> { adHeight = 0.dp; NapSspSdkIntegration.rewardVideoView(context, customAdUnitId); null }
                                "interstitialVideo" -> { adHeight = 0.dp; NapSspSdkIntegration.interstitialVideoView(context, customAdUnitId); null }
                                "interstitialBanner" -> { adHeight = 0.dp; NapSspSdkIntegration.interstitialBannerView(context, customAdUnitId); null }
                                else -> { adHeight = 0.dp; null }
                            }
                            
                            if (isActive) {
                                currentAdView = adView
                            }
                        } finally {
                            isRequestingAd = false
                        }
                    }
                }

                // SDK 이벤트를 웹뷰 브릿지로 연결
                NapSspSdkIntegration.onAdEventCallback = { event, format, detail ->
                    bridge.sendResponse("event", "success", "[$format] $event: $detail")
                }

                webView.apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        setSupportMultipleWindows(true)
                        javaScriptCanOpenWindowsAutomatically = true
                        val originalUA = userAgentString
                        userAgentString = "$originalUA NapSspHybridBridge"
                    }
                    webViewClient = WebViewClient()
                    addJavascriptInterface(bridge, "NapSspBridge")
                    loadUrl("file:///android_asset/index.html")
                }
            }
        )

        key(adSessionId) {
            val h = adHeight
            if (currentAdView != null && h > 0.dp) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(h).background(Color.White),
                    factory = { factoryContext ->
                        FrameLayout(factoryContext).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            currentAdView?.let { adView ->
                                (adView.parent as? ViewGroup)?.removeView(adView)
                                adView.layoutParams = FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT
                                )
                                addView(adView)
                                if (adView is AdView) adView.onResume()
                                adView.requestLayout()
                            }
                        }
                    }
                )
            }
        }
    }
}
