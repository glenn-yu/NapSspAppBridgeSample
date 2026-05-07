import Foundation

/**
 * 앱 내부에서 광고 이벤트를 NotificationCenter로 전파합니다.
 * 광고 이벤트를 다른 모듈에서 수신해야 할 때 활용하세요.
 * 필요 없으면 이 파일과 NapSspSdkIntegration의 호출부를 제거해도 됩니다.
 */

extension Notification.Name {
    static let napSspAdEvent = Notification.Name("NapSspAdEvent")
}

struct NapSspAdEventBridge {
    static func post(event: String, format: String, id: String, detail: String? = nil) {
        var userInfo: [String: Any] = [
            "event": event,
            "format": format,
            "id": id,
        ]
        if let detail { userInfo["detail"] = detail }
        NotificationCenter.default.post(name: .napSspAdEvent, object: nil, userInfo: userInfo)
    }
}
