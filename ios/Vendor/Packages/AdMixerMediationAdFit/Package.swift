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
            url: "https://github.com/Nasmedia-Tech/iOS-AdMixerDownload/raw/main/AdMixerMediationAdFit1.0.7.xcframework.zip",
            checksum: "800d663320d261ef22f646cd221741c9a1ea3f0ecb0f79c31708f3a2e08e4c14"
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
