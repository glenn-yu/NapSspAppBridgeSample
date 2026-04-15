import Foundation

enum SdkHooks {
    static func describe(_ format: SampleFormat) -> String {
        switch format {
        case .banner: return "Banner SDK hook here"
        case .native: return "Native SDK hook here"
        case .video: return "Video SDK hook here"
        case .rewardVideo: return "Reward video SDK hook here"
        case .interstitialVideo: return "Interstitial video SDK hook here"
        case .hybridWebView: return "WebView hybrid bridge hook here"
        }
    }

    static func hybridStatus() -> String {
        "NapSsp hybrid bridge ready"
    }
}
