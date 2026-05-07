import Foundation

/**
 * NapSSP 광고 키 설정
 *
 * mediaKey와 adUnitIDs를 실제 발급받은 값으로 교체하세요.
 * 운영 앱에서는 xcconfig / Build Setting / 서버 설정으로 주입하는 것을 권장합니다.
 */
enum NapSspConfig {

    // ── 필수: 실제 키로 교체 ──────────────────────────────────────────────
    private static let defaultMediaKey = "10771"            // TODO: 실제 MEDIA_KEY로 교체

    private static let defaultAdUnitIDs: [String: String] = [
        "banner_320x100":          "104704",                // TODO: 실제 AD_UNIT_ID로 교체
        "native":                  "104588",
        "outstream_video":         "104589",
        "reward_video":            "103722",
        "interstitial_320x480":    "104702",
        "interstitial_320x480_f":  "104703",
    ]
    // ──────────────────────────────────────────────────────────────────────

    // MARK: - 접근자 (UserDefaults 오버라이드 지원 — 필요 없으면 단순화 가능)

    static var mediaKey: String {
        let saved = UserDefaults.standard.string(forKey: "napssp_media_key")?.trimmingCharacters(in: .whitespacesAndNewlines)
        return (saved?.isEmpty == false) ? saved! : defaultMediaKey
    }

    static var adUnitIDs: [String: String] {
        var result = defaultAdUnitIDs
        for key in defaultAdUnitIDs.keys {
            if let saved = UserDefaults.standard.string(forKey: "napssp_adunit_\(key)")?.trimmingCharacters(in: .whitespacesAndNewlines),
               !saved.isEmpty {
                result[key] = saved
            }
        }
        return result
    }

    static var mediaKeyInt: Int { Int(mediaKey) ?? 0 }

    static func adUnitID(_ key: String) -> Int {
        Int(adUnitIDs[key] ?? "") ?? Int(defaultAdUnitIDs[key] ?? "") ?? 0
    }

    static var adUnitIDInts: [String: Int] {
        Dictionary(uniqueKeysWithValues: adUnitIDs.map { ($0.key, Int($0.value) ?? 0) })
    }
}
