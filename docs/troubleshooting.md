# 문제 해결

문제가 생기면 아래 순서대로 본다.

1. JDK / Android SDK / Xcode가 있는지 확인한다.
2. media key와 광고 단위 ID가 맞는지 확인한다.
3. WebView 버튼 이름과 네이티브 브리지 이름이 같은지 확인한다.
4. 빌드 산출물을 지우고 다시 빌드한다.
5. 그래도 안 되면 `quickstart.md`부터 다시 본다.

## 가장 흔한 실수

- SDK 버전과 문서 버전이 다른 경우
- Android/iOS 광고 ID를 섞어 쓴 경우
- WebView 메시지 이름을 잘못 적은 경우
