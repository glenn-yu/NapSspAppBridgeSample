// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "AdMixerMediationAdFit",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "AdMixerMediationAdFit",
            targets: ["iOS_SSP_AdFit_SPM"]),
    ],
    dependencies: [
        // AdFit SDK
        .package(
            url: "https://github.com/adfit/adfit-spm",
            "3.14.7"..<"3.18.6"
        ),
        // SSP AdMixerMediation SDK
        .package(
            path: "../AdMixerMediation"
        )
    ],
    targets: [
        .binaryTarget(
            name: "AdMixerMediationAdFitBinary",
            url: "https://github.com/Nasmedia-Tech/iOS-AdMixerDownload/raw/main/AdMixerMediationAdFit1.0.8.xcframework.zip",
            checksum: "d2e5a8ea596b798f3036a11ea23ec8ca7bb6e7b46d7d401876e465596b197c6e"
        ),
        .target(
            name: "iOS_SSP_AdFit_SPM",
            dependencies: [
                "AdMixerMediationAdFitBinary",
                .product(name: "AdFitSDK", package: "adfit-spm"),
                .product(name: "AdMixerMediation", package: "AdMixerMediation")
            ],
            path: "Sources/iOS-SSP-AdFit-SPM"
        ),
    ]
)
