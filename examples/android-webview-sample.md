# Android WebView 예시

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            addJavascriptInterface(AndroidBridge(this), "AndroidBridge")
            webViewClient = WebViewClient()
            loadUrl("https://example.com")
        }

        setContentView(webView)
    }
}

class AndroidBridge(private val context: Context) {
    @JavascriptInterface
    fun postMessage(message: String) {
        Log.d("AppBridge", message)
    }
}
```
