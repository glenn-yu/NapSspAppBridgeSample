# Android 포맷별 샘플

이 문서는 Android에서 각 광고 포맷을 **어디에 붙이는지** 보여준다.

## 공통 브리지 이름

```kotlin
val bridgeName = "AppBridge"
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
- 화면 맨 아래 고정
- 앱 흐름을 크게 끊지 않음

### 화면 배치
- `ConstraintLayout`이나 `Compose` 하단 영역에 넣는다

### 샘플 흐름
- 웹에서 `format: 'banner'` 요청
- 네이티브가 하단 배너를 띄움
- 클릭 / 노출 이벤트를 웹에 전달

## 2) 네이티브

### 언제 쓰나
- 리스트 중간
- 카드형 UI
- 콘텐츠처럼 섞어서 보이게 할 때

### 화면 배치
- 리사이클러뷰 셀
- 피드 중간
- 상세 상단 카드

### 샘플 흐름
- 웹에서 `format: 'native'` 요청
- 네이티브가 카드 뷰를 만들고 표시
- 버튼 클릭 시 웹에 이벤트 전달

## 3) 동영상

### 언제 쓰나
- 앱 안에 재생 영역이 필요할 때
- 사용자가 직접 누르고 보는 영역

### 화면 배치
- 재생용 컨테이너 뷰
- `VideoAdView`가 들어가는 자리

### 샘플 흐름
- 웹에서 `format: 'video'` 요청
- 네이티브가 동영상 뷰를 생성
- 로딩 성공 / 재생 완료 이벤트를 웹으로 전달

## 4) 리워드 동영상

### 언제 쓰나
- 보상 지급이 있는 흐름
- "광고 보면 보너스 지급" 같은 화면

### 화면 배치
- 보상 버튼 클릭 뒤
- 보상 확인 화면 뒤

### 샘플 흐름
- 웹에서 `format: 'reward'` 요청
- 네이티브가 리워드 광고를 띄움
- `EARNEDREWARD`를 웹으로 보냄

## 5) 전면 동영상

### 언제 쓰나
- 화면 전환 직전
- 작업 완료 직후
- 다음 화면으로 넘어가기 전에 한 번 보여줄 때

### 화면 배치
- 전체 화면 팝업
- 닫기 버튼이 있는 전면 영역

### 샘플 흐름
- 웹에서 `format: 'interstitial_video'` 요청
- 네이티브가 전면 동영상 광고를 띄움
- 닫기 / 완료 이벤트를 웹으로 전달

## Android에서 공통으로 쓰는 상태 메시지

```kotlin
fun sendToWeb(webView: WebView, json: String) {
    webView.evaluateJavascript("window.onNativeMessage($json)", null)
}
```

## 핵심 포인트

- 포맷마다 **붙는 위치**가 다르다
- 웹은 요청만 보낸다
- 네이티브가 실제 뷰를 만든다
- 이벤트는 다시 웹으로 보낸다
