# 📚 Nap SSP AppBridge Document Center / 문서 센터

[English](#english) | [한국어](#한국어)

---

## English

This directory contains all the information needed to integrate the **Nap SSP SDK** into hybrid applications. If you are new to this project, please follow the recommended reading order.

### Recommended Reading Order
1. **`quickstart.md`** — Single-page quickstart
2. **`publisher-minimal-integration.md`** — Minimum files/configurations required for publishers
3. **`delivery-packages.md`** — Criteria for selecting external delivery package scopes
4. **`public-hybrid-bridge-guide.md`** — Public specification for WebView ↔ Native Bridge
5. **`guide.md`** — End-to-end integration flow and detailed code examples
6. **`hybrid-webview.md`** — Web-Native bridge JSON schema specification
7. **`hybrid-bridge-advanced.md`** — Advanced integration and troubleshooting guides
8. **Platform Guides** — `nap-ssp-android-sdk-native.md`, `nap-ssp-ios-sdk-native.md`

### Document Scopes & Recipients
- **External Publishers**: `publisher-minimal-integration.md` + Native SDK guide for each platform.
- **Hybrid WebView Publishers**: `public-hybrid-bridge-guide.md` + `publisher-minimal-integration.md`.
- **PoC & Technical Reviews**: Full repository + `quickstart.md`.

### Build Prerequisites
- **Android**: JDK 17, `JAVA_HOME` configuration, and Android Studio/Gradle.
- **iOS**: Xcode 15.3+ (uses local xcframeworks located in `ios/Vendor/`).
- **Common**: While demo/mock mode works without actual credentials, real validation requires an issued `MediaKey` and `AdUnitID`.

### Troubleshooting
- **No Ads Loading**: Verify that the `MediaKey` and `AdUnitID` in `Configure Keys` are active.
- **No Click Events Received**: Ensure that `AdListener` (Android) or `Delegate` (iOS) is set on the ad view object.
- **Build Compilation Failure**: Check JDK, Xcode, SDK version mappings and dependency targets first.
- **SPM / Gradle Dependency Issues**: Refer to the official platform guides.

---

## 한국어

이곳은 **Nap SSP SDK**를 하이브리드 앱에 통합하기 위한 모든 정보를 담고 있습니다. 처음 오셨다면 아래 순서로 보세요.

### 추천 순서
1. **`quickstart.md`** — 한 화면 빠른 시작
2. **`publisher-minimal-integration.md`** — 일반 매체 앱에 필요한 최소 파일/설정
3. **`delivery-packages.md`** — 외부 전달 범위 선택 기준
4. **`public-hybrid-bridge-guide.md`** — WebView ↔ Native 브릿지 공개 규격
5. **`guide.md`** — 전체 흐름과 상세 예시
6. **`hybrid-webview.md`** — 웹-네이티브 브릿지 JSON 규격
7. **`hybrid-bridge-advanced.md`** — 고급 연동 및 트러블슈팅 가이드
8. **플랫폼 공식 가이드** — `nap-ssp-android-sdk-native.md`, `nap-ssp-ios-sdk-native.md`

### 문서 전달 기준
- **일반 매체사**: `publisher-minimal-integration.md` + 플랫폼별 Native SDK 가이드
- **Hybrid WebView 매체사**: `public-hybrid-bridge-guide.md` + `publisher-minimal-integration.md`
- **PoC/레퍼런스 검토**: 저장소 전체와 `quickstart.md`

### 빌드 전 확인
- **Android**: JDK 17, `JAVA_HOME`, Android Studio 또는 Gradle 실행 환경
- **iOS**: Xcode 15.3 이상, 현재 샘플은 `ios/Vendor/` 아래 로컬 xcframework를 포함
- **공통**: MediaKey / AdUnitID 없이도 데모 모드 확인은 가능하지만 실제 광고 응답 검증에는 발급값이 필요합니다.

### 문제 해결
- **광고가 안 나와요**: `Configure Keys`의 MediaKey와 AdUnitID가 실제 발급값인지 확인하세요.
- **클릭 이벤트가 안 와요**: 광고 뷰에 `AdListener`(Android) 또는 `Delegate`(iOS)가 설정되었는지 확인하세요.
- **빌드 에러**: JDK, Xcode, SDK 버전과 의존성 설정을 먼저 확인하세요.
- **SPM / Gradle 의존성 오류**: 플랫폼별 공식 가이드를 확인하세요.
