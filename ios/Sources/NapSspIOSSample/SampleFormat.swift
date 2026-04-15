import Foundation

enum SampleFormat: String, CaseIterable, Identifiable {
    case banner = "배너"
    case native = "네이티브"
    case video = "동영상"
    case rewardVideo = "리워드 동영상"
    case interstitialVideo = "전면 동영상"
    case hybridWebView = "웹뷰 하이브리드"

    var id: String { rawValue }

    var descriptionText: String {
        switch self {
        case .banner: return "화면 아래에 붙는 작은 광고"
        case .native: return "앱 화면에 자연스럽게 섞이는 광고"
        case .video: return "앱 안에서 재생되는 광고"
        case .rewardVideo: return "끝까지 보면 보상이 있는 광고"
        case .interstitialVideo: return "화면 전체를 덮는 광고"
        case .hybridWebView: return "웹 버튼으로 네이티브 광고를 여는 방식"
        }
    }
}
