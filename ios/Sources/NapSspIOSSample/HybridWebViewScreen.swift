#if os(iOS) && canImport(UIKit)
import SwiftUI
import WebKit
import AdMixer

// JSON 브릿지 데이터 구조
struct HybridRequest: Codable {
    let action: String
    let params: [String: String]?
}

struct HybridResponse: Codable {
    let action: String
    let status: String
    let data: String
}

// 웹뷰 브릿지 핸들러
final class NapSspHybridBridge: NSObject, WKScriptMessageHandler {
    weak var webView: WKWebView?
    var onAdLoaded: ((UIView?) -> Void)?

    override init() {
        super.init()
        NapSspSdkIntegration.shared.onAdEventCallback = { [weak self] event, format, detail in
            self?.sendResponse(action: "event", status: "success", data: "SDK: \(event) | \(format)")
        }
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        guard let body = message.body as? String,
              let data = body.data(using: .utf8),
              let request = try? JSONDecoder().decode(HybridRequest.self, from: data) else { return }

        switch request.action {
        case "init":
            NapSspSdkIntegration.initialize()
            sendResponse(action: "init", status: "success", data: "Initialized")
        case "loadAd":
            if let format = request.params?["format"] {
                handleLoadAd(format: format)
            }
        case "clearAds":
            DispatchQueue.main.async {
                self.onAdLoaded?(nil)
                NapSspSdkIntegration.clearAllAds()
                self.sendResponse(action: "clearAds", status: "success", data: "All ads cleared")
            }
        default:
            sendResponse(action: request.action, status: "error", data: "Unknown")
        }
    }

    private func handleLoadAd(format: String) {
        DispatchQueue.main.async {
            guard let rootVC = UIApplication.shared.windows.first?.rootViewController else { return }
            var view: UIView? = nil
            
            switch format {
            case "banner": view = NapSspSdkIntegration.banner(rootVC: rootVC)
            case "native": view = NapSspSdkIntegration.native(rootVC: rootVC)
            case "video": view = NapSspSdkIntegration.video(rootVC: rootVC)
            case "rewardVideo": NapSspSdkIntegration.rewardVideo(rootVC: rootVC)
            case "interstitialVideo": NapSspSdkIntegration.interstitialVideo(rootVC: rootVC)
            case "interstitialBanner": NapSspSdkIntegration.interstitialBanner(rootVC: rootVC)
            default: break
            }
            
            self.onAdLoaded?(view)
            self.sendResponse(action: "loadAd", status: "success", data: "Triggered \(format)")
        }
    }

    private func sendResponse(action: String, status: String, data: String) {
        let response = HybridResponse(action: action, status: status, data: data)
        if let jsonData = try? JSONEncoder().encode(response),
           let jsonStr = String(data: jsonData, encoding: .utf8) {
            let escaped = jsonStr.replacingOccurrences(of: "'", with: "\\'")
            DispatchQueue.main.async {
                self.webView?.evaluateJavaScript("window.onNapSspMessage && window.onNapSspMessage('\(escaped)')")
            }
        }
    }
}

struct WebViewContainer: UIViewRepresentable {
    let bridge: NapSspHybridBridge
    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        config.userContentController.add(bridge, name: "NapSspBridge")
        let webView = WKWebView(frame: .zero, configuration: config)
        bridge.webView = webView
        if let url = Bundle.main.url(forResource: "index", withExtension: "html") {
            webView.loadFileURL(url, allowingReadAccessTo: url.deletingLastPathComponent())
        }
        return webView
    }
    func updateUIView(_ uiView: WKWebView, context: Context) {}
}

struct HybridWebViewScreen: View {
    @State private var adView: UIView? = nil
    @State private var adHeight: CGFloat = 0
    @State private var adViewId = UUID()
    
    private let bridge = NapSspHybridBridge()

    var body: some View {
        VStack(spacing: 0) {
            WebViewContainer(bridge: bridge)
                .onAppear {
                    bridge.onAdLoaded = { view in
                        self.adView = view
                        self.adViewId = UUID()
                        
                        if let _ = view as? AMMBannerView { self.adHeight = 100 }
                        else if let _ = view as? AMMNativeAdViewContainer { self.adHeight = 350 }
                        else if let _ = view as? AMMVideoAdView { self.adHeight = 250 }
                        else { self.adHeight = 0 }
                    }
                }

            if let view = adView, adHeight > 0 {
                Divider()
                AdViewRepresentable(adView: view)
                    .id(adViewId)
                    .frame(maxWidth: .infinity)
                    .frame(height: adHeight)
                    .background(Color(UIColor.secondarySystemBackground))
            }
        }
        .onDisappear {
            // 화면 종료 시 모든 광고 자원 해제
            NapSspSdkIntegration.clearAllAds()
        }
    }
}

struct AdViewRepresentable: UIViewRepresentable {
    let adView: UIView
    func makeUIView(context: Context) -> UIView {
        adView.setContentHuggingPriority(.defaultLow, for: .horizontal)
        adView.setContentHuggingPriority(.defaultLow, for: .vertical)
        return adView
    }
    func updateUIView(_ uiView: UIView, context: Context) {}
}
#endif
