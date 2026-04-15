package com.gwangy.nassspandroidsample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SampleViewModel {
    var uiState by mutableStateOf(SampleUiState())
        private set

    fun selectFormat(format: SampleFormat) {
        uiState = uiState.copy(
            selectedFormat = format,
            message = "${format.title} 준비됨"
        )
    }

    fun markBridgeReady() {
        uiState = uiState.copy(message = "연결 위치 확인 완료")
    }

    fun reportResult(result: String) {
        uiState = uiState.copy(message = result)
    }
}
