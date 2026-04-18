import Foundation
import UIKit
import AdMixer
import AdMixerMediation

final class InterstitialModule: NSObject {
    static let shared = InterstitialModule()
    private var currentAdUnitId: Int = 0

    func load(adUnitId: Int, completion: @escaping (AMMInterstitial?, Error?) -> Void) {
        currentAdUnitId = adUnitId
        let config = AMMInterstitialConfig()
        config.viewType = .popup
        config.popupOption = AMMInterstitialPopupOption(
            buttonTitle: "광고종료",
            buttonTextColor: .white,
            buttonBackgroundColor: UIColor(red: 35.0/255.0, green: 66.0/255.0, blue: 52.0/255.0, alpha: 1.0)
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
