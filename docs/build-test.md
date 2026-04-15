# 빌드와 테스트

이 문서는 나중에 실제 프로젝트가 들어가면 바로 쓸 수 있게 만든다.

## Android

```bash
./gradlew testDebugUnitTest
./gradlew installDebug
```

## iOS

```bash
xcodebuild test -project YOUR_PROJECT.xcodeproj -scheme YOUR_SCHEME -destination 'platform=iOS Simulator,id=YOUR_SIMULATOR_ID'
```

## 확인할 것

- 앱이 실제로 뜨는가
- 웹뷰가 열리는가
- 웹 메시지가 네이티브로 오는가
- 네이티브 답이 웹에 돌아가는가
