import Foundation

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

    static func banner() -> String { "배너 광고를 붙일 준비가 됨" }
    static func native() -> String { "네이티브 광고를 붙일 준비가 됨" }
    static func video() -> String { "동영상 광고를 붙일 준비가 됨" }
    static func rewardVideo() -> String { "리워드 동영상 광고를 붙일 준비가 됨" }
    static func interstitialVideo() -> String { "전면 동영상 광고를 붙일 준비가 됨" }
    static func hybridWebView() -> String { "웹뷰에서 네이티브 브리지를 사용할 준비가 됨" }

    static func execute(_ format: SampleFormat) -> String {
        switch format {
        case .banner:
            _ = NapSspSdkIntegration.banner()
            return "배너 광고 실행"
        case .native:
            _ = NapSspSdkIntegration.native()
            return "네이티브 광고 실행"
        case .video:
            _ = NapSspSdkIntegration.video()
            return "동영상 광고 실행"
        case .rewardVideo:
            _ = NapSspSdkIntegration.rewardVideo()
            return "리워드 동영상 광고 실행"
        case .interstitialVideo:
            _ = NapSspSdkIntegration.interstitialVideo()
            return "전면 동영상 광고 실행"
        case .hybridWebView:
            return "웹뷰 브리지 실행"
        }
    }

    static func hybridStatus() -> String {
        "NapSsp hybrid bridge ready"
    }
}
