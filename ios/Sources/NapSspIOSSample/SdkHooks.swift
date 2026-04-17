import Foundation
import UIKit

enum SdkHooks {
    static func describe(_ format: SampleFormat) -> String {
        switch format {
        case .banner: return banner()
        case .native: return native()
        case .video: return video()
        case .rewardVideo: return rewardVideo()
        case .interstitialVideo: return interstitialVideo()
        case .hybridWebView: return hybridWebView()
        }
    }

    static func banner() -> String { "배너 광고 준비됨" }
    static func native() -> String { "네이티브 광고 준비됨" }
    static func video() -> String { "동영상 광고 준비됨" }
    static func rewardVideo() -> String { "리워드 광고 준비됨" }
    static func interstitialVideo() -> String { "전면 광고 준비됨" }
    static func hybridWebView() -> String { "웹뷰 브리지 준비됨" }

    static func execute(_ format: SampleFormat) -> String {
        guard let rootVC = rootViewController() else {
            return "루트 뷰컨트롤러를 찾지 못함"
        }

        switch format {
        case .banner:
            _ = NapSspSdkIntegration.banner(rootVC: rootVC)
            return "배너 광고 실행"
        case .native:
            _ = NapSspSdkIntegration.native(rootVC: rootVC)
            return "네이티브 광고 실행"
        case .video:
            _ = NapSspSdkIntegration.video(rootVC: rootVC)
            return "동영상 광고 실행"
        case .rewardVideo:
            NapSspSdkIntegration.rewardVideo(rootVC: rootVC)
            return "리워드 동영상 광고 실행"
        case .interstitialVideo:
            NapSspSdkIntegration.interstitialVideo(rootVC: rootVC)
            return "전면 동영상 광고 실행"
        case .hybridWebView:
            return "웹뷰 브리지 실행"
        }
    }

    private static func rootViewController() -> UIViewController? {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first(where: \.isKeyWindow)?
            .rootViewController
    }

    static func hybridStatus() -> String {
        "NapSsp hybrid bridge ready"
    }
}
