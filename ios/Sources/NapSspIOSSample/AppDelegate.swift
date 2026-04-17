#if canImport(UIKit)
import UIKit
import AppTrackingTransparency

final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        NapSspInitializer.initialize()
        return true
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        requestTrackingAuthorization()
    }

    private func requestTrackingAuthorization() {
        if #available(iOS 14, *) {
            Task {
                _ = await ATTrackingManager.requestTrackingAuthorization()
            }
        }
    }
}
#endif
