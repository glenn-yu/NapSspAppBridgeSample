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
