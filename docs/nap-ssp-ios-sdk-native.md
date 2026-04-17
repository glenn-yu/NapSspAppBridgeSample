# nap ssp iOS SDK - Native 연동 가이드

> **출처**: [nasmob.atlassian.net](https://nasmob.atlassian.net/wiki/spaces/ASIG/pages/744620513/iOS+SDK+-+Native)  
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
| 2026-03-23 | 2.2.1 | native 지면 loadAD() 시 removeView 추가 |
| 2026-03-17 | 2.2.0 | 버그 픽스 |
| 2026-03-11 | 2.1.9 | 버그 픽스 |
| 2026-02-20 | 2.1.8 | 미디에이션 기능 업데이트 |
| 2026-01-20 | 2.1.7 | 배너 320x100 - 네트워크 추가 |
| 2026-01-16 | 2.1.6 | 매체 커스텀 파람 수정 |
| 2026-01-16 | 2.1.5 | 리워드 비디오 콜백 로직 수정 |
| 2026-01-09 | 2.1.4 | 네트워크 버전 업데이트 / 리워드 이벤트 추가 (`onRewardVideoEarned`) |
| 2025-12-22 | 2.1.3 | 하우스애드 수정 |
| 2025-12-09 | 2.1.2 | AMMBannerView - adapter 전역으로 수정 |
| 2025-12-04 | 2.1.1 | objc 추가 |
| 2025-09-09 | 2.1.0 | AMMBannerView isLoading flag 추가 / failProcess()에서 loadNetwork async로 수정 |
| 2025-08-27 | 2.0.9 | 버그 픽스 |
| 2025-08-27 | 2.0.8 | 버그 픽스 |
| 2025-08-26 | 2.0.7 | Applovin 추가 |
| 2025-08-22 | 2.0.7 | AMMVideoView init 시 rootViewController 주입하도록 수정 |
| 2025-08-22 | 2.0.6 | 버그 픽스, 미디에이션 로직 개선 |
| 2025-08-04 | 2.0.5 | 전면 배너 닫기버튼 기능 추가 / 클릭 이벤트 Delegate 메서드 추가 |
| 2025-07-25 | 2.0.4 | Pangle 추가 |
| 2025-07-08 | 2.0.4 | Mobwith 추가, 버그 픽스 |
| 2025-04-25 | 2.0.3 | 이벤트 추가 |
| 2025-04-14 | 2.0.2 | Adfit 추가, 버그 픽스 |
| 2025-03-24 | 2.0.1 | 버그 픽스 |
| 2025-02-28 | 2.0.0 | 버그 픽스 |
| 2024-12-27 | 1.0.1 | 네이티브 에셋 옵션 변경 |
| 2024-10-29 | 1.0.0 | 릴리즈 |

---

### 지원 OS 버전 및 환경

- 최신 버전의 Admixer SDK와 최신 버전의 Xcode 사용을 권장합니다.
- **최소 지원 OS**: iOS 13.0 이상
- **최소 지원 Xcode**: 15.3 이상
- 설치 방식: **CocoaPods**, **SPM** 지원

---

### 개요

이 문서는 nap ssp SDK를 iOS 앱에 연동하기 위한 가이드 문서이며, nap ssp Mediation을 지원합니다.

- **연동 방식**: SDK 연동 방식 / API 연동 방식

**연동 작업 전 사전 정보 안내**

- nap SSP 파트너 사이트에 가입 후 미디어 등록 및 애드유닛 생성을 완료해야 **media key**와 **adunit id**를 확인할 수 있습니다.
- media key와 adunit id가 파트너 사이트와 상이할 경우 광고가 원활히 노출되지 않을 수 있습니다.

**별도 key 값이 필요한 네트워크 (발급 문의: nap_adx@nasmedia.co.kr)**

- Google App ID
- Pangle App ID
- Unity Ads App ID

---

### 샘플 프로젝트

- [iOS SDK Sample](https://github.com/Nasmedia-Tech/iOS-AdMixerSSP-TestApp)

---

### Step 1. SDK 설치하기

#### 1-1. CocoaPods을 통한 설치

1. CocoaPods이 없는 경우 설치 후 초기화:

```bash
$ pod init
```

2. `Podfile`에 nap ssp Mediation과 사용할 네트워크 SDK 추가:

```ruby
target 'MyApp' do
  use_frameworks!
  pod 'AdMixerMediation'
  pod 'AdMixerMediationGAM'      # Google AdManager
  pod 'AdMixerMediationAdFit'    # Kakao AdFit
  pod 'AdMixerMediationPangle'   # Pangle
  pod 'AdMixerMediationAppLovin' # AppLovin
  pod 'AdMixerMediationUnityAds' # UnityAds
end
```

> 포맷별로 바이너리를 더 가볍게 가져가야 하는 경우, 벤더가 제공하는 subspec / 모듈 분리를 우선 사용하세요.
> 예: `AdMixerMediation/Interstitial`, `AdMixerMediation/RewardVideo`처럼 필요한 포맷만 포함.
> 실제 이름은 배포되는 podspec 기준으로 맞추면 됩니다.

3. pod 업데이트:

```bash
$ pod update
```

#### 시뮬레이터 빠른 검증

1. Xcode에서 `.xcworkspace`를 열고 iPhone Simulator 대상으로 실행합니다.
2. 테스트용 Media Key / AdUnit ID를 넣고, `setDebugEnabled(isEnabled: true)`를 켭니다.
3. 인터스티셜은 `load` 후 `show(rootViewController:)`, 리워드는 `load` 후 `show(rootViewController:)` 순서로 확인합니다.
4. Xcode Console에서 loaded / displayed / clicked / rewarded / closed 이벤트가 찍히는지 봅니다.
5. 시뮬레이터에서 응답이 없으면 실제 광고 지면이 아닌 테스트 지면으로 다시 확인합니다.

#### 1-2. SPM을 통한 설치

**Project > Package Dependencies** 탭에서 아래 주소 추가:

**nap ssp Mediation (필수):**
- `https://github.com/Nasmedia-Tech/iOS-SSP-Mediation-SPM.git`
- `https://github.com/Nasmedia-Tech/iOS-SSP-SPM.git`

**추가 네트워크:**

| 네트워크 | SPM 주소 |
|---------|---------|
| Google AdManager | `https://github.com/Nasmedia-Tech/iOS-SSP-GAM-SPM.git` |
| Kakao AdFit | `https://github.com/Nasmedia-Tech/iOS-SSP-AdFit-SPM.git` |
| Pangle | `https://github.com/Nasmedia-Tech/iOS-SSP-Pangle-SPM.git` |

#### 1-3. Google 네트워크 - SDK 입찰 광고 소스 설정

Google 네트워크 사용 시 아래 광고 소스를 **모두** 추가해야 합니다.

**CocoaPods:**

```ruby
pod 'GoogleMobileAdsMediationPangle'    # Pangle
pod 'GoogleMobileAdsMediationFacebook'  # Meta
pod 'GoogleMobileAdsMediationAppLovin'  # AppLovin
pod 'GoogleMobileAdsMediationUnity'     # Unity Ads
pod 'GoogleMobileAdsMediationVungle'    # Liftoff Monetize
pod 'GoogleMobileAdsMediationMintegral' # Mintegral
pod 'GoogleMobileAdsMediationFyber'     # DT Exchange
pod 'GoogleMobileAdsMediationInMobi'    # InMobi
pod 'GoogleMobileAdsMediationMoloco'    # Moloco
```

**SPM (각 GitHub Repository를 개별 Package Dependency로 추가):**

| 네트워크 | GitHub Repository |
|---------|------------------|
| Meta (Facebook) | https://github.com/googleads/googleads-mobile-ios-mediation-meta |
| AppLovin | https://github.com/googleads/googleads-mobile-ios-mediation-applovin |
| ironSource | https://github.com/googleads/googleads-mobile-ios-mediation-ironsource |
| Unity Ads | https://github.com/googleads/googleads-mobile-ios-mediation-unity |
| Liftoff (Vungle) | https://github.com/googleads/googleads-mobile-ios-mediation-liftoffmonetize |
| Mintegral | https://github.com/googleads/googleads-mobile-ios-mediation-mintegral |
| Pangle | https://github.com/googleads/googleads-mobile-ios-mediation-pangle |

> **SPM 주의사항**: Dependency Rule을 `Up to Next Major Version`(권장)으로 설정하고, 앱 타겟(App target)에 추가하세요.

---

### Step 2. SDK 설정

#### 추적 권한 요청 (ATT)

1. `Info.plist`의 **Privacy - Tracking Usage Description**에 추적 권한 요청 문구 입력
2. ATT 팝업 실행 코드 추가:

```swift
import UIKit
import AppTrackingTransparency

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        return true
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        requestTrackingAuthorization()
    }

    // TODO: Info.plist에 Privacy - Tracking Usage Description 입력 필요
    private func requestTrackingAuthorization() {
        Task {
            _ = await ATTrackingManager.requestTrackingAuthorization()
        }
    }
}
```

#### Info.plist 설정

파트너 네트워크별 가이드에 따라 **SKAdNetwork ID** 및 필수 체크 사항을 확인하세요.

- **Google AdManager**: `GADApplicationIdentifier` 추가 (형식: `ca-app-pub-################~##########`)
  - **Google App ID 발급**: nap ssp 운영팀(nap_adx@nasmedia.co.kr) 문의
  - [구글 애드매니저 가이드](https://developers.google.com/ad-manager/mobile-ads-sdk/ios/quick-start?hl=ko#update_your_infoplist)
- [카카오 AdFit 가이드](https://adfit.github.io/adfit-ios-sdk/documentation/adfitsdk/skadnetwork)
- [모비위드 가이드](https://github.com/mobon/MobWithAD_iOS)
- [Pangle 가이드](https://www.pangleglobal.com/kr/integration/integrate-pangle-sdk-for-ios)
- [AppLovin 가이드](https://developers.axon.ai/en/max/ios/overview/integration/)

---

### Step 3. SDK 초기화

광고 호출 전 앱에서 **1회** 초기화 호출이 필요합니다.

- **Applovin SDK Key**: `nObIkviLd_FQIkP6yMGsTI7vKdDheVRJfwRkxzH7ie0T2o2slTnPIBcbTRelfXPuwGQcPf2bVGKTtaxtTrR0c9`

```swift
import UIKit
import AdMixerMediation
import GoogleMobileAds
import PAGAdSDK
import AppLovinSDK
import UnityAds

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {

        // AdMixer 초기화 (필수)
        AMMediation.shared.initialize(
            mediaKey: MEDIA_KEY,
            adunitID: [ADUNIT_ID_BANNER, ADUNIT_ID_INTERSTITIAL_BANNER, ADUNIT_ID_NATIVE]
        )

        // Google AdManager 초기화 (해당 네트워크 사용 시)
        MobileAds.shared.start()

        // Pangle 초기화 (해당 네트워크 사용 시)
        let pagConfig = PAGConfig.share()
        pagConfig.appID = "앱ID"  // 운영팀에 발급 요청
        PAGSdk.start(with: pagConfig) { isSuccess, error in }

        // AppLovin 초기화 (해당 네트워크 사용 시 - SDK key 적용 필수)
        let sdkKey = "nObIkviLd_FQIkP6yMGsTI7vKdDheVRJfwRkxzH7ie0T2o2slTnPIBcbTRelfXPuwGQcPf2bVGKTtaxtTrR0c9"
        let alConfig = ALSdkInitializationConfiguration(sdkKey: sdkKey)
        ALSdk.shared().initialize(with: alConfig) { _ in }

        // UnityAds 초기화 (해당 네트워크 사용 시)
        UnityAds.initialize("앱ID")  // 운영팀에 발급 요청

        return true
    }
}
```

---

### 에러 코드

| no | 에러 코드 | 설명 |
|----|---------|------|
| 0 | missingBaseURL | API 요청에 필요한 base URL이 누락된 경우 |
| 1 | invalidURLString | 유효하지 않은 URL로 요청하는 경우 |
| 2 | invalidServerResponse | 서버로부터 유효하지 않은 응답을 받은 경우. 네트워크 상태 확인 필요 |
| 3 | decodeError | 데이터 처리에 오류가 있는 경우 |
| 4 | apiResponseFail | 서버 통신에 실패한 경우. 서버 상태 확인 또는 재시도 필요 |
| 5 | vastParsingError | 비디오 광고 데이터 처리에 오류가 있는 경우 |
| 6 | emptyAd | 노출 가능한 광고가 없는 경우. 잠시 후 재요청 필요 |

---

### 자주 하는 질문 (FAQ)

**Q1. 로그 설정 방법은?**  
`AMMediation.shared.setDebugEnabled(isEnabled: Bool)` 코드를 사용하면 Console에서 상세 로그를 확인할 수 있습니다.

**Q2. 하나의 App에 복수 개의 Media Key 적용이 가능한가요?**  
한 개의 App에는 한 개의 Media Key만 적용 가능합니다.

**Q3. 동일한 AdUnit ID로 여러 광고 객체를 생성해도 되나요?**  
한 개의 AdUnit ID는 한 개의 광고 객체에서만 사용할 수 있습니다.

**Q4. 광고가 나오지 않는 경우 대처 방법은?**  
1. Xcode Console 로그 확인
2. 파트너 사이트 광고 키 설정 확인
3. SDK 초기화 호출 여부 확인
4. 올바른 Media Key와 AdUnit ID 사용 여부 확인

**Q5. Adfit 상용 광고는 언제 응답되나요?**  
`[연동 테스트] → [매체 라이브] → [Adfit 매체 심사] → [심사 완료] → [상용 광고 송출]`  
라이브 후 운영팀(nap_adx@nasmedia.co.kr) 문의 필요.

---

### 타 네트워크 버전 정보

기존 적용 중인 네트워크사 버전이 있는 경우, 매체 버전과 nap ssp 버전 중 **더 낮은 버전**으로 탑재됩니다.

**중복 사용 가능 네트워크:**
- **Google**: 기존 운영 중인 지면과 다른 지면의 경우 가능
- **Pangle**: 기존 운영 중인 지면과 다른 지면의 경우 가능
- **Adfit**: 네트워크사의 앱 심사 진행 후 사용 가능

| Adapter SDK | 이름 | 버전 범위 | 비고 |
|------------|------|---------|------|
| AdMixerMediationGAM | Google-Mobile-Ads-SDK | 12.7.0 이상 ~ 12.14.0 이하 | |
| AdMixerMediationAdFit | AdFitSDK | 3.14.7 이상 ~ 3.18.6 이하 | 최소 지원 OS: 14 |
| AdMixerMediationPangle | Ads-Global | 7.4.0.8 이상 ~ 7.8.8.8 이하 | |
| AdMixerMediationUnityAds | UnityAds | - | |

---

## 2. 배너 광고

> 배너 광고 추가 전, [Step 1~3 설정](#1-sdk-시작하기)이 완료되었는지 확인하세요.

배너 광고는 광고 요청 후 즉시 노출하는 방식을 지원합니다.

### 1) Banner 뷰 인스턴스 생성 및 설정

```swift
class ViewController: UIViewController {
    var bannerView: AMMBannerView!

    override func viewDidLoad() {
        super.viewDidLoad()
        bannerView = AMMBannerView(rootViewController: self)
        addBannerViewToView(bannerView)
        bannerView.adUnitId = "ADUNIT_ID"
        bannerView.delegate = self
    }

    func addBannerViewToView(_ bannerView: UIView) {
        bannerView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(bannerView)
        NSLayoutConstraint.activate([
            bannerView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            bannerView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            bannerView.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -50),
        ])
    }
}
```

### 2) 광고 요청 및 노출

```swift
bannerView.load()
```

### 3) 리소스 해제

```swift
override func viewDidDisappear(_ animated: Bool) {
    super.viewDidDisappear(animated)
    if isMovingFromParent || isBeingDismissed {
        bannerView.stop()
        bannerView = nil
    }
}
```

### 4) Delegate

`AMMBannerViewDelegate` 채택 후 사용:

```swift
extension ViewController: AMMBannerViewDelegate {
    func onSuccessBanner() {
        // 배너 광고 로드 성공
    }
    func onFailBanner() {
        // 배너 광고 로드 실패
    }
    func onTapBanner() {
        // 배너 광고 탭
    }
}
```

---

### 전면 배너 광고 (Interstitial Banner)

전면 배너는 광고 요청 후 받은 뒤, **원하는 시점에 노출**하는 방식을 지원합니다.

#### 전면 배너 형태 (3종)

| 형식 | 설명 |
|------|------|
| **basic** | 우측 상단에 "X" 이미지 형태의 닫기 버튼 노출 |
| **popup** | 광고 소재 하단에 텍스트 형태의 닫기 버튼 노출. `popupOption`으로 텍스트/타이틀색상/버튼배경색 커스터마이징 가능 |
| **countDown** | 설정 시간 후 닫기 버튼 노출. `countDownOption`으로 시간(2~5초) 및 UI 타입(gauge/text) 설정 |

> `closeButtonTouchAreaRatio`: 닫기버튼 터치 영역 비율 설정 (0.2~1.0, basic/countDown 형에만 적용)

#### 1) 인스턴스 생성 및 설정

```swift
class ViewController: UIViewController {
    var interstitial: AMMInterstital?

    override func viewDidLoad() {
        let config = AMMInterstitialConfig()
        config.viewType = .popup
        config.popupOption = AMMInterstitialPopupOption(
            buttonTitle: "닫기",
            buttonTextColor: .white,
            buttonBackgroundColor: .black
        )
        config.countDownOption = AMMInterstitialCountDownOption(
            countDownTime: 4,      // 2~5초
            countDownType: .gauge  // .gauge 또는 .text
        )
        config.closeButtonTouchAreaRatio = 1.0  // 0.2~1.0 (기본값: 1.0)
    }
}
```

#### 2) 광고 요청

```swift
AMMInterstitial.load(adUnitID: "ADUNIT_ID", config: config) { [weak self] interstitial, error in
    guard let self = self else { return }
    if let error = error {
        print("AMMInterstitial error: \(error)")
    }
    if let interstitial = interstitial {
        self.interstitial = interstitial
        self.interstitial?.delegate = self
    }
}
```

#### 3) 광고 노출

```swift
interstitial?.show(rootViewController: self)
```

#### 4) 리소스 해제

```swift
override func viewDidDisappear(_ animated: Bool) {
    super.viewDidDisappear(animated)
    if isMovingFromParent || isBeingDismissed {
        interstitial?.stop()
        interstitial = nil
    }
}
```

#### 5) Delegate

`AMMInterstitialDelegate` 채택 후 사용:

```swift
extension ViewController: AMMInterstitialDelegate {
    func onSuccessShowInterstitial() {
        // 전면 광고 노출 성공
    }
    func onFailShowInterstitial(error: Error?) {
        // 전면 광고 노출 실패
    }
    func onTapInterstitial() {
        // 전면 광고 탭
    }
    func onCloseInterstitial() {
        // 전면 광고 닫기
    }
}
```

---

## 3. 네이티브 광고

> 네이티브 광고 추가 전, [Step 1~3 설정](#1-sdk-시작하기)이 완료되었는지 확인하세요.

네이티브 광고는 광고 요청 후 즉시 노출하는 방식을 지원합니다.

### 구성

네이티브 광고는 6가지 asset으로 구성됩니다. 각 asset으로 자유롭게 UI를 구성할 수 있습니다.

- **아이콘** (icon)
- **제목** (headline)
- **광고주** (advertiser)
- **설명** (description)
- **미디어** (Media: Image 또는 Video)
- **버튼** (cta)

> **필수**: 제목(title), 아이콘(icon), 미디어(Media) 중 **1개 이상** 반드시 사용해야 합니다.

### Step 1. AMMNativeAdView.xib 파일 추가

사이즈별 권장 xib 파일을 프로젝트에 추가합니다.

### Step 2. 인스턴스 변수 생성

```swift
import AdMixerMediation

class AMMNativeAdViewController: UIViewController {
    var nativeAd: AMMNativeAdViewContainer!

    override func viewDidLoad() {
        let nibView = Bundle.main.loadNibNamed("AMMNativeAdView", owner: nil, options: nil)?.first
        let nativeAdView = nibView as? AMMNativeAdView

        nativeAd = AMMNativeAdViewContainer(rootViewController: self)
        nativeAd.nativeAdView = nativeAdView
        nativeAd.adUnitID = "ADUNIT_ID"
        nativeAd.delegate = self
    }
}
```

### 광고 요청

```swift
nativeAd.load()
```

### 리소스 해제

```swift
override func viewDidDisappear(_ animated: Bool) {
    super.viewDidDisappear(animated)
    if isMovingFromParent || isBeingDismissed {
        nativeAd.stop()
        nativeAd = nil
    }
}
```

### Delegate

`AMMNativeDelegate` 채택 후 사용:

```swift
extension AMMNativeAdViewController: AMMNativeDelegate {
    func onSuccessNative() {
        // 네이티브 광고 호출 성공
    }
    func onFailNative() {
        // 네이티브 광고 호출 실패
    }
    func onTapNative() {
        // 네이티브 광고 탭
    }
}
```

---

## 4. 리워드 동영상 광고

> 리워드 동영상 광고 추가 전, [Step 1~3 설정](#1-sdk-시작하기)이 완료되었는지 확인하세요.

리워드 동영상 광고는 광고 요청 후 받은 뒤, **원하는 시점에 노출**하는 방식을 지원합니다.

### 1) 인스턴스 변수 생성

```swift
class ViewController: UIViewController {
    var rewardVideo: AMMRewardVideo?
}
```

### 2) 광고 요청

```swift
class ViewController: UIViewController {
    var rewardVideo: AMMRewardVideo?

    override func viewDidLoad() {
        let customParam = [
            "useid": "nas",
            "name": "hdragon",
            "phone": "010-1111-1111"
        ]
        AMMRewardVideo.load(adUnitID: "ADUNIT_ID", customParam: customParam) { [weak self] reward, error in
            guard let self = self else { return }
            if let error = error {
                print("AMMRewardVideo error: \(error)")
            }
            if let reward = reward {
                self.rewardVideo = reward
                self.rewardVideo?.delegate = self
            }
        }
    }
}
```

### 3) 광고 노출

```swift
rewardVideo?.show(rootViewController: self)
```

### 4) 리소스 해제

```swift
override func viewDidDisappear(_ animated: Bool) {
    super.viewDidDisappear(animated)
    if isMovingFromParent || isBeingDismissed {
        rewardVideo?.stop()
        rewardVideo = nil
    }
}
```

### 5) Delegate

`AMMRewardVideoDelegate` 채택 후 사용:

> 광고 네트워크 별로 이벤트가 상이할 수 있습니다. (일부 네트워크는 재생 완료 이벤트 미제공)

```swift
extension ViewController: AMMRewardVideoDelegate {
    func onSuccessShowReward() {
        // 리워드 동영상 광고 노출 성공
    }
    func onFailShowReward(error: Error?) {
        // 리워드 동영상 광고 노출 실패
    }
    func onCloseRewardVideo() {
        // 리워드 동영상 광고 닫기
    }
    func onTapRewardVideo() {
        // 리워드 동영상 광고 탭
    }
    func onRewardVideoComplete() {
        // 리워드 동영상 재생 완료
    }
    func onRewardVideoEarned() {
        // 리워드 지급 완료 → 여기서 리워드 지급 처리!
    }
}
```

---

### Reward 처리

유저가 리워드 동영상 광고 시청을 완료하면 `onRewardVideoEarned` 이벤트 콜백이 발생합니다. 이 때 유저에게 리워드를 지급하세요.

```swift
extension ViewController: AMMRewardVideoDelegate {
    func onRewardVideoEarned() {
        // 리워드 지급 처리
    }
}
```

> 광고 네트워크 별로 리워드 지급 완료 시점이 상이할 수 있습니다.

---

### S2S Reward Callback (선택사항)

매체사가 정의한 외부 서버로 리워드 지급 완료 여부를 전달하는 기능입니다. 콜백 수신까지 몇 분 정도 지연될 수 있습니다.

#### 설정 1: 파트너 사이트에서 콜백 서버 URL 입력

**파트너 사이트 → 미디어 관리 → 애드유닛 광고 설정**에서 콜백 서버 URL을 입력하세요.

**기본 파라미터 (자동 포함):**

| 파라미터 | 설명 | 예시 |
|---------|------|------|
| media_key | 미디어 키 (nap ssp 파트너사이트에서 발급) | 12345678 |
| adunit_id | 애드유닛 아이디 (nap ssp 파트너사이트에서 발급) | 87654321 |
| ifa | iOS 기기 고유 식별자 (IDFA) | 860635ea-65bc-eaed-d355-1b5283b30b94 |
| timestamp | 리워드 지급 이벤트 발생 시간 | 1546300800 |

#### 설정 2: SDK CustomParam 추가 (선택사항)

CustomParam은 `Dictionary` 형태로 추가해야 합니다.

```swift
rewardVideo.customParam = [
    "useid": "nas",
    "name": "hdragon",
    "phone": "010-1111-1111"
]
```

**커스텀 파라미터 포함 예시 URL:**
```
{매체사콜백url}?media_key={mediakey}&adunit_id={adunitid}&ifa={adid}&timestamp={timestamp}&useid=nas&name=hdragon&phone=010-1111-1111
```

---

## 5. 동영상 광고

> 동영상 광고 추가 전, [Step 1~3 설정](#1-sdk-시작하기)이 완료되었는지 확인하세요.  
> Interstitial Ad 연동을 희망하는 경우, [배너 - 전면 배너 광고](#전면-배너-광고-interstitial-banner)를 연동하세요.

### 인라인 동영상 광고 (AMMVideoAdView)

동영상 광고는 광고 요청 후 즉시 노출하는 방식을 지원합니다.

#### 1) 인스턴스 생성 및 설정

```swift
class ViewController: UIViewController {
    var ammVideoView: AMMVideoAdView!

    override func viewDidLoad() {
        super.viewDidLoad()
        ammVideoView = AMMVideoView(rootViewController: self)
        addViewToView(ammVideoView)
        ammVideoView.adUnitID = "ADUNIT_ID"
        ammVideoView.delegate = self
        ammVideoView.load()
    }

    func addViewToView(_ view: UIView) {
        view.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(view)
        NSLayoutConstraint.activate([
            view.leadingAnchor.constraint(equalTo: self.view.leadingAnchor),
            view.trailingAnchor.constraint(equalTo: self.view.trailingAnchor),
            view.bottomAnchor.constraint(equalTo: self.view.bottomAnchor, constant: -50),
            view.heightAnchor.constraint(equalToConstant: 200)
        ])
    }
}
```

#### 2) 광고 요청 및 노출

```swift
ammVideoView.load()
```

#### 3) 리소스 해제

```swift
override func viewDidDisappear(_ animated: Bool) {
    super.viewDidDisappear(animated)
    if isMovingFromParent || isBeingDismissed {
        ammVideoView.stop()
        ammVideoView = nil
    }
}
```

#### 4) Delegate

`AMMVideoViewDelegate` 채택 후 사용:

```swift
extension ViewController: AMMVideoViewDelegate {
    func onSuccessVideo() {
        // 동영상 광고 로드 성공
    }
    func onFailVideo() {
        // 동영상 광고 로드 실패
    }
    func onSkipVideo() {
        // 동영상 skip 버튼 클릭
    }
    func onTapAdViewMore() {
        // 동영상 더보기 버튼 클릭
    }
    func onCompleteVideo() {
        // 동영상 재생 완료
    }
}
```

---

### 전면 동영상 광고 (AMMVideoInterstitial)

전면 동영상 광고는 광고 요청 후 받은 뒤, **원하는 시점에 노출**하는 방식을 지원합니다.

> **참고**: 전면 동영상 재생 시 Skip 버튼은 제공되지 않으며, Skip 가능한 시점에 닫기 버튼이 노출됩니다.

#### 1) 인스턴스 변수 생성

```swift
class ViewController: UIViewController {
    var interstitial: AMMVideoInterstitial?
}
```

#### 2) 광고 요청

```swift
AMMVideoInterstitial.load(adUnitID: "ADUNIT_ID") { [weak self] videointerstitial, error in
    guard let self = self else { return }
    if let error = error {
        print("AMMVideoInterstitial error: \(error)")
    }
    if let videointerstitial {
        self.interstitial = videointerstitial
        self.interstitial?.delegate = self
    }
}
```

#### 3) 광고 노출

```swift
interstitial?.show(rootViewController: self)
```

#### 4) 리소스 해제

```swift
override func viewDidDisappear(_ animated: Bool) {
    super.viewDidDisappear(animated)
    if isMovingFromParent || isBeingDismissed {
        interstitial?.stop()
        interstitial = nil
    }
}
```

#### 5) Delegate

`AMMVideoInterstitialDelegate` 채택 후 사용:

```swift
extension ViewController: AMMVideoInterstitialDelegate {
    func onSuccessShowVideoInterstitial() {
        // 전면 동영상 광고 노출 성공
    }
    func onFailShowVideoInterstitial(error: Error?) {
        // 전면 동영상 광고 노출 실패
    }
    func onCloseVideoInterstitial() {
        // 전면 동영상 광고 닫기
    }
    func onTapVideoInterstitialViewMore() {
        // 전면 동영상 더보기 버튼 클릭
    }
    func onCompleteVideoInterstitial() {
        // 전면 동영상 재생 완료
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

- iOS 가이드: https://adfit.github.io/adfit-ios-sdk/documentation/adfitsdk/bizboardtemplate

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
