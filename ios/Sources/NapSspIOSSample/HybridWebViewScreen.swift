import SwiftUI
import WebKit

final class NapSspHybridBridge: NSObject, WKScriptMessageHandler {
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        print("NapSsp hybrid bridge message: \(message.body)")
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
        webView.loadHTMLString(sampleHybridHTML, baseURL: nil)
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) { }
}
