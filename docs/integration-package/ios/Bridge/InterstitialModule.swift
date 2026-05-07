import Foundation
import UIKit
import AdMixer
import AdMixerMediation

/**
 * 전면 배너/팝업 광고 로드 및 표시 모듈
 * NapSspSdkIntegration.interstitialBanner(...)에서 사용합니다.
 */
final class InterstitialModule: NSObject {
    static let shared = InterstitialModule()
    private var currentAdUnitId: Int = 0

    func load(adUnitId: Int, completion: @escaping (AMMInterstitial?, Error?) -> Void) {
        currentAdUnitId = adUnitId
        let config = AMMInterstitialConfig()
        config.viewType = .popup
        config.popupOption = AMMInterstitialPopupOption(
            buttonTitle: "광고종료",         // TODO: 실제 앱 언어/디자인에 맞게 수정
            buttonTextColor: .white,
            buttonBackgroundColor: UIColor(red: 35/255, green: 66/255, blue: 52/255, alpha: 1)
        )
        config.countDownOption = AMMInterstitialCountDownOption(
            countDownTime: 5,
            countDownType: .gauge
        )
        AMMInterstitial.load(adUnitID: adUnitId, config: config) { interstitial, error in
            if let interstitial = interstitial {
                interstitial.delegate = self
                completion(interstitial, nil)
            } else {
                completion(nil, error)
            }
        }
    }

    func show(_ interstitial: AMMInterstitial, rootViewController: UIViewController) {
        interstitial.show(rootViewController: rootViewController)
    }
}

extension InterstitialModule: AMMInterstitialDelegate {
    func onSuccessShowInterstitial() {
        NapSspAdEventBridge.post(event: "displayed", format: "interstitialBanner", id: String(currentAdUnitId))
    }
    func onFailShowInterstitial(error: (any Error)?) {
        NapSspAdEventBridge.post(event: "failed", format: "interstitialBanner", id: String(currentAdUnitId), detail: error?.localizedDescription ?? "show failed")
    }
    func onTapInterstitial() {
        NapSspAdEventBridge.post(event: "clicked", format: "interstitialBanner", id: String(currentAdUnitId))
    }
    func onCloseInterstitial() {
        NapSspAdEventBridge.post(event: "closed", format: "interstitialBanner", id: String(currentAdUnitId))
    }
}
