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
    onRunSample: () -> Unit,
    onHookSdk: () -> Unit,
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
                    SampleFormat.Banner -> "화면 아래에 작게 붙는 광고"
                    SampleFormat.Native -> "앱 화면에 자연스럽게 섞이는 광고"
                    SampleFormat.Video -> "앱 안에서 재생되는 광고"
                    SampleFormat.RewardVideo -> "끝까지 보면 보상이 있는 광고"
                    SampleFormat.InterstitialVideo -> "화면 전체를 덮는 광고"
                    SampleFormat.HybridWebView -> "웹페이지 버튼으로 네이티브 광고를 여는 방식"
                }
            )
            Text(SdkHooks.describe(format), color = androidx.compose.ui.graphics.Color(0xFF5F6B7A))
            Button(onClick = onRunSample, modifier = Modifier.fillMaxWidth()) {
                Text("현재 포맷 다시 보기")
            }
            Button(onClick = onHookSdk, modifier = Modifier.fillMaxWidth()) {
                Text("연결 자리 보기")
            }
            Button(onClick = onExecuteSdk, modifier = Modifier.fillMaxWidth()) {
                Text("광고 띄우기")
            }
        }
    }
}
