// swift-tools-version: 5.9
import PackageDescription
let package = Package(
    name: "AdMixerMediation",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "AdMixerMediation",
            targets: ["AdMixerMediation"]),
    ],
    targets: [
        .binaryTarget(
            name: "AdMixerMediation",
            url: "https://github.com/Nasmedia-Tech/iOS-AdMixerDownload/raw/main/AdMixerMediation2.3.2.xcframework.zip",
            checksum: "78c7edb00263cade63925a2e91eddfa9cc84b4a2b73c2036b87b45e97cf43ca7"
        ),
    ]
)
