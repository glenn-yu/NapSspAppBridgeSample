package com.gwangy.nassspandroidsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FormatDetailScreen(
    format: SampleFormat,
    onExecuteSdk: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFF8F9FB))) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(format.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(format.description)
            Text(
                text = when (format) {
                    SampleFormat.Banner -> "아래에 붙는 작은 광고"
                    SampleFormat.Native -> "화면에 섞는 광고"
                    SampleFormat.Video -> "앱 안에서 재생되는 광고"
                    SampleFormat.RewardVideo -> "보상이 있는 광고"
                    SampleFormat.InterstitialVideo -> "전체 화면 광고"
                    SampleFormat.HybridWebView -> "웹 버튼으로 여는 광고"
                }
            )
            Text(
                text = if (format == SampleFormat.HybridWebView) {
                    "하이브리드 화면에서는 아래 웹뷰에서 init 후 광고 버튼을 누르세요."
                } else {
                    "처음 테스트라면 배너 또는 네이티브부터 확인하는 것을 권장합니다."
                }
            )
            Button(onClick = onExecuteSdk, modifier = Modifier.fillMaxWidth()) {
                Text(if (format == SampleFormat.HybridWebView) "하이브리드 화면 열기" else "광고 띄우기")
            }
        }
    }
}
