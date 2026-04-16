#if os(iOS) && canImport(UIKit)
import SwiftUI
import WebKit

private enum HybridMessage: String {
    case initMessage = "init"
    case loadBanner = "loadBanner"
    case loadNative = "loadNative"
    case loadVideo = "loadVideo"
    case loadRewardVideo = "loadRewardVideo"
    case loadInterstitialVideo = "loadInterstitialVideo"
    case getStatus = "getStatus"
    case adRequest = "adRequest"
    case adLoaded = "adLoaded"
    case adDisplayed = "adDisplayed"
    case adClicked = "adClicked"
    case adFailed = "adFailed"
}

private final class NapSspHybridDispatcher {
    func handle(_ message: String) -> String {
        switch HybridMessage(rawValue: message) {
        case .initMessage:
            NapSspInitializer.initialize()
            return "init ok - 이제 광고 버튼을 눌러보세요"
        case .loadBanner:
            HybridEventBridge.logRequest("loadBanner")
            _ = NapSspSdkIntegration.banner()
            return "banner hook ok - 광고 뷰 시도 완료"
        case .loadNative:
            HybridEventBridge.logRequest("loadNative")
            _ = NapSspSdkIntegration.native()
            return "native hook ok - 광고 뷰 시도 완료"
        case .loadVideo:
            HybridEventBridge.logRequest("loadVideo")
            _ = NapSspSdkIntegration.video()
            return "video hook ok - 광고 뷰 시도 완료"
        case .loadRewardVideo:
            HybridEventBridge.logRequest("loadRewardVideo")
            _ = NapSspSdkIntegration.rewardVideo()
            return "reward hook ok - 광고 뷰 시도 완료"
        case .loadInterstitialVideo:
            HybridEventBridge.logRequest("loadInterstitialVideo")
            _ = NapSspSdkIntegration.interstitialVideo()
            return "interstitial hook ok - 광고 뷰 시도 완료"
        case .getStatus:
            return "status ok"
        case .adRequest:
            HybridEventBridge.logRequest(message)
            return "request logged"
        case .adLoaded:
            HybridEventBridge.logLoaded(message)
            return "loaded logged"
        case .adDisplayed:
            HybridEventBridge.logDisplayed(message)
            return "displayed logged"
        case .adClicked:
            HybridEventBridge.logClicked(message)
            return "clicked logged"
        case .adFailed:
            HybridEventBridge.logFailed(message, reason: "manual fail")
            return "failed logged"
        case .none:
            return "unknown message"
        }
    }
}

final class NapSspHybridBridge: NSObject, WKScriptMessageHandler {
    weak var webView: WKWebView?
    private let dispatcher = NapSspHybridDispatcher()

    override init() {
        super.init()
        // Register callback to notify WebView of asynchronous SDK events
        NapSspSdkIntegration.shared.onAdEventCallback = { [weak self] event, format, detail in
            let jsMessage = "SDK Event: \(event) | Format: \(format) | Detail: \(detail)"
            let escapedMessage = jsMessage.replacingOccurrences(of: "'", with: "\\'")
            DispatchQueue.main.async {
                self?.webView?.evaluateJavaScript("window.__napSspAck && window.__napSspAck('" + escapedMessage + "')")
            }
        }
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        let rawMessage = String(describing: message.body)
        print("NapSsp hybrid bridge message: \(rawMessage)")
        let ack = dispatcher.handle(rawMessage)
        webView?.evaluateJavaScript("window.__napSspAck && window.__napSspAck('" + ack.replacingOccurrences(of: "'", with: "\\'") + "')")
    }
}

private let sampleHybridHTML = """
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
  <h1>NapSsp Hybrid WKWebView</h1>
  <p>웹 버튼이 네이티브 광고 코드를 부른다.</p>
  <button class=\"primary\" onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('init')\">init</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('loadBanner')\">loadBanner</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('loadNative')\">loadNative</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('loadVideo')\">loadVideo</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('loadRewardVideo')\">loadRewardVideo</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('loadInterstitialVideo')\">loadInterstitialVideo</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('adRequest')\">adRequest</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('adLoaded')\">adLoaded</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('adDisplayed')\">adDisplayed</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('adClicked')\">adClicked</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('adFailed')\">adFailed</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('getStatus')\">getStatus</button>
  <div id=\"log\">status: waiting</div>
  <script>
    window.__napSspAck = function(message) {
      document.getElementById('log').textContent = 'status: ' + message
    }
  </script>
</body>
</html>
"""

struct HybridWebViewScreen: UIViewRepresentable {
    func makeCoordinator() -> NapSspHybridBridge {
        NapSspHybridBridge()
    }

    func makeUIView(context: Context) -> WKWebView {
        let contentController = WKUserContentController()
        contentController.add(context.coordinator, name: "NapSspBridge")

        let config = WKWebViewConfiguration()
        config.userContentController = contentController

        let webView = WKWebView(frame: .zero, configuration: config)
        context.coordinator.webView = webView
        webView.loadHTMLString(sampleHybridHTML, baseURL: nil)
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) { }
}
#endif
