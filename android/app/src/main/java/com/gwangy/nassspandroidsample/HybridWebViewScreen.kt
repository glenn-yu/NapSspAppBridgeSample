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
import com.nasmedia.admixerssp.ads.AMMBannerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

/**
 * WebView(JavaScript)와 Android Native 광고 SDK 사이를 연결하는 작은 브릿지입니다.
 *
 * 전체 흐름은 다음과 같습니다.
 * 1. HTML/JavaScript가 `window.NapSspBridge.postMessage(...)`로 JSON 문자열을 보냅니다.
 * 2. 이 클래스가 JSON을 파싱해 `init`, `loadAd`, `clearAds` 같은 action을 구분합니다.
 * 3. 실제 광고 SDK 호출이나 Compose UI 갱신이 필요한 작업은 [onAdRequest]로 위임합니다.
 * 4. 처리 결과는 `window.onNapSspMessage(...)`를 호출해 다시 JavaScript로 전달합니다.
 *
 * 중요한 점은 `loadAd`의 즉시 응답이 "광고 로드 성공"이 아니라는 것입니다.
 * 여기서 보내는 `Accepted <format>` 응답은 Native가 요청을 정상적으로 받았다는 ACK입니다.
 * 실제 광고 로드/노출/클릭/실패 이벤트는 SDK callback을 통해 `event` action으로 별도 전달됩니다.
 */
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
        // 버튼 연타나 JS 중복 호출로 같은 요청이 너무 빠르게 들어오면 SDK 상태가 꼬일 수 있습니다.
        // 샘플 HTML의 debounce 기준과 맞춰 0.5초 안에 들어온 중복 요청은 무시합니다.
        if (currentTime - lastActionTime < 500) return
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
                    // 브릿지가 모르는 광고 포맷은 SDK까지 넘기지 않고 여기서 즉시 차단합니다.
                    // 이렇게 해야 JS/Native 양쪽 contract가 어긋났을 때 원인을 빠르게 찾을 수 있습니다.
                    if (format !in supportedFormats) {
                        sendResponse("loadAd", "error", "Unsupported format: $format")
                        return
                    }
                    // WebView의 JavaScriptInterface 메서드는 UI 스레드가 아닐 수 있습니다.
                    // 광고 View 생성과 Compose 상태 변경은 UI 스레드에서 처리해야 하므로 webView.post로 넘깁니다.
                    webView.post { onAdRequest(format, adUnitId) }
                    // ACK: 요청 접수 성공을 의미합니다. 실제 광고 로드 성공은 SDK 이벤트로 별도 전달됩니다.
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
        // response.toString()을 JavaScript 코드에 그대로 끼워 넣으면 따옴표, 줄바꿈, 역슬래시 같은
        // 특수문자 때문에 JS 문법 오류나 의도치 않은 문자열 종료가 발생할 수 있습니다.
        // JSONObject.quote는 JSON 문자열을 안전한 JS 문자열 리터럴 형태로 escape합니다.
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
    // Compose 화면에 붙일 수 있는 광고 View입니다.
    // 배너/네이티브/동영상처럼 화면 일부를 차지하는 광고만 이 값에 들어갑니다.
    // 전면/보상형 광고는 SDK가 자체적으로 전체화면을 띄우므로 여기에 View를 보관하지 않습니다.
    var currentAdView by remember { mutableStateOf<View?>(null) }
    var adHeight by remember { mutableStateOf(0.dp) }

    // 같은 Android View를 Compose AndroidView에 다시 붙이면 parent 충돌이 날 수 있습니다.
    // 광고 요청마다 key를 바꿔 새 컨테이너를 만들면 이전 View와 새 View의 생명주기를 분리할 수 있습니다.
    var adSessionId by remember { mutableStateOf(UUID.randomUUID().toString()) }

    // 광고 SDK는 동시에 여러 load 요청이 들어오면 내부 상태가 불안정해질 수 있습니다.
    // 하나의 요청이 처리되는 동안 추가 요청을 막아 샘플 동작을 예측 가능하게 유지합니다.
    var isRequestingAd by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = androidx.compose.ui.platform.LocalContext.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                // WebView 화면의 생명주기와 SDK 광고 객체의 생명주기를 맞춥니다.
                // 화면이 백그라운드로 가면 pause, 다시 보이면 resume, 사라지면 clear를 호출합니다.
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
                                // JS에서 명시적으로 clearAds를 호출한 경우입니다.
                                // Compose에 붙은 View와 SDK가 들고 있는 광고 객체를 함께 정리합니다.
                                currentAdView = null
                                adHeight = 0.dp
                                NapSspSdkIntegration.clearAllAds()
                                return@launch
                            }

                            // 새 광고 요청 전에 기존 광고를 먼저 제거합니다.
                            // 이전 광고 View가 남아 있으면 같은 parent에 중복으로 붙거나,
                            // 이전 SDK 객체의 callback이 새 요청과 섞여 디버깅이 어려워질 수 있습니다.
                            currentAdView = null 
                            NapSspSdkIntegration.clearAllAds()
                            delay(200)

                            adSessionId = UUID.randomUUID().toString()
                            // 화면 안에 들어가는 광고는 View를 반환하고 높이를 지정합니다.
                            // 전체화면 계열(reward/interstitial)은 SDK 호출 자체가 표시 트리거이므로 null을 반환합니다.
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
                // loadAd ACK 이후 실제 광고 상태 변화는 여기서 JS로 전달됩니다.
                // 예: loaded, displayed, clicked, failed 등 SDK callback 기반 이벤트.
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
                                // Android View는 동시에 하나의 parent만 가질 수 있습니다.
                                // 이미 다른 컨테이너에 붙어 있던 광고 View라면 먼저 떼고 새 FrameLayout에 붙입니다.
                                (adView.parent as? ViewGroup)?.removeView(adView)
                                adView.layoutParams = FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT
                                )
                                addView(adView)
                                if (adView is AMMBannerView) adView.onResume()
                                adView.requestLayout()
                            }
                        }
                    }
                )
            }
        }
    }
}
