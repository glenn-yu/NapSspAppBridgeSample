import Foundation

enum AdEventLogger {
    static func request(format: String, id: String) { print("NapSsp request format=\(format) id=\(id)") }
    static func loaded(format: String, id: String) { print("NapSsp loaded format=\(format) id=\(id)") }
    static func displayed(format: String, id: String) { print("NapSsp displayed format=\(format) id=\(id)") }
    static func clicked(format: String, id: String) { print("NapSsp clicked format=\(format) id=\(id)") }
    static func failed(format: String, id: String, reason: String) { print("NapSsp failed format=\(format) id=\(id) reason=\(reason)") }
}
