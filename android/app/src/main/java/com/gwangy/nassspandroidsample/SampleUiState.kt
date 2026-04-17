package com.gwangy.nassspandroidsample

data class SampleUiState(
    val selectedFormat: SampleFormat = SampleFormat.Banner,
    val message: String = "처음이면 Configure Keys 확인 후 배너 또는 네이티브부터 테스트해 주세요"
)
