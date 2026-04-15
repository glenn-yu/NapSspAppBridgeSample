// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "NapSspIOSSample",
    platforms: [.iOS(.v13)],
    products: [
        .library(name: "NapSspIOSSample", targets: ["NapSspIOSSample"])
    ],
    targets: [
        .target(name: "NapSspIOSSample", path: "Sources")
    ]
)

// nap ssp SPM package URLs from the vendor guide:
// .package(url: "https://github.com/Nasmedia-Tech/iOS-SSP-Mediation-SPM.git", from: "<version>")
// .package(url: "https://github.com/Nasmedia-Tech/iOS-SSP-SPM.git", from: "<version>")
// Optional network adapters:
// .package(url: "https://github.com/Nasmedia-Tech/iOS-SSP-GAM-SPM.git", from: "<version>")
// .package(url: "https://github.com/Nasmedia-Tech/iOS-SSP-AdFit-SPM.git", from: "<version>")
// .package(url: "https://github.com/Nasmedia-Tech/iOS-SSP-Pangle-SPM.git", from: "<version>")
// .package(url: "https://github.com/Nasmedia-Tech/iOS-SSP-UnityAds-SPM.git", from: "<version>")
// .package(url: "https://github.com/Nasmedia-Tech/iOS-SSP-AppLovin-SPM.git", from: "<version>")
