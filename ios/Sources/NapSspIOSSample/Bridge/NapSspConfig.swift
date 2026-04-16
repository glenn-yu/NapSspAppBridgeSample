import Foundation

/**
 * NapSSP 하이브리드 광고 설정 파일
 * [보안 권고] 실제 서비스 시 mediaKey와 adUnitIDs는 소스 코드에 하드코딩하지 않고,
 * .xcconfig 파일이나 서버에서 동적으로 내려받는 방식을 권장합니다.
 */
enum NapSspConfig {
    // 발급받은 미디어 키를 여기에 입력하세요.
    static let mediaKey = "10347"
    
    // 각 포맷별 발급받은 애드유닛 아이디를 매핑하세요.
    static let adUnitIDs: [String: String] = [
        "banner_320x100": "104707",
        "interstitial_320x480_f": "103868",
        "interstitial_320x480": "104708",
        "banner_320x50": "103790",
        "instream_video": "104711",
        "outstream_video": "104709",
        "native": "101626",
        "reward_video": "104710"
    ]
}
