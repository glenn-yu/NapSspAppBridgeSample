package com.glennyu.nassspandroidsample

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HybridWebViewScreen(
    modifier: Modifier = Modifier,
    url: String = "https://example.com"
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
                loadUrl(url)
                webViewState.value = this
            }
        },
        update = { it.loadUrl(url) }
    )

    DisposableEffect(Unit) {
        onDispose {
            webViewState.value?.destroy()
            webViewState.value = null
        }
    }
}
