// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "NapSspIOSSample",
    platforms: [.iOS(.v14)],
    products: [
        .library(name: "NapSspIOSSample", targets: ["NapSspIOSSample"])
    ],
    targets: [
        .binaryTarget(
            name: "AdMixer",
            path: "Vendor/AdMixer/AdMixer.xcframework"
        ),
        .binaryTarget(
            name: "AdMixerMediation",
            path: "Vendor/AdMixerMediation/AdMixerMediation.xcframework"
        ),
        .target(
            name: "NapSspIOSSample",
            dependencies: ["AdMixer", "AdMixerMediation"],
            path: "Sources"
        )
    ]
)
