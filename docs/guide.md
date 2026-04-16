# 🚀 Nap SSP 하이브리드 앱 브릿지 마스터 가이드

이 가이드는 초보 개발자도 **Nap SSP(Nasmedia Ad Platform) SDK**를 하이브리드 앱(WebView)에 완벽하게 통합할 수 있도록 안내합니다.

---

## 1. 하이브리드 앱 브릿지란? 🤔
웹(JavaScript)에서 버튼을 눌렀을 때, 웹뷰가 직접 광고를 띄우는 것이 아니라 **네이티브(Android/iOS)의 강력한 광고 SDK 기능을 호출**하여 광고를 보여주는 방식입니다.

### 왜 이 방식을 쓰나요?
- **성능**: 네이티브 광고 SDK가 웹보다 훨씬 빠르고 안정적입니다.
- **다양성**: 전면 광고, 리워드 비디오 등 웹에서 구현하기 힘든 포맷을 쉽게 사용합니다.
- **수익**: 네이티브 미디에이션 기능을 통해 광고 수익을 극대화할 수 있습니다.

---

## 2. 시작하기 전 준비물 (SDK 설치)

이 샘플 프로젝트는 **리플렉션 없이 직접 SDK를 호출하는 글로벌 표준 방식**을 사용합니다.

### Android (Gradle)
`app/build.gradle.kts` 파일에 아래 내용을 추가하세요.
```kotlin
dependencies {
    implementation("io.github.nasmedia-tech:admixer-ssp:1.0.21")
    implementation("com.google.android.gms:play-services-ads-identifier:18.3.0")
}
```

### iOS (SPM)
Xcode에서 `File > Add Packages...`를 선택하고 아래 URL을 입력하세요.
- `https://github.com/Nasmedia-Tech/iOS-SSP-Mediation-SPM.git`

---

## 3. 핵심 동작 원리 (Flow)

웹과 네이티브는 아래와 같은 3단계로 대화합니다.

1.  **발신 (Web → Native)**: 웹에서 `postMessage`로 명령을 내립니다.
2.  **실행 (Native)**: 네이티브가 명령을 받아 Nap SSP SDK 광고를 로드합니다.
3.  **응답 (Native → Web)**: 광고 결과(성공, 클릭, 실패 등)를 웹의 `__napSspAck` 함수로 돌려줍니다.

---

## 4. 한 눈에 보는 포맷별 짧은 코드 (Short Code)

공식 가이드의 핵심 패턴을 압축한 코드입니다. 복사하여 `NapSspSdkIntegration` 등에 바로 활용하세요.

### [Android - Kotlin]
```kotlin
// 1. 배너 (Banner)
val adView = AdView(context).apply {
    setAdInfo(AdInfo.Builder("AD_UNIT_ID").setIsUseMediation(true).build())
    setAdListener(object : AdListener { /* 이벤트 처리 */ })
    loadAd()
}

// 2. 전면 동영상 (Interstitial)
val interstitial = InterstitialVideoAd(context).apply {
    setAdInfo(AdInfo.Builder("AD_UNIT_ID").setIsUseMediation(true).build())
    setAdListener(object : InterstitialVideoAdListener { /* 이벤트 처리 */ })
    loadInterstitialVideoAd()
}

// 3. 리워드 동영상 (Reward)
val rewardAd = RewardInterstitialVideoAd(context).apply {
    setAdInfo(AdInfo.Builder("AD_UNIT_ID").setIsUseMediation(true).build())
    setAdListener(object : RewardVideoAdListener { /* 보상 및 이벤트 처리 */ })
    loadRewardVideoAd()
}
```

### [iOS - Swift]
```swift
// 1. 배너 (Banner)
let bannerView = AMMBannerView(adUnitId: "AD_UNIT_ID")
bannerView.delegate = self
bannerView.isUseMediation = true
bannerView.loadAd()

// 2. 전면 동영상 (Interstitial)
let interstitial = AMMVideoInterstitial(adUnitId: "AD_UNIT_ID")
interstitial.delegate = self
interstitial.isUseMediation = true
interstitial.loadAd()

// 3. 리워드 동영상 (Reward)
let rewardVideo = AMMRewardVideo(adUnitId: "AD_UNIT_ID")
rewardVideo.delegate = self
rewardVideo.isUseMediation = true
rewardVideo.loadAd()
```

---

## 5. 네이티브 광고 호출 코드 (표준 방식)

### Android
`AdListener`를 달아주어야 광고가 실제 떴는지, 클릭되었는지 알 수 있습니다.
```kotlin
val adView = AdView(context)
adView.setAdListener(object : AdListener {
    override fun onAdLoaded() { /* 웹으로 성공 알림 */ }
    override fun onAdClicked() { /* 웹으로 클릭 알림 */ }
    override fun onAdFailedToLoad(error: AdError) { /* 웹으로 실패 원인 전송 */ }
})
adView.loadAd()
```

### iOS
`Delegate` 패턴을 사용하여 이벤트를 수신합니다.
```swift
let bannerView = AMMBannerView(adUnitId: "YOUR_ID")
bannerView.delegate = self
bannerView.loadAd()

// Delegate 구현
func bannerViewDidClick(_ bannerView: AMMBannerView) {
    // 웹뷰 브릿지로 클릭 이벤트 전달
}
```

---

## 5. 하이브리드 연동 테스트 방법 🧪

1.  **샘플 앱 실행**: 안드로이드 스튜디오나 Xcode에서 앱을 실행합니다.
2.  **메뉴 선택**: 상단 탭에서 `HybridWebView`를 선택합니다.
3.  **초기화**: 웹 화면의 `init` 버튼을 누릅니다. (네이티브 SDK가 초기화됩니다.)
4.  **광고 호출**: `loadBanner` 또는 `loadVideo` 버튼을 누릅니다.
5.  **상태 확인**: 하단 `status:` 영역에 **"SDK Event: loaded"** 또는 **"SDK Event: clicked"** 메시지가 실시간으로 뜨는지 확인합니다.

---

## 6. 자주 묻는 질문 (FAQ) ❓

**Q: 광고 버튼을 눌렀는데 status가 'failed'로 떠요.**
A: `NapSspConfig` 파일에 설정된 `MEDIA_KEY`와 `AD_UNIT_ID`가 본인의 발급 정보와 일치하는지 확인하세요.

**Q: 클릭 이벤트가 웹으로 안 와요.**
A: 네이티브 코드에서 광고 뷰에 리스너(AdListener/Delegate)가 제대로 설정되었는지 확인하세요. 이 샘플의 `NapSspSdkIntegration.kt` 코드를 참고하면 됩니다.

---
*도움이 필요하신가요? Nasmedia 기술지원 팀에 문의하거나 `docs/troubleshooting.md`를 확인하세요.*
