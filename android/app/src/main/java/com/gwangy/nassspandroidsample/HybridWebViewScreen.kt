package com.gwangy.nassspandroidsample

import android.annotation.SuppressLint
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nasmedia.admixerssp.ads.AdView
import com.nasmedia.admixerssp.ads.NativeAdView
import com.nasmedia.admixerssp.ads.VideoAdView
import org.json.JSONObject
import java.util.UUID

class NapSspHybridBridge(
    private val webView: WebView,
    private val onAdRequest: (String) -> Unit
) {
    private var lastActionTime = 0L

    @JavascriptInterface
    fun postMessage(jsonString: String) {
        val currentTime = System.currentTimeMillis()
        // [중복 방지] 호출 간격 1초로 강화
        if (currentTime - lastActionTime < 1000) return
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
                    // 메인 스레드 호출 동기화
                    webView.post { onAdRequest(format) }
                }
                "clearAds" -> {
                    webView.post {
                        onAdRequest("clear")
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

    private fun sendResponse(action: String, status: String, data: Any) {
        val response = JSONObject().apply {
            put("action", action)
            put("status", status)
            put("data", data)
        }
        val jsonStr = response.toString().replace("'", "\\'")
        webView.post {
            webView.evaluateJavascript("window.onNapSspMessage && window.onNapSspMessage('$jsonStr')", null)
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
    
    // [중복 방지] UI 레벨의 작업 중 플래그
    var isRequestingAd by remember { mutableStateOf(false) }
    
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
                WebView(factoryContext).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    
                    addJavascriptInterface(NapSspHybridBridge(this) { format ->
                        // [중복 방지] 요청 중이면 무시
                        if (isRequestingAd) return@NapSspHybridBridge
                        isRequestingAd = true

                        if (format == "clear") {
                            currentAdView = null
                            adHeight = 0.dp
                            isRequestingAd = false
                            return@NapSspHybridBridge
                        }
                        
                        // 세션 갱신 및 광고 생성
                        adSessionId = UUID.randomUUID().toString()
                        val adView = when (format) {
                            "banner" -> NapSspSdkIntegration.bannerView(context)
                            "native" -> NapSspSdkIntegration.nativeView(context)
                            "video" -> NapSspSdkIntegration.videoView(context)
                            "rewardVideo" -> { NapSspSdkIntegration.rewardVideoView(context); null }
                            "interstitialVideo" -> { NapSspSdkIntegration.interstitialVideoView(context); null }
                            "interstitialBanner" -> { NapSspSdkIntegration.interstitialBannerView(context); null }
                            else -> null
                        }
                        
                        currentAdView = adView
                        adHeight = when {
                            adView is AdView -> 100.dp
                            adView is NativeAdView -> 350.dp
                            adView is VideoAdView -> 250.dp
                            else -> 0.dp
                        }
                        
                        // 작업 완료 후 락 해제
                        isRequestingAd = false
                    }, "NapSspBridge")
                    
                    loadUrl("file:///android_asset/index.html")
                }
            }
        )

        key(adSessionId) {
            val currentAdHeight = adHeight
            if (currentAdView != null && currentAdHeight > 0.dp) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(currentAdHeight).background(Color(0xFFEEEEEE)),
                    factory = { factoryContext ->
                        FrameLayout(factoryContext).apply {
                            currentAdView?.let { adView ->
                                (adView.parent as? android.view.ViewGroup)?.removeView(adView)
                                adView.layoutParams = FrameLayout.LayoutParams(-1, -1)
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
