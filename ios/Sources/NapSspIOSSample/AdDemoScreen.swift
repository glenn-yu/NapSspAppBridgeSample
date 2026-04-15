#if os(iOS)
import SwiftUI

struct AdDemoScreen: View {
    let title: String
    let subtitle: String
    var onClose: (() -> Void)? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title).font(.title2).bold()
            Text(subtitle).foregroundStyle(.secondary)
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(red: 0.92, green: 0.94, blue: 0.97))
                .frame(height: 140)
                .overlay(
                    VStack(spacing: 8) {
                        Text("광고 자리").bold()
                        Text("실제 nap ssp SDK가 연결되면 여기에 광고 뷰가 표시된다.")
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
        .background(Color.white)
        .cornerRadius(16)
    }
}
#endif
