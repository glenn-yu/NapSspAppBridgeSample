import Foundation

#if canImport(GoogleMobileAds)
import GoogleMobileAds
#endif

#if canImport(PAGAdSDK)
import PAGAdSDK
#endif

enum NapSspInitializer {
    static func initialize() {
        NapSspSdkIntegration.initializeSdk()
        print("NapSsp iOS init mediaKey=\(NapSspConfig.mediaKey) adUnitIds=\(NapSspConfig.adUnitIDs.values.joined(separator: ","))")
        print("NapSsp iOS mediation hints=\(NapSspConfig.mediationHints.map { $0.0 + ":" + $0.1 }.joined(separator: ","))")

        #if canImport(GoogleMobileAds)
        GADMobileAds.sharedInstance().start(completionHandler: nil)
        #endif

        #if canImport(PAGAdSDK)
        let pagConfig = PAGConfig.share()
        pagConfig.appID = "8245842" // 테스트용 발급 앱 ID (필요 시 수정)
        PAGSdk.start(with: pagConfig) { isSuccess, error in
            if let error = error {
                print("Pangle init error: \(error)")
            } else {
                print("Pangle init success")
            }
        }
        #endif
    }
}
