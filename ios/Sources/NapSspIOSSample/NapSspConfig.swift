import Foundation

enum NapSspConfig {
    private static let defaultMediaKey = "10771"
    private static let defaultAdUnitIDs: [String: String] = [
        "banner_320x100": "104704",
        "interstitial_320x480_f": "104703",
        "interstitial_320x480": "104702",
        "banner_320x50": "104701",
        "instream_video": "104591",
        "outstream_video": "104589",
        "native": "104588",
        "banner_300x250_f": "104703",
        "reward_video": "103722",
        "banner_300x250": "104704"
    ]

    static var mediaKey: String {
        let saved = UserDefaults.standard.string(forKey: "media_key")?.trimmingCharacters(in: .whitespacesAndNewlines)
        return (saved?.isEmpty == false) ? saved! : defaultMediaKey
    }

    static var adUnitIDs: [String: String] {
        var result = defaultAdUnitIDs
        for key in defaultAdUnitIDs.keys {
            if let saved = UserDefaults.standard.string(forKey: "adunit_\(key)")?.trimmingCharacters(in: .whitespacesAndNewlines), !saved.isEmpty {
                result[key] = saved
            }
        }
        return result
    }

    static var mediaKeyInt: Int {
        Int(mediaKey) ?? Int(defaultMediaKey) ?? 0
    }

    static func adUnitID(_ key: String) -> Int {
        Int(adUnitIDs[key] ?? "") ?? Int(defaultAdUnitIDs[key] ?? "") ?? 0
    }

    static var adUnitIDInts: [String: Int] {
        Dictionary(uniqueKeysWithValues: adUnitIDs.map { ($0.key, Int($0.value) ?? 0) })
    }

    static let mediationHints: [(String, String)] = [
        ("GAM", "https://github.com/Nasmedia-Tech/iOS-SSP-GAM-SPM.git"),
        ("AdFit", "https://github.com/Nasmedia-Tech/iOS-SSP-AdFit-SPM.git"),
        ("Pangle", "https://github.com/Nasmedia-Tech/iOS-SSP-Pangle-SPM.git"),
        ("Unity Ads", "https://github.com/Nasmedia-Tech/iOS-SSP-UnityAds-SPM.git"),
        ("AppLovin", "https://github.com/Nasmedia-Tech/iOS-SSP-AppLovin-SPM.git")
    ]
}
