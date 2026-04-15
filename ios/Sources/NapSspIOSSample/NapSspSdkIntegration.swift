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

    static func banner() {
        let adUnitId = NapSspConfig.adUnitIDs["banner_320x100"] ?? ""
        AdEventLogger.request(format: "banner", id: adUnitId)
        // let bannerView = AMMBannerView(rootViewController: self)
        // bannerView.adUnitID = adUnitId
        // bannerView.load()
        AdEventLogger.loaded(format: "banner", id: adUnitId)
        AdEventLogger.displayed(format: "banner", id: adUnitId)
    }

    static func native() {
        let adUnitId = NapSspConfig.adUnitIDs["native"] ?? ""
        AdEventLogger.request(format: "native", id: adUnitId)
        // let nativeAd = AMMNativeAdViewContainer(rootViewController: self)
        // nativeAd.adUnitID = adUnitId
        // nativeAd.load()
        AdEventLogger.loaded(format: "native", id: adUnitId)
        AdEventLogger.displayed(format: "native", id: adUnitId)
    }

    static func video() {
        let adUnitId = NapSspConfig.adUnitIDs["outstream_video"] ?? ""
        AdEventLogger.request(format: "video", id: adUnitId)
        // let videoView = AMMVideoAdView(rootViewController: self)
        // videoView.adUnitID = adUnitId
        // videoView.load()
        AdEventLogger.loaded(format: "video", id: adUnitId)
        AdEventLogger.displayed(format: "video", id: adUnitId)
    }

    static func rewardVideo() {
        let adUnitId = NapSspConfig.adUnitIDs["reward_video"] ?? ""
        AdEventLogger.request(format: "rewardVideo", id: adUnitId)
        // let rewardVideo = AMMRewardVideo(rootViewController: self)
        // rewardVideo.adUnitID = adUnitId
        // rewardVideo.load()
        AdEventLogger.loaded(format: "rewardVideo", id: adUnitId)
        AdEventLogger.displayed(format: "rewardVideo", id: adUnitId)
    }

    static func interstitialVideo() {
        let adUnitId = NapSspConfig.adUnitIDs["interstitial_320x480"] ?? ""
        AdEventLogger.request(format: "interstitialVideo", id: adUnitId)
        // let interstitial = AMMVideoInterstitial(rootViewController: self)
        // interstitial.adUnitID = adUnitId
        // interstitial.load()
        AdEventLogger.loaded(format: "interstitialVideo", id: adUnitId)
        AdEventLogger.displayed(format: "interstitialVideo", id: adUnitId)
    }
}
