import Foundation
import UIKit
import SwiftUI
import AdMixer

class NapSspSdkIntegration: NSObject {
    static let shared = NapSspSdkIntegration()
    
    var onAdEventCallback: ((String, String, String) -> Void)?
    
    // 현재 활성화된 광고 객체들을 추적
    private var activeAds: [String: Any] = [:]

    private func notifyEvent(event: String, format: String, id: String) {
        switch event {
        case "loaded": AdEventLogger.loaded(format: format, id: id)
        case "displayed": AdEventLogger.displayed(format: format, id: id)
        case "clicked": AdEventLogger.clicked(format: format, id: id)
        default: break
        }
        onAdEventCallback?(event, format, id)
    }

    // [쌍둥이 로직] 기존 광고를 완전히 파괴하고 Dictionary에서 제거
    private func destroyAndRemoveAd(format: String) {
        if let ad = activeAds[format] {
            // 1. 가시성 제거
            if let view = ad as? UIView { view.isHidden = true }
            
            // 2. stop() 호출: 리스너 해제 및 리소스 정리 (가장 확실한 방법)
            if let b = ad as? AMMBannerView { b.stop() }
            else if let n = ad as? AMMNativeAdViewContainer { n.stop() }
            else if let v = ad as? AMMVideoAdView { v.stop() }
            else if let i = ad as? AMMInterstitial { i.stop() }
            else if let r = ad as? AMMRewardVideo { r.stop() }
            else if let iv = ad as? AMMVideoInterstitial { iv.stop() }
            
            // 3. 부모 뷰에서 제거
            if let view = ad as? UIView {
                view.removeFromSuperview()
            }
        }
        // 4. 참조 제거 (null화)
        activeAds.removeValue(forKey: format)
    }

    static func initialize() {
        AdEventLogger.request(format: "initialize", id: NapSspConfig.mediaKey)
        AMMediation.shared().initialize(withMediaKey: NapSspConfig.mediaKey, adUnitIds: Array(NapSspConfig.adUnitIDs.values))
        shared.notifyEvent(event: "loaded", format: "initialize", id: NapSspConfig.mediaKey)
    }

    static func banner(rootVC: UIViewController) -> UIView? {
        let adUnitId = NapSspConfig.adUnitIDs["banner_320x100"] ?? ""
        let format = "banner"
        shared.destroyAndRemoveAd(format: format)
        let bannerView = AMMBannerView(rootViewController: rootVC)
        bannerView.delegate = shared
        bannerView.isUseMediation = true
        bannerView.adUnitId = adUnitId
        shared.activeAds[format] = bannerView
        bannerView.loadAd()
        return bannerView
    }

    static func native(rootVC: UIViewController) -> UIView? {
        let adUnitId = NapSspConfig.adUnitIDs["native"] ?? ""
        let format = "native"
        shared.destroyAndRemoveAd(format: format)
        
        let nibNames = [
            "AMMNativeAdView_320x480",
            "AMMNativeAdView_300x250",
            "AMMNativeAdView_320x100",
            "AMMNativeAdView_320x50"
        ]
        let selectedNib = nibNames.randomElement() ?? "AMMNativeAdView"
        let nibView = Bundle.main.loadNibNamed(selectedNib, owner: nil, options: nil)?.first
        let nativeAdView = nibView as? AMMNativeAdView

        let nativeView = AMMNativeAdViewContainer(rootViewController: rootVC)
        nativeView.nativeAdView = nativeAdView
        nativeView.delegate = shared
        nativeView.isUseMediation = true
        nativeView.adUnitID = adUnitId
        shared.activeAds[format] = nativeView
        nativeView.loadAd()
        return nativeView
    }

    static func video(rootVC: UIViewController) -> UIView? {
        let adUnitId = NapSspConfig.adUnitIDs["outstream_video"] ?? ""
        let format = "video"
        shared.destroyAndRemoveAd(format: format)
        let videoView = AMMVideoAdView(rootViewController: rootVC)
        videoView.delegate = shared
        videoView.isUseMediation = true
        videoView.adUnitID = adUnitId
        shared.activeAds[format] = videoView
        videoView.loadAd()
        return videoView
    }

    static func interstitialBanner(rootVC: UIViewController) {
        let adUnitId = NapSspConfig.adUnitIDs["interstitial_320x480_f"] ?? ""
        let format = "interstitialBanner"
        shared.destroyAndRemoveAd(format: format)
        let config = AMMInterstitialConfig()
        config.viewType = .basic
        AMMInterstitial.load(withAdUnitID: adUnitId, config: config) { interstitial, error in
            if let interstitial = interstitial {
                interstitial.delegate = shared
                shared.activeAds[format] = interstitial
                interstitial.show(with: rootVC)
                shared.notifyEvent(event: "loaded", format: format, id: adUnitId)
            } else if let error = error {
                shared.onAdEventCallback?("failed", format, error.localizedDescription)
            }
        }
    }

