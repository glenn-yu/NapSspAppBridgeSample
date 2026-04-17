import Foundation
import UIKit
import SwiftUI
import AdMixer
import AdMixerMediation

/**
 * NapSSP 광고 SDK 연동 엔진 (iOS)
 * 광고의 생성, 파괴, 생명주기 관리 및 Already Exist 오류 방지 로직이 포함되어 있습니다.
 */
class NapSspSdkIntegration: NSObject {
    static let shared = NapSspSdkIntegration()

    var onAdEventCallback: ((String, String, String) -> Void)?
    
    // 현재 활성화된 광고 객체들을 추적
    private var activeAds: [String: Any] = [:]

    private func notifyEvent(event: String, format: String, id: String, detail: String? = nil) {
        switch event {
        case "loaded": AdEventLogger.loaded(format: format, id: id)
        case "displayed": AdEventLogger.displayed(format: format, id: id)
        case "clicked": AdEventLogger.clicked(format: format, id: id)
        default: break
        }
        onAdEventCallback?(event, format, id)
        NapSspAdEventBridge.post(event: event, format: format, id: id, detail: detail)
    }

    private func destroyAndRemoveAd(format: String) {
        if let ad = activeAds[format] {
            if let view = ad as? UIView {
                view.isHidden = true
            }

            if let b = ad as? AMMBannerView { b.stop() }
            else if let n = ad as? AMMNativeAdViewContainer { n.stop() }
            else if let v = ad as? AMMVideoView { v.stop() }
            else if let i = ad as? AMMInterstitial { i.stop() }
            else if let r = ad as? AMMRewardVideo { r.stop() }
            else if let iv = ad as? AMMVideoInterstitial { iv.stop() }

            if let view = ad as? UIView {
                view.removeFromSuperview()
            }
        }
        activeAds.removeValue(forKey: format)
    }

    static func initializeSdk() {
        let adUnitIDs = Set(NapSspConfig.adUnitIDInts.values.filter { $0 > 0 })
        AMMediation.shared.initialize(mediaKey: NapSspConfig.mediaKeyInt, adunitID: adUnitIDs)
        shared.notifyEvent(event: "loaded", format: "initialize", id: String(NapSspConfig.mediaKeyInt))
    }

    static func banner(rootVC: UIViewController) -> UIView? {
        let adUnitId = NapSspConfig.adUnitID("banner_320x100")
        let format = "banner"
        shared.destroyAndRemoveAd(format: format)

        let bannerView = AMMBannerView(rootViewController: rootVC)
        bannerView.delegate = shared
        bannerView.adUnitID = adUnitId
        shared.activeAds[format] = bannerView
        bannerView.load()
        return bannerView
    }

    static func native(rootVC: UIViewController) -> UIView? {
        let adUnitId = NapSspConfig.adUnitID("native")
        let format = "native"
        shared.destroyAndRemoveAd(format: format)

        var nativeAdView: AMMNativeAdView? = nil
        if let nibView = Bundle.main.loadNibNamed("AMMNativeAdView", owner: nil, options: nil)?.first as? AMMNativeAdView {
            nativeAdView = nibView
        }

        let nativeView = AMMNativeAdViewContainer(rootViewController: rootVC)
        nativeView.nativeAdView = nativeAdView
        nativeView.delegate = shared
        nativeView.adUnitID = adUnitId
        shared.activeAds[format] = nativeView
        nativeView.load()
        return nativeView
    }

    static func video(rootVC: UIViewController) -> UIView? {
        let adUnitId = NapSspConfig.adUnitID("outstream_video")
        let format = "video"
        shared.destroyAndRemoveAd(format: format)

        let videoView = AMMVideoView(rootViewController: rootVC)
        videoView.delegate = shared
        videoView.adUnitID = adUnitId
        shared.activeAds[format] = videoView
        videoView.load()
        return videoView
    }

    static func interstitialBanner(rootVC: UIViewController) {
        let adUnitId = NapSspConfig.adUnitID("interstitial_320x480_f")
        let format = "interstitialBanner"
        shared.destroyAndRemoveAd(format: format)
        InterstitialModule.shared.load(adUnitId: adUnitId) { interstitial, error in
            if let interstitial = interstitial {
                shared.activeAds[format] = interstitial
                shared.notifyEvent(event: "loaded", format: format, id: String(adUnitId))
                InterstitialModule.shared.show(interstitial, rootViewController: rootVC)
            } else if let error = error {
                shared.notifyEvent(event: "failed", format: format, id: String(adUnitId), detail: error.localizedDescription)
            }
        }
    }

    static func rewardVideo(rootVC: UIViewController) {
        let adUnitId = NapSspConfig.adUnitID("reward_video")
        let format = "rewardVideo"
        shared.destroyAndRemoveAd(format: format)
        RewardedModule.shared.load(adUnitId: adUnitId) { reward, error in
            if let reward = reward {
                shared.activeAds[format] = reward
                shared.notifyEvent(event: "loaded", format: format, id: String(adUnitId))
                RewardedModule.shared.show(reward, rootViewController: rootVC)
            } else if let error = error {
                shared.notifyEvent(event: "failed", format: format, id: String(adUnitId), detail: error.localizedDescription)
            }
        }
    }

