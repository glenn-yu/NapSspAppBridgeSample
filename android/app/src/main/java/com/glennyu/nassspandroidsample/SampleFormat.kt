package com.glennyu.nassspandroidsample

enum class SampleFormat(val title: String, val description: String) {
    Banner("배너", "화면 아래 고정형 샘플"),
    Native("네이티브", "카드형 UI에 섞는 샘플"),
    Video("동영상", "앱 안 재생 영역 샘플"),
    RewardVideo("리워드 동영상", "시청 완료 보상 샘플"),
    InterstitialVideo("전면 동영상", "전체 화면 노출 샘플"),
    HybridWebView("웹뷰 하이브리드", "WebView 안에 광고 브릿지를 붙이는 샘플")
}
