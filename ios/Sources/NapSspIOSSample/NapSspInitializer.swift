import Foundation

enum NapSspInitializer {
    static func initialize() {
        NapSspSdkIntegration.initialize()
        print("NapSsp iOS init mediaKey=\(NapSspConfig.mediaKey) adUnitIds=\(NapSspConfig.adUnitIDs.values.joined(separator: ","))")
        print("NapSsp iOS mediation hints=\(NapSspConfig.mediationHints.map { $0.0 + ":" + $0.1 }.joined(separator: ","))")
    }
}
