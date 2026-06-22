#if os(iOS) && canImport(UIKit)
import SwiftUI
import WebKit
import AdMixer
import AdMixerMediation

/// JavaScript에서 Native로 보내는 요청 payload입니다.
///
/// JS는 `window.webkit.messageHandlers.NapSspBridge.postMessage(...)`로 문자열을 보내고,
/// iOS는 그 문자열을 JSON으로 decode해 이 구조체로 변환합니다.
/// `action`은 `init`, `loadAd`, `clearAds`처럼 수행할 작업을 의미하고,
/// `params`에는 광고 포맷(format), 광고 유닛 ID(adUnitId) 같은 부가 정보를 담습니다.
struct HybridRequest: Codable {
    let action: String
    /// JS가 요청마다 발급하는 상관관계 ID입니다(옵션). 응답과 이후 SDK 이벤트에 그대로 echo됩니다.
    let requestId: String?
    let params: [String: String]?
}

/// Native에서 JavaScript로 돌려주는 응답 payload입니다.
///
/// 이 응답은 `window.onNapSspMessage(...)` 콜백으로 전달됩니다.
/// `loadAd`의 success 응답은 "광고 로드 성공"이 아니라 "요청 접수 완료(ACK)"입니다.
/// 실제 광고 로드/노출/클릭/실패 여부는 SDK callback을 통해 `event` action으로 별도 전달됩니다.
struct HybridResponse: Codable {
    let action: String
    let status: String
    let data: String // JS에서 JSON.parse를 일관되게 쓸 수 있도록 문자열로 전달하거나 구조화된 데이터를 담습니다.
    /// 요청에 requestId가 있었을 때만 채워져 JS가 응답을 원래 요청과 짝지을 수 있게 합니다.
    let requestId: String?
}

/// WKWebView와 iOS Native 광고 SDK 사이를 연결하는 브릿지입니다.
///
/// 전체 흐름은 다음과 같습니다.
/// 1. HTML/JavaScript가 `NapSspBridge` message handler로 JSON 문자열을 보냅니다.
/// 2. 이 클래스가 JSON을 파싱해 `init`, `loadAd`, `clearAds` action을 구분합니다.
/// 3. 광고 SDK 호출은 `NapSspSdkIntegration`에 위임합니다.
/// 4. 화면에 붙여야 하는 광고 View는 `onAdLoaded`를 통해 SwiftUI 화면으로 전달합니다.
/// 5. 처리 결과와 SDK 이벤트는 `window.onNapSspMessage(...)`로 다시 JS에 전달합니다.
final class NapSspHybridBridge: NSObject, WKScriptMessageHandler {
    weak var webView: WKWebView?
    var onAdLoaded: ((UIView?, CGFloat) -> Void)?

    private var lastActionTime: Date = .distantPast

    // 가장 최근 loadAd 요청의 requestId입니다.
    // loadAd ACK 이후 비동기로 도착하는 SDK 이벤트(loaded/displayed/clicked 등)를
    // 어느 요청에서 비롯됐는지 JS가 매핑할 수 있도록, 이벤트 응답에 이 값을 echo합니다.
    private var activeRequestId: String?

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
        // SDK 통합 모듈의 콜백 연결.
        // loadAd ACK 이후 실제 광고 상태 변화(loaded/displayed/clicked/failed 등)는
        // 이 callback을 통해 JS의 `event` action으로 전달됩니다.
        NapSspSdkIntegration.shared.onAdEventCallback = { [weak self] event, format, detail in
            // 이벤트는 가장 최근 loadAd 요청에서 비롯되므로 그 requestId를 함께 echo합니다.
            self?.sendResponse(action: "event", status: "success", data: "[\(format)] \(event): \(detail)", requestId: self?.activeRequestId)
        }
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        // requestId까지 echo하려면 throttle 차단 시점에도 requestId를 알아야 하므로,
        // debounce 체크보다 먼저 JSON을 디코드합니다. 디코드 자체가 실패하면 requestId 없이 error를 돌려줍니다.
        guard let body = message.body as? String,
              let data = body.data(using: .utf8),
              let request = try? JSONDecoder().decode(HybridRequest.self, from: data) else {
            sendResponse(action: "error", status: "error", data: "Invalid JSON")
            return
        }

        let requestId = request.requestId

        // Android debounce와 동일하게 0.5초 이내 중복 요청은 처리하지 않고, busy 상태로 즉시 응답합니다.
        let now = Date()
        guard now.timeIntervalSince(lastActionTime) >= 0.5 else {
            sendResponse(action: "busy", status: "busy", data: "Request ignored within 500ms throttle window", requestId: requestId)
            return
        }
        lastActionTime = now

