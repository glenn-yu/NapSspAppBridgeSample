// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "AdMixer",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "AdMixer",
            targets: ["iOS_SSP_SPM"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "AdMixerBinary",
            url: "https://github.com/Nasmedia-Tech/iOS-AdMixerDownload/raw/main/AdMixer1.1.6.xcframework.zip",
            checksum: "60a7896d90e2d1ccab9f1b051ebeb42c8bb53c7838a460fc3b5c8b0c867652de"
        ),
        .target(
            name: "iOS_SSP_SPM",
            dependencies: ["AdMixerBinary"],
            path: "Sources/iOS-SSP-SPM",
            linkerSettings: [
                .linkedFramework("WebKit")
            ]
        )
    ]
)
