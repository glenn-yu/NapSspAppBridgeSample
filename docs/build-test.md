# 빌드와 테스트

이 문서는 샘플을 실제로 돌릴 때 확인할 것만 적는다.

## Android

- Gradle에 nap ssp SDK와 필요한 네트워크 SDK를 넣는다
- `AdMixer.registerAdapter(...)`와 `setIsUseMediation(true)`가 필요한지 확인한다
- 앱을 실행하고 배너 / 네이티브 / 동영상 / 리워드 / 전면 동영상이 뜨는지 본다

## iOS

- CocoaPods 또는 SPM으로 SDK를 넣는다
- ATT, Info.plist, 네트워크 설정을 확인한다
- 앱을 실행하고 각 포맷이 뜨는지 본다

## 확인할 것

- 앱이 실제로 뜨는가
- 광고 뷰가 화면에 붙는가
- 로드 성공 이벤트가 오는가
- 닫기 / 완료 / 리워드 이벤트가 오는가
