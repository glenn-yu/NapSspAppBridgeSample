# 미디에이션 / 네트워크 정리

이 문서는 nap ssp 네이티브 SDK에서 함께 쓸 수 있는 네트워크와 미디에이션 방향을 정리한다.

## Android

### 핵심 의존성

- `io.github.nasmedia-tech:admixer-ssp:1.0.21`
- `com.google.android.gms:play-services-ads-identifier:18.3.0`

### 선택 미디에이션

- `io.github.nasmedia-tech:admixer-admanager:1.0.14`
- `io.github.nasmedia-tech:admixer-adfit:1.0.10`
- `io.github.nasmedia-tech:admixer-pangle:1.0.10`
- `io.github.nasmedia-tech:admixer-applovin:1.0.8`
- `io.github.nasmedia-tech:admixer-unity:1.0.6`

### 추가 저장소

- AdFit: `https://devrepo.kakao.com/nexus/content/groups/public/`
- Pangle: `https://artifact.bytedance.com/repository/pangle/`

### 네트워크 메모

- Google 계열은 MCM/입찰 광고 설정이 별도로 필요할 수 있다.
- 동일 네트워크 SDK를 앱에서 이미 쓰는 경우 exclude 처리가 필요할 수 있다.
- 운영팀과 매체 앱의 네트워크 구성이 다르면 중복 예외를 확인해야 한다.

## iOS

### 현재 샘플의 핵심 의존성 방식

- 이 샘플은 현재 `ios/Vendor/AdMixer/AdMixer.xcframework`
- 그리고 `ios/Vendor/AdMixerMediation/AdMixerMediation.xcframework`
- 두 로컬 binary target을 `ios/Package.swift`에서 직접 참조합니다.
- 이유: 원격 SPM binary fetch 지연이나 실패와 무관하게, 샘플을 같은 상태로 재현 가능하게 유지하기 위해서입니다.

### 원본 패키지 출처

- `https://github.com/Nasmedia-Tech/iOS-SSP-Mediation-SPM.git`
- `https://github.com/Nasmedia-Tech/iOS-SSP-SPM.git`

### 선택 네트워크 SPM

- Google AdManager: `https://github.com/Nasmedia-Tech/iOS-SSP-GAM-SPM.git`
- Kakao AdFit: `https://github.com/Nasmedia-Tech/iOS-SSP-AdFit-SPM.git`
- Pangle: `https://github.com/Nasmedia-Tech/iOS-SSP-Pangle-SPM.git`
- Unity Ads: `https://github.com/Nasmedia-Tech/iOS-SSP-UnityAds-SPM.git`
- AppLovin: `https://github.com/Nasmedia-Tech/iOS-SSP-AppLovin-SPM.git`

### 네트워크 메모

- Google 계열은 SDK 입찰 광고 소스 설정이 필요할 수 있다.
- Unity Ads는 nap ssp Mediation과 Google 입찰 광고 소스에서 중복 적용을 피해야 한다.
- 운영팀과 맞지 않는 네트워크 구성이 있으면 광고가 안 붙을 수 있다.
- 선택 네트워크를 추가할 때는 이 샘플의 vendored 방식과 별도로, 각 네트워크 패키지의 배포 방식을 프로젝트 정책에 맞게 결정해야 한다.

## 공통

- 미디어 키와 ADUNIT ID는 반드시 파트너 사이트 기준으로 사용한다.
- 포맷별로 사용할 ADUNIT ID를 샘플에 맞게 분리해서 둔다.
- 요청/로드/노출/클릭/실패 로그는 `AdEventLogger`로 남긴다.
- `NapSspConfig`에 앱별 media key / ad unit / mediation 힌트를 모아 둔다.
- 실제 광고 객체는 `NapSspSdkIntegration`에 넣고, SDK가 없는 환경에서는 fallback을 보여준다.
