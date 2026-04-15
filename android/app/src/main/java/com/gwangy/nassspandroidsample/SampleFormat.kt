package com.gwangy.nassspandroidsample

enum class SampleFormat(val title: String, val description: String) {
    Banner("배너", "화면 아래에 붙는 작은 광고"),
    Native("네이티브", "앱 화면에 자연스럽게 섞이는 광고"),
    Video("동영상", "앱 안에서 재생되는 광고"),
    RewardVideo("리워드 동영상", "끝까지 보면 보상이 있는 광고"),
    InterstitialVideo("전면 동영상", "화면 전체를 덮는 광고"),
    HybridWebView("웹뷰 하이브리드", "웹 버튼으로 네이티브 광고를 여는 방식")
}
