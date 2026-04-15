import Foundation

enum NapSspConfig {
    static let mediaKey = "10347"
    static let adUnitIDs: [String: String] = [
        "banner_320x100": "104707",
        "interstitial_320x480_f": "103868",
        "interstitial_320x480": "104708",
        "banner_320x50": "103790",
        "instream_video": "104711",
        "outstream_video": "104709",
        "native": "101626",
        "banner_300x250_f": "103869",
        "reward_video": "104710",
        "banner_300x250": "101624"
    ]

    static let mediationHints: [(String, String)] = [
        ("GAM", "https://github.com/Nasmedia-Tech/iOS-SSP-GAM-SPM.git"),
        ("AdFit", "https://github.com/Nasmedia-Tech/iOS-SSP-AdFit-SPM.git"),
        ("Pangle", "https://github.com/Nasmedia-Tech/iOS-SSP-Pangle-SPM.git"),
        ("Unity Ads", "https://github.com/Nasmedia-Tech/iOS-SSP-UnityAds-SPM.git"),
        ("AppLovin", "https://github.com/Nasmedia-Tech/iOS-SSP-AppLovin-SPM.git")
    ]
}
