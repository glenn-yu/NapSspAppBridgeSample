# 빌드와 테스트

이 샘플은 현재 **실제 프로젝트 뼈대 + 포맷별 선택 화면 + 초기화 hook**으로 구성되어 있다.

## Android

- `android/` 아래 Gradle 프로젝트를 연다
- nap ssp SDK와 필요한 네트워크 SDK를 붙인다
- 버전은 실제 가이드에 맞춰 채운다
- 앱 시작 시 `NapSspInitializer.initialize()`를 호출한다
- 앱을 실행하고 배너 / 네이티브 / 동영상 / 리워드 / 전면 동영상을 선택해 본다
- 빌드 전 `java -version`이 동작해야 한다. 이 workspace는 Android Gradle wrapper를 포함하지만, Java runtime이 없으면 `./gradlew :app:assembleDebug`가 실패한다.

## iOS

- `ios/` 아래 Swift Package 기반 샘플을 연다
- CocoaPods 또는 SPM으로 SDK를 넣는다
- 앱 시작 시 `NapSspInitializer.initialize()`를 호출한다
- 앱을 실행하고 각 포맷을 선택해 본다

## 확인할 것

- 앱이 실제로 뜨는가
- 포맷 선택 화면이 보이는가
- 실제 SDK 연결 코드를 넣을 자리가 분명한가
- 빌드 후 바로 포맷별 작업을 이어갈 수 있는가
