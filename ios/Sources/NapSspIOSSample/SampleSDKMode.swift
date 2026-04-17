import Foundation

enum SampleSDKMode: String, CaseIterable, Identifiable {
    case vendored
    case remoteSPM

    var id: String { rawValue }

    var title: String {
        switch self {
        case .vendored:
            return "Use vendored"
        case .remoteSPM:
            return "Use remote SPM"
        }
    }

    var subtitle: String {
        switch self {
        case .vendored:
            return "Prefer ios/Vendor xcframeworks."
        case .remoteSPM:
            return "Prefer remote Swift Package dependencies."
        }
    }

    var badgeLabel: String {
        switch self {
        case .vendored:
            return "Vendored xcframeworks"
        case .remoteSPM:
            return "Remote SPM"
        }
    }
}
