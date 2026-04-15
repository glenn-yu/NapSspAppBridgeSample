#if os(iOS)
import SwiftUI

struct ContentView: View {
    @StateObject private var viewModel = SampleViewModel()

    var body: some View {
        NavigationView {
            List {
                Section {
                    AdDemoScreen(
                        title: "nap ssp iOS 샘플",
                        subtitle: "1) 포맷 선택 → 2) 광고 코드 실행 → 3) 화면에서 결과 확인"
                    )
                }

                Section("현재 상태") {
                    Text(viewModel.state.message)
                }

                Section("지금 선택된 광고") {
                    Text(viewModel.state.selectedFormat.rawValue)
                        .font(.headline)
                    Text(viewModel.state.selectedFormat.descriptionText)
                    Text(SdkHooks.describe(viewModel.state.selectedFormat))
                        .foregroundStyle(.secondary)
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

                Section {
                    Button("연결 자리 보기") {
                        viewModel.markBridgeReady()
                    }
                    Button("광고 띄우기") {
                        let result: String
                        switch viewModel.state.selectedFormat {
                        case .banner:
                            result = NapSspSdkIntegration.banner()
                        case .native:
                            result = NapSspSdkIntegration.native()
                        case .video:
                            result = NapSspSdkIntegration.video()
                        case .rewardVideo:
                            result = NapSspSdkIntegration.rewardVideo()
                        case .interstitialVideo:
                            result = NapSspSdkIntegration.interstitialVideo()
                        case .hybridWebView:
                            result = "하이브리드 WebView는 init 후 웹 버튼으로 광고를 부른다"
                        }
                        if viewModel.state.selectedFormat == .hybridWebView {
                            viewModel.reportResult(result)
                        } else if result.contains("fallback") {
                            viewModel.reportResult("\(viewModel.state.selectedFormat.rawValue) 광고 연결 실패 또는 폴백")
                        } else {
                            viewModel.reportResult("\(viewModel.state.selectedFormat.rawValue) 광고 뷰 연결 완료")
                        }
                        print("NapSsp iOS result: \(result)")
                    }
                }

                if viewModel.state.selectedFormat == .hybridWebView {
                    Section("웹뷰 하이브리드 미리보기") {
                        Text("먼저 init을 누르고, 그다음 광고 버튼을 눌러보면 된다")
                        HybridWebViewScreen()
                            .frame(height: 360)
                    }
                } else {
                    Section("SDK 결과 화면") {
                        AdDemoScreen(
                            title: viewModel.state.selectedFormat.rawValue,
                            subtitle: "NapSsp iOS SDK 결과 화면"
                        )
                    }
                }
            }
            .navigationTitle("nap ssp")
        }
    }
}
#endif
