# Android 포맷별 예시

아래 코드는 각 포맷을 붙일 때의 **최소 형태**를 보여준다.

## 배너

```java
AdInfo adInfo = new AdInfo.Builder(Application.ADUNIT_ID_BANNER)
    .setIsUseMediation(true)
    .build();

AdView banner = new AdView(this);
banner.setAdInfo(adInfo);
container.addView(banner);
```

## 네이티브

```java
AdInfo adInfo = new AdInfo.Builder(Application.ADUNIT_ID_NATIVE)
    .setIsUseMediation(true)
    .build();

NativeAdView nativeAdView = new NativeAdView(this);
nativeAdView.setAdInfo(adInfo, this);
nativeAdView.loadNativeAd();
```

## 동영상

```java
VideoAdView videoAdView = new VideoAdView(this);
videoAdView.setAdInfo(adInfo, this);
videoAdView.loadAd();
```

## 리워드 동영상

```java
RewardInterstitialVideoAd rewardAd = new RewardInterstitialVideoAd(this);
rewardAd.setAdInfo(adInfo, this);
rewardAd.setListener(listener);
rewardAd.loadRewardVideoAd();
```

## 전면 동영상

```java
InterstitialVideoAd interstitial = new InterstitialVideoAd(this);
interstitial.setAdInfo(adInfo, this);
interstitial.setListener(listener);
interstitial.loadInterstitialVideoAd();
```
