import Foundation
import SwiftUI
import AdMixer

class NapSspSdkIntegration: NSObject {
    static let shared = NapSspSdkIntegration()
    
    // Bridge callback
    var onAdEventCallback: ((String, String, String) -> Void)?

    private func notifyEvent(event: String, format: String, id: String) {
        switch event {
        case "loaded": AdEventLogger.loaded(format: "initialize", id: id)
        case "displayed": AdEventLogger.displayed(format: format, id: id)
        case "clicked": AdEventLogger.clicked(format: format, id: id)
        default: break
        }
        onAdEventCallback?(event, format, id)
    }

    static func initialize() {
        AdEventLogger.request(format: "initialize", id: NapSspConfig.mediaKey)
        AMMediation.shared().initialize(withMediaKey: NapSspConfig.mediaKey, adUnitIds: Array(NapSspConfig.adUnitIDs.values))
        shared.notifyEvent(event: "loaded", format: "initialize", id: NapSspConfig.mediaKey)
    }

    static func banner() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["banner_320x100"] ?? ""
        AdEventLogger.request(format: "banner", id: adUnitId)
        
        let bannerView = AMMBannerView(adUnitId: adUnitId)
        bannerView.delegate = shared
        bannerView.isUseMediation = true
        bannerView.loadAd()
        
        return "banner requested: \(adUnitId)"
    }

    static func native() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["native"] ?? ""
        AdEventLogger.request(format: "native", id: adUnitId)
        
        let nativeView = AMMNativeAdViewContainer(adUnitId: adUnitId)
        nativeView.delegate = shared
        nativeView.isUseMediation = true
        nativeView.loadAd()
        
        return "native requested: \(adUnitId)"
    }

    static func video() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["outstream_video"] ?? ""
        AdEventLogger.request(format: "video", id: adUnitId)
        
        let videoView = AMMVideoAdView(adUnitId: adUnitId)
        videoView.delegate = shared
        videoView.isUseMediation = true
        videoView.loadAd()
        
        return "video requested: \(adUnitId)"
    }

    static func rewardVideo() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["reward_video"] ?? ""
        AdEventLogger.request(format: "rewardVideo", id: adUnitId)
        
        let rewardVideo = AMMRewardVideo(adUnitId: adUnitId)
        rewardVideo.delegate = shared
        rewardVideo.isUseMediation = true
        rewardVideo.loadAd()
        
        return "reward requested: \(adUnitId)"
    }

    static func interstitialVideo() -> String {
        let adUnitId = NapSspConfig.adUnitIDs["interstitial_320x480"] ?? ""
        AdEventLogger.request(format: "interstitialVideo", id: adUnitId)
        
        let interstitial = AMMVideoInterstitial(adUnitId: adUnitId)
        interstitial.delegate = shared
        interstitial.isUseMediation = true
        interstitial.loadAd()
        
        return "interstitial requested: \(adUnitId)"
    }
}

// MARK: - SDK Delegates
extension NapSspSdkIntegration: AMMBannerViewDelegate, AMMNativeAdViewDelegate, AMMVideoAdViewDelegate, AMMRewardVideoDelegate, AMMVideoInterstitialDelegate {
    
    // Banner
    func bannerViewDidLoad(_ bannerView: AMMBannerView) { notifyEvent(event: "loaded", format: "banner", id: bannerView.adUnitId) }
    func bannerView(_ bannerView: AMMBannerView, didFailWithError error: Error) { onAdEventCallback?("failed", "banner", error.localizedDescription) }
    func bannerViewDidClick(_ bannerView: AMMBannerView) { notifyEvent(event: "clicked", format: "banner", id: bannerView.adUnitId) }

    // Native
    func nativeAdViewDidLoad(_ nativeAdView: AMMNativeAdViewContainer) { notifyEvent(event: "loaded", format: "native", id: nativeAdView.adUnitId) }
    func nativeAdView(_ nativeAdView: AMMNativeAdViewContainer, didFailWithError error: Error) { onAdEventCallback?("failed", "native", error.localizedDescription) }
    func nativeAdViewDidClick(_ nativeAdView: AMMNativeAdViewContainer) { notifyEvent(event: "clicked", format: "native", id: nativeAdView.adUnitId) }

    // Video
    func videoAdViewDidLoad(_ videoAdView: AMMVideoAdView) { notifyEvent(event: "loaded", format: "video", id: videoAdView.adUnitId) }
    func videoAdView(_ videoAdView: AMMVideoAdView, didFailWithError error: Error) { onAdEventCallback?("failed", "video", error.localizedDescription) }
    func videoAdViewDidClick(_ videoAdView: AMMVideoAdView) { notifyEvent(event: "clicked", format: "video", id: videoAdView.adUnitId) }

    // Reward
    func rewardVideoDidLoad(_ rewardVideo: AMMRewardVideo) { notifyEvent(event: "loaded", format: "rewardVideo", id: rewardVideo.adUnitId) }
    func rewardVideo(_ rewardVideo: AMMRewardVideo, didFailWithError error: Error) { onAdEventCallback?("failed", "rewardVideo", error.localizedDescription) }
    func rewardVideoDidClick(_ rewardVideo: AMMRewardVideo) { notifyEvent(event: "clicked", format: "rewardVideo", id: rewardVideo.adUnitId) }
    func rewardVideoDidReward(_ rewardVideo: AMMRewardVideo) { onAdEventCallback?("rewarded", "rewardVideo", "success") }

    // Interstitial
    func videoInterstitialDidLoad(_ videoInterstitial: AMMVideoInterstitial) { notifyEvent(event: "loaded", format: "interstitialVideo", id: videoInterstitial.adUnitId) }
    func videoInterstitial(_ videoInterstitial: AMMVideoInterstitial, didFailWithError error: Error) { onAdEventCallback?("failed", "interstitialVideo", error.localizedDescription) }
    func videoInterstitialDidClick(_ videoInterstitial: AMMVideoInterstitial) { notifyEvent(event: "clicked", format: "interstitialVideo", id: videoInterstitial.adUnitId) }
}
