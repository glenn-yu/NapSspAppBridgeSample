import Foundation
import UIKit
import AdMixer
import AdMixerMediation

/**
 * 보상형 비디오 광고 로드 및 표시 모듈
 * NapSspSdkIntegration.rewardVideo(...)에서 사용합니다.
 */
final class RewardedModule: NSObject {
    static let shared = RewardedModule()
    private var currentAdUnitId: Int = 0

    func load(adUnitId: Int, completion: @escaping (AMMRewardVideo?, Error?) -> Void) {
        currentAdUnitId = adUnitId
        // TODO: customParam의 useid / name / phone을 실제 유저 식별값으로 교체하세요
        let params: [String: String] = [
            "useid": "YOUR_USER_ID",
            "name":  "USER_NAME",
            "phone": "USER_PHONE",
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
        NapSspAdEventBridge.post(event: "completed", format: "rewardVideo", id: String(currentAdUnitId))
    }
    func onRewardVideoEarned() {
        NapSspAdEventBridge.post(event: "rewarded", format: "rewardVideo", id: String(currentAdUnitId))
    }
}
