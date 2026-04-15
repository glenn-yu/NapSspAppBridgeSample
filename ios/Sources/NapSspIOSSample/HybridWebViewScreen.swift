import SwiftUI
import WebKit

final class NapSspHybridBridge: NSObject, WKScriptMessageHandler {
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        print("NapSsp hybrid bridge message: \(message.body)")
    }
}

struct HybridWebViewScreen: UIViewRepresentable {
    let urlString: String

    func makeCoordinator() -> NapSspHybridBridge {
        NapSspHybridBridge()
    }

    func makeUIView(context: Context) -> WKWebView {
        let contentController = WKUserContentController()
        contentController.add(context.coordinator, name: "NapSspBridge")

        let config = WKWebViewConfiguration()
        config.userContentController = contentController

        let webView = WKWebView(frame: .zero, configuration: config)
        if let url = URL(string: urlString) {
            webView.load(URLRequest(url: url))
        }
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {
        guard let url = URL(string: urlString), uiView.url != url else { return }
        uiView.load(URLRequest(url: url))
    }
}
