import Foundation
import SwiftUI

struct ConfigureKeysView: View {
    @Environment(\.presentationMode) var presentationMode
    @State private var mediaKey: String = UserDefaults.standard.string(forKey: "media_key") ?? ""
    @State private var banner: String = UserDefaults.standard.string(forKey: "adunit_banner_320x100") ?? ""
    @State private var nativeId: String = UserDefaults.standard.string(forKey: "adunit_native") ?? ""
    @State private var videoId: String = UserDefaults.standard.string(forKey: "adunit_outstream_video") ?? ""

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("MEDIA_KEY")) {
                    TextField("MEDIA_KEY", text: $mediaKey)
                }
                Section(header: Text("Ad Unit IDs")) {
                    TextField("banner_320x100", text: $banner)
                    TextField("native", text: $nativeId)
                    TextField("outstream_video", text: $videoId)
                }
            }
            .navigationBarTitle("Configure Keys", displayMode: .inline)
            .navigationBarItems(leading: Button("Cancel") { presentationMode.wrappedValue.dismiss() }, trailing: Button("Save") { saveAndClose() })
        }
    }

    func saveAndClose() {
        UserDefaults.standard.set(mediaKey, forKey: "media_key")
        UserDefaults.standard.set(banner, forKey: "adunit_banner_320x100")
        UserDefaults.standard.set(nativeId, forKey: "adunit_native")
        UserDefaults.standard.set(videoId, forKey: "adunit_outstream_video")
        UserDefaults.standard.synchronize()
        presentationMode.wrappedValue.dismiss()
    }
}

#if DEBUG
struct ConfigureKeysView_Previews: PreviewProvider {
    static var previews: some View { ConfigureKeysView() }
}
#endif
