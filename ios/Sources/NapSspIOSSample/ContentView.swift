#if os(iOS)
import SwiftUI

struct ContentView: View {
    var body: some View {
        NavigationView {
            HybridWebViewScreen()
                .navigationTitle("NapSsp Hybrid Bridge")
                .navigationBarTitleDisplayMode(.inline)
        }
    }
}
#endif
