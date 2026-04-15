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

    static func hybridStatus() -> String {
        "NapSsp hybrid bridge ready"
    }
}
