# 📚 Nap SSP AppBridge 문서 센터

이곳은 **Nap SSP SDK**를 하이브리드 앱에 통합하기 위한 모든 정보를 담고 있습니다. 처음 오셨다면 아래 순서로 보세요.

## 추천 순서
1. **`quickstart.md`** — 한 화면 빠른 시작
2. **`guide.md`** — 전체 흐름과 상세 예시
3. **`hybrid-webview.md`** — 웹-네이티브 브릿지 JSON 규격
4. **`ios-vendor-lfs-migration-plan.md`** — `ios/Vendor/` Git LFS 이전 계획
5. **`release-notes-draft.md`** — 다음 릴리즈 초안
6. **플랫폼 공식 가이드** — `nap-ssp-android-sdk-native.md`, `nap-ssp-ios-sdk-native.md`

## 빌드 전 확인
- Android: JDK 17, `JAVA_HOME`, Android Studio 또는 Gradle 실행 환경
- iOS: Xcode 15.3 이상, 현재 샘플은 `ios/Vendor/` 아래 로컬 xcframework를 포함
- 공통: MediaKey / AdUnitID 없이도 데모 모드 확인은 가능하지만 실제 광고 응답 검증에는 발급값이 필요합니다.

## 문제 해결
- **광고가 안 나와요**: `Configure Keys`의 MediaKey와 AdUnitID가 실제 발급값인지 확인하세요.
- **클릭 이벤트가 안 와요**: 광고 뷰에 `AdListener`(Android) 또는 `Delegate`(iOS)가 설정되었는지 확인하세요.
- **빌드 에러**: JDK, Xcode, SDK 버전과 의존성 설정을 먼저 확인하세요.
- **SPM / Gradle 의존성 오류**: `docs/mediation.md`와 플랫폼별 공식 가이드를 함께 확인하세요.
