import Foundation
import UIKit
import AdMixer
import AdMixerMediation

final class RewardedModule: NSObject {
    static let shared = RewardedModule()
    private var currentAdUnitId: Int = 0

    func load(adUnitId: Int, completion: @escaping (AMMRewardVideo?, Error?) -> Void) {
        currentAdUnitId = adUnitId
        let params: [String: String] = [
            "useid": "nas",
            "name": "hdragon",
            "phone": "010-1111-1111"
        ]
        AMMRewardVideo.load(adUnitID: adUnitId, customParam: params) { reward, error in
            if let reward = reward {
                reward.delegate = self
                completion(reward, nil)
            } else {
                completion(nil, error)
            }
        }
    }

    func show(_ reward: AMMRewardVideo, rootViewController: UIViewController) {
        reward.show(rootViewController: rootViewController)
    }
}

extension RewardedModule: AMMRewardVideoDelegate {
    func onSuccessShowReward() {
        NapSspAdEventBridge.post(event: "displayed", format: "rewardVideo", id: String(currentAdUnitId))
    }

    func onFailShowReward(error: (any Error)?) {
        NapSspAdEventBridge.post(event: "failed", format: "rewardVideo", id: String(currentAdUnitId), detail: error?.localizedDescription ?? "show failed")
    }

    func onCloseRewardVideo() {
        NapSspAdEventBridge.post(event: "closed", format: "rewardVideo", id: String(currentAdUnitId))
    }

    func onTapRewardVideo() {
        NapSspAdEventBridge.post(event: "clicked", format: "rewardVideo", id: String(currentAdUnitId))
    }

    func onRewardVideoComplete() {
        NapSspAdEventBridge.post(event: "rewarded", format: "rewardVideo", id: String(currentAdUnitId))
    }

    func onRewardVideoEarned() {
        NapSspAdEventBridge.post(event: "rewarded", format: "rewardVideo", id: String(currentAdUnitId))
    }
}
