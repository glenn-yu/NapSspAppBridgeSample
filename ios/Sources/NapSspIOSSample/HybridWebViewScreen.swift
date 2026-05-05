#if os(iOS) && canImport(UIKit)
import SwiftUI
import WebKit
import AdMixer
import AdMixerMediation

// JSON 브릿지 데이터 구조
struct HybridRequest: Codable {
    let action: String
    let params: [String: String]?
}

struct HybridResponse: Codable {
    let action: String
    let status: String
    let data: String // JS에서 JSON.parse를 일관되게 쓸 수 있도록 문자열로 전달하거나 구조화된 데이터를 담습니다.
}

// 웹뷰 브릿지 핸들러
final class NapSspHybridBridge: NSObject, WKScriptMessageHandler {
    weak var webView: WKWebView?
    var onAdLoaded: ((UIView?, CGFloat) -> Void)?

    private let supportedFormats: Set<String> = [
        "banner",
        "native",
        "video",
        "rewardVideo",
        "interstitialVideo",
        "interstitialBanner"
    ]

    override init() {
        super.init()
        // SDK 통합 모듈의 콜백 연결
        NapSspSdkIntegration.shared.onAdEventCallback = { [weak self] event, format, detail in
            self?.sendResponse(action: "event", status: "success", data: "[\(format)] \(event): \(detail)")
        }
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        guard let body = message.body as? String,
              let data = body.data(using: .utf8),
              let request = try? JSONDecoder().decode(HybridRequest.self, from: data) else {
            sendResponse(action: "error", status: "error", data: "Invalid JSON")
            return
        }

        switch request.action {
        case "init":
            NapSspSdkIntegration.initializeSdk()
            sendResponse(action: "init", status: "success", data: "SDK Initialized")
        case "loadAd":
            guard let format = request.params?["format"], supportedFormats.contains(format) else {
                sendResponse(action: "loadAd", status: "error", data: "Unsupported format: \(request.params?["format"] ?? "")")
                return
            }
            let adUnitId = request.params?["adUnitId"]
            handleLoadAd(format: format, adUnitId: adUnitId)
        case "clearAds":
            DispatchQueue.main.async {
                self.onAdLoaded?(nil, 0)
                NapSspSdkIntegration.clearAllAds()
                self.sendResponse(action: "clearAds", status: "success", data: "All ads cleared")
            }
        default:
            sendResponse(action: request.action, status: "error", data: "Unknown action")
        }
    }

    private func handleLoadAd(format: String, adUnitId: String? = nil) {
        let customId = adUnitId.flatMap { Int($0) }
        
        DispatchQueue.main.async {
            guard let rootVC = UIApplication.shared.windows.first?.rootViewController else {
                self.sendResponse(action: "loadAd", status: "error", data: "Root view controller not found")
                return
            }
            var view: UIView? = nil
            var height: CGFloat = 0
            
            switch format {
            case "banner": 
                view = NapSspSdkIntegration.banner(rootVC: rootVC, customAdUnitId: customId)
                height = 100
            case "native": 
                view = NapSspSdkIntegration.native(rootVC: rootVC, customAdUnitId: customId)
                height = 400
            case "video": 
                view = NapSspSdkIntegration.video(rootVC: rootVC, customAdUnitId: customId)
                height = 250
            case "rewardVideo": NapSspSdkIntegration.rewardVideo(rootVC: rootVC, customAdUnitId: customId)
            case "interstitialVideo": NapSspSdkIntegration.interstitialVideo(rootVC: rootVC, customAdUnitId: customId)
            case "interstitialBanner": NapSspSdkIntegration.interstitialBanner(rootVC: rootVC, customAdUnitId: customId)
            default: break
            }
            
            self.onAdLoaded?(view, height)
            // loadAd에 대한 즉시 응답 (이벤트는 나중에 event 액션으로 별도 전달됨)
            self.sendResponse(action: "loadAd", status: "success", data: "Accepted \(format)")
        }
    }

    private func sendResponse(action: String, status: String, data: String) {
        let responseDict: [String: Any] = [
            "action": action,
            "status": status,
            "data": data
        ]
        
        guard let jsonData = try? JSONSerialization.data(withJSONObject: responseDict, options: []),
              let jsonStr = String(data: jsonData, encoding: .utf8),
              let jsArgData = try? JSONSerialization.data(withJSONObject: jsonStr, options: [.fragmentsAllowed]),
              let jsArg = String(data: jsArgData, encoding: .utf8) else { return }
              
        DispatchQueue.main.async {
            self.webView?.evaluateJavaScript("window.onNapSspMessage && window.onNapSspMessage(\(jsArg))", completionHandler: nil)
        }
    }
}

// 순환 참조 방지를 위한 프록시 클래스
final class LeakAvoider: NSObject, WKScriptMessageHandler {
    weak var delegate: WKScriptMessageHandler?
    init(_ delegate: WKScriptMessageHandler) {
        self.delegate = delegate
        super.init()
    }
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        delegate?.userContentController(userContentController, didReceive: message)
    }
}

struct WebViewContainer: UIViewRepresentable {
    let bridge: NapSspHybridBridge
    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        // LeakAvoider를 사용하여 순환 참조 방지
        config.userContentController.add(LeakAvoider(bridge), name: "NapSspBridge")
        config.allowsInlineMediaPlayback = true
        config.mediaTypesRequiringUserActionForPlayback = []
        let webView = WKWebView(frame: .zero, configuration: config)
        bridge.webView = webView
        webView.evaluateJavaScript("navigator.userAgent") { result, _ in
            if let oldUA = result as? String {
                webView.customUserAgent = oldUA + " NapSspHybridBridge"
            }
        }
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
                    bridge.onAdLoaded = { view, height in
                        self.adView = view
                        self.adHeight = height
                        self.adViewId = UUID()

                        if let _ = view as? AMMBannerView { self.adHeight = 100 }
                        else if let _ = view as? AMMNativeAdViewContainer { self.adHeight = 350 }
                        else if let _ = view as? AMMVideoView { self.adHeight = 250 }
                        else { self.adHeight = 0 }
                    }
                }

            if let view = adView, adHeight > 0 {
                Divider()
                AdViewRepresentable(adView: view)
                    .id(adViewId)
                    .frame(maxWidth: .infinity)
                    .frame(height: adHeight) // 🎯 동적 높이 할당
                    .background(Color(UIColor.secondarySystemBackground))
            }
        }
        .onDisappear {
            NapSspSdkIntegration.clearAllAds()
        }
    }
}

struct AdViewRepresentable: UIViewRepresentable {
    let adView: UIView
    func makeUIView(context: Context) -> UIView {
        adView.setContentCompressionResistancePriority(.required, for: .vertical)
        adView.setContentHuggingPriority(.required, for: .vertical)
        return adView
    }
    func updateUIView(_ uiView: UIView, context: Context) {}
}
#endif
