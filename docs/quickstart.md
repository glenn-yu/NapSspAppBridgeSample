# Quickstart — NapSsp Native SDK 샘플 (초보자용, 5분 안내)

이 페이지는 처음 보는 개발자도 샘플 앱을 바로 실행해볼 수 있도록 최소한의 단계만 적어놓은 빠른 안내서입니다.

요약(한 줄)
- 필수 설치: JDK, Android Studio (또는 Xcode for iOS)
- 레포 클론 → 샘플 설정(미디어 키/AD_UNIT_ID) → 앱 실행 → 포맷 선택 → 광고 호출

사전 준비
1. Git이 설치되어 있어야 합니다.
2. Android: JDK 11 이상 설치 및 JAVA_HOME 설정
   - macOS 예: /usr/libexec/java_home -v 11
   - Linux/Windows: JDK 설치 경로를 JAVA_HOME으로 설정
3. Android Studio (권장) 또는 Xcode (iOS 빌드용)

레포 클론

```bash
git clone https://github.com/glenn-yu/NapSspAppBridgeSample.git
cd NapSspAppBridgeSample
```

샘플 설정
1. 미디어 키와 광고 단위 ID 설정
   - 파일: `android/app/src/main/java/com/gwangy/nassspandroidsample/NapSspConfig.kt`
   - 파일: `ios/Sources/NapSspIOSSample/NapSspConfig.swift`
   - 기본 더미값이 있으면 그대로 두고, 실제 테스트하려면 벤더에서 받은 MEDIA_KEY와 AD_UNIT_ID를 각각 대체하세요.
2. (선택) 샘플에서 바로 편집하려면 `FormatDetailScreen` 또는 앱 내 설정 화면에 키/ID를 넣을 수 있도록 수정하세요.

Android 실행 (로컬)
1. 터미널에서 앱 루트로 이동:
   ```bash
   cd android
   ./gradlew assembleDebug
   ```
2. Android Studio로 열어 `app` 모듈을 실행하면 에뮬레이터 또는 연결된 기기에서 샘플 앱이 실행됩니다.

iOS 실행 (로컬)
1. 루트에서 `ios/` 패키지를 Xcode로 열고 `NapSspIOSSample`를 선택해 빌드 및 실행합니다.

하이브리드 WebView 사용법 (샘플)
- 앱의 `HybridWebViewScreen`에서 내장 HTML UI(버튼)를 통해 다음 메시지를 보낼 수 있습니다:
  - init, loadBanner, loadNative, loadVideo, loadRewardVideo, loadInterstitialVideo
- 동작 원리(간단)
  1. 웹뷰 버튼 → `window.NapSspBridge.postMessage('init')`
  2. 네이티브 브리지(NapSspHybridBridge)에서 메시지 파싱 후 SDK 훅 실행
  3. 네이티브에서 광고 뷰를 만들어 웹뷰 아래의 네이티브 컨테이너에 추가
  4. 웹뷰에 상태 ack를 다시 보냄 (`window.__napSspAck` 호출)

검증 포인트(문제가 있을 때 확인할 것)
- Android: `Unable to locate a Java Runtime` → JDK 설치 및 JAVA_HOME 확인
- Android: `Class.forName` 실패 → 벤더 SDK(AAR)가 `libs/` 또는 Gradle 종속성으로 포함되었는지 확인
- iOS: `NSClassFromString("AMMediation")` 실패 → Framework 링크가 제대로 되었는지 확인
- WebView: `window.__napSspAck`가 호출되지 않으면 브리지가 등록되었는지( `addJavascriptInterface`)와 JS 콘솔 로그를 확인

개발 팁 (초보자용)
- 로그로 먼저 확인: Android `Logcat`, iOS `Console` 출력에 `NapSsp` 관련 로그가 있는지 확인
- 샘플에서 문제가 있으면 벤더 SDK가 실제로 프로젝트에 포함되어 있지 않은 경우가 많음 — 샘플은 "SDK 없음" 상황의 폴백 화면을 보여주도록 설계되어 있음
- 빠르게 확인하려면 샘플의 `NapSspSdkIntegration` 파일에서 `AdEventLogger` 호출을 보고 어떤 분기가 호출되는지 확인

문제 해결(자주 묻는 질문)
- 광고가 보이지 않아요 → 미디어 키/AD_UNIT_ID가 올바른지, 그리고 벤더 SDK가 포함되었는지 확인하세요.
- WebView에서 브리지 응답이 없어요 → 브리지 이름(`NapSspBridge`)이 맞는지, HTML 버튼의 `postMessage` 문자열이 올바른지 확인하세요.

추가 자료
- 레포 내 `docs/guide.md` — 전체 흐름과 상세 설명
- 벤더 SDK 설치 가이드(프로젝트별): Android AAR / iOS Framework 위치 설명

끝.
