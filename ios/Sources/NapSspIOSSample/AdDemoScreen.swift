import SwiftUI

struct AdDemoScreen: View {
    let title: String
    let subtitle: String
    var onClose: (() -> Void)? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title).font(.title2).bold()
            Text(subtitle)
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.systemGray5))
                .frame(height: 140)
                .overlay(
                    VStack(spacing: 8) {
                        Text("SDK 화면 폴백").bold()
                        Text("실제 nap ssp SDK 바이너리가 들어오면 이 자리에 광고 뷰가 붙는다.")
                            .multilineTextAlignment(.center)
                            .font(.footnote)
                    }
                    .padding()
                )
            if let onClose {
                Button("닫기", action: onClose)
                    .buttonStyle(.borderedProminent)
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(16)
    }
}
