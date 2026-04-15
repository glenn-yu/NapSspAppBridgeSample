# Android 포맷별 샘플

이 문서는 Android에서 nap ssp 네이티브 SDK를 붙일 때,
포맷마다 어떤 뷰와 어떤 호출이 필요한지 보여준다.

## 공통 전제

- `Android SDK 시작하기 - Native`의 초기화와 Gradle 설정이 먼저 끝나 있어야 한다.
- 광고를 붙이기 전에 `AdMixer.registerAdapter(...)`와 `setIsUseMediation(true)`가 필요한 경우가 있다.
- 광고 단위 ID(`ADUNIT_ID`)는 파트너 사이트에서 받은 값을 쓴다.

## 1) 배너

### 언제 쓰나
- 화면 아래 고정
- 앱 흐름을 크게 끊지 않을 때

### 핵심 흐름
1. `AdInfo`를 만든다
2. `AdView`를 만든다
3. `container.addView(banner)` 또는 레이아웃 XML에 넣는다
4. `loadAd()` 또는 자동 로드를 쓴다
5. `onResume / onPause / onDestroy`를 정리한다

### 예시

```java
AdInfo adInfo = new AdInfo.Builder(Application.ADUNIT_ID_BANNER)
    .setIsUseMediation(true)
    .build();

AdView banner = new AdView(this);
banner.setAdInfo(adInfo);
container.addView(banner);
```

## 2) 네이티브

### 언제 쓰나
- 리스트 중간
- 카드형 UI
- 콘텐츠처럼 섞어 보여줄 때

### 핵심 구성
- 제목(title)
- 아이콘(icon)
- 광고주(advertiser)
- 설명(description)
- 메인 이미지/영상(mainView)
- 버튼(cta)

### 핵심 흐름
1. 레이아웃 XML을 만든다
2. 필요한 뷰 ID를 잡는다
3. `AdInfo.Builder`에 view id를 연결한다
4. `NativeAdView`를 만든다
5. `loadNativeAd()`를 호출한다
6. 성공 후 화면에 붙인다

### 예시

```java
AdInfo adInfo = new AdInfo.Builder(Application.ADUNIT_ID_NATIVE)
    .setIsUseMediation(true)
    .build();

NativeAdView nativeAdView = new NativeAdView(this);
nativeAdView.setAdInfo(adInfo, this);
nativeAdView.loadNativeAd();
```

## 3) 동영상

### 언제 쓰나
- 앱 안에 재생 영역이 필요할 때
- 사용자가 직접 보는 영역이 있을 때

### 핵심 흐름
1. `VideoAdView`를 만든다
2. `AdInfo`를 연결한다
3. `loadAd()`를 호출한다
4. 성공하면 뷰를 화면에 붙인다
5. `onResume / onPause / onDestroy`를 정리한다

### 예시

```java
VideoAdView videoAdView = new VideoAdView(this);
videoAdView.setAdInfo(adInfo, this);
videoAdView.loadAd();
```

## 4) 리워드 동영상

### 언제 쓰나
- 광고를 보면 보상을 주는 흐름
- 포인트, 보너스, 아이템 지급 화면

### 핵심 흐름
1. `RewardInterstitialVideoAd`를 만든다
2. `setListener()`로 이벤트를 받는다
3. `loadRewardVideoAd()`로 미리 불러온다
4. 준비되면 `showRewardVideoAd()`를 호출한다
5. `EARNEDREWARD` 이벤트에서 보상을 준다

### 예시

```java
RewardInterstitialVideoAd rewardAd = new RewardInterstitialVideoAd(this);
rewardAd.setAdInfo(adInfo, this);
rewardAd.setListener(listener);
rewardAd.loadRewardVideoAd();
```

## 5) 전면 동영상

### 언제 쓰나
- 화면 전환 직전
- 작업 완료 후
- 전체 화면으로 보여줄 때

### 핵심 흐름
1. `InterstitialVideoAd`를 만든다
2. `setListener()`로 이벤트를 받는다
3. `loadInterstitialVideoAd()`를 호출한다
4. 준비되면 `showInterstitialVideoAd()`를 호출한다
5. `CLOSE`, `COMPLETION`, `SKIPPED` 이벤트를 처리한다

### 예시

```java
InterstitialVideoAd interstitial = new InterstitialVideoAd(this);
interstitial.setAdInfo(adInfo, this);
interstitial.setListener(listener);
interstitial.loadInterstitialVideoAd();
```

## 마지막으로 기억할 것

- 포맷마다 **만드는 뷰가 다르다**
- 포맷마다 **로드 함수가 다르다**
- 포맷마다 **이벤트 이름이 다르다**
- 종료 시에는 꼭 리소스를 정리해야 한다