        switch request.action {
        case "init":
            NapSspSdkIntegration.initializeSdk()
            sendResponse(action: "init", status: "success", data: "SDK Initialized", requestId: requestId)
        case "loadAd":
            guard let format = request.params?["format"], supportedFormats.contains(format) else {
                sendResponse(action: "loadAd", status: "error", data: "Unsupported format: \(request.params?["format"] ?? "")", requestId: requestId)
                return
            }
            let adUnitId = request.params?["adUnitId"]
            // 이후 비동기로 도착하는 SDK 이벤트를 이 요청과 매핑하기 위해 requestId를 보관합니다.
            activeRequestId = requestId
            handleLoadAd(format: format, adUnitId: adUnitId, requestId: requestId)
        case "clearAds":
            DispatchQueue.main.async {
                self.onAdLoaded?(nil, 0)
                NapSspSdkIntegration.clearAllAds()
                // 광고를 모두 정리했으므로 이후 지연 이벤트를 매핑할 활성 요청도 비웁니다.
                self.activeRequestId = nil
                self.sendResponse(action: "clearAds", status: "success", data: "All ads cleared", requestId: requestId)
            }
        default:
            sendResponse(action: request.action, status: "error", data: "Unknown action", requestId: requestId)
        }
    }

    private func handleLoadAd(format: String, adUnitId: String? = nil, requestId: String? = nil) {
        // JS에서는 문자열로 adUnitId가 들어오므로 SDK가 요구하는 Int 형태로 변환합니다.
        // 값이 비어 있거나 숫자가 아니면 nil로 두고 SDK 기본 설정을 사용합니다.
        let customId = adUnitId.flatMap { Int($0) }
        
        DispatchQueue.main.async {
            // 광고 View 생성과 화면 표시는 UIKit 메인 스레드에서 처리해야 합니다.
            // WKScriptMessageHandler 호출 위치와 관계없이 main queue로 고정해 UI race condition을 줄입니다.
            // UIApplication.shared.windows는 iOS 15+에서 deprecated — UIWindowScene 기반으로 교체
            guard let rootVC = UIApplication.shared.connectedScenes
                .compactMap({ $0 as? UIWindowScene })
                .flatMap({ $0.windows })
                .first(where: { $0.isKeyWindow })?.rootViewController else {
                self.sendResponse(action: "loadAd", status: "error", data: "Root view controller not found", requestId: requestId)
                return
            }
            // loadAd에 대한 즉시 응답입니다.
            // 이 응답은 "광고가 성공적으로 로드되었다"가 아니라 "Native가 요청을 받았다"는 ACK입니다.
            // Full-screen 광고는 SDK 호출 직후 화면을 덮을 수 있으므로, SDK 호출 전에 JS 로그로 ACK를 먼저 돌려줍니다.
            // 실제 성공/실패 이벤트는 SDK callback에서 `event` action으로 별도 전달됩니다.
            self.sendResponse(action: "loadAd", status: "success", data: "Accepted \(format)", requestId: requestId)

            var view: UIView? = nil
            var height: CGFloat = 0
            
            // 배너/네이티브/동영상 광고는 SwiftUI 화면 안에 붙일 UIView를 반환합니다.
            // 보상형/전면 광고는 SDK가 전체화면으로 직접 표시하므로 별도 UIView를 반환하지 않습니다.
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
        }
    }

    private func sendResponse(action: String, status: String, data: String, requestId: String? = nil) {
        var responseDict: [String: Any] = [
            "action": action,
            "status": status,
            "data": data
        ]
        // requestId는 JS가 보냈을 때만 echo합니다(없으면 필드 자체를 생략).
        if let requestId = requestId {
            responseDict["requestId"] = requestId
        }

        // JSON 문자열을 JavaScript 코드에 직접 끼워 넣으면 따옴표, 줄바꿈, 역슬래시 같은
        // 특수문자로 인해 JS 문법 오류가 발생할 수 있습니다.
        // 그래서 한 번 response JSON 문자열을 만들고, 다시 JS 문자열 리터럴로 안전하게 escape합니다.
        guard let jsonData = try? JSONSerialization.data(withJSONObject: responseDict, options: []),
              let jsonStr = String(data: jsonData, encoding: .utf8),
              let jsArgData = try? JSONSerialization.data(withJSONObject: jsonStr, options: [.fragmentsAllowed]),
              let jsArg = String(data: jsArgData, encoding: .utf8) else { return }
              
        DispatchQueue.main.async {
            self.webView?.evaluateJavaScript("window.onNapSspMessage && window.onNapSspMessage(\(jsArg))", completionHandler: nil)
        }
    }
}

/// WKUserContentController가 message handler를 강하게 참조하면서 생길 수 있는 순환 참조를 막는 프록시입니다.
///
/// `WKUserContentController -> handler -> WKWebView/bridge` 형태로 서로 강하게 잡으면
/// 화면이 사라져도 WebView와 bridge가 해제되지 않을 수 있습니다.
/// 이 클래스는 실제 delegate를 weak로 들고 있다가 메시지만 전달합니다.
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
        // LeakAvoider를 사용하여 순환 참조 방지.
        // JS에서는 `window.webkit.messageHandlers.NapSspBridge.postMessage(...)` 이름으로 접근합니다.
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
    // SwiftUI 화면에 붙일 수 있는 광고 View입니다.
    // 배너/네이티브/동영상처럼 화면 일부를 차지하는 광고만 이 값에 들어갑니다.
    // 전면/보상형 광고는 SDK가 자체적으로 전체화면을 띄우므로 nil 상태를 유지합니다.
    @State private var adView: UIView? = nil
    @State private var adHeight: CGFloat = 0

    // 같은 UIView를 SwiftUI에 다시 붙일 때 재사용 충돌이 날 수 있어 id를 갱신합니다.
    // 광고 요청마다 새 representable 인스턴스를 만들도록 유도하는 역할입니다.
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
        // 광고 View가 SwiftUI 레이아웃 안에서 의도한 높이를 유지하도록 우선순위를 높입니다.
        // 네이티브 광고처럼 내부 컨텐츠 높이가 중요한 View가 압축되어 잘리는 문제를 줄입니다.
        adView.setContentCompressionResistancePriority(.required, for: .vertical)
        adView.setContentHuggingPriority(.required, for: .vertical)
        return adView
    }
    func updateUIView(_ uiView: UIView, context: Context) {}
}
#endif
