import Foundation
import SwiftUI

#if canImport(UIKit)
import UIKit
#endif

struct NapSspSdkIntegration {
    static func initialize() {
        // nap ssp SDK init placeholder based on guide API
        // AMMediation.shared.initialize(mediaKey: NapSspConfig.mediaKey, adunitID: Array(NapSspConfig.adUnitIDs.values))
        AdEventLogger.request(format: "initialize", id: NapSspConfig.mediaKey)
        print("NapSsp iOS SDK initialize hook ready")
    }

    static func banner() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["banner_320x100"] ?? ""
        AdEventLogger.request(format: "banner", id: adUnitId)
        AdEventLogger.loaded(format: "banner", id: adUnitId)
        AdEventLogger.displayed(format: "banner", id: adUnitId)
        return "banner ready: \(adUnitId)"
    }

    static func native() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["native"] ?? ""
        AdEventLogger.request(format: "native", id: adUnitId)
        AdEventLogger.loaded(format: "native", id: adUnitId)
        AdEventLogger.displayed(format: "native", id: adUnitId)
        return "native ready: \(adUnitId)"
    }

    static func video() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["outstream_video"] ?? ""
        AdEventLogger.request(format: "video", id: adUnitId)
        AdEventLogger.loaded(format: "video", id: adUnitId)
        AdEventLogger.displayed(format: "video", id: adUnitId)
        return "video ready: \(adUnitId)"
    }

    static func rewardVideo() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["reward_video"] ?? ""
        AdEventLogger.request(format: "rewardVideo", id: adUnitId)
        AdEventLogger.loaded(format: "rewardVideo", id: adUnitId)
        AdEventLogger.displayed(format: "rewardVideo", id: adUnitId)
        return "reward ready: \(adUnitId)"
    }

    static func interstitialVideo() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["interstitial_320x480"] ?? ""
        AdEventLogger.request(format: "interstitialVideo", id: adUnitId)
        AdEventLogger.loaded(format: "interstitialVideo", id: adUnitId)
        AdEventLogger.displayed(format: "interstitialVideo", id: adUnitId)
        return "interstitial ready: \(adUnitId)"
    }
}
