#if os(iOS)
import SwiftUI

@main
struct NapSspIOSSampleApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
#endif
