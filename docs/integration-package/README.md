# NapSSP App-WebView 브릿지 연동 패키지

이 폴더에 있는 파일을 프로젝트에 복사하면 연동이 완성됩니다.

---

## 폴더 구조

```
integration-package/
├── android/
│   ├── NapSspConfig.kt            ← 키/설정값 관리
│   ├── NapSspSdkIntegration.kt    ← SDK 연동 엔진 (6개 포맷)
│   └── HybridWebViewScreen.kt     ← WebView + 광고 화면 (Jetpack Compose)
├── ios/
│   ├── NapSspConfig.swift         ← 키/설정값 관리
│   ├── NapSspSdkIntegration.swift ← SDK 연동 엔진 (6개 포맷 + Delegate)
│   ├── HybridWebViewScreen.swift  ← WebView + 광고 화면 (SwiftUI)
│   └── Bridge/
│       ├── NapSspAdEventBridge.swift  ← 앱 내부 이벤트 알림 (NotificationCenter)
│       ├── InterstitialModule.swift   ← 전면 광고 로드/표시
│       └── RewardedModule.swift      ← 보상형 광고 로드/표시
└── web/
    ├── napssp-bridge.js           ← JS 브릿지 유틸 (기존 웹 페이지에 삽입)
    └── sample.html                ← 동작하는 완성 샘플 HTML
```

---

## 빠른 시작

### 1단계 — 파일 복사

**Android**: `android/` 안의 3개 파일을 프로젝트 패키지에 복사합니다.  
**iOS**: `ios/` 안의 파일 전체를 Xcode 타겟에 추가합니다.  
**Web**: `web/napssp-bridge.js`를 기존 웹 페이지에 삽입하거나, `sample.html`을 참고합니다.

### 2단계 — 패키지명 / 키 수정

| 파일 | 수정할 부분 |
|---|---|
| `android/NapSspConfig.kt` | `package com.yourapp` → 실제 패키지명, `MEDIA_KEY` / `AD_UNIT_IDS` → 실제 키 |
| `android/NapSspSdkIntegration.kt` | `package com.yourapp` → 실제 패키지명 |
| `android/HybridWebViewScreen.kt` | `package com.yourapp` → 실제 패키지명, `loadUrl(...)` → 실제 WebView URL |
| `ios/NapSspConfig.swift` | `mediaKey` / `adUnitIDs` → 실제 키 |

### 3단계 — SDK 의존성 추가

**Android** (`build.gradle.kts`):

```kotlin
// settings.gradle.kts에 저장소 추가
maven(url = "https://devrepo.kakao.com/nexus/content/groups/public/")
maven(url = "https://artifact.bytedance.com/repository/pangle/")

// 의존성
implementation("io.github.nasmedia-tech:admixer-ssp:1.0.23")
implementation("com.google.android.gms:play-services-ads-identifier:18.3.0")
// 미디에이션 (사용하는 것만 선택)
implementation("io.github.nasmedia-tech:admixer-admanager:1.0.15_delta")
implementation("io.github.nasmedia-tech:admixer-adfit:1.0.11")
implementation("io.github.nasmedia-tech:admixer-pangle:1.0.11")
implementation("com.pangle.global:pag-sdk:8.0.0.4")
implementation("io.github.nasmedia-tech:admixer-applovin:1.0.8")
implementation("io.github.nasmedia-tech:admixer-unity:1.0.6")
```

**iOS** (SPM / Xcode):  
Nasmedia에서 안내받은 SPM URL로 `AdMixer`, `AdMixerMediation` 패키지를 추가합니다.

### 4단계 — AndroidManifest.xml 권한 추가

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="com.google.android.gms.permission.AD_ID" />
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="YOUR_GOOGLE_MOBILE_ADS_APP_ID" />
```

### 5단계 — WebView 화면 표시

**Android** — Composable 호출:
```kotlin
HybridWebViewScreen()
```

**iOS** — SwiftUI View 표시:
```swift
HybridWebViewScreen()
```

---

## 자세한 연동 가이드

`docs/gitbook-hybrid-bridge-guide.md`를 참고하세요.

## 문의

nap_adx@nasmedia.co.kr
