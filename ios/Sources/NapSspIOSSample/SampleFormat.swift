import Foundation

enum SampleFormat: String, CaseIterable, Identifiable {
    case banner = "배너"
    case native = "네이티브"
    case video = "동영상"
    case rewardVideo = "리워드 동영상"
    case interstitialVideo = "전면 동영상"

    var id: String { rawValue }

    var descriptionText: String {
        switch self {
        case .banner: return "화면 아래 고정형 샘플"
        case .native: return "카드형 UI에 섞는 샘플"
        case .video: return "앱 안 재생 영역 샘플"
        case .rewardVideo: return "시청 완료 보상 샘플"
        case .interstitialVideo: return "전체 화면 노출 샘플"
        }
    }
}
