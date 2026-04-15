import SwiftUI
import WebKit

final class NapSspHybridBridge: NSObject, WKScriptMessageHandler {
    weak var webView: WKWebView?

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        print("NapSsp hybrid bridge message: \(message.body)")
        let ack: String
        switch String(describing: message.body) {
        case "init": ack = "init ok"
        case "loadBanner": ack = "banner hook ok"
        case "loadNative": ack = "native hook ok"
        case "loadVideo": ack = "video hook ok"
        case "loadRewardVideo": ack = "reward hook ok"
        case "loadInterstitialVideo": ack = "interstitial hook ok"
        default: ack = "unknown message"
        }
        webView?.evaluateJavaScript("window.__napSspAck && window.__napSspAck('\(ack)')")
    }
}

private let sampleHybridHTML = """
<!doctype html>
<html>
<head>
  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />
  <title>NapSsp Hybrid</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, sans-serif; padding: 24px; }
    button { display:block; width:100%; margin: 8px 0; padding: 12px; font-size: 16px; }
    #log { margin-top: 16px; padding: 12px; background: #f2f2f2; min-height: 60px; }
  </style>
</head>
<body>
  <h1>NapSsp Hybrid WKWebView</h1>
  <p>브리지 메시지 버튼을 눌러 네이티브로 전달한다.</p>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('init')\">init</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('loadBanner')\">loadBanner</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('loadNative')\">loadNative</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('loadVideo')\">loadVideo</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('loadRewardVideo')\">loadRewardVideo</button>
  <button onclick=\"window.webkit.messageHandlers.NapSspBridge.postMessage('loadInterstitialVideo')\">loadInterstitialVideo</button>
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
