package com.gwangy.nassspandroidsample

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContextHolder.appContext = applicationContext
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SampleScreen(this@MainActivity)
                }
            }
        }
    }
}

@Composable
private fun SampleScreen(activity: MainActivity) {
    val viewModel = remember { SampleViewModel() }
    val uiState = viewModel.uiState
    var adView by remember { mutableStateOf<View?>(null) }

    LaunchedEffect(uiState.selectedFormat) {
        adView = when (uiState.selectedFormat) {
            SampleFormat.Banner -> NapSspSdkIntegration.bannerView(activity)
            SampleFormat.Native -> NapSspSdkIntegration.nativeView(activity)
            SampleFormat.Video -> NapSspSdkIntegration.videoView(activity)
            SampleFormat.RewardVideo -> NapSspSdkIntegration.rewardVideoView(activity)
            SampleFormat.InterstitialVideo -> NapSspSdkIntegration.interstitialVideoView(activity)
            SampleFormat.HybridWebView -> null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AdDemoScreen(
                title = "nap ssp Android 샘플",
                subtitle = "1) 포맷 선택 → 2) 광고 코드 실행 → 3) 화면에서 결과 확인"
            )
        }

        item {
            Text("현재 상태", fontWeight = FontWeight.SemiBold)
            Text(uiState.message)
        }

        item {
            FormatDetailScreen(
                format = uiState.selectedFormat,
                onRunSample = { viewModel.selectFormat(uiState.selectedFormat) },
                onHookSdk = { viewModel.markBridgeReady() },
                onExecuteSdk = {
                    val result = when (uiState.selectedFormat) {
                        SampleFormat.Banner -> NapSspSdkIntegration.bannerView(activity)
                        SampleFormat.Native -> NapSspSdkIntegration.nativeView(activity)
                        SampleFormat.Video -> NapSspSdkIntegration.videoView(activity)
                        SampleFormat.RewardVideo -> NapSspSdkIntegration.rewardVideoView(activity)
                        SampleFormat.InterstitialVideo -> NapSspSdkIntegration.interstitialVideoView(activity)
                        SampleFormat.HybridWebView -> null
                    }
                    val message = if (uiState.selectedFormat == SampleFormat.HybridWebView) {
                        "하이브리드 WebView는 init 후 웹 버튼으로 광고를 부른다"
                    } else if (result != null) {
                        "${uiState.selectedFormat.title} 광고 뷰 연결 완료"
                    } else {
                        "${uiState.selectedFormat.title} 광고 연결 실패 또는 폴백"
                    }
                    viewModel.reportResult(message)
                    println("NapSsp Android result: $result")
                }
            )
        }

        item {
            if (uiState.selectedFormat == SampleFormat.HybridWebView) {
                Text("웹뷰 하이브리드 미리보기", fontWeight = FontWeight.SemiBold)
                Text("웹 버튼을 누르면 네이티브 SDK가 반응한다")
                HybridWebViewScreen(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                )
            } else {
                if (adView != null) {
                    Text("SDK 뷰가 화면에 붙은 상태", fontWeight = FontWeight.SemiBold)
                    AndroidView(
                        factory = { adView!! },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    )
                } else {
                    AdDemoScreen(
                        title = uiState.selectedFormat.title,
                        subtitle = "NapSsp Android SDK 결과 화면"
                    )
                }
            }
        }

        item {
            Text("포맷 목록", fontWeight = FontWeight.SemiBold)
        }

        items(SampleFormat.entries.toList()) { format ->
            Button(
                onClick = { viewModel.selectFormat(format) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(format.title, fontWeight = FontWeight.Bold)
                    Text(format.description)
                }
            }
        }
    }
}
