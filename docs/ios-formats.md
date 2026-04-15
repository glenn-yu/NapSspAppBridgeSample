# iOS 포맷별 샘플

이 문서는 iOS에서 각 광고 포맷을 **어디에 붙이는지** 보여준다.

## 공통 브리지 이름

```swift
let bridgeName = "AppBridge"
```

웹에서 보내는 메시지 예시:

```js
window.AppBridge.postMessage({
  type: 'load_ad',
  format: 'banner',
  adUnitId: 'YOUR_AD_UNIT_ID'
})
```

## 1) 배너

### 언제 쓰나
- 화면 아래 고정
- 앱 흐름을 크게 끊지 않음

### 화면 배치
- `UIView` 하단 영역
- `SwiftUI`에서는 `UIViewRepresentable`

### 샘플 흐름
- 웹에서 `format: 'banner'` 요청
- 네이티브가 배너를 띄움
- 노출 / 탭 이벤트를 웹으로 전달

## 2) 네이티브

### 언제 쓰나
- 콘텐츠 사이
- 카드형 UI
- 피드에 자연스럽게 섞을 때

### 화면 배치
- 커스텀 `UIView`
- `xib` 또는 코드로 구성

### 샘플 흐름
- 웹에서 `format: 'native'` 요청
- 네이티브가 카드 뷰를 만들고 표시
- 버튼 클릭 시 웹에 이벤트 전달

## 3) 동영상

### 언제 쓰나
- 앱 안에 재생 영역이 필요할 때
- 사용자가 직접 눌러 보는 영역

### 화면 배치
- `WKWebView` 위 / 아래의 재생 컨테이너

### 샘플 흐름
- 웹에서 `format: 'video'` 요청
- 네이티브가 동영상 뷰를 생성
- 로딩 성공 / 재생 완료 이벤트를 웹으로 전달

## 4) 리워드 동영상

### 언제 쓰나
- 보상 지급 흐름이 있을 때
- "광고 보면 포인트 지급" 같은 화면

### 화면 배치
- 보상 버튼 뒤
- 보상 확인 화면 뒤

### 샘플 흐름
- 웹에서 `format: 'reward'` 요청
- 네이티브가 리워드 광고를 띄움
- `onRewardVideoEarned()`를 웹으로 보냄

## 5) 전면 동영상

### 언제 쓰나
- 화면 전환 전에 보여줄 때
- 완료 후 한 번 더 보여줄 때

### 화면 배치
- 전체 화면 모달
- 닫기 버튼이 있는 전면 영역

### 샘플 흐름
- 웹에서 `format: 'interstitial_video'` 요청
- 네이티브가 전면 동영상 광고를 띄움
- 닫기 / 완료 이벤트를 웹으로 전달

## iOS에서 공통으로 쓰는 상태 메시지

```swift
func sendToWeb(_ webView: WKWebView, json: String) {
    webView.evaluateJavaScript("window.onNativeMessage(\(json))")
}
```

## 핵심 포인트

- 포맷마다 붙는 뷰가 다르다
- 웹은 요청만 보낸다
- 네이티브가 실제 화면을 만든다
- 이벤트는 다시 웹으로 보낸다
