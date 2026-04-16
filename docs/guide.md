# NapSsp App Bridge 샘플 - 개발 가이드

> 초보자도 처음부터 따라할 수 있도록 단계별로 정리한 문서입니다.

---

## 1. 이 앱이 하는 일

**App Bridge** 방식이란:
웹(JavaScript)에서 버튼을 눌렀을 때, 웹 자체가 광고를 띄우지 않고 **네이티브(Android/iOS)의 SDK를 호출해서 광고를 보여주는 방식**입니다.

```
[웹 버튼 클릭]
     ↓  window.NapSspBridge.postMessage('loadBanner')
[Android Native]
     ↓  AdView.loadAd()
[광고 SDK → 광고 표시]
     ↓  window.__napSspAck('SDK Event: loaded | ...')
[웹 상태 업데이트]
```

### 왜 이 방식을 쓰나요?
- 네이티브 SDK가 웹 광고보다 성능/수익이 좋습니다.
- 전면 광고, 리워드 동영상 등 웹에서 구현 불가한 포맷을 사용할 수 있습니다.
- 미디에이션(AdManager, AdFit, Pangle 등)으로 수익을 극대화할 수 있습니다.

---

## 2. 프로젝트 구조

```
android/
  app/src/main/java/com/gwangy/nassspandroidsample/
    NapSspConfig.kt          ← Media Key, AdUnit ID 설정
    NapSspApplication.kt     ← 앱 시작 시 SDK 자동 초기화
    NapSspInitializer.kt     ← SDK initialize() 호출 래퍼
    NapSspSdkIntegration.kt  ← [핵심] 광고 포맷별 SDK 호출
    HybridWebViewScreen.kt   ← WebView + JavaScript Bridge
    HybridEventBridge.kt     ← 브릿지 이벤트 로거
    MainActivity.kt          ← UI 진입점
```

---

## 3. 빠른 시작 (Quick Start)

### Step 1. 의존성 추가

`android/app/build.gradle.kts`:

```kotlin
dependencies {
    // 필수
    implementation("io.github.nasmedia-tech:admixer-ssp:1.0.21")
    implementation("com.google.android.gms:play-services-ads-identifier:18.3.0")

    // 선택 - 미디에이션 네트워크
    // implementation("io.github.nasmedia-tech:admixer-admanager:1.0.14")
    // implementation("io.github.nasmedia-tech:admixer-adfit:1.0.10")
    // implementation("io.github.nasmedia-tech:admixer-pangle:1.0.10")
    // implementation("io.github.nasmedia-tech:admixer-applovin:1.0.8")
    // implementation("io.github.nasmedia-tech:admixer-unity:1.0.6")
}
```

`android/settings.gradle.kts` - Adfit / Pangle 사용 시 추가:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://devrepo.kakao.com/nexus/content/groups/public/")  // Adfit
        maven(url = "https://artifact.bytedance.com/repository/pangle/")        // Pangle
    }
}
```

### Step 2. AndroidManifest.xml 설정

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- 필수 권한 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="com.google.android.gms.permission.AD_ID" />

    <application ...>
        <!-- NapSsp SDK 필수 액티비티 (AdBrowserActivity만 명시, 나머지는 SDK manifest에 포함됨) -->
        <activity
            android:name="com.nasmedia.admixerssp.ads.AdBrowserActivity"
            android:configChanges="orientation|screenSize|keyboardHidden"
            android:exported="false" />

        <!-- Google 광고 사용 시 필수 (실제 App ID로 교체) -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-xxxxxx~xxxxxx" />
    </application>
</manifest>
```

> **주의**: `InterstitialVideoAdActivity`는 SDK의 자체 manifest에 이미 선언되어 있어 앱 manifest에 중복 선언하면 빌드 오류가 납니다.

### Step 3. SDK 초기화 (Application.onCreate)

```kotlin
class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 1회 호출, 앱 시작 시 자동 실행
        AdMixer.getInstance().initialize(
            this,
            "YOUR_MEDIA_KEY",
            ArrayList(listOf("AD_UNIT_1", "AD_UNIT_2"))
        )
    }
}
```

`AndroidManifest.xml`에 `android:name=".YourApplication"` 등록 필수.

### Step 4. 광고 ID 설정

```kotlin
// NapSspConfig.kt
object NapSspConfig {
    const val MEDIA_KEY = "파트너 사이트에서 발급받은 Media Key"
    val AD_UNIT_IDS = mapOf(
        "banner_320x100"      to "배너 AdUnit ID",
        "native"              to "네이티브 AdUnit ID",
        "outstream_video"     to "동영상 AdUnit ID",
        "reward_video"        to "리워드 AdUnit ID",
        "interstitial_320x480" to "전면 AdUnit ID",
    )
}
```

---

## 4. 실제 SDK API (Kotlin 코드 예시)

