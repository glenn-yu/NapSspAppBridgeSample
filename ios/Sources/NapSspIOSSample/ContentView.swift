#if os(iOS)
import SwiftUI

struct ContentView: View {
    @State private var showConfigureKeys = false

    var body: some View {
        NavigationView {
            HybridWebViewScreen()
                .navigationTitle("NapSsp Hybrid Bridge")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button("Configure Keys") {
                            showConfigureKeys = true
                        }
                    }
                }
                .sheet(isPresented: $showConfigureKeys) {
                    ConfigureKeysView()
                }
        }
    }
}
#endif
