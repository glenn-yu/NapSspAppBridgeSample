# iOS 포맷별 예시

아래 코드는 각 포맷을 붙일 때의 **최소 형태**를 보여준다.

## 배너

```swift
let banner = AMMBannerView(rootViewController: self)
banner.adUnitID = "ADUNIT_ID"
banner.load()
```

## 네이티브

```swift
let nativeAd = AMMNativeAdViewContainer(rootViewController: self)
nativeAd.adUnitID = "ADUNIT_ID"
nativeAd.load()
```

## 동영상

```swift
let videoView = AMMVideoAdView(rootViewController: self)
videoView.adUnitID = "ADUNIT_ID"
videoView.load()
```

## 리워드 동영상

```swift
let rewardVideo = AMMRewardVideo(rootViewController: self)
rewardVideo.adUnitID = "ADUNIT_ID"
rewardVideo.delegate = self
rewardVideo.load()
```

## 전면 동영상

```swift
let interstitial = AMMVideoInterstitial(rootViewController: self)
interstitial.adUnitID = "ADUNIT_ID"
interstitial.delegate = self
interstitial.load()
```
