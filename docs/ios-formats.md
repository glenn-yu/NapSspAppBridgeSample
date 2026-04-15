# iOS 포맷별 샘플

이 문서는 iOS에서 nap ssp 네이티브 SDK를 붙일 때,
포맷마다 어떤 뷰와 어떤 호출이 필요한지 보여준다.

## 공통 전제

- `iOS SDK 시작하기 -Native`의 설치와 설정이 먼저 끝나 있어야 한다.
- CocoaPods 또는 SPM으로 SDK를 넣는다.
- 광고 단위 ID(`ADUNIT_ID`)는 파트너 사이트에서 받은 값을 쓴다.

## 1) 배너

### 언제 쓰나
- 화면 아래 고정
- 앱 흐름을 크게 끊지 않을 때

### 핵심 흐름
1. `AMMBannerView`를 만든다
2. `rootViewController`를 준다
3. `adUnitID`를 설정한다
4. `load()`를 호출한다
5. `stop()`으로 정리한다

### 예시

```swift
let banner = AMMBannerView(rootViewController: self)
banner.adUnitID = "ADUNIT_ID"
banner.load()
```

## 2) 네이티브

### 언제 쓰나
- 리스트 중간
- 카드형 UI
- 콘텐츠처럼 섞어 보여줄 때

### 핵심 구성
- 아이콘(icon)
- 제목(headline)
- 광고주(advertiser)
- 설명(description)
- 미디어(Media)
- 버튼(cta)

### 핵심 흐름
1. `AMMNativeAdView.xib`를 만든다
2. 뷰 연결 정보를 설정한다
3. `AMMNativeAdViewContainer`를 만든다
4. `load()`를 호출한다
5. 성공 후 화면에 붙인다

### 예시

```swift
let nativeAd = AMMNativeAdViewContainer(rootViewController: self)
nativeAd.adUnitID = "ADUNIT_ID"
nativeAd.load()
```

## 3) 동영상

### 언제 쓰나
- 앱 안에 재생 영역이 필요할 때
- 사용자가 직접 보는 영역이 있을 때

### 핵심 흐름
1. `AMMVideoAdView`를 만든다
2. `adUnitID`를 준다
3. `load()`를 호출한다
4. 성공하면 화면에 붙인다
5. `stop()`으로 정리한다

### 예시

```swift
let videoView = AMMVideoAdView(rootViewController: self)
videoView.adUnitID = "ADUNIT_ID"
videoView.load()
```

## 4) 리워드 동영상

### 언제 쓰나
- 광고를 보면 보상을 주는 흐름
- 포인트, 보너스, 아이템 지급 화면

### 핵심 흐름
1. `AMMRewardVideo`를 만든다
2. `delegate`를 설정한다
3. `load()`를 호출한다
4. 준비되면 보여준다
5. `onRewardVideoEarned()`에서 보상을 준다

### 예시

```swift
let rewardVideo = AMMRewardVideo(rootViewController: self)
rewardVideo.adUnitID = "ADUNIT_ID"
rewardVideo.delegate = self
rewardVideo.load()
```

## 5) 전면 동영상

### 언제 쓰나
- 화면 전환 직전
- 작업 완료 후
- 전체 화면으로 보여줄 때

### 핵심 흐름
1. `AMMVideoInterstitial`를 만든다
2. `delegate`를 설정한다
3. `load()`를 호출한다
4. 준비되면 보여준다
5. `stop()`으로 정리한다

### 예시

```swift
let interstitial = AMMVideoInterstitial(rootViewController: self)
interstitial.adUnitID = "ADUNIT_ID"
interstitial.delegate = self
interstitial.load()
```

## 마지막으로 기억할 것

- 포맷마다 **만드는 뷰가 다르다**
- 포맷마다 **로드 함수가 다르다**
- 포맷마다 **delegate 이벤트가 다르다**
- 종료 시에는 꼭 리소스를 정리해야 한다
