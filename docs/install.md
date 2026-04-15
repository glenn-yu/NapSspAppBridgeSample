# 설치

## 목표

nap ssp 네이티브 SDK 샘플을 열기 전에 필요한 기본 환경을 확인하는 것.

## Android

- Android Studio 또는 Gradle 빌드 환경이 있어야 한다.
- JDK와 Android SDK가 설치되어 있어야 한다.
- nap ssp Android SDK는 `io.github.nasmedia-tech:admixer-ssp:1.0.21`를 사용한다.
- Google Ads ID 의존성도 함께 넣는다: `com.google.android.gms:play-services-ads-identifier:18.9.0`

## iOS

- Xcode와 iOS 빌드 환경이 있어야 한다.
- CocoaPods 또는 SPM 중 하나로 SDK를 넣을 수 있어야 한다.
- nap ssp iOS SDK는 SPM 패키지로 넣는다.
- 기본 패키지: `https://github.com/Nasmedia-Tech/iOS-SSP-Mediation-SPM.git`
- 코어 패키지: `https://github.com/Nasmedia-Tech/iOS-SSP-SPM.git`

## 먼저 확인할 것

- 미디어 키가 준비되어 있는가
- 광고 단위 ID가 준비되어 있는가
- 파트너 가이드의 버전과 샘플 문서가 같은가

## 다음 단계

설치가 끝나면 `setup.md`와 `quickstart.md`를 순서대로 본다.
