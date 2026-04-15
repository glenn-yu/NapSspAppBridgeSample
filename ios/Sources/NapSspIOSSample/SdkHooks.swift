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

    static func banner() -> String { "Banner SDK hook ready" }
    static func native() -> String { "Native SDK hook ready" }
    static func video() -> String { "Video SDK hook ready" }
    static func rewardVideo() -> String { "Reward video SDK hook ready" }
    static func interstitialVideo() -> String { "Interstitial video SDK hook ready" }
    static func hybridWebView() -> String { "WebView hybrid bridge ready" }

    static func execute(_ format: SampleFormat) -> String {
        switch format {
        case .banner:
            _ = NapSspSdkIntegration.banner()
            return "banner executed"
        case .native:
            _ = NapSspSdkIntegration.native()
            return "native executed"
        case .video:
            _ = NapSspSdkIntegration.video()
            return "video executed"
        case .rewardVideo:
            _ = NapSspSdkIntegration.rewardVideo()
            return "reward executed"
        case .interstitialVideo:
            _ = NapSspSdkIntegration.interstitialVideo()
            return "interstitial executed"
        case .hybridWebView:
            return "hybrid uses WebView bridge"
        }
    }

    static func hybridStatus() -> String {
        "NapSsp hybrid bridge ready"
    }
}