    static func interstitialVideo(rootVC: UIViewController) {
        let adUnitId = NapSspConfig.adUnitID("interstitial_320x480")
        let format = "interstitialVideo"
        shared.destroyAndRemoveAd(format: format)
        AMMVideoInterstitial.load(adUnitID: adUnitId) { interstitial, error in
            if let interstitial = interstitial {
                interstitial.delegate = shared
                shared.activeAds[format] = interstitial
                shared.notifyEvent(event: "loaded", format: format, id: String(adUnitId))
                interstitial.show(rootViewController: rootVC)
            } else if let error = error {
                shared.onAdEventCallback?("failed", format, error.localizedDescription)
            }
        }
    }

    static func clearAllAds() {
        let keys = Array(shared.activeAds.keys)
        for key in keys {
            shared.destroyAndRemoveAd(format: key)
        }
    }
}

extension NapSspSdkIntegration: AMMBannerViewDelegate, AMMNativeDelegate, AMMVideoViewDelegate, AMMInterstitialDelegate, AMMRewardVideoDelegate, AMMVideoInterstitialDelegate {
    func onSuccessBanner() {
        if let bannerView = activeAds["banner"] as? AMMBannerView {
            notifyEvent(event: "loaded", format: "banner", id: String(bannerView.adUnitID))
        }
    }

    func onFailBanner() { NapSspAdEventBridge.post(event: "failed", format: "banner", id: String(NapSspConfig.adUnitID("banner_320x100")), detail: "load failed") }
    func onTapBanner() {
        if let bannerView = activeAds["banner"] as? AMMBannerView {
            notifyEvent(event: "clicked", format: "banner", id: String(bannerView.adUnitID))
        }
    }

    func onSuccessNative() {
        if let nativeView = activeAds["native"] as? AMMNativeAdViewContainer {
            notifyEvent(event: "loaded", format: "native", id: String(nativeView.adUnitID))
        }
    }

    func onFailNative() { NapSspAdEventBridge.post(event: "failed", format: "native", id: String(NapSspConfig.adUnitID("native")), detail: "load failed") }
    func onTapNative() {
        if let nativeView = activeAds["native"] as? AMMNativeAdViewContainer {
            notifyEvent(event: "clicked", format: "native", id: String(nativeView.adUnitID))
        }
    }

    func onSuccessVideo() {
        if let videoView = activeAds["video"] as? AMMVideoView {
            notifyEvent(event: "loaded", format: "video", id: String(videoView.adUnitID))
        }
    }

    func onFailVideo() { NapSspAdEventBridge.post(event: "failed", format: "video", id: String(NapSspConfig.adUnitID("outstream_video")), detail: "load failed") }
    func onTapVideoViewMore() {
        if let videoView = activeAds["video"] as? AMMVideoView {
            notifyEvent(event: "clicked", format: "video", id: String(videoView.adUnitID))
        }
    }

    func onSuccessShowInterstitial() { notifyEvent(event: "displayed", format: "interstitialBanner", id: String(NapSspConfig.adUnitID("interstitial_320x480_f"))) }
    func onFailShowInterstitial(error: (any Error)?) { NapSspAdEventBridge.post(event: "failed", format: "interstitialBanner", id: String(NapSspConfig.adUnitID("interstitial_320x480_f")), detail: error?.localizedDescription ?? "show failed") }
    func onTapInterstitial() { notifyEvent(event: "clicked", format: "interstitialBanner", id: String(NapSspConfig.adUnitID("interstitial_320x480_f"))) }
    func onCloseInterstitial() {}

    func onSuccessShowReward() { notifyEvent(event: "displayed", format: "rewardVideo", id: String(NapSspConfig.adUnitID("reward_video"))) }
    func onFailShowReward(error: (any Error)?) { NapSspAdEventBridge.post(event: "failed", format: "rewardVideo", id: String(NapSspConfig.adUnitID("reward_video")), detail: error?.localizedDescription ?? "show failed") }
    func onTapRewardVideo() { notifyEvent(event: "clicked", format: "rewardVideo", id: String(NapSspConfig.adUnitID("reward_video"))) }
    func onCloseRewardVideo() {}
    func onRewardVideoComplete() {}
    func onRewardVideoEarned() { NapSspAdEventBridge.post(event: "rewarded", format: "rewardVideo", id: String(NapSspConfig.adUnitID("reward_video")), detail: "success") }

    func onSuccessShowVideoInterstitial() { notifyEvent(event: "displayed", format: "interstitialVideo", id: String(NapSspConfig.adUnitID("interstitial_320x480"))) }
    func onFailShowVideoInterstitial(error: (any Error)?) { NapSspAdEventBridge.post(event: "failed", format: "interstitialVideo", id: String(NapSspConfig.adUnitID("interstitial_320x480")), detail: error?.localizedDescription ?? "show failed") }
    func onCloseVideoInterstitial() {}
    func onTapVideoInterstitialViewMore() { notifyEvent(event: "clicked", format: "interstitialVideo", id: String(NapSspConfig.adUnitID("interstitial_320x480"))) }
    func onCompleteVideoInterstitial() {}
}
