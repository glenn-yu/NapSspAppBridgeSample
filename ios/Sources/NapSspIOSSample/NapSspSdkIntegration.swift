import Foundation
import SwiftUI

struct NapSspSdkIntegration {
    static func initialize() {
        AdEventLogger.request(format: "initialize", id: NapSspConfig.mediaKey)
        if NSClassFromString("AMMediation") != nil {
            AdEventLogger.loaded(format: "initialize", id: NapSspConfig.mediaKey)
            print("NapSsp iOS SDK initialize hook ready")
        } else {
            AdEventLogger.failed(format: "initialize", id: NapSspConfig.mediaKey, reason: "AMMediation not found")
        }
    }

    static func banner() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["banner_320x100"] ?? ""
        AdEventLogger.request(format: "banner", id: adUnitId)
        if NSClassFromString("AMMBannerView") != nil {
            AdEventLogger.loaded(format: "banner", id: adUnitId)
            AdEventLogger.displayed(format: "banner", id: adUnitId)
            return "banner ready: \(adUnitId)"
        }
        AdEventLogger.failed(format: "banner", id: adUnitId, reason: "AMMBannerView not found")
        return "banner fallback: \(adUnitId)"
    }

    static func native() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["native"] ?? ""
        AdEventLogger.request(format: "native", id: adUnitId)
        if NSClassFromString("AMMNativeAdViewContainer") != nil {
            AdEventLogger.loaded(format: "native", id: adUnitId)
            AdEventLogger.displayed(format: "native", id: adUnitId)
            return "native ready: \(adUnitId)"
        }
        AdEventLogger.failed(format: "native", id: adUnitId, reason: "AMMNativeAdViewContainer not found")
        return "native fallback: \(adUnitId)"
    }

    static func video() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["outstream_video"] ?? ""
        AdEventLogger.request(format: "video", id: adUnitId)
        if NSClassFromString("AMMVideoAdView") != nil {
            AdEventLogger.loaded(format: "video", id: adUnitId)
            AdEventLogger.displayed(format: "video", id: adUnitId)
            return "video ready: \(adUnitId)"
        }
        AdEventLogger.failed(format: "video", id: adUnitId, reason: "AMMVideoAdView not found")
        return "video fallback: \(adUnitId)"
    }

    static func rewardVideo() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["reward_video"] ?? ""
        AdEventLogger.request(format: "rewardVideo", id: adUnitId)
        if NSClassFromString("AMMRewardVideo") != nil {
            AdEventLogger.loaded(format: "rewardVideo", id: adUnitId)
            AdEventLogger.displayed(format: "rewardVideo", id: adUnitId)
            return "reward ready: \(adUnitId)"
        }
        AdEventLogger.failed(format: "rewardVideo", id: adUnitId, reason: "AMMRewardVideo not found")
        return "reward fallback: \(adUnitId)"
    }

    static func interstitialVideo() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["interstitial_320x480"] ?? ""
        AdEventLogger.request(format: "interstitialVideo", id: adUnitId)
        if NSClassFromString("AMMVideoInterstitial") != nil {
            AdEventLogger.loaded(format: "interstitialVideo", id: adUnitId)
            AdEventLogger.displayed(format: "interstitialVideo", id: adUnitId)
            return "interstitial ready: \(adUnitId)"
        }
        AdEventLogger.failed(format: "interstitialVideo", id: adUnitId, reason: "AMMVideoInterstitial not found")
        return "interstitial fallback: \(adUnitId)"
    }
}
