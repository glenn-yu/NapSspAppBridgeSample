# 📚 Nap SSP AppBridge 문서 센터

이곳은 **Nap SSP SDK**를 하이브리드 앱에 통합하기 위한 모든 정보를 담고 있습니다. 처음 오셨다면 이 문서부터 시작하세요.

---

## 빠른 시작 (Quick Start)
1. **환경 확인**: Android Studio(Gradle) 및 Xcode(SPM) 환경을 준비합니다.
2. **샘플 실행**: 앱을 실행한 뒤 `Configure Keys`에서 MEDIA_KEY와 광고 키를 입력합니다. 입력하지 않으면 샘플 기본값으로 동작합니다.
3. **기본 검증**: 배너 또는 네이티브 광고를 먼저 호출해 SDK 연결과 이벤트를 확인합니다.
4. **하이브리드 검증**: `HybridWebView` 화면에서 `init` 후 광고 버튼을 눌러 WebView 브리지를 확인합니다.

## 문서 지도 (Documentation Map)
- [🚀 통합 개발 가이드 (guide.md)](./guide.md): **필독.** 설치부터 하이브리드 연동까지의 핵심 가이드
- [🔗 하이브리드 브릿지 명세 (hybrid-webview.md)](./hybrid-webview.md): 웹-네이티브 통신(JS Bridge) JSON 규격 상세
- [📡 미디에이션 및 네트워크 (mediation.md)](./mediation.md): 추가 광고 네트워크 및 상세 설정 참조

## 문제 해결 (Troubleshooting)
- **광고가 안 나와요**: `Configure Keys`에 넣은 MediaKey와 AdUnitID가 실제 발급값인지 먼저 확인하세요. 입력하지 않았다면 샘플 기본값으로 동작합니다.
- **클릭 이벤트가 안 와요**: 광고 뷰에 `AdListener`(Android) 또는 `Delegate`(iOS)가 설정되었는지 확인하세요.
- **빌드 에러**: JDK, Xcode, SDK 버전과 의존성 설정을 먼저 확인하세요. Android는 JDK가 없으면 Gradle 실행 자체가 되지 않습니다.

---
*Nasmedia SDK Technical Support*
