# NapSsp App Bridge Sample

NapSsp(Nasmedia AdMixer SSP) SDK를 WebView 하이브리드 방식으로 연동하는 Android 샘플 앱입니다.

## 구조

```
android/     - Android 샘플 앱 (Kotlin + Compose)
docs/        - 가이드 문서
examples/    - 포맷별 코드 예시
```

## 빠른 시작

### Android

```bash
cd android
# Gradle 빌드 (JDK 17 이상 필요)
./gradlew assembleDebug
```

### 문서

| 파일 | 내용 |
|---|---|
| `docs/guide.md` | **메인 가이드** - 초보자용 단계별 설명, 실제 API 예시 |
| `docs/hybrid-webview.md` | WebView ↔ Native 브릿지 상세 |
| `docs/nap-ssp-android-sdk-native.md` | Android SDK 공식 연동 가이드 (Java 기준) |
| `docs/mediation.md` | 미디에이션 네트워크 설정 |

## 핵심 포인트

### AdMixer SSP SDK 실제 API

```kotlin
// AdListener 콜백 (이 3개뿐)
interface AdListener {
    fun onReceivedAd(adapterName: String?, adView: Any?)
    fun onFailedToReceiveAd(adView: Any?, adapterName: String?, errorCode: Int, errorMsg: String?)
    fun onEventAd(adView: Any?, event: AdEvent?)
}

// 클래스별 사용 방법
AdView(context).apply {
    setAdInfo(adInfo)
    setAdViewListener(listener)  // setAdViewListener
    loadAd()
}

NativeAdView(context).apply {
    setAdInfo(adInfo)
    setAdViewListener(listener)
    loadNativeAd()               // loadNativeAd
}

VideoAdView(context).apply {
    setAdInfo(adInfo)
    setAdViewListener(listener)
    loadAd()
}

RewardInterstitialVideoAd(context).apply {
    setAdInfo(adInfo)
    setListener(listener)        // setListener (전면형은 setListener)
    loadRewardVideoAd()
    // onReceivedAd에서 showRewardVideoAd() 호출
}

InterstitialVideoAd(context).apply {
    setAdInfo(adInfo)
    setListener(listener)        // setListener
    loadInterstitialVideoAd()
    // onReceivedAd에서 showInterstitialVideoAd() 호출
}
```

### SDK 초기화

```kotlin
AdMixer.getInstance().initialize(
    context,
    "MEDIA_KEY",
    ArrayList(listOf("AD_UNIT_1", "AD_UNIT_2"))  // ArrayList 필수
)
```

## 설정 파일

`android/app/src/main/java/.../NapSspConfig.kt` 에서 Media Key와 AdUnit ID를 교체하세요:

```kotlin
object NapSspConfig {
    const val MEDIA_KEY = "여기에_미디어키_입력"
    val AD_UNIT_IDS = mapOf(
        "banner_320x100"       to "여기에_배너_ID",
        "native"               to "여기에_네이티브_ID",
        "outstream_video"      to "여기에_동영상_ID",
        "reward_video"         to "여기에_리워드_ID",
        "interstitial_320x480" to "여기에_전면_ID",
    )
}
```

## 문의

- 이메일: nap_adx@nasmedia.co.kr
- 공식 가이드: `docs/nap-ssp-android-sdk-native.md`
