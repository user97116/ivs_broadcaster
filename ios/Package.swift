// swift-tools-version: 5.10
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "IVSBroadcaster",
    platforms: [.iOS(.v14)],
    products: [
        // Products define the executables and libraries a package produces, making them visible to other packages.
        .library(
            name: "IVSBroadcaster",
            targets: ["IVSBroadcaster"]
        ),
    ],
    targets: [
        // Targets are the basic building blocks of a package, defining a module or a test suite.
        // Targets can depend on other targets in this package and products from dependencies.
        .target(
            name: "IVSBroadcaster",
             .binaryTarget(
               name: "AmazonIVSChatMessaging",
               url: "https://ivschat.live-video.net/1.0.0/AmazonIVSChatMessaging.xcframework.zip",
               checksum: "c92ac3adc061a3fa5558311f8d99fa9b9dba7a00b3482a4f4ac90b2ff4f65b66"
            ),
        ),
        .testTarget(
            name: "IVSBroadcasterTests",
            dependencies: ["IVSBroadcaster"]
        ),
    ]
)