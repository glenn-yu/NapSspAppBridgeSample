# Delivery Packages Guide / 외부 전달용 문서/파일 패키지 분리안

[English](#english) | [한국어](#한국어)

---

## English

This document provides a set of criteria to determine **how and to what extent** the NapSsp AppBridge sample repository should be packaged and delivered to different target recipients.

Since the full repository bundles testing UI and automation tools, delivery of the entire codebase may overwhelm external developers. Use this criteria to separate files and documentation dynamically based on your audience.

### 1. Recommended Structures

| Package | Recipient | Purpose | Delivery Scope |
|---|---|---|---|
| **Minimal Integration** | Standard Native App Developer | Quick reference to minimum required dependencies | `publisher-minimal-integration.md` + Native SDK Guides |
| **Hybrid Bridge** | Hybrid WebView Developer | JavaScript ↔ Native communication protocols | Public Bridge Guide + Minimal Integration Guide |
| **Internal Dev/QA** | Repo Maintainers & QA | Architecture maintenance and risk mitigation | Internal Developer Guide + Validation Reports + Full Code |
| **Full Sample** | PoC & Technical Reviewers | Interactive reference layouts and demo testing | Entire Repository |

---

### 2. Minimal Integration Package

#### Target Audience
- Publishers looking to integrate the SDK into a standard native app environment.
- Developers who prefer direct configuration snippets over complex demo screens.

#### Delivery Documentation
```text
docs/publisher-minimal-integration.md
docs/nap-ssp-android-sdk-native.md
docs/nap-ssp-ios-sdk-native.md
```

#### Android Core Code Files
```text
android/app/src/main/java/.../bridge/NapSspConfig.kt
android/app/src/main/java/.../bridge/NapSspSdkIntegration.kt
android/app/src/main/res/layout/admixer_item_*.xml   # Only for Native layouts
```

Accompanying configurations:
```text
build.gradle.kts dependency updates
AndroidManifest.xml permissions & meta-data entries
```

#### iOS Core Code Files
```text
ios/Sources/NapSspIOSSample/NapSspConfig.swift
ios/Sources/NapSspIOSSample/Bridge/NapSspSdkIntegration.swift
ios/Sources/NapSspIOSSample/Bridge/InterstitialModule.swift   # For Interstitial Ads
ios/Sources/NapSspIOSSample/Bridge/RewardedModule.swift       # For Rewarded Video Ads
ios/Sources/NapSspIOSSample/AMMNativeAdView.xib               # Only for Native layouts
```

Accompanying configurations:
```text
project.yml package dependencies
Info.plist ad key mappings
Config.xcconfig credential variables
```

---

### 3. Hybrid Bridge Package

#### Target Audience
- Publishers invoking native SDKs from JavaScript inside WebViews.
- Developers maintaining a single, unified JavaScript codebase for both Android and iOS targets.

#### Delivery Documentation
```text
docs/public-hybrid-bridge-guide.md
docs/publisher-minimal-integration.md
docs/hybrid-webview.md
```

#### Additional Code Files
* **Android**:
  ```text
  android/app/src/main/java/.../HybridWebViewScreen.kt
  android/app/src/main/assets/index.html   # Sample Reference
  ```
* **iOS**:
  ```text
  ios/Sources/NapSspIOSSample/HybridWebViewScreen.swift
  ios/Sources/NapSspIOSSample/index.html   # Sample Reference
  ```

> [!WARNING]
> - `index.html` is provided as a reference framework; publishers should only adapt the script bindings to their existing web page code.
> - The `Accepted <format>` response indicates request acknowledgment, not necessarily successful ad load.
> - SDK events (`loaded`, `failed`, `clicked`, `closed`) are delivered separately through the `event` action payload.

---

### 4. Internal Developer / QA Package

#### Target Audience
- Repository maintainers and project contributors.
- QA testers validating integration setups.
- Developers running localized test automation.

#### Delivery Documentation
```text
docs/internal-bridge-developer-guide.md
docs/bridge-validation-report.md
docs/mediation.md
docs/release-notes-draft.md
```

#### Included Assets
```text
maestro/*
examples/*
Full Sample Application Repository Source Code
```
This configuration may include debugging utilities, sample data models, view models, and testing logs.

---

### 5. Recommended Exclusions for External Delivery

To minimize clutter for external publishers, exclude these files from standard delivery packages:

* **Android**:
  `AdDemoScreen.kt`, `FormatDetailScreen.kt`, `Sample*.kt`, `IntentBridgeReceiver.kt`, `MainActivity.kt`
* **iOS**:
  `AdDemoScreen.swift`, `Sample*.swift`, `ContentView.swift`, `SdkHooks.swift`
* **Docs**:
  `docs/internal-bridge-developer-guide.md`, `docs/bridge-validation-report.md`, `docs/ios-vendor-lfs-migration-plan.md`, `docs/release-notes-draft.md`
*(Unless required for deep technical reviews or active QA compliance testing).*

---

### 6. Recommended Operations Workflow

Maintain the repository as a **fully functional, interactive reference project**, and organize documentation according to clear target audiences:

```text
docs/publisher-minimal-integration.md  # Target: External publishers (Native)
docs/public-hybrid-bridge-guide.md     # Target: External publishers (Hybrid WebView)
docs/delivery-packages.md              # Target: Project packaging criteria
docs/internal-bridge-developer-guide.md # Target: Internal engineering / QA
docs/bridge-validation-report.md       # Target: Integration verification logs
```
This guarantees publishers receive clean, focused reference materials, with options to scale up to internal documentation or full repository access as integration requirements evolve.

---

## 한국어

이 문서는 NapSsp AppBridge 샘플 저장소를 **누구에게, 어느 범위까지 전달할지** 나누기 위한 기준입니다.

샘플앱은 데모 UI와 테스트 도구까지 포함하기 때문에 외부 매체사에게 그대로 전달하면 실제 필수 파일보다 크게 느껴질 수 있습니다. 따라서 전달 대상별로 문서와 파일을 분리합니다.

### 1. 권장 구조

| 패키지 | 대상 | 목적 | 전달 범위 |
|---|---|---|---|
| **최소 연동 패키지** | 일반 매체 앱 개발자 | Native SDK 연동에 필요한 최소 파일 안내 | `publisher-minimal-integration.md` + 플랫폼별 SDK 가이드 |
| **Hybrid Bridge 패키지** | WebView 기반 매체 앱 개발자 | JS ↔ Native 광고 호출 규격 안내 | 공개 브릿지 가이드 + 최소 연동 가이드 |
| **내부 개발/QA 패키지** | 저장소 유지보수자, QA | 샘플앱 구조, 테스트, 리스크 관리 | 내부 개발자 가이드 + 검증 리포트 + 샘플 전체 |
| **전체 샘플 패키지** | PoC/레퍼런스 검토자 | 실제 실행 가능한 데모 앱 확인 | 저장소 전체 |

---

### 2. 최소 연동 패키지

#### 대상
- 일반 Native 앱에 광고 SDK를 붙이려는 매체사
- 샘플앱 UI는 필요 없고, 필수 설정과 코드만 보고 싶은 개발자

#### 전달 문서
```text
docs/publisher-minimal-integration.md
docs/nap-ssp-android-sdk-native.md
docs/nap-ssp-ios-sdk-native.md
```

#### Android 전달 파일 예시
```text
android/app/src/main/java/.../bridge/NapSspConfig.kt
android/app/src/main/java/.../bridge/NapSspSdkIntegration.kt
android/app/src/main/res/layout/admixer_item_*.xml   # Native 광고 사용 시만
```

함께 안내할 설정:
```text
android/app/build.gradle.kts dependency 일부
android/app/src/main/AndroidManifest.xml 권한/meta-data 일부
```

#### iOS 전달 파일 예시
```text
ios/Sources/NapSspIOSSample/NapSspConfig.swift
ios/Sources/NapSspIOSSample/Bridge/NapSspSdkIntegration.swift
ios/Sources/NapSspIOSSample/Bridge/InterstitialModule.swift   # 전면 광고 사용 시
ios/Sources/NapSspIOSSample/Bridge/RewardedModule.swift       # 보상형 광고 사용 시
ios/Sources/NapSspIOSSample/AMMNativeAdView.xib               # Native 광고 사용 시
```

함께 안내할 설정:
```text
ios/project.yml package dependency 일부
Info.plist 광고 관련 항목
Config.xcconfig 키 관리 방식
```

---

### 3. Hybrid Bridge 패키지

#### 대상
- WebView 화면에서 JavaScript로 광고를 요청해야 하는 매체사
- Android/iOS 양쪽에서 같은 JS API를 유지하고 싶은 개발자

#### 전달 문서
```text
docs/public-hybrid-bridge-guide.md
docs/publisher-minimal-integration.md
docs/hybrid-webview.md
```

#### 추가 전달 파일
* **Android**:
  ```text
  android/app/src/main/java/.../HybridWebViewScreen.kt
  android/app/src/main/assets/index.html   # 샘플 참고용
  ```
* **iOS**:
  ```text
  ios/Sources/NapSspIOSSample/HybridWebViewScreen.swift
  ios/Sources/NapSspIOSSample/index.html   # 샘플 참고용
  ```

주의:
- `index.html`은 샘플 UI입니다. 실제 매체 앱에서는 기존 웹 코드에 bridge 호출 규격만 반영하면 됩니다.
- `loadAd`의 `Accepted <format>` 응답은 광고 성공이 아니라 요청 접수 ACK입니다.
- 실제 광고 로드/실패/클릭/종료 이벤트는 `event` action으로 별도 전달됩니다.

---

### 4. 내부 개발/QA 패키지

#### 대상
- 저장소 유지보수자
- QA 담당자
- 자동화 테스트를 실행해야 하는 내부 개발자

#### 전달 문서
```text
docs/internal-bridge-developer-guide.md
docs/bridge-validation-report.md
docs/mediation.md
docs/release-notes-draft.md
```

#### 포함해도 되는 파일
```text
maestro/*
examples/*
샘플 앱 전체 소스
```
내부용에는 테스트용 receiver, 데모 화면, 샘플 state/view model, validation log까지 포함해도 됩니다.

---

### 5. 외부 전달 시 제외 권장

일반 매체사에게 최소 연동만 안내할 때는 아래 항목을 기본 전달 범위에서 제외하는 것이 좋습니다.

* **Android**: `AdDemoScreen.kt`, `FormatDetailScreen.kt`, `Sample*.kt`, `IntentBridgeReceiver.kt`, `MainActivity.kt`
* **iOS**: `AdDemoScreen.swift`, `Sample*.swift`, `ContentView.swift`, `SdkHooks.swift`
* **문서**: `docs/internal-bridge-developer-guide.md`, `docs/bridge-validation-report.md`, `docs/ios-vendor-lfs-migration-plan.md`, `docs/release-notes-draft.md`
*(단, 기술 검토나 QA 목적이면 내부 문서까지 함께 전달할 수 있습니다).*

---

### 6. 추천 운영 방식

현재 저장소는 **실행 가능한 통합 샘플**로 유지하고, 문서는 아래처럼 역할을 나누는 것이 좋습니다.

```text
docs/publisher-minimal-integration.md  # 외부 매체사 최소 연동
docs/public-hybrid-bridge-guide.md     # 외부 하이브리드 브릿지 규격
docs/delivery-packages.md              # 전달 범위 선택 기준
docs/internal-bridge-developer-guide.md # 내부 구현/QA 상세
docs/bridge-validation-report.md       # 검증 근거
```
이렇게 나누면 외부 매체사에게는 작은 문서부터 보여주고, 필요할 때만 샘플앱 전체나 내부 문서를 추가로 전달할 수 있습니다.
