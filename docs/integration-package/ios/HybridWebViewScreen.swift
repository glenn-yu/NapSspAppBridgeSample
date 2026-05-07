#if os(iOS) && canImport(UIKit)
import SwiftUI
import WebKit
import AdMixer
import AdMixerMediation

// ──────────────────────────────────────────────────────────────────────────────
// 메시지 구조체
// ──────────────────────────────────────────────────────────────────────────────

/** JS → Native 요청 payload */
struct HybridRequest: Codable {
    let action: String
    let params: [String: String]?
}

// ──────────────────────────────────────────────────────────────────────────────
// JS ↔ Native 브릿지
//
// JS에서:
//   iOS: window.webkit.messageHandlers.NapSspBridge.postMessage(jsonString)
//   응답: window.onNapSspMessage(responseJsonString)
// ──────────────────────────────────────────────────────────────────────────────

final class NapSspHybridBridge: NSObject, WKScriptMessageHandler {
    weak var webView: WKWebView?
    var onAdLoaded: ((UIView?, CGFloat) -> Void)?

    private var lastActionTime: Date = .distantPast

    private let supportedFormats: Set<String> = [
        "banner", "native", "video",
        "rewardVideo", "interstitialVideo", "interstitialBanner"
    ]

    override init() {
        super.init()
        // SDK 이벤트 → JS 콜백 연결
        NapSspSdkIntegration.shared.onAdEventCallback = { [weak self] event, format, detail in
            self?.sendResponse(action: "event", status: "success", data: "[\(format)] \(event): \(detail)")
        }
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        // 0.5초 이내 중복 요청 무시 (버튼 연타 방지)
        let now = Date()
        guard now.timeIntervalSince(lastActionTime) >= 0.5 else { return }
        lastActionTime = now

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
                sendResponse(action: "loadAd", status: "error",
                             data: "Unsupported format: \(request.params?["format"] ?? "")")
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
        // JS에서 문자열로 전달된 adUnitId를 SDK용 Int로 변환
        let customId = adUnitId.flatMap { Int($0) }

        DispatchQueue.main.async {
            guard let rootVC = UIApplication.shared.connectedScenes
                .compactMap({ $0 as? UIWindowScene })
                .flatMap({ $0.windows })
                .first(where: { $0.isKeyWindow })?.rootViewController else {
                self.sendResponse(action: "loadAd", status: "error", data: "Root VC not found")
                return
            }

            var view: UIView? = nil
            var height: CGFloat = 0

            // 배너/네이티브/비디오는 UIView를 반환 → SwiftUI 하단에 표시
            // 전면/보상형은 SDK가 전체화면으로 직접 표시
            switch format {
            case "banner":             view = NapSspSdkIntegration.banner(rootVC: rootVC, customAdUnitId: customId);             height = 100
            case "native":             view = NapSspSdkIntegration.native(rootVC: rootVC, customAdUnitId: customId);             height = 400
            case "video":              view = NapSspSdkIntegration.video(rootVC: rootVC, customAdUnitId: customId);              height = 250
            case "rewardVideo":        NapSspSdkIntegration.rewardVideo(rootVC: rootVC, customAdUnitId: customId)
            case "interstitialVideo":  NapSspSdkIntegration.interstitialVideo(rootVC: rootVC, customAdUnitId: customId)
            case "interstitialBanner": NapSspSdkIntegration.interstitialBanner(rootVC: rootVC, customAdUnitId: customId)
            default: break
            }

            self.onAdLoaded?(view, height)
            // ACK 응답 (광고 로드 성공이 아님 — 실제 결과는 event action으로 별도 전달)
            self.sendResponse(action: "loadAd", status: "success", data: "Accepted \(format)")
        }
    }

    func sendResponse(action: String, status: String, data: String) {
        let responseDict: [String: Any] = ["action": action, "status": status, "data": data]
        // JS 문자열 리터럴로 안전하게 이중 escape
        guard let jsonData = try? JSONSerialization.data(withJSONObject: responseDict, options: []),
              let jsonStr = String(data: jsonData, encoding: .utf8),
              let jsArgData = try? JSONSerialization.data(withJSONObject: jsonStr, options: [.fragmentsAllowed]),
              let jsArg = String(data: jsArgData, encoding: .utf8) else { return }
        DispatchQueue.main.async {
            self.webView?.evaluateJavaScript(
                "window.onNapSspMessage && window.onNapSspMessage(\(jsArg))", completionHandler: nil
            )
        }
    }
}

// WKWebView retain cycle 방지 프록시
final class LeakAvoider: NSObject, WKScriptMessageHandler {
    weak var delegate: WKScriptMessageHandler?
    init(_ delegate: WKScriptMessageHandler) { self.delegate = delegate; super.init() }
    func userContentController(_ ucc: WKUserContentController, didReceive message: WKScriptMessage) {
        delegate?.userContentController(ucc, didReceive: message)
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// WebView UIViewRepresentable
// ──────────────────────────────────────────────────────────────────────────────

struct WebViewContainer: UIViewRepresentable {
    let bridge: NapSspHybridBridge

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        config.userContentController.add(LeakAvoider(bridge), name: "NapSspBridge")
        config.allowsInlineMediaPlayback = true
        config.mediaTypesRequiringUserActionForPlayback = []

        let webView = WKWebView(frame: .zero, configuration: config)
        bridge.webView = webView

        // TODO: 실제 웹 페이지 URL 또는 Bundle 파일로 교체
        //   서버 URL 예시:  webView.load(URLRequest(url: URL(string: "https://your.domain.com")!))
        //   Bundle 파일 예시 (현재):
        if let url = Bundle.main.url(forResource: "index", withExtension: "html") {
            webView.loadFileURL(url, allowingReadAccessTo: url.deletingLastPathComponent())
        }
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}
}

// ──────────────────────────────────────────────────────────────────────────────
// SwiftUI 화면 — WebView + 광고 View 영역
// ──────────────────────────────────────────────────────────────────────────────

struct HybridWebViewScreen: View {
    @State private var adView: UIView? = nil
    @State private var adHeight: CGFloat = 0
    @State private var adViewId = UUID()
    private let bridge = NapSspHybridBridge()

    var body: some View {
        VStack(spacing: 0) {
            // WebView 영역
            WebViewContainer(bridge: bridge)
                .onAppear {
                    bridge.onAdLoaded = { view, _ in
                        self.adView = view
                        self.adViewId = UUID()
                        // 타입별 높이 보정
                        if view is AMMBannerView                { self.adHeight = 100 }
                        else if view is AMMNativeAdViewContainer { self.adHeight = 350 }
                        else if view is AMMVideoView             { self.adHeight = 250 }
                        else                                     { self.adHeight = 0 }
                    }
                }

            // 광고 View 영역 (배너/네이티브/비디오만 표시)
            if let view = adView, adHeight > 0 {
                Divider()
                AdViewRepresentable(adView: view)
                    .id(adViewId)
                    .frame(maxWidth: .infinity)
                    .frame(height: adHeight)
                    .background(Color(UIColor.secondarySystemBackground))
            }
        }
        .onDisappear { NapSspSdkIntegration.clearAllAds() }
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
