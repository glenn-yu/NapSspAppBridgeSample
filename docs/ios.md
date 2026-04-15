# iOS 샘플

## 이 샘플이 하는 일

- WKWebView를 연다
- 웹 페이지를 띄운다
- 웹에서 받은 메시지를 화면에 보여준다
- 네이티브에서 웹으로 다시 답을 보낸다

## 웹에서 보내는 예시

```js
window.AppBridge.postMessage({
  type: 'log',
  payload: 'ios test'
})
```

## 네이티브에서 하는 예시

- `WKScriptMessageHandler`로 메시지를 받는다
- 로그를 찍는다
- 화면에 문자열을 보여준다
- `evaluateJavaScript`로 웹 함수를 호출한다

## 보통 들어갈 파일

- `ContentView` 또는 `WebView`
- `Coordinator`
- 웹을 띄우는 SwiftUI / UIKit 코드
