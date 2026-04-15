package com.gwangy.nassspandroidsample

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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

private class NapSspHybridDispatcher {
    fun handle(message: String): String {
        return when (HybridMessage.from(message)) {
            HybridMessage.Init -> {
                NapSspInitializer.initialize()
                "init ok - 이제 광고 버튼을 눌러보세요"
            }
            HybridMessage.LoadBanner -> {
                HybridEventBridge.logRequest("loadBanner")
                AppContextHolder.appContext?.let { NapSspSdkIntegration.bannerView(it) }
                "banner hook ok - 광고 뷰 시도 완료"
            }
            HybridMessage.LoadNative -> {
                HybridEventBridge.logRequest("loadNative")
                AppContextHolder.appContext?.let { NapSspSdkIntegration.nativeView(it) }
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

    @JavascriptInterface
    fun postMessage(message: String) {
        println("NapSsp hybrid bridge message: $message")
        val ack = dispatcher.handle(message)
        webView.post {
            webView.evaluateJavascript("window.__napSspAck && window.__napSspAck('${ack.replace("'", "\\'")}')", null)
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
    <p><span class=\"step\">2.</span> 그다음 <b>loadBanner / loadNative / loadVideo</b> 같은 광고 버튼을 눌러본다.</p>
    <p><span class=\"step\">3.</span> 아래 상태창에서 네이티브 응답을 확인한다.</p>
  </div>
  <h1>NapSsp Hybrid WebView</h1>
  <p>웹 버튼이 네이티브 광고 코드를 부른다.</p>
  <button class=\"primary\" onclick=\"window.NapSspBridge.postMessage('init')\">init</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadBanner')\">loadBanner</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadNative')\">loadNative</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadVideo')\">loadVideo</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadRewardVideo')\">loadRewardVideo</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadInterstitialVideo')\">loadInterstitialVideo</button>
  <button onclick=\"window.NapSspBridge.postMessage('adRequest')\">adRequest</button>
  <button onclick=\"window.NapSspBridge.postMessage('adLoaded')\">adLoaded</button>
  <button onclick=\"window.NapSspBridge.postMessage('adDisplayed')\">adDisplayed</button>
  <button onclick=\"window.NapSspBridge.postMessage('adClicked')\">adClicked</button>
  <button onclick=\"window.NapSspBridge.postMessage('adFailed')\">adFailed</button>
  <button onclick=\"window.NapSspBridge.postMessage('getStatus')\">getStatus</button>
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

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                addJavascriptInterface(NapSspHybridBridge(this), "NapSspBridge")
                loadDataWithBaseURL(null, SAMPLE_HYBRID_HTML, "text/html", "utf-8", null)
                webViewState.value = this
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            webViewState.value?.destroy()
            webViewState.value = null
        }
    }
}
