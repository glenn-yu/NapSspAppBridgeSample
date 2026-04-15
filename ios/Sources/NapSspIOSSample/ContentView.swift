import SwiftUI

struct ContentView: View {
    @StateObject private var viewModel = SampleViewModel()

    var body: some View {
        NavigationView {
            List {
                Section("nap ssp iOS Native Sample") {
                    Text("포맷을 고르고, 나중에 실제 SDK 코드를 꽂는 구조")
                    Text(viewModel.state.message)
                }

                Section("현재 선택된 포맷") {
                    Text(viewModel.state.selectedFormat.rawValue)
                    Text(viewModel.state.selectedFormat.descriptionText)
                    Text(SdkHooks.describe(viewModel.state.selectedFormat))
                    Text("임시 media key: 11111")
                }

                Section("포맷 선택") {
                    ForEach(SampleFormat.allCases) { format in
                        Button {
                            viewModel.select(format)
                        } label: {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(format.rawValue)
                                Text(format.descriptionText)
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                }

                if viewModel.state.selectedFormat == .hybridWebView {
                    Section("웹뷰 하이브리드 미리보기") {
                        HybridWebViewScreen(urlString: "https://example.com")
                            .frame(height: 360)
                    }
                }

                Section {
                    Button("SDK 연결 위치 표시") {
                        viewModel.markBridgeReady()
                    }
                    Button("SDK 훅 실행") {
                        switch viewModel.state.selectedFormat {
                        case .banner:
                            NapSspSdkIntegration.banner()
                        case .native:
                            NapSspSdkIntegration.native()
                        case .video:
                            NapSspSdkIntegration.video()
                        case .rewardVideo:
                            NapSspSdkIntegration.rewardVideo()
                        case .interstitialVideo:
                            NapSspSdkIntegration.interstitialVideo()
                        case .hybridWebView:
                            viewModel.markBridgeReady()
                        }
                    }
                }
            }
            .navigationTitle("nap ssp")
        }
    }
}
