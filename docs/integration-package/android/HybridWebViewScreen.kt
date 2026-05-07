package com.yourapp // TODO: 실제 패키지명으로 변경

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nasmedia.admixerssp.ads.AdView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

// ──────────────────────────────────────────────────────────────────────────────
// JS ↔ Native 브릿지
//
// JS에서:
//   Android: window.NapSspBridge.postMessage(JSON.stringify({action, params}))
//   응답:    window.onNapSspMessage(responseJsonString)
// ──────────────────────────────────────────────────────────────────────────────

class NapSspHybridBridge(
    private val webView: WebView,
    private val onAdRequest: (format: String, adUnitId: String?) -> Unit
) {
    private var lastActionTime = 0L

    private val supportedFormats = setOf(
        "banner", "native", "video",
        "rewardVideo", "interstitialVideo", "interstitialBanner"
    )

    @JavascriptInterface
    fun postMessage(jsonString: String) {
        // 0.5초 이내 중복 요청 무시 (버튼 연타 방지)
        val now = System.currentTimeMillis()
        if (now - lastActionTime < 500) return
        lastActionTime = now

        try {
            val req = JSONObject(jsonString)
            val action = req.optString("action")
            val params = req.optJSONObject("params") ?: JSONObject()

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
                    // UI 스레드에서 광고 요청 처리
                    webView.post { onAdRequest(format, adUnitId) }
                    // 브릿지 수신 확인 ACK (광고 로드 성공이 아님)
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
        // JS 문자열 리터럴로 안전하게 escape
        val escaped = JSONObject.quote(response.toString())
        webView.post {
            webView.evaluateJavascript(
                "window.onNapSspMessage && window.onNapSspMessage($escaped)", null
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// WebView + 광고 영역 Compose 화면
// ──────────────────────────────────────────────────────────────────────────────

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HybridWebViewScreen(modifier: Modifier = Modifier) {
    // 배너/네이티브/비디오: 화면 하단 Native View 영역에 표시
    // 전면/보상형: SDK가 전체화면으로 직접 표시 (currentAdView = null)
    var currentAdView by remember { mutableStateOf<View?>(null) }
    var adHeight by remember { mutableStateOf(0.dp) }
    // 광고 교체 시 Compose 컨테이너를 새로 만들어 parent 충돌 방지
    var adSessionId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    // SDK 동시 요청 방지 flag
    var isRequestingAd by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // 화면 생명주기 → SDK 생명주기 연결
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME  -> NapSspSdkIntegration.resumeAll()
                Lifecycle.Event.ON_PAUSE   -> NapSspSdkIntegration.pauseAll()
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

        // ── WebView 영역 ──────────────────────────────────────────────────
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { ctx ->
                val webView = WebView(ctx)
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

                            // 기존 광고 제거 후 새 광고 요청
                            currentAdView = null
                            NapSspSdkIntegration.clearAllAds()
                            delay(200)
                            adSessionId = UUID.randomUUID().toString()

                            val adView = when (format) {
                                "banner"             -> { adHeight = 100.dp; NapSspSdkIntegration.bannerView(context, customAdUnitId) }
                                "native"             -> { adHeight = 400.dp; NapSspSdkIntegration.nativeView(context, customAdUnitId) }
                                "video"              -> { adHeight = 250.dp; NapSspSdkIntegration.videoView(context, customAdUnitId) }
                                "rewardVideo"        -> { adHeight = 0.dp;   NapSspSdkIntegration.rewardVideoView(context, customAdUnitId); null }
                                "interstitialVideo"  -> { adHeight = 0.dp;   NapSspSdkIntegration.interstitialVideoView(context, customAdUnitId); null }
                                "interstitialBanner" -> { adHeight = 0.dp;   NapSspSdkIntegration.interstitialBannerView(context, customAdUnitId); null }
                                else -> { adHeight = 0.dp; null }
                            }
                            if (isActive) currentAdView = adView
                        } finally {
                            isRequestingAd = false
                        }
                    }
                }

                // SDK 이벤트 → JS 콜백 연결
                NapSspSdkIntegration.onAdEventCallback = { event, format, detail ->
                    bridge.sendResponse("event", "success", "[$format] $event: $detail")
                }

                webView.apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    addJavascriptInterface(bridge, "NapSspBridge")

                    // TODO: 실제 WebView에 표시할 URL 또는 파일 경로로 교체
                    //   서버 URL 예시: loadUrl("https://your.domain.com/ad-page")
                    //   로컬 파일 예시: loadUrl("file:///android_asset/index.html")
                    loadUrl("file:///android_asset/index.html")
                }
            }
        )

        // ── 광고 View 영역 (배너/네이티브/비디오) ────────────────────────
        key(adSessionId) {
            val h = adHeight
            if (currentAdView != null && h > 0.dp) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(h).background(Color.White),
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            currentAdView?.let { adView ->
                                // 기존 parent에서 먼저 분리
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
