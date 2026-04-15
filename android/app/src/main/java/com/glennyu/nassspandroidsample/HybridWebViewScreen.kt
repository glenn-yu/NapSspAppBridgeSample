package com.glennyu.nassspandroidsample

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

class NapSspHybridBridge {
    @JavascriptInterface
    fun postMessage(message: String) {
        println("NapSsp hybrid bridge message: $message")
    }
}

private const val SAMPLE_HYBRID_HTML = """
<!doctype html>
<html>
<head>
  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />
  <title>NapSsp Hybrid</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, sans-serif; padding: 24px; }
    button { display:block; width:100%; margin: 8px 0; padding: 12px; font-size: 16px; }
  </style>
</head>
<body>
  <h1>NapSsp Hybrid WebView</h1>
  <p>브리지 메시지 버튼을 눌러 네이티브로 전달한다.</p>
  <button onclick=\"window.NapSspBridge.postMessage('init')\">init</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadBanner')\">loadBanner</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadNative')\">loadNative</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadVideo')\">loadVideo</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadRewardVideo')\">loadRewardVideo</button>
  <button onclick=\"window.NapSspBridge.postMessage('loadInterstitialVideo')\">loadInterstitialVideo</button>
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
                addJavascriptInterface(NapSspHybridBridge(), "NapSspBridge")
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
