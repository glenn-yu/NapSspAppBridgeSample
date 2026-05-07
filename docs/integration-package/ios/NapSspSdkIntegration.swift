import Foundation
import UIKit
import AdMixer
import AdMixerMediation

/**
 * NapSSP SDK 연동 엔진
 *
 * 광고 생성 / 생명주기 / 이벤트 콜백을 일괄 관리합니다.
 * 사용법:
 *   1. onAdEventCallback을 설정해 광고 이벤트를 수신합니다.
 *   2. initializeSdk()를 호출해 SDK를 초기화합니다.
 *   3. banner / native / video 등 메서드로 광고 View를 요청합니다.
 *   4. 화면 종료 시 clearAllAds()를 호출합니다.
 */
class NapSspSdkIntegration: NSObject {
    static let shared = NapSspSdkIntegration()

    /** SDK 광고 이벤트 콜백. (event, format, detail) 형태로 수신합니다. */
    var onAdEventCallback: ((String, String, String) -> Void)?

    private var activeAds: [String: Any] = [:]

    // MARK: - 초기화

    static func initializeSdk() {
        let adUnitIDs = Set(NapSspConfig.adUnitIDInts.values.filter { $0 > 0 })
        AMMediation.shared.initialize(mediaKey: NapSspConfig.mediaKeyInt, adunitID: adUnitIDs)
        shared.onAdEventCallback?("loaded", "initialize", String(NapSspConfig.mediaKeyInt))
        NapSspAdEventBridge.post(event: "loaded", format: "initialize", id: String(NapSspConfig.mediaKeyInt))
    }

    // MARK: - 배너 광고

    static func banner(rootVC: UIViewController, customAdUnitId: Int? = nil) -> UIView? {
        let adUnitId = customAdUnitId ?? NapSspConfig.adUnitID("banner_320x100")
        shared.destroyAndRemoveAd(format: "banner")
        let bannerView = AMMBannerView(rootViewController: rootVC)
        bannerView.delegate = shared
        bannerView.adUnitID = adUnitId
        shared.activeAds["banner"] = bannerView
        bannerView.load()
        return bannerView
    }

    // MARK: - 네이티브 광고 (AMMNativeAdView.xib 필요)

    static func native(rootVC: UIViewController, customAdUnitId: Int? = nil) -> UIView? {
        let adUnitId = customAdUnitId ?? NapSspConfig.adUnitID("native")
        shared.destroyAndRemoveAd(format: "native")
        // TODO: AMMNativeAdView.xib를 프로젝트에 추가하세요
        let nativeAdView = Bundle.main.loadNibNamed("AMMNativeAdView", owner: nil, options: nil)?.first as? AMMNativeAdView
        let nativeView = AMMNativeAdViewContainer(rootViewController: rootVC)
        nativeView.nativeAdView = nativeAdView
        nativeView.delegate = shared
        nativeView.adUnitID = adUnitId
        shared.activeAds["native"] = nativeView
        nativeView.load()
        return nativeView
    }

    // MARK: - 아웃스트림 비디오

    static func video(rootVC: UIViewController, customAdUnitId: Int? = nil) -> UIView? {
        let adUnitId = customAdUnitId ?? NapSspConfig.adUnitID("outstream_video")
        shared.destroyAndRemoveAd(format: "video")
        let videoView = AMMVideoView(rootViewController: rootVC)
        videoView.delegate = shared
        videoView.adUnitID = adUnitId
        shared.activeAds["video"] = videoView
        videoView.load()
        return videoView
    }

    // MARK: - 보상형 비디오 (전체화면, RewardedModule 사용)

    static func rewardVideo(rootVC: UIViewController, customAdUnitId: Int? = nil) {
        let adUnitId = customAdUnitId ?? NapSspConfig.adUnitID("reward_video")
        shared.destroyAndRemoveAd(format: "rewardVideo")
        RewardedModule.shared.load(adUnitId: adUnitId) { reward, error in
            if let reward = reward {
                reward.delegate = shared
                shared.activeAds["rewardVideo"] = reward
                shared.onAdEventCallback?("loaded", "rewardVideo", String(adUnitId))
                RewardedModule.shared.show(reward, rootViewController: rootVC)
            } else {
                shared.onAdEventCallback?("failed", "rewardVideo", error?.localizedDescription ?? "load failed")
                NapSspAdEventBridge.post(event: "failed", format: "rewardVideo", id: String(adUnitId))
            }
        }
    }

    // MARK: - 전면 비디오 (전체화면)

    static func interstitialVideo(rootVC: UIViewController, customAdUnitId: Int? = nil) {
        let adUnitId = customAdUnitId ?? NapSspConfig.adUnitID("interstitial_320x480")
        shared.destroyAndRemoveAd(format: "interstitialVideo")
        AMMVideoInterstitial.load(adUnitID: adUnitId) { interstitial, error in
            if let interstitial = interstitial {
                interstitial.delegate = shared
                shared.activeAds["interstitialVideo"] = interstitial
                shared.onAdEventCallback?("loaded", "interstitialVideo", String(adUnitId))
                interstitial.show(rootViewController: rootVC)
            } else {
                shared.onAdEventCallback?("failed", "interstitialVideo", error?.localizedDescription ?? "load failed")
                NapSspAdEventBridge.post(event: "failed", format: "interstitialVideo", id: String(adUnitId))
            }
        }
    }

    // MARK: - 전면 배너/팝업 (전체화면, InterstitialModule 사용)

