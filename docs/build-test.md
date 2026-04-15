# 빌드와 테스트

이 문서는 "실제로 돌아가나?"를 확인할 때 본다.

## Android

- `./gradlew :app:assembleDebug`
- 성공하면 APK가 만들어진다.

## iOS

- `swift build`
- 성공하면 Swift 코드가 컴파일된다.

## 하이브리드

- WebView 화면에서 버튼을 눌러 본다.
- 네이티브 앱이 메시지를 받으면 성공이다.
- 광고가 안 뜨면 폴백 화면이 보여도 된다.
