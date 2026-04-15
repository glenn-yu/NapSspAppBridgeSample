import Foundation

enum NapSspInitializer {
    static func initialize() {
        // 테스트값 적용됨
        // 실제 nap ssp iOS SDK 초기화 코드
        // 예: SDK 초기화, consent 처리, ad unit 준비
        print("NapSsp iOS init mediaKey=\(NapSspConfig.mediaKey) adUnitIds=\(NapSspConfig.adUnitIDs.values.joined(separator: ","))")
        print("NapSsp iOS mediation hints=\(NapSspConfig.mediationHints.map { $0.0 + ":" + $0.1 }.joined(separator: ","))")
    }
}
