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
                    Text("여기에 실제 nap ssp SDK 연결 코드를 넣는다")
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

                Section {
                    Button("SDK 연결 위치 표시") {
                        viewModel.markBridgeReady()
                    }
                }
            }
            .navigationTitle("nap ssp")
        }
    }
}
