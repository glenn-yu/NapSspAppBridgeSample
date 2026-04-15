package com.glennyu.nassspandroidsample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SampleViewModel {
    var uiState by mutableStateOf(SampleUiState())
        private set

    fun selectFormat(format: SampleFormat) {
        uiState = uiState.copy(
            selectedFormat = format,
            message = "${format.title} 샘플을 여는 자리"
        )
    }

    fun markBridgeReady() {
        uiState = uiState.copy(message = "실제 nap ssp SDK 연결 지점을 여기에 붙인다")
    }
}