    static func interstitialBanner(rootVC: UIViewController, customAdUnitId: Int? = nil) {
        let adUnitId = customAdUnitId ?? NapSspConfig.adUnitID("interstitial_320x480_f")
        shared.destroyAndRemoveAd(format: "interstitialBanner")
        InterstitialModule.shared.load(adUnitId: adUnitId) { interstitial, error in
            if let interstitial = interstitial {
                interstitial.delegate = shared
                shared.activeAds["interstitialBanner"] = interstitial
                shared.onAdEventCallback?("loaded", "interstitialBanner", String(adUnitId))
                InterstitialModule.shared.show(interstitial, rootViewController: rootVC)
            } else {
                shared.onAdEventCallback?("failed", "interstitialBanner", error?.localizedDescription ?? "load failed")
                NapSspAdEventBridge.post(event: "failed", format: "interstitialBanner", id: String(adUnitId))
            }
        }
    }

    // MARK: - 생명주기

    static func clearAllAds() {
        Array(shared.activeAds.keys).forEach { shared.destroyAndRemoveAd(format: $0) }
    }

    private func destroyAndRemoveAd(format: String) {
        if let ad = activeAds[format] {
            if let v = ad as? UIView { v.isHidden = true }
            if let b = ad as? AMMBannerView { b.stop() }
            else if let n = ad as? AMMNativeAdViewContainer { n.stop() }
            else if let v = ad as? AMMVideoView { v.stop() }
            else if let i = ad as? AMMInterstitial { i.stop() }
            else if let r = ad as? AMMRewardVideo { r.stop() }
            else if let iv = ad as? AMMVideoInterstitial { iv.stop() }
            if let v = ad as? UIView { v.removeFromSuperview() }
        }
        activeAds.removeValue(forKey: format)
    }

    // MARK: - 이벤트 헬퍼

    private func notify(event: String, format: String, id: String, detail: String? = nil) {
        onAdEventCallback?(event, format, id)
        NapSspAdEventBridge.post(event: event, format: format, id: id, detail: detail)
    }
}

// MARK: - SDK Delegate 구현

extension NapSspSdkIntegration: AMMBannerViewDelegate, AMMNativeDelegate,
    AMMVideoViewDelegate, AMMInterstitialDelegate, AMMRewardVideoDelegate, AMMVideoInterstitialDelegate {

    // 배너
    func onSuccessBanner() {
        if let v = activeAds["banner"] as? AMMBannerView { notify(event: "loaded", format: "banner", id: String(v.adUnitID)) }
    }
    func onFailBanner() { notify(event: "failed", format: "banner", id: String(NapSspConfig.adUnitID("banner_320x100"))) }
    func onTapBanner() {
        if let v = activeAds["banner"] as? AMMBannerView { notify(event: "clicked", format: "banner", id: String(v.adUnitID)) }
    }

    // 네이티브
    func onSuccessNative() {
        if let v = activeAds["native"] as? AMMNativeAdViewContainer { notify(event: "loaded", format: "native", id: String(v.adUnitID)) }
    }
    func onFailNative() { notify(event: "failed", format: "native", id: String(NapSspConfig.adUnitID("native"))) }
    func onTapNative() {
        if let v = activeAds["native"] as? AMMNativeAdViewContainer { notify(event: "clicked", format: "native", id: String(v.adUnitID)) }
    }

    // 비디오
    func onSuccessVideo() {
        if let v = activeAds["video"] as? AMMVideoView { notify(event: "loaded", format: "video", id: String(v.adUnitID)) }
    }
    func onFailVideo() { notify(event: "failed", format: "video", id: String(NapSspConfig.adUnitID("outstream_video"))) }
    func onTapVideoViewMore() {
        if let v = activeAds["video"] as? AMMVideoView { notify(event: "clicked", format: "video", id: String(v.adUnitID)) }
    }
    func onSkipVideo() { notify(event: "skipped", format: "video", id: "") }
    func onCompleteVideo() { notify(event: "completed", format: "video", id: "") }

    // 보상형
    func onSuccessShowReward()                          { notify(event: "displayed",  format: "rewardVideo", id: "") }
    func onFailShowReward(error: (any Error)?)          { notify(event: "failed",     format: "rewardVideo", id: "", detail: error?.localizedDescription) }
    func onTapRewardVideo()                             { notify(event: "clicked",    format: "rewardVideo", id: "") }
    func onCloseRewardVideo()                           { notify(event: "closed",     format: "rewardVideo", id: "") }
    func onRewardVideoComplete()                        { notify(event: "completed",  format: "rewardVideo", id: "") }
    func onRewardVideoEarned()                          { notify(event: "rewarded",   format: "rewardVideo", id: "") }

    // 전면 배너
    func onSuccessShowInterstitial()                    { notify(event: "displayed",  format: "interstitialBanner", id: "") }
    func onFailShowInterstitial(error: (any Error)?)    { notify(event: "failed",     format: "interstitialBanner", id: "", detail: error?.localizedDescription) }
    func onTapInterstitial()                            { notify(event: "clicked",    format: "interstitialBanner", id: "") }
    func onCloseInterstitial()                          { notify(event: "closed",     format: "interstitialBanner", id: "") }

    // 전면 비디오
    func onSuccessShowVideoInterstitial()               { notify(event: "displayed",  format: "interstitialVideo", id: "") }
    func onFailShowVideoInterstitial(error: (any Error)?) { notify(event: "failed",   format: "interstitialVideo", id: "", detail: error?.localizedDescription) }
    func onCloseVideoInterstitial()                     { notify(event: "closed",     format: "interstitialVideo", id: "") }
    func onTapVideoInterstitialViewMore()               { notify(event: "clicked",    format: "interstitialVideo", id: "") }
    func onCompleteVideoInterstitial()                  { notify(event: "completed",  format: "interstitialVideo", id: "") }
}
