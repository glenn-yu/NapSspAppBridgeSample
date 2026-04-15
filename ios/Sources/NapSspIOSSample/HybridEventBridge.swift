import Foundation

enum HybridEventBridge {
    static func logRequest(_ message: String) { AdEventLogger.request(format: "hybrid", id: message) }
    static func logLoaded(_ message: String) { AdEventLogger.loaded(format: "hybrid", id: message) }
    static func logDisplayed(_ message: String) { AdEventLogger.displayed(format: "hybrid", id: message) }
    static func logClicked(_ message: String) { AdEventLogger.clicked(format: "hybrid", id: message) }
    static func logFailed(_ message: String, reason: String) { AdEventLogger.failed(format: "hybrid", id: message, reason: reason) }
}
