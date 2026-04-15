import SwiftUI

struct ContentView: View {
    @State private var message = "포맷을 선택해 주세요"

    var body: some View {
        NavigationView {
            VStack(alignment: .leading, spacing: 12) {
                Text("nap ssp iOS Native Sample")
                    .font(.headline)
                Text(message)
                    .font(.subheadline)

                Button("배너") { message = "배너 샘플을 여는 자리" }
                Button("네이티브") { message = "네이티브 샘플을 여는 자리" }
                Button("동영상") { message = "동영상 샘플을 여는 자리" }
                Button("리워드 동영상") { message = "리워드 동영상 샘플을 여는 자리" }
                Button("전면 동영상") { message = "전면 동영상 샘플을 여는 자리" }

                Spacer()
            }
            .padding()
            .navigationTitle("nap ssp")
        }
    }
}
