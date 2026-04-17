# nap ssp Android SDK - Native 연동 가이드

> **출처**: [nasmob.atlassian.net](https://nasmob.atlassian.net/wiki/spaces/ASIG/pages/1888747571/Android+SDK+-+Native)  
> **문의**: nap_adx@nasmedia.co.kr

---

## 목차

1. [SDK 시작하기](#1-sdk-시작하기)
2. [배너 광고](#2-배너-광고)
3. [네이티브 광고](#3-네이티브-광고)
4. [리워드 동영상 광고](#4-리워드-동영상-광고)
5. [동영상 광고](#5-동영상-광고)
6. [비즈보드](#6-비즈보드)

---

## 1. SDK 시작하기

### 버전 기록

| Date | Version | Notes |
|------|---------|-------|
| 2026-02-20 | 1.0.20 | 미디에이션 기능 업데이트 / 네트워크 버전 업데이트 (Adfit, Pangle, Unity ads) / 리워드 콜백 추가 |
| 2026-01-21 | 1.0.19 | 네트워크 SDK에서 리워드 획득 커스텀 파람 추가 / 리워드 획득 내부 로깅 URL 추가 |
| 2026-01-07 | 1.0.18 | 네트워크 버전 업데이트 / 리워드 이벤트 추가 (`EARNEDREWARD`) |
| 2025-10-30 | 1.0.16 | 소재 사이즈 수정 기능 추가 |
| 2025-10-16 | 1.0.15 | Unity Ads 추가 |
| 2025-10-01 | 1.0.14 | 미디에이션 처리 로직 수정 |
| 2025-08-28 | 1.0.13 | 전면배너 옵션 추가 |
| 2025-08-18 | 1.0.12 | Applovin 추가 |
| 2025-08-14 | 1.0.12 | Mobwith 버전 업데이트 (1.0.2 → 1.0.3) |
| 2025-08-11 | 1.0.11 | 전면배너 옵션 추가, 버그 픽스 |
| 2025-07-24 | 1.0.10 | Mobwith, Pangle 추가, 버그 픽스 |
| 2025-05-22 | 1.0.9 | 버그 픽스 |
| 2025-04-14 | 1.0.8 | Kakao Adfit 추가, 버그 픽스 |
| 2025-03-10 | 1.0.7 | 버그 픽스 |
| 2025-02-20 | 1.0.6 | 버그 픽스 |
| 2024-12-24 | 1.0.4 | 버그 픽스 |
| 2024-11-21 | 1.0.3 | 백그라운드 기능 추가 |
| 2024-11-13 | 1.0.2 | 버그 픽스 |
| 2024-10-14 | 1.0.1 | 버그 픽스 |
| 2024-10-07 | 1.0.0 | 릴리즈 |

---

### 지원 OS 버전 및 개발 환경

- 최신 버전의 nap ssp SDK 사용을 권장합니다.
- 최신 버전의 Android Studio 사용을 권장합니다.
- **최소 지원 OS**: Android 5.0 (API 레벨 21, LOLLIPOP) 이상

---

### 개요

이 문서는 nap ssp SDK를 Android 앱에 연동하기 위한 가이드입니다.

- **지원 언어**: Android Java, Android Kotlin
- **연동 방식**: SDK 연동 방식 / API 연동 방식

**연동 작업 전 사전 정보 안내**

- nap ssp 파트너 사이트에 가입 후 미디어 등록 및 애드유닛 생성을 완료해야 **media key**와 **adunit id**를 확인할 수 있습니다.
- media key와 adunit id가 파트너 사이트에 등록된 내용과 상이할 경우 광고가 원활히 노출되지 않을 수 있습니다.

**별도 key 값이 필요한 네트워크**

- Google App ID
- Pangle App ID

→ 발급 문의: nap_adx@nasmedia.co.kr

---

### 샘플 프로젝트

연동 시작 시, 샘플 프로젝트 전체를 적용하여 광고 응답 확인하는 방식을 권장합니다.

- [Android Java SDK Sample](https://github.com/Nasmedia-Tech/AOS-AdMixerSSP-TestApp/tree/main/AdMixerSDKSample)
- [Android Kotlin SDK Sample](https://github.com/Nasmedia-Tech/AOS-AdMixerSSP-TestApp/tree/main/AdMixerSDKKotlinSample)

---

### Step 1. Gradle 설정

#### 프로젝트 최상위 `build.gradle`

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

#### app level `build.gradle`

**라이브러리 버전은 항상 최신 버전으로 유지해야 합니다.**

```gradle
dependencies {
    // (필수)
    implementation 'io.github.nasmedia-tech:admixer-ssp:1.0.21'
    // (필수)
    implementation 'com.google.android.gms:play-services-ads-identifier:18.9.0'
    // (선택) Google AdManager
    implementation 'io.github.nasmedia-tech:admixer-admanager:1.0.14'  // play-services-ads:24.8.0 포함
    // (선택) Kakao Adfit
    implementation 'io.github.nasmedia-tech:admixer-adfit:1.0.10'      // ads-base:3.21.17 포함
    // (선택) Pangle
    implementation 'io.github.nasmedia-tech:admixer-pangle:1.0.10'     // pag-sdk:7.7.0.2 포함
    // (팽글 사용 시 필수)
    implementation "com.pangle.global:pag-sdk:7.1.0.4"
    // (선택) Applovin
    implementation 'io.github.nasmedia-tech:admixer-applovin:1.0.8'    // applovin-sdk:13.5.0 포함
    // (선택) Unity Ads
    implementation 'io.github.nasmedia-tech:admixer-unity:1.0.6'       // unity-ads:4.15.0 포함
}
```

#### Adfit 또는 Pangle 사용 시 추가 설정

`settings.gradle`의 `dependencyResolutionManagement` 섹션에 추가:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        // ...
        maven { url 'https://devrepo.kakao.com/nexus/content/groups/public/' }  // Adfit 필수
        maven { url "https://artifact.bytedance.com/repository/pangle/" }        // Pangle 필수
    }
}
```

---

### Google 네트워크 - SDK 입찰 광고 소스 설정

Google 네트워크 사용 시, 아래 광고 소스 라이브러리를 모두 추가해야 합니다.

- 참고 가이드: [Google developers 문서](https://developers.google.com/ad-manager/mobile-ads-sdk/android/choose-networks?hl=ko)

**추가해야 할 광고 소스 (모두 추가):**

- Pangle
- AppLovin
- DT Exchange
- InMobi
- Liftoff Monetize
- Meta
- Moloco
- Unity Ads
- Mintegral

> **주의**: 프로젝트 수준 `build.gradle`과 앱 수준 `build.gradle` 양쪽에 모두 추가해야 합니다.

---

### 네트워크 SDK 중복 예외 처리 가이드

이미 자체/타사 솔루션으로 네트워크 SDK를 운영 중인 경우 중복 예외 처리가 필요합니다.

**중복 사용 가능 네트워크:**

- **Google**: 기존 운영 중인 지면과 다른 지면의 경우 가능
- **Pangle**: 기존 운영 중인 지면과 다른 지면의 경우 가능
- **Adfit**: 앱 심사 진행 후 사용 가능 여부 확인

**Gradle 예외 처리 설정 방법:**

```gradle
dependencies {
    implementation("io.github.nasmedia-tech:admixer-admanager:1.0.14") {
        exclude group: "com.google.android.gms", module: "play-services-ads"
    }
    implementation("io.github.nasmedia-tech:admixer-adfit:1.0.10") {
        exclude group: "com.kakao.adfit", module: "ads-base"
    }
    implementation("io.github.nasmedia-tech:admixer-pangle:1.0.10")
}
```

**적용 후 확인 사항:**

1. Gradle 의존성 트리에서 동일 네트워크 SDK가 1개만 포함되어 있는지 확인
2. 빌드 정상 여부 확인
3. nap ssp 및 기존 지면 모두에서 광고 로드 정상 동작 여부 확인

---

### Google 광고 사용 시 AndroidManifest.xml 설정

nap ssp 운영팀으로부터 발급받은 Google App ID를 아래와 같이 추가합니다.

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="nap ssp 운영팀으로부터 발급받은 Google APP ID"/>
```

---

### Step 2. nap ssp SDK 초기화

광고 호출 전 앱에서 **1회** 호출해야 합니다.

```java
public class YourApplication extends android.app.Application {
    public static String MEDIA_KEY = "AdMixer 플랫폼에서 발급받은 미디어 키";
    public static String ADUNIT_ID_BANNER = "AdMixer 플랫폼에서 발급받은 배너 ADUNIT_ID";
    public static String ADUNIT_ID_INTERSTITIAL_BANNER = "AdMixer 플랫폼에서 발급받은 전면 배너 ADUNIT_ID";
    public static String ADUNIT_ID_NATIVE = "AdMixer 플랫폼에서 발급받은 네이티브 ADUNIT_ID";
    public static ArrayList<String> adUnits = new ArrayList<>(
        Arrays.asList(ADUNIT_ID_BANNER, ADUNIT_ID_INTERSTITIAL_BANNER, ADUNIT_ID_NATIVE)
    );

    @Override
    public void onCreate() {
        super.onCreate();
        // 로그 레벨 설정
        AdMixerLog.setLogLevel(AdMixerLog.LogLevel.VERBOSE);
        // AdMixer 초기화
        AdMixer.getInstance().initialize(this, MEDIA_KEY, adUnits);
        // 미디에이션 초기화 (선택)
        AdMixer.registerAdapter(AdMixer.ADAPTER_ADMANAGER);
        AdMixer.registerAdapter(AdMixer.ADAPTER_ADFIT);
        AdMixer.registerAdapter(AdMixer.ADAPTER_PANGLE);
        AdMixer.registerAdapter(AdMixer.ADAPTER_APPLOVIN);
        AdMixer.registerAdapter(AdMixer.ADAPTER_UNITY);
        // Pangle 초기화 (Pangle 사용 시 필수)
        PAGConfig pAGInitConfig = new PAGConfig.Builder()
            .appId("발급받은 appid")
            .debugLog(true)
            .supportMultiProcess(false)
            .build();
        PAGSdk.init(this, pAGInitConfig, new PAGSdk.PAGInitCallback() {
            @Override
            public void success() {
                Log.i("Pangle", "pangle init success");
            }
            @Override
            public void fail(int code, String msg) {
                Log.i("Pangle", "pangle init fail: " + code);
            }
        });
    }
}
```

> **참고 선택 옵션:**
> - `AdMixer.setBrowser(...)` - 특정 브라우저 지정
> - `AdMixer.setTagForChildDirectedTreatment(...)` - COPPA(아동보호법) 관련 설정

---

### Step 3. Proguard 설정

```proguard
# AdMixer Setting
-keep class com.nasmedia.admixerssp.** { *; }
# Mediation Admanager Setting
-keep class com.nasmedia.admanager.** { *; }
# Mediation Adfit Setting
-keep class com.nasmedia.adfit.** { *; }
# Mediation pangle Setting
-keep class com.nasmedia.pangle.** { *; }
# Mediation applovin Setting
-keep class com.nasmedia.applovin.** { *; }
# Mediation unity Setting
-keep class com.nasmedia.unity.** { *; }
```

---

### 자주 하는 질문 (FAQ)

**Q1. 문제 확인을 위한 로그 설정 방법은?**  
`AdMixerLog.setLogLevel(AdMixerLog.LogLevel.VERBOSE);` 코드를 사용하면 LogCat에서 상세 로그를 확인할 수 있습니다.

**Q2. 하나의 App에 복수 개의 Media Key 적용이 가능한가요?**  
한 개의 App에는 한 개의 Media Key만 적용 가능합니다.

**Q3. 동일한 AdUnit ID로 여러 광고 객체를 생성해도 되나요?**  
한 개의 AdUnit ID는 한 개의 광고 객체에서만 사용할 수 있습니다.

**Q4. 광고가 나오지 않는 경우 대처 방법은?**  
1. Android Studio Logcat 로그 확인
2. 파트너 사이트 광고 키 설정 확인
3. SDK 초기화 호출 여부 확인
4. 올바른 Media Key와 AdUnit ID 사용 여부 확인

**Q5. AdUnit 설정 사이즈와 다른 광고 사이즈가 노출되는 경우?**  
AdUnit 설정 사이즈는 보장되지만, 광고 유형에 따라 다르게 노출될 수 있습니다.

**Q6. 네이티브 광고 레이아웃에서 RelativeLayout을 필수로 사용해야 하나요?**  
RelativeLayout 사용을 권장합니다. 불가한 경우 RelativeLayout을 부모뷰로 선언 후 사용하는 레이아웃을 넣는 방법을 사용할 수 있으나 권장하지는 않습니다.

**Q7. Adfit 상용 광고는 언제 응답되나요?**  
Adfit 라이브 후 매체 심사 과정을 통해 상용 광고가 응답됩니다.  
`[연동 테스트] → [매체 라이브] → [Adfit 매체 심사] → [심사 완료] → [상용 광고 송출]`

---

## 2. 배너 광고

> 배너 광고 추가 전, [Step 1~3 설정](#1-sdk-시작하기)이 완료되었는지 확인하세요.

### 배너 광고 뷰 추가 (코드 방식)

배너 광고 뷰는 두 가지 방법으로 사용할 수 있습니다.

**방법 1: 광고 수신 즉시 노출**  
→ `container.addView(banner);` 사용

**방법 2: 원하는 시점에 노출**  
1. `banner.loadAd();` 실행
2. `onReceivedAd` 이벤트에서 `if (banner.hasAd)` 로 광고 수신 여부 판단
3. `container.removeView(banner);` 기존 뷰 제거
4. `container.addView(banner);` 새 뷰 추가
5. `banner.showAd();` 원하는 시점에 호출

**유의사항:**
- 광고 로딩 성공 후 너무 오랜 시간이 지나면 `showAd()` 호출 시 정상 표시되지 않을 수 있습니다.
- 레이아웃에 배너 광고 뷰를 추가하지 않으면 광고가 표시되지 않습니다.
- `showAd()` 를 호출하지 않으면 광고가 표시되지 않습니다.

**미디에이션 사용 시 유의사항:**
- Google 등 타 네트워크 진행 시 Adapter와 Mediation 코드 삽입이 반드시 필요합니다.
  - 예시: `AdMixer.registerAdapter(AdMixer.ADAPTER_ADMANAGER);` + `setIsUseMediation(true);`
- Adfit 사용 시 `AdView(this)` 는 Activity context 선언이 필수입니다. (`getApplicationContext()` 는 Adfit에서 지원하지 않음)

#### `BannerActivity.java` 예제

```java
public class BannerActivity extends AppCompatActivity {
    private RelativeLayout container;
    private AdView banner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);
        container = findViewById(R.id.container_banner);

        AdMixer.registerAdapter(AdMixer.ADAPTER_ADMANAGER); // 미디에이션 선택
        AdMixer.registerAdapter(AdMixer.ADAPTER_ADFIT);     // 미디에이션 선택
        AdMixer.registerAdapter(AdMixer.ADAPTER_PANGLE);    // 미디에이션 선택
        AdMixer.registerAdapter(AdMixer.ADAPTER_APPLOVIN);  // 미디에이션 선택
        AdMixer.registerAdapter(AdMixer.ADAPTER_UNITY);     // 미디에이션 선택

        AdInfo.Builder builder = new AdInfo.Builder(Application.ADUNIT_ID_BANNER);
        builder.setIsUseMediation(true); // 미디에이션 필수
        AdInfo adInfo = builder.build();

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        banner = new AdView(this);
        banner.setLayoutParams(params);
        banner.setAdInfo(adInfo);
        banner.setAlwaysShowAdView(false); // 광고 로딩 전 영역 차지 여부 (false: 기본값)
        banner.setAdViewListener(new AdListener() {
            @Override
            public void onReceivedAd(String adapterName, Object adView) {
                // 광고 수신 성공
                // [방법 2] 원하는 시점에 노출:
                // if (banner.hasAd) {
                //     container.removeView(banner);
                //     container.addView(banner);
                //     banner.showAd();
                // }
            }
            @Override
            public void onFailedToReceiveAd(Object adView, String adapterName, int errorCode, String errorMsg) {
                // 광고 수신 실패
            }
            @Override
            public void onEventAd(Object o, AdEvent adEvent) {
                switch(adEvent) {
                    case CLICK:     // 클릭 시
                    case DISPLAYED: // 노출 시
                        break;
                }
            }
        });

        // [방법 1] 광고 요청 후 즉시 노출
        container.addView(banner);
        // [방법 2] 미리 로드
        // banner.loadAd();
    }

    @Override
    protected void onResume() {
        if (banner != null) banner.onResume();
        super.onResume();
    }
    @Override
    protected void onPause() {
        if (banner != null) banner.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        if (banner != null) banner.onDestroy();
        super.onDestroy();
    }
}
```

---

### 배너 광고 뷰 추가 (Layout XML 방식)

Layout 파일 이용 방법 사용 시, **광고 요청 후 받은 즉시 노출**합니다.

#### `activity_banner2.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <com.nasmedia.admixer.ads.AdView
        android:id="@+id/banner2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentStart="true"
        android:layout_alignParentEnd="true"
        android:layout_alignParentBottom="true" />
</RelativeLayout>
```

#### `Banner2Activity.java`

```java
public class Banner2Activity extends AppCompatActivity {
    private AdView banner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner2);

        AdInfo.Builder builder = new AdInfo.Builder(Application.ADUNIT_ID_BANNER);
        builder.setIsUseMediation(true);
        AdInfo adInfo = builder.build();

        banner = findViewById(R.id.banner2);
        banner.setAdInfo(adInfo);
        banner.setAlwaysShowAdView(false);
        banner.setAdViewListener(new AdListener() {
            @Override
            public void onReceivedAd(Object o) { /* 광고 수신 성공 */ }
            @Override
            public void onFailedToReceiveAd(Object o, int i, String s) { /* 광고 수신 실패 */ }
            @Override
            public void onEventAd(Object o, AdEvent adEvent) {
                switch(adEvent) {
                    case CLICK:
                    case DISPLAYED:
                        break;
                }
            }
        });
    }

    @Override protected void onResume() { if (banner != null) banner.onResume(); super.onResume(); }
    @Override protected void onPause() { if (banner != null) banner.onPause(); super.onPause(); }
    @Override protected void onDestroy() { if (banner != null) banner.onDestroy(); super.onDestroy(); }
}
```

---

### 전면 광고 (Interstitial Banner)

전면 광고는 두 가지 방법으로 사용할 수 있습니다.

**방법 1: 수신 즉시 노출**  
→ `interstitialAd.startInterstitial();` 사용

**방법 2: 원하는 시점에 노출**  
1. `interstitialAd.loadInterstitial();` 실행
2. `onReceivedAd` 이벤트 수신 후 `if(interstitialAd.hasInterstitial)` 로 수신 여부 판단
3. `interstitialAd.showInterstitial();` 로 노출

#### 전면 광고 형태 (3종)

| 형식 | 설명 |
|------|------|
| **basic** | 우측 상단에 "X" 이미지 형태의 닫기 버튼 노출 |
| **popup** | 광고 소재 하단에 텍스트 형태의 닫기 버튼 노출. 배경색 커스터마이징 가능 |
| **countDown** | 설정된 시간 후 닫기 버튼 노출. `setCountDown(UI타입, 시간)` 으로 설정 (gauge: 0, text: 1 / 시간: 2~5초) |

**팝업형 전면 광고 옵션 설정:**

```java
PopupInterstitialAdOption adConfig = new PopupInterstitialAdOption();
adConfig.setDisableBackKey(false);          // 뒤로가기 버튼 방지 (true: 비활성화)
adConfig.setButtonLeft("광고종료", "#234234"); // 닫기 버튼 (필수)
adConfig.setButtonRight("오른쪽버튼", null);  // 앱 종료 버튼 (선택)
adConfig.setButtonFrameColor(null);          // 버튼 영역 색상
adConfig.setCountDown(0, 5);                 // countDown 타입: 0=gauge, 시간: 5초

AdInfo adInfo = new AdInfo.Builder(Application.ADUNIT_ID_INTERSTITIAL_BANNER)
    .isUseBackgroundAlpha(true)
    .popupAdOption(adConfig)
    .interstitialAdType(AdInfo.InterstitialAdType.Popup) // Basic, Popup, CountDown
    .setIsUseMediation(true)
    .build();
```

#### `InterstitialActivity.java` 예제

```java
public class InterstitialActivity extends AppCompatActivity {
    InterstitialAd interstitialAd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);

        AdMixer.registerAdapter(AdMixer.ADAPTER_ADMANAGER);
        AdMixer.registerAdapter(AdMixer.ADAPTER_ADFIT);
        AdMixer.registerAdapter(AdMixer.ADAPTER_APPLOVIN);
        AdMixer.registerAdapter(AdMixer.ADAPTER_PANGLE);
        AdMixer.registerAdapter(AdMixer.ADAPTER_UNITY);

        PopupInterstitialAdOption adConfig = new PopupInterstitialAdOption();
        adConfig.setDisableBackKey(false);
        adConfig.setButtonLeft("광고종료", "#234234");
        adConfig.setButtonRight("오른쪽버튼", null);
        adConfig.setButtonFrameColor(null);

        AdInfo.Builder builder = new AdInfo.Builder(Application.ADUNIT_ID_INTERSTITIAL_BANNER)
            .isUseBackgroundAlpha(true)
            .popupAdOption(adConfig)
            .interstitialAdType(AdInfo.InterstitialAdType.Popup);
        builder.setIsUseMediation(true);
        AdInfo adInfo = builder.build();

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdInfo(adInfo);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onReceivedAd(String adapterName, Object adView) { /* 수신 성공 */ }
            @Override
            public void onFailedToReceiveAd(Object adView, String adapterName, int errorCode, String errorMsg) { /* 수신 실패 */ }
            @Override
            public void onEventAd(Object o, AdEvent adEvent) {
                switch (adEvent) {
                    case LEFT_CLICK:  // 왼쪽 버튼 클릭
                    case RIGHT_CLICK: // 오른쪽 버튼 클릭 (일반적으로 finish() 호출)
                    case CLOSE:       // 광고 창 닫힘
                    case DISPLAYED:   // 광고 노출
                        break;
                }
            }
        });

        // [방법 2] 원하는 시점에 노출
        Button btnInterstitialShow = findViewById(R.id.btn_interstitial_show);
        btnInterstitialShow.setOnClickListener(v -> {
            if (interstitialAd.hasInterstitial)
                interstitialAd.showInterstitial();
            else {
                interstitialAd.loadInterstitial();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (interstitialAd != null) {
            interstitialAd.stopInterstitial();
            interstitialAd = null;
        }
        super.onDestroy();
    }
}
```

---

## 3. 네이티브 광고

> 네이티브 광고 추가 전, [Step 1~3 설정](#1-sdk-시작하기)이 완료되었는지 확인하세요.

### 구성

네이티브 광고는 6가지 asset으로 구성되어 있으며, 각 asset을 사용하여 자유롭게 UI를 구성할 수 있습니다.

- **제목** (title)
- **아이콘** (icon)
- **광고주** (advertiser)
- **설명** (description)
- **메인** (mainView: Image 또는 Video)
- **버튼** (cta)

### 노출 방법

**방법 1: 수신 즉시 노출**  
→ `nativeAdView.loadNativeAd();` 후 `onReceivedAd` 리스너에 `container.addView(nativeAdView);` 추가

**방법 2: 원하는 시점에 노출**  
1. `nativeAdView.loadNativeAd();` 로드 요청
2. `onReceivedAd` 이벤트 수신 후 `if(nativeAdView.hasAd)` 로 수신 여부 판단
3. `container.addView(nativeAdView);` 로 레이아웃에 추가

**유의사항:**
- Admixer 단독 사용 시, title / icon / mainView 중 1개는 필수로 사용해야 합니다.
- Google 사용 시, 최소한의 필수 view를 설정해야 합니다. (아래 예제 참조)
- Adfit 사용 시 `NativeAdView(this)` 는 Activity context 선언이 필수입니다.

### 레이아웃 XML 예제

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#f7f7f7">

    <!-- 아이콘 -->
    <ImageView
        android:id="@+id/iv_icon"
        android:layout_width="90dp"
        android:layout_height="90dp"
        android:layout_alignParentStart="true"
        android:layout_margin="12dp" />

    <!-- 제목 -->
    <TextView
        android:id="@+id/tv_title"
        android:layout_width="match_parent"
        android:layout_height="68dp"
        android:layout_marginStart="12dp"
        android:layout_marginTop="19dp"
        android:layout_toEndOf="@id/iv_icon"
        android:textSize="15sp"
        android:textStyle="bold" />

    <!-- 광고주 -->
    <TextView
        android:id="@+id/tv_adv"
        android:layout_width="match_parent"
        android:layout_height="45dp"
        android:layout_below="@id/tv_title"
        android:layout_toEndOf="@id/iv_icon"
        android:textColor="#c7c7c7"
        android:textSize="11sp" />

    <!-- 설명 -->
    <TextView
        android:id="@+id/tv_desc"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/iv_icon"
        android:textColor="#2a2a2a"
        android:textSize="11sp" />

    <!-- 메인 이미지/동영상 -->
    <com.nasmedia.admixerssp.common.nativeads.NativeMainAdView
        android:id="@+id/iv_main"
        android:layout_width="match_parent"
        android:layout_height="250dp"
        android:layout_below="@+id/tv_desc">
        <ImageView
            android:id="@+id/imageView_ad"
            android:layout_width="320dp"
            android:layout_height="250dp" />
    </com.nasmedia.admixerssp.common.nativeads.NativeMainAdView>

    <!-- CTA 버튼 -->
    <Button
        android:id="@+id/btn_cta"
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:layout_below="@+id/iv_main"
        android:background="#2a2a2a"
        android:textColor="#ffffff" />
</RelativeLayout>
```

### `NativeActivity.java` 예제

```java
public class NativeActivity extends AppCompatActivity {
    private NativeAdView nativeAdView;
    private RelativeLayout container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native);
        container = findViewById(R.id.container_native);

        AdMixer.registerAdapter(AdMixer.ADAPTER_ADMANAGER);
        AdMixer.registerAdapter(AdMixer.ADAPTER_ADFIT);
        AdMixer.registerAdapter(AdMixer.ADAPTER_PANGLE);

        AdInfo.Builder builder = new AdInfo.Builder(Application.ADUNIT_ID_NATIVE);
        builder.setIsUseMediation(true);

        // View ID 매핑 (Google, Adfit, Pangle 공통)
        Map<String, Integer> adViewIds = new HashMap<>();
        adViewIds.put("nativeLayout", R.layout.item_320x480);
        adViewIds.put("iv_icon", R.id.iv_icon);
        adViewIds.put("tv_title", R.id.tv_title);
        adViewIds.put("tv_adv", R.id.tv_adv);
        adViewIds.put("tv_desc", R.id.tv_desc);
        adViewIds.put("iv_main", R.id.iv_main);
        adViewIds.put("btn_cta", R.id.btn_cta);
        builder.setViewIds(AdMixer.ADAPTER_ADMANAGER, adViewIds);
        builder.setViewIds(AdMixer.ADAPTER_ADFIT, adViewIds);
        builder.setViewIds(AdMixer.ADAPTER_PANGLE, adViewIds);
        AdInfo adInfo = builder.build();

        NativeAdViewBinder viewBinder = new NativeAdViewBinder.Builder(R.layout.item_320x480)
            .setIconImageId(R.id.iv_icon)
            .setTitleId(R.id.tv_title)
            .setAdvertiserId(R.id.tv_adv)
            .setDescriptionId(R.id.tv_desc)
            .setMainViewId(R.id.iv_main)
            .setCtaId(R.id.btn_cta)
            .build();

        nativeAdView = new NativeAdView(this);
        nativeAdView.setAdInfo(adInfo);
        nativeAdView.setViewBinder(viewBinder); // 필수
        nativeAdView.setAdViewListener(new AdListener() {
            @Override
            public void onReceivedAd(String adapterName, Object adView) {
                if (nativeAdView.hasAd) {
                    container.removeAllViews();
                    container.addView(nativeAdView);
                }
            }
            @Override
            public void onFailedToReceiveAd(Object adView, String adapterName, int errorCode, String errorMsg) { }
            @Override
            public void onEventAd(Object o, AdEvent adEvent) {
                switch (adEvent) {
                    case CLICK:
                    case DISPLAYED:
                        break;
                }
            }
        });
        nativeAdView.loadNativeAd();
    }

    @Override protected void onResume() { if (nativeAdView != null) nativeAdView.onResume(); super.onResume(); }
    @Override protected void onPause() { if (nativeAdView != null) nativeAdView.onPause(); super.onPause(); }
    @Override
    protected void onDestroy() {
        if (nativeAdView != null) {
            nativeAdView.onDestroy();
            nativeAdView = null;
        }
        super.onDestroy();
    }
}
```

---

## 4. 리워드 동영상 광고

> 리워드 동영상 광고 추가 전, [Step 1~3 설정](#1-sdk-시작하기)이 완료되었는지 확인하세요.

### 노출 방법

**방법 1: 수신 즉시 노출**  
→ `rewardInterstitialVideoAd.loadRewardVideoAd();` 후 `onReceivedAd` 리스너에 `showRewardVideoAd();` 추가

**방법 2: 원하는 시점에 노출**  
1. `rewardInterstitialVideoAd.loadRewardVideoAd();` 실행
2. `onReceivedAd` 이벤트 수신 후 `if (rewardInterstitialVideoAd.hasInterstitial)` 로 수신 여부 판단
3. `rewardInterstitialVideoAd.showRewardVideoAd();` 로 노출

### 코드 예제

```java
public class RewardInterstitialVideoActivity extends AppCompatActivity {
    private RewardInterstitialVideoAd rewardInterstitialVideoAd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitialvideo);

        AdMixer.registerAdapter(AdMixer.ADAPTER_ADMANAGER);
        AdMixer.registerAdapter(AdMixer.ADAPTER_APPLOVIN);
        AdMixer.registerAdapter(AdMixer.ADAPTER_PANGLE);
        AdMixer.registerAdapter(AdMixer.ADAPTER_UNITY);

        Map<String, String> params = new HashMap<>();
        params.put("use_id", "nas");
        params.put("name", "choi");
        params.put("phone", "010-1111-1111");

        AdInfo.Builder builder = new AdInfo.Builder(Application.adUnitId_interstitialVideo)
            .setCustomParams(params) // Reward Callback 커스텀 데이터 (선택사항)
            .setMute(true);          // 음소거 (true: 활성화)
        builder.setIsUseMediation(true);
        AdInfo adInfo = builder.build();

        rewardInterstitialVideoAd = new RewardInterstitialVideoAd(this);
        rewardInterstitialVideoAd.setAdInfo(adInfo); // setAdInfo는 AdInfo만 받음
        rewardInterstitialVideoAd.setListener(new AdListener() {
            @Override
            public void onReceivedAd(String adapterName, Object adView) { /* 수신 성공 */ }
            @Override
            public void onFailedToReceiveAd(Object adView, String adapterName, int errorCode, String errorMsg) { /* 수신 실패 */ }
            @Override
            public void onEventAd(Object adView, AdEvent adEvent) {
                switch (adEvent) {
                    case CLOSE:       // 광고 창 닫힘
                    case SKIPPED:     // Skip 버튼 클릭
                        break;
                    case COMPLETION:  // 재생 완료
                    case CLICK:       // 더보기 버튼 클릭
                    case EARNEDREWARD: // 리워드 획득 → 리워드 지급 처리!
                        break;
                }
            }
        });

        Button btnInterstitialVideoShow = findViewById(R.id.btn_interstitial_video_show);
        btnInterstitialVideoShow.setOnClickListener(v -> {
            if (rewardInterstitialVideoAd.hasInterstitial)
                rewardInterstitialVideoAd.showRewardVideoAd();
            else
                rewardInterstitialVideoAd.loadRewardVideoAd();
        });
    }

    @Override
    protected void onDestroy() {
        if (rewardInterstitialVideoAd != null) {
            rewardInterstitialVideoAd.stopRewardVideoAd();
            rewardInterstitialVideoAd.setListener(null);
            rewardInterstitialVideoAd = null;
        }
        super.onDestroy();
    }
}
```

---

### Reward 처리

유저가 리워드 동영상 광고 시청을 완료하면 `EARNEDREWARD` 이벤트 콜백이 발생합니다. 이 때 유저에게 리워드를 지급하세요.

```java
@Override
public void onEventAd(Object adView, AdEvent adEvent) {
    switch (adEvent) {
        case EARNEDREWARD:
            // 리워드 지급 처리
            break;
    }
}
```

> **참고**: 광고 네트워크 별로 리워드 지급 완료 시점이 상이할 수 있습니다.

---

### S2S Reward Callback (선택사항)

리워드 지급 이벤트 발생 시, 매체사가 정의한 외부 서버로 광고 시청 완료 여부를 전달하는 기능입니다.

#### 설정 1: 파트너 사이트에서 콜백 서버 URL 입력

**파트너 사이트 → 미디어 관리 → 애드유닛 광고 설정** 에서 콜백 서버 URL을 입력합니다.

**기본 파라미터 (자동 포함):**

| 파라미터 | 설명 | 예시 |
|---------|------|------|
| media_key | 미디어 키 | 12345678 |
| adunit_id | 애드유닛 아이디 | 87654321 |
| adid | Android: Google Advertise ID / iOS: IDFA | 860635ea-... |
| earnedreward | 리워드 지급 처리 이벤트 | - |
| timestamp | earnedreward 이벤트 발생 시간 | 1546300800 |

**예시 URL:**
```
{매체사콜백url}?media_key={mediakey}&adunit_id={adunitid}&adid={adid}&complete={complete}&timestamp={timestamp}
```

#### 설정 2: SDK CustomData 추가 (선택사항)

```java
Map<String, String> params = new HashMap<>();
params.put("useid", "nas");
params.put("name", "hdragon");
params.put("phone", "010-1111-1111");

AdInfo adInfo = new AdInfo.Builder(Application.adUnitId)
    .setCustomParams(params) // String Map 형태로 추가
    .build();
```

**커스텀 파라미터 포함 예시 URL:**
```
{매체사콜백url}?media_key={mediakey}&adunit_id={adunitid}&adid={adid}&complete={complete}&timestamp={timestamp}&useid=nas&name=hdragon&phone=010-1111-1111
```

---

## 5. 동영상 광고

> 동영상 광고 추가 전, [Step 1~3 설정](#1-sdk-시작하기)이 완료되었는지 확인하세요.  
> Interstitial Ad 연동을 희망하시는 경우, [배너 - 전면 배너](#전면-광고-interstitial-banner)를 연동하세요.

### 인라인 동영상 광고 (VideoAdView)

**방법 1: 수신 즉시 노출**  
`videoAdView.loadAd();` 후 `onReceivedAd` 리스너에서 `container.addView(videoAdView, params);` 추가

**방법 2: 원하는 시점에 노출**  
1. `videoAdView.loadAd();` 실행
2. 기존 뷰 제거: `container.removeView(videoAdView);`
3. 새 뷰 추가: `container.addView(videoAdView, params);`

**레이아웃 XML:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout ...>
    <RelativeLayout
        android:id="@+id/container_video"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#000000"
        ...>
        <TextView
            android:id="@+id/tv_complete"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:text="Video Play Complete"
            android:visibility="gone" />
    </RelativeLayout>
</androidx.constraintlayout.widget.ConstraintLayout>
```

**코드 예제:**

```java
public class VideoActivity extends AppCompatActivity {
    private VideoAdView videoAdView;
    private RelativeLayout container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        container = findViewById(R.id.container_video);

        AdInfo adInfo = new AdInfo.Builder(Application.adUnitId_video)
            .isRetry(false) // 광고 재요청 설정 (false: 1회 요청 후 바로 콜백)
            .build();

        videoAdView = new VideoAdView(this);
        videoAdView.setAdInfo(adInfo); // setAdInfo는 AdInfo만 받음
        videoAdView.setAdViewListener(new AdListener() {
            @Override
            public void onReceivedAd(String adapterName, Object adView) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                container.removeView(videoAdView);
                container.addView(videoAdView, params);
            }
            @Override
            public void onFailedToReceiveAd(Object adView, String adapterName, int errorCode, String errorMsg) { }
            @Override
            public void onEventAd(Object o, AdEvent adEvent) {
                switch (adEvent) {
                    case COMPLETION: // 재생 완료
                    case SKIPPED:    // Skip 클릭
                    case CLICK:      // 더보기 클릭
                        break;
                }
            }
        });
        videoAdView.loadAd();
    }

    @Override protected void onResume() { if (videoAdView != null) videoAdView.onResume(); super.onResume(); }
    @Override protected void onPause() { if (videoAdView != null) videoAdView.onPause(); super.onPause(); }
    @Override
    protected void onDestroy() {
        if (videoAdView != null) {
            videoAdView.onDestroy();
            videoAdView = null;
        }
        super.onDestroy();
    }
}
```

---

### 전면 동영상 광고 (InterstitialVideoAd)

**노출 방법 (원하는 시점에 노출):**  
1. `interstitialVideoAdView.loadInterstitialVideoAd();` 실행
2. `onReceivedAd` 이벤트 수신 후 `if(interstitialVideoAdView.hasInterstitial)` 로 수신 여부 판단
3. `interstitialVideoAdView.showInterstitialVideoAd();` 로 노출

**코드 예제:**

```java
public class InterstitialVideoActivity extends AppCompatActivity {
    private InterstitialVideoAd interstitialVideoAdView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AdInfo adInfo = new AdInfo.Builder(Application.adUnitId_interstitialVideo)
            .interstitialTimeout(0)   // 전면 광고 타임아웃 초단위 (0: 서버지정, 기본 20초)
            .maxRetryCountInSlot(-1)  // 리로드 반복 횟수 (-1: 무한, 0: 없음, n: n번)
            .build();

        interstitialVideoAdView = new InterstitialVideoAd(this);
        interstitialVideoAdView.setAdInfo(adInfo); // setAdInfo는 AdInfo만 받음
        interstitialVideoAdView.setListener(new AdListener() {
            @Override
            public void onReceivedAd(Object o) { /* 수신 성공 */ }
            @Override
            public void onFailedToReceiveAd(Object o, int i, String s) { /* 수신 실패 */ }
            @Override
            public void onEventAd(Object o, AdEvent adEvent) {
                switch (adEvent) {
                    case CLOSE:
                    case SKIPPED:
                        interstitialVideoAdView.closeInterstitialVideoAd();
                        break;
                    case COMPLETION:
                    case CLICK:
                        break;
                }
            }
        });

        Button btnInterstitialVideoShow = findViewById(R.id.btn_interstitial_video_show);
        btnInterstitialVideoShow.setOnClickListener(v -> {
            if (interstitialVideoAdView.hasInterstitial)
                interstitialVideoAdView.showInterstitialVideoAd();
            else
                interstitialVideoAdView.loadInterstitialVideoAd();
        });
    }

    @Override
    protected void onDestroy() {
        if (interstitialVideoAdView != null) {
            interstitialVideoAdView.stopInterstitialVideoAd();
            interstitialVideoAdView.setListener(null);
            interstitialVideoAdView = null;
        }
        super.onDestroy();
    }
}
```

---

## 6. 비즈보드

> KaKao Adfit 비즈보드 연동을 위한 가이드입니다.  
> **문의**: nap_adx@nasmedia.co.kr

### 비즈보드 지면 정책

- 비즈보드 지면은 **비즈보드만 단독 사용** (타사 네트워크 등 미디에이션 불가)
- **비즈보드 심사 과정 필수** (앱 내 비즈보드 테스트 광고 적용된 지면 스크린샷 전달 필요)

### 상품 설명

- 앱 사용자에게 최적화된 디자인으로 맞춤형 광고를 제공합니다.
- 철저한 심사를 거쳐 높은 퀄리티의 소재를 보장합니다.

### SDK 연동

- Android 가이드: https://github.com/adfit/adfit-android-sdk/blob/master/docs/BIZBOARDAD.md

### 비즈보드 코드 발급 및 리포트 매핑

**코드 발급:**  
운영팀에 문의하여 코드 발급을 요청하세요.

**리포트 매핑:**  
1. nap ssp 파트너 사이트에서 애드유닛 이름 '비즈보드' 기입 후 리포트용 애드유닛 발급 (포맷/사이즈: Banner - 320x50 선택)
2. 발급 후 운영팀에 해당 애드유닛의 비즈보드 리포트 매핑 요청

### 비즈보드 지면 심사 (중요)

- 비즈보드 지면은 지면 심사 과정이 필수입니다.
- SDK 연동 테스트 과정에서 비즈보드 테스트 광고가 노출된 실제 지면 캡처 이미지를 운영팀에 전달해야 합니다.

---

## 문의

- **이메일**: nap_adx@nasmedia.co.kr
- **파트너 사이트**: nap ssp 파트너 사이트