> **중요**: AdMixer SSP SDK의 `AdListener` 메서드는 아래 3개뿐입니다.
> 기존 코드에 `onAdLoaded()`, `onAdClicked()` 같은 메서드는 존재하지 않습니다.

### AdListener 인터페이스 (실제 API)

```kotlin
interface AdListener {
    // 광고 수신 성공
    fun onReceivedAd(adapterName: String?, adView: Any?)

    // 광고 수신 실패
    fun onFailedToReceiveAd(adView: Any?, adapterName: String?, errorCode: Int, errorMsg: String?)

    // 광고 이벤트 (클릭, 노출, 완료, 리워드 등)
    fun onEventAd(adView: Any?, event: AdEvent?)
}
```

### AdEvent enum 값

| 값 | 의미 |
|---|---|
| `AdEvent.DISPLAYED` | 광고 화면에 표시됨 |
| `AdEvent.CLICK` | 광고 클릭됨 |
| `AdEvent.COMPLETION` | 동영상 재생 완료 |
| `AdEvent.EARNEDREWARD` | 리워드 획득 (리워드 광고) |
| `AdEvent.CLOSE` | 광고 닫힘 (전면/리워드) |
| `AdEvent.SKIPPED` | 광고 건너뜀 |

---

### 배너 광고 (AdView)

```kotlin
val adInfo = AdInfo.Builder("AD_UNIT_ID")
    .setIsUseMediation(true)
    .build()

val adView = AdView(context)
adView.setAdInfo(adInfo)                    // AdInfo 설정
adView.setAdViewListener(object : AdListener {  // setAdViewListener 사용!
    override fun onReceivedAd(adapterName: String?, view: Any?) {
        // 광고 수신 성공 → 레이아웃에 adView 추가
        container.addView(adView)
    }
    override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {
        Log.e("Ad", "[$errorCode] $errorMsg")
    }
    override fun onEventAd(view: Any?, event: AdEvent?) {
        when (event) {
            AdEvent.DISPLAYED -> { /* 노출 */ }
            AdEvent.CLICK -> { /* 클릭 */ }
            else -> {}
        }
    }
})
adView.loadAd()                             // 광고 요청
```

### 네이티브 광고 (NativeAdView)

```kotlin
val nativeView = NativeAdView(context)
nativeView.setAdInfo(AdInfo.Builder("AD_UNIT_ID").setIsUseMediation(true).build())
nativeView.setAdViewListener(object : AdListener {  // setAdViewListener 사용!
    override fun onReceivedAd(adapterName: String?, view: Any?) {
        container.addView(nativeView)
    }
    override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {}
    override fun onEventAd(view: Any?, event: AdEvent?) {}
})
nativeView.loadNativeAd()                   // loadNativeAd() 사용!
```

### 아웃스트림 동영상 광고 (VideoAdView)

```kotlin
val videoView = VideoAdView(context)
videoView.setAdInfo(AdInfo.Builder("AD_UNIT_ID").setIsUseMediation(true).build())
videoView.setAdViewListener(object : AdListener {
    override fun onReceivedAd(adapterName: String?, view: Any?) {
        container.addView(videoView)
    }
    override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {}
    override fun onEventAd(view: Any?, event: AdEvent?) {
        if (event == AdEvent.COMPLETION) { /* 재생 완료 */ }
    }
})
videoView.loadAd()
```

### 리워드 동영상 광고 (RewardInterstitialVideoAd)

```kotlin
// 전면형 광고 - 별도 View 없음, 로드 후 자동 표시
val rewardAd = RewardInterstitialVideoAd(context)
rewardAd.setAdInfo(AdInfo.Builder("AD_UNIT_ID").setIsUseMediation(true).build())
rewardAd.setListener(object : AdListener {      // setListener 사용! (setAdViewListener 아님)
    override fun onReceivedAd(adapterName: String?, view: Any?) {
        rewardAd.showRewardVideoAd()            // 로드 완료 후 표시
    }
    override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {}
    override fun onEventAd(view: Any?, event: AdEvent?) {
        when (event) {
            AdEvent.EARNEDREWARD -> { /* 리워드 지급 처리 */ }
            AdEvent.CLOSE -> { /* 광고 닫힘 */ }
            else -> {}
        }
    }
})
rewardAd.loadRewardVideoAd()
```

### 전면 동영상 광고 (InterstitialVideoAd)

```kotlin
// 전면형 광고 - 별도 View 없음, 로드 후 자동 표시
val interstitialAd = InterstitialVideoAd(context)
interstitialAd.setAdInfo(AdInfo.Builder("AD_UNIT_ID").setIsUseMediation(true).build())
interstitialAd.setListener(object : AdListener {    // setListener 사용!
    override fun onReceivedAd(adapterName: String?, view: Any?) {
        interstitialAd.showInterstitialVideoAd()    // 로드 완료 후 표시
    }
    override fun onFailedToReceiveAd(view: Any?, adapterName: String?, errorCode: Int, errorMsg: String?) {}
    override fun onEventAd(view: Any?, event: AdEvent?) {
        when (event) {
            AdEvent.CLOSE -> { /* 광고 닫힘 */ }
            AdEvent.COMPLETION -> { /* 재생 완료 */ }
            else -> {}
        }
    }
})
interstitialAd.loadInterstitialVideoAd()
```

