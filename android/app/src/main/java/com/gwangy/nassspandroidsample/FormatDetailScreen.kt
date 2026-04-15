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
) {
    Card(colors = CardDefaults.cardColors()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(format.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(format.description)
            Text(
                text = when (format) {
                    SampleFormat.Banner -> "화면 하단에 배너 뷰를 붙이는 자리"
                    SampleFormat.Native -> "카드형 레이아웃에 네이티브 자산을 꽂는 자리"
                    SampleFormat.Video -> "재생용 컨테이너를 붙이는 자리"
                    SampleFormat.RewardVideo -> "보상 시청 후 EARNEDREWARD를 받는 자리"
                    SampleFormat.InterstitialVideo -> "전체 화면 전면 동영상을 띄우는 자리"
                    SampleFormat.HybridWebView -> "WebView 안에서 네이티브 브리지를 타는 자리"
                }
            )
            Text(SdkHooks.describe(format))
            Button(onClick = onRunSample, modifier = Modifier.fillMaxWidth()) {
                Text("샘플 상태 갱신")
            }
            Button(onClick = onHookSdk, modifier = Modifier.fillMaxWidth()) {
                Text("SDK 연결 위치 표시")
            }
        }
    }
}