    static func rewardVideo(rootVC: UIViewController) {
        let adUnitId = NapSspConfig.adUnitIDs["reward_video"] ?? ""
        let format = "rewardVideo"
        shared.destroyAndRemoveAd(format: format)
        AMMRewardVideo.load(withAdUnitID: adUnitId) { reward, error in
            if let reward = reward {
                reward.delegate = shared
                shared.activeAds[format] = reward
                reward.show(with: rootVC)
                shared.notifyEvent(event: "loaded", format: format, id: adUnitId)
            } else if let error = error {
                shared.onAdEventCallback?("failed", format, error.localizedDescription)
            }
        }
    }

    static func interstitialVideo(rootVC: UIViewController) {
        let adUnitId = NapSspConfig.adUnitIDs["interstitial_320x480"] ?? ""
        let format = "interstitialVideo"
        shared.destroyAndRemoveAd(format: format)
        AMMVideoInterstitial.load(withAdUnitID: adUnitId) { interstitial, error in
            if let interstitial = interstitial {
                interstitial.delegate = shared
                shared.activeAds[format] = interstitial
                interstitial.show(with: rootVC)
                shared.notifyEvent(event: "loaded", format: format, id: adUnitId)
            } else if let error = error {
                shared.onAdEventCallback?("failed", format, error.localizedDescription)
            }
        }
    }

    static func clearAllAds() {
        let formats = Array(shared.activeAds.keys)
        for format in formats {
            shared.destroyAndRemoveAd(format: format)
        }
        shared.activeAds.removeAll()
        AdEventLogger.request(format: "cleanup", id: "All ads destroyed and memory released")
    }
}

extension NapSspSdkIntegration: AMMBannerViewDelegate, AMMNativeAdViewDelegate, AMMVideoAdViewDelegate, AMMRewardVideoDelegate, AMMVideoInterstitialDelegate {
    func bannerViewDidLoad(_ bannerView: AMMBannerView) { notifyEvent(event: "loaded", format: "banner", id: bannerView.adUnitId) }
    func bannerView(_ bannerView: AMMBannerView, didFailWithError error: Error) { onAdEventCallback?("failed", "banner", error.localizedDescription) }
    func bannerViewDidClick(_ bannerView: AMMBannerView) { notifyEvent(event: "clicked", format: "banner", id: bannerView.adUnitId) }
    func nativeAdViewDidLoad(_ nativeAdView: AMMNativeAdViewContainer) { notifyEvent(event: "loaded", format: "native", id: nativeAdView.adUnitId) }
    func nativeAdView(_ nativeAdView: AMMNativeAdViewContainer, didFailWithError error: Error) { onAdEventCallback?("failed", "native", error.localizedDescription) }
    func nativeAdViewDidClick(_ nativeAdView: AMMNativeAdViewContainer) { notifyEvent(event: "clicked", format: "native", id: nativeAdView.adUnitId) }
    func videoAdViewDidLoad(_ videoAdView: AMMVideoAdView) { notifyEvent(event: "loaded", format: "video", id: videoAdView.adUnitId) }
    func videoAdView(_ videoAdView: AMMVideoAdView, didFailWithError error: Error) { onAdEventCallback?("failed", "video", error.localizedDescription) }
    func videoAdViewDidClick(_ videoAdView: AMMVideoAdView) { notifyEvent(event: "clicked", format: "video", id: videoAdView.adUnitId) }
    func rewardVideoDidLoad(_ rewardVideo: AMMRewardVideo) { notifyEvent(event: "loaded", format: "rewardVideo", id: rewardVideo.adUnitId) }
    func rewardVideo(_ rewardVideo: AMMRewardVideo, didFailWithError error: Error) { onAdEventCallback?("failed", "rewardVideo", error.localizedDescription) }
    func rewardVideoDidClick(_ rewardVideo: AMMRewardVideo) { notifyEvent(event: "clicked", format: "rewardVideo", id: rewardVideo.adUnitId) }
    func rewardVideoDidReward(_ rewardVideo: AMMRewardVideo) { onAdEventCallback?("rewarded", "rewardVideo", "success") }
    func videoInterstitialDidLoad(_ videoInterstitial: AMMVideoInterstitial) { notifyEvent(event: "loaded", format: "interstitialVideo", id: videoInterstitial.adUnitId) }
    func videoInterstitial(_ videoInterstitial: AMMVideoInterstitial, didFailWithError error: Error) { onAdEventCallback?("failed", "interstitialVideo", error.localizedDescription) }
    func videoInterstitialDidClick(_ videoInterstitial: AMMVideoInterstitial) { notifyEvent(event: "clicked", format: "interstitialVideo", id: videoInterstitial.adUnitId) }
}
#endif
