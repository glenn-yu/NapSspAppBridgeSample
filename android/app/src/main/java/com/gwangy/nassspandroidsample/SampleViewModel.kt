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
            message = if (format == SampleFormat.HybridWebView) {
                "HybridWebView에서는 먼저 init, 그 다음 광고 버튼을 눌러 주세요"
            } else {
                "${format.title} 준비됨, 처음이면 배너나 네이티브부터 확인해 주세요"
            }
        )
    }

    fun markBridgeReady() {
        uiState = uiState.copy(message = "연결 위치 확인 완료")
    }

    fun reportResult(result: String) {
        uiState = uiState.copy(message = result)
    }
}
