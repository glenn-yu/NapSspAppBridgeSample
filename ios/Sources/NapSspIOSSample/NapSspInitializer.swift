import Foundation

enum NapSspInitializer {
    private static let mediaKey = "10347"
    private static let adUnitIDs = [
        "104707", // 320x100
        "103868", // 320x480 (F)
        "104708", // 320x480
        "103790", // 320x50
        "104711", // instream Video
        "104709", // outstream video
        "101626", // native
        "103869", // 300x250 (F)
        "104710", // reward video
        "101624"  // 300x250
    ]

    static func initialize() {
        // 테스트값 적용됨
        // 실제 nap ssp iOS SDK 초기화 코드
        // 예: SDK 초기화, consent 처리, ad unit 준비
        print("NapSsp iOS init mediaKey=\(mediaKey) adUnitIds=\(adUnitIDs.joined(separator: ","))")
    }
}
