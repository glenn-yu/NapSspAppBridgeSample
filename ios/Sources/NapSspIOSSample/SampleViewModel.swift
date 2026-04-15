#if os(iOS)
import Foundation
import SwiftUI

final class SampleViewModel: ObservableObject {
    @Published var state = SampleState()

    func select(_ format: SampleFormat) {
        state.selectedFormat = format
        state.message = "\(format.rawValue) 준비됨"
    }

    func markBridgeReady() {
        state.message = "연결 위치 확인 완료"
    }

    func reportResult(_ result: String) {
        state.message = result
    }
}
#endif