---

## 5. 하이브리드 WebView 브릿지

### Android - JavaScript Interface 등록

```kotlin
val bridge = NapSspHybridBridge(webView)
webView.addJavascriptInterface(bridge, "NapSspBridge")
```

### 웹(HTML/JS) 코드

```html
<script>
// 네이티브 명령 전달 (Android / iOS 통합)
function callNative(message) {
    if (window.NapSspBridge) {
        window.NapSspBridge.postMessage(message);       // Android
    } else if (window.webkit?.messageHandlers?.NapSspBridge) {
        window.webkit.messageHandlers.NapSspBridge.postMessage(message); // iOS
    }
}

// 네이티브로부터 이벤트 수신
window.__napSspAck = function(response) {
    document.getElementById('status').innerText = response;
};
</script>

<button onclick="callNative('init')">SDK 초기화</button>
<button onclick="callNative('loadBanner')">배너 광고</button>
<button onclick="callNative('loadNative')">네이티브 광고</button>
<button onclick="callNative('loadVideo')">동영상 광고</button>
<button onclick="callNative('loadRewardVideo')">리워드 광고</button>
<button onclick="callNative('loadInterstitialVideo')">전면 광고</button>
<div id="status">준비됨</div>
```

### 지원 명령어 (Web → Native)

| JS 명령어 | 동작 |
|---|---|
| `init` | SDK 초기화 |
| `loadBanner` | 배너 광고 로드 + 웹뷰 아래에 View 삽입 |
| `loadNative` | 네이티브 광고 로드 + 웹뷰 아래에 View 삽입 |
| `loadVideo` | 아웃스트림 동영상 로드 |
| `loadRewardVideo` | 리워드 동영상 로드 + 자동 표시 |
| `loadInterstitialVideo` | 전면 동영상 로드 + 자동 표시 |
| `getStatus` | 브릿지 상태 확인 |

### 응답 형식 (Native → Web)

```
SDK Event: loaded | Format: banner | Detail: 104704
SDK Event: clicked | Format: banner | Detail: 104704
SDK Event: rewarded | Format: rewardVideo | Detail: 103722
SDK Event: failed | Format: banner | Detail: [-1] No fill
```

---

## 6. 테스트 방법

1. Android Studio에서 앱 실행
2. 포맷 목록에서 **웹뷰 하이브리드** 선택
3. 웹 화면의 `init` 버튼 클릭 → SDK 초기화
4. `loadBanner` 버튼 클릭 → 웹뷰 아래에 배너 View 삽입 여부 확인
5. `status:` 영역에 `SDK Event: loaded | Format: banner` 출력 확인

---

## 7. 자주 발생하는 오류와 해결법

| 오류 | 원인 | 해결 |
|---|---|---|
| `sdk init failed` | Media Key 오류 또는 네트워크 없음 | `NapSspConfig.MEDIA_KEY` 확인 |
| `[-1] No fill` | 테스트 AdUnit ID에 광고 없음 | 실제 발급받은 ID로 교체 |
| 광고 View가 안 보임 | 레이아웃에 View 미추가 | `container.addView(adView)` 호출 확인 |
| Manifest merger 오류 | SDK manifest와 앱 manifest 중복 선언 | `InterstitialVideoAdActivity` 중복 제거 |
| `onAdLoaded` 컴파일 오류 | SDK에 존재하지 않는 메서드 | `onReceivedAd` / `onEventAd` 로 교체 |
| `setAdListener` 컴파일 오류 | `AdView`는 `setAdViewListener` 사용 | `setAdViewListener()` 로 교체 |

---

## 8. API 빠른 참조표

| 클래스 | 리스너 등록 메서드 | 광고 요청 메서드 |
|---|---|---|
| `AdView` (배너) | `setAdViewListener(AdListener)` | `loadAd()` |
| `NativeAdView` (네이티브) | `setAdViewListener(AdListener)` | `loadNativeAd()` |
| `VideoAdView` (동영상) | `setAdViewListener(AdListener)` | `loadAd()` |
| `RewardInterstitialVideoAd` (리워드) | `setListener(AdListener)` | `loadRewardVideoAd()` + `showRewardVideoAd()` |
| `InterstitialVideoAd` (전면) | `setListener(AdListener)` | `loadInterstitialVideoAd()` + `showInterstitialVideoAd()` |

---

*문의: nap_adx@nasmedia.co.kr*
