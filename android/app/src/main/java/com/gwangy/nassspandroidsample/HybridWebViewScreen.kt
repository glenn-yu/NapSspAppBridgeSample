package com.gwangy.nassspandroidsample

import android.annotation.SuppressLint
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private enum class HybridMessage(val raw: String) {
    Init("init"),
    LoadBanner("loadBanner"),
    LoadNative("loadNative"),
    LoadVideo("loadVideo"),
    LoadRewardVideo("loadRewardVideo"),
    LoadInterstitialVideo("loadInterstitialVideo"),
    GetStatus("getStatus"),
    AdRequest("adRequest"),
    AdLoaded("adLoaded"),
    AdDisplayed("adDisplayed"),
    AdClicked("adClicked"),
    AdFailed("adFailed");

    companion object {
        fun from(raw: String): HybridMessage? = entries.firstOrNull { it.raw == raw }
    }
}

private fun String.toJsString(): String = buildString {
    append('"')
    for (ch in this@toJsString) {
        when (ch) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(ch)
        }
    }
    append('"')
}

private class NapSspHybridDispatcher {
    fun handle(message: String): String {
        return when (HybridMessage.from(message)) {
            HybridMessage.Init -> {
                NapSspInitializer.initialize()
                "init ok - 이제 광고 버튼을 눌러보세요"
            }
            HybridMessage.LoadBanner -> {
                HybridEventBridge.logRequest("loadBanner")
                "banner hook ok - 광고 뷰 시도 완료"
            }
            HybridMessage.LoadNative -> {
                HybridEventBridge.logRequest("loadNative")
                "native hook ok - 광고 뷰 시도 완료"
            }
            HybridMessage.LoadVideo -> {
                HybridEventBridge.logRequest("loadVideo")
                AppContextHolder.appContext?.let { NapSspSdkIntegration.videoView(it) }
                "video hook ok - 광고 뷰 시도 완료"
            }
            HybridMessage.LoadRewardVideo -> {
                HybridEventBridge.logRequest("loadRewardVideo")
                AppContextHolder.appContext?.let { NapSspSdkIntegration.rewardVideoView(it) }
                "reward hook ok - 광고 뷰 시도 완료"
            }
            HybridMessage.LoadInterstitialVideo -> {
                HybridEventBridge.logRequest("loadInterstitialVideo")
                AppContextHolder.appContext?.let { NapSspSdkIntegration.interstitialVideoView(it) }
                "interstitial hook ok - 광고 뷰 시도 완료"
            }
            HybridMessage.GetStatus -> "status ok"
            HybridMessage.AdRequest -> { HybridEventBridge.logRequest(message); "request logged" }
            HybridMessage.AdLoaded -> { HybridEventBridge.logLoaded(message); "loaded logged" }
            HybridMessage.AdDisplayed -> { HybridEventBridge.logDisplayed(message); "displayed logged" }
            HybridMessage.AdClicked -> { HybridEventBridge.logClicked(message); "clicked logged" }
            HybridMessage.AdFailed -> { HybridEventBridge.logFailed(message, "manual fail"); "failed logged" }
            null -> "unknown message"
        }
    }
}

class NapSspHybridBridge(private val webView: WebView) {
    private val dispatcher = NapSspHybridDispatcher()
    var onLoadAdView: ((View) -> Unit)? = null

    init {
        // Register callback to notify WebView of asynchronous SDK events
        NapSspSdkIntegration.onAdEventCallback = { event, format, detail ->
            val jsMessage = "SDK Event: $event | Format: $format | Detail: $detail"
            webView.post {
                webView.evaluateJavascript("window.__napSspAck && window.__napSspAck(${jsMessage.toJsString()})", null)
            }
        }
    }

    @JavascriptInterface
    fun postMessage(message: String) {
        println("NapSsp hybrid bridge message: $message")

        val msgType = HybridMessage.from(message)
        // Handle views that need to be added to the UI
        if (msgType == HybridMessage.LoadBanner || msgType == HybridMessage.LoadNative) {
            webView.post {
                val adView = when(msgType) {
                    HybridMessage.LoadBanner -> NapSspSdkIntegration.bannerView(webView.context)
                    HybridMessage.LoadNative -> NapSspSdkIntegration.nativeView(webView.context)
                    else -> null
                }
                adView?.let { onLoadAdView?.invoke(it) }
            }
        }

        val ack = dispatcher.handle(message)
        webView.post {
            webView.evaluateJavascript("window.__napSspAck && window.__napSspAck(${ack.toJsString()})", null)
        }
    }
}

private const val SAMPLE_HYBRID_HTML = """
<!doctype html>
<html>
<head>
  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />
  <title>NapSsp Hybrid</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, sans-serif; padding: 24px; background: #ffffff; }
    .tip { background: #eef4ff; border-radius: 14px; padding: 14px; margin-bottom: 16px; }
    .tip h2 { margin: 0 0 8px 0; font-size: 18px; }
    .tip p { margin: 0; line-height: 1.45; }
    .step { color: #446; font-weight: 600; }
    button { display:block; width:100%; margin: 8px 0; padding: 12px; font-size: 16px; border-radius: 10px; border: 1px solid #d7dce5; background: #f8fafc; }
    button.primary { background: #1d4ed8; color: white; border-color: #1d4ed8; }
    #log { margin-top: 16px; padding: 12px; background: #f2f2f2; min-height: 60px; border-radius: 12px; }
  </style>
</head>
<body>
  <div class=\"tip\">
    <h2>하이브리드 WebView 사용법</h2>
    <p><span class=\"step\">1.</span> 먼저 <b>init</b>을 누른다.</p>
    <p><span class=\"step\">2.</span> 그다음 <b>loadBanner / loadNative</b> 등을 눌러 네이티브 광고를 호출한다.</p>
    <p><span class=\"step\">3.</span> 웹뷰 아래에 실제 네이티브 광고 뷰가 꽂히는지 확인한다.</p>
  </div>
  <h1>NapSsp Hybrid WebView</h1>
  <button class=\"primary\" onclick=\"window.NapSspBridge.postMessage('init')\">init</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadBanner')\">loadBanner</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadNative')\">loadNative</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadVideo')\">loadVideo</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadRewardVideo')\">loadRewardVideo</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadInterstitialVideo')\">loadInterstitialVideo</button>
  <div id=\"log\">status: waiting</div>
  <script>
    window.__napSspAck = function(message) {
      document.getElementById('log').textContent = 'status: ' + message
    }
  </script>
</body>
</html>
"""

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HybridWebViewScreen(
    modifier: Modifier = Modifier,
) {
    val webViewState = remember { mutableStateOf<WebView?>(null) }
    val adContainerState = remember { mutableStateOf<FrameLayout?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    
                    val bridge = NapSspHybridBridge(this)
                    bridge.onLoadAdView = { adView ->
                        adContainerState.value?.let { container ->
                            container.post {
                                container.removeAllViews()
                                container.addView(adView)
                            }
                        }
                    }
                    
                    addJavascriptInterface(bridge, "NapSspBridge")
                    loadDataWithBaseURL(null, SAMPLE_HYBRID_HTML, "text/html", "utf-8", null)
                    webViewState.value = this
                }
            }
        )

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            factory = { context ->
                FrameLayout(context).apply {
                    adContainerState.value = this
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewState.value?.destroy()
            webViewState.value = null
        }
    }
}
