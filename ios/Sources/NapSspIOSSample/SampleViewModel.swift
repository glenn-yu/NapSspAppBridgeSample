#if os(iOS)
import Foundation
import SwiftUI

final class SampleViewModel: ObservableObject {
    @Published var state = SampleState()

    func select(_ format: SampleFormat) {
        state.selectedFormat = format
        state.message = "\(format.rawValue) 샘플을 여는 자리"
    }

    func markBridgeReady() {
        state.message = "실제 nap ssp SDK 연결 지점을 여기에 붙인다"
    }
}
#endif
