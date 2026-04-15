package com.gwangy.nassspandroidsample

data class SampleUiState(
    val selectedFormat: SampleFormat = SampleFormat.Banner,
    val message: String = "포맷을 선택해 주세요"
)
