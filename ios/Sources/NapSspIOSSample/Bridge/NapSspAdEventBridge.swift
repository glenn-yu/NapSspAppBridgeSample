import Foundation

extension Notification.Name {
    static let napSspAdEvent = Notification.Name("NapSspAdEvent")
}

struct NapSspAdEventBridge {
    static func post(event: String, format: String, id: String, detail: String? = nil) {
        var userInfo: [String: Any] = [
            "event": event,
            "format": format,
            "id": id
        ]
        if let detail {
            userInfo["detail"] = detail
        }
        NotificationCenter.default.post(name: .napSspAdEvent, object: nil, userInfo: userInfo)
    }
}
