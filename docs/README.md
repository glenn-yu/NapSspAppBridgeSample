# 📚 Nap SSP AppBridge 문서 센터

이곳은 **Nap SSP SDK**를 하이브리드 앱에 통합하기 위한 모든 정보를 담고 있습니다. 처음 오셨다면 이 문서부터 시작하세요.

---

## 빠른 시작 (Quick Start)
1. **환경 확인**: Android Studio(Gradle) 및 Xcode(SPM) 환경을 준비합니다.
2. **SDK 설치**: `docs/guide.md`의 설치 섹션을 참고하여 의존성을 추가합니다.
3. **샘플 실행**: 앱의 `HybridWebView` 메뉴에서 `init` -> `광고 호출` 순으로 버튼을 눌러 동작을 확인합니다.

## 문서 지도 (Documentation Map)
- [🚀 마스터 가이드 (guide.md)](./guide.md): **필독.** 설치부터 광고 포맷별 호출 방법까지의 핵심 가이드
- [🔗 하이브리드 브릿지 상세 (hybrid-webview.md)](./hybrid-webview.md): 웹-네이티브 통신(JS Bridge) 심화 가이드
- [📡 미디에이션 및 네트워크 (mediation.md)](./mediation.md): 추가 광고 네트워크 및 상세 설정 참조

## 문제 해결 (Troubleshooting)
- **광고가 안 나와요**: `NapSspConfig`의 MediaKey와 AdUnitID가 유효한지 확인하세요.
- **클릭 이벤트가 안 와요**: 광고 뷰에 `AdListener`(Android) 또는 `Delegate`(iOS)가 설정되었는지 확인하세요.
- **빌드 에러**: 캐시를 삭제하고 다시 빌드하거나, SDK 버전(`1.0.21` 등)이 최신인지 확인하세요.

---
*Nasmedia SDK Technical Support*
