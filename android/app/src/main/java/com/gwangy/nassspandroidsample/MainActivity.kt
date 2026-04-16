package com.gwangy.nassspandroidsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 애플리케이션 컨텍스트 조기 할당
        AppContextHolder.appContext = applicationContext
        
        // 윈도우 인셋 최적화 (시스템 바 아이콘 로딩 이슈 방어)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HybridWebViewScreen()
                }
            }
        }
    }
}

@Composable
private fun SampleScreen(activity: MainActivity) {
    val viewModel = remember { SampleViewModel() }
    val uiState = viewModel.uiState

    // (counter, format) — incrementing counter forces key() to create a fresh AndroidView
    // even if the same format is requested twice in a row
    var slotCounter by remember { mutableStateOf(0) }
    var adSlot by remember { mutableStateOf<Pair<Int, SampleFormat>?>(null) }

    // Wire SDK events → ViewModel so the user sees load / fail / click feedback in the UI
    DisposableEffect(Unit) {
        NapSspSdkIntegration.onAdEventCallback = { event, format, detail ->
            val msg = when (event) {
                "loaded"    -> "$format 광고 로드 완료"
                "failed"    -> "$format 실패: $detail"
                "displayed" -> "$format 노출"
                "clicked"   -> "$format 클릭"
                "rewarded"  -> "리워드 획득!"
                "completed" -> "$format 재생 완료"
                "closed"    -> "$format 닫힘"
                else        -> "$format $event"
            }
            viewModel.reportResult(msg)
        }
        onDispose { NapSspSdkIntegration.onAdEventCallback = null }
    }

    @Composable
    fun ConfigureKeysDialog(
        onDismiss: () -> Unit,
        onSaved: (mediaKey: String, pairs: List<Pair<String,String>>) -> Unit
    ) {
        var mediaKey by remember { mutableStateOf(AppConfig.getMediaKey(activity) ?: "") }
        var banner by remember { mutableStateOf(AppConfig.getAdUnit(activity, "banner_320x100") ?: "") }
        var nativeId by remember { mutableStateOf(AppConfig.getAdUnit(activity, "native") ?: "") }
        var videoId by remember { mutableStateOf(AppConfig.getAdUnit(activity, "outstream_video") ?: "") }

        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = { onSaved(mediaKey, listOf("banner_320x100" to banner, "native" to nativeId, "outstream_video" to videoId)) }) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("취소") }
            },
            title = { Text("Configure Keys") },
            text = {
                Column {
                    OutlinedTextField(value = mediaKey, onValueChange = { mediaKey = it }, label = { Text("MEDIA_KEY") })
                    OutlinedTextField(value = banner, onValueChange = { banner = it }, label = { Text("banner_320x100") })
                    OutlinedTextField(value = nativeId, onValueChange = { nativeId = it }, label = { Text("native") })
                    OutlinedTextField(value = videoId, onValueChange = { videoId = it }, label = { Text("outstream_video") })
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        )
    }

    var showConfig by remember { mutableStateOf(false) }

    if (showConfig) {
        ConfigureKeysDialog(onDismiss = { showConfig = false }, onSaved = { mediaKey, pairs ->
            // save media key and ad units
            AppConfig.setMediaKey(activity, mediaKey)
            pairs.forEach { (k,v) -> AppConfig.setAdUnit(activity, k, v) }
            viewModel.reportResult("설정 저장됨")
            showConfig = false
        })
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
                subtitle = "포맷을 고르고 광고를 눌러보세요"
            )
            Button(onClick = { showConfig = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Configure Keys")
            }
        }

        item {
            Text(uiState.message)
        }

        item {
            FormatDetailScreen(
                format = uiState.selectedFormat,
                onExecuteSdk = {
                    when (uiState.selectedFormat) {
                        SampleFormat.RewardVideo -> {
                            NapSspSdkIntegration.rewardVideoView(activity)
                            viewModel.reportResult("리워드 영상 요청 중…")
                        }
                        SampleFormat.InterstitialVideo -> {
                            NapSspSdkIntegration.interstitialVideoView(activity)
                            viewModel.reportResult("전면 영상 요청 중…")
                        }
                        SampleFormat.HybridWebView -> {
                            viewModel.reportResult("웹뷰: init 후 웹 버튼으로 광고를 불러주세요")
                        }
                        else -> {
                            // increment counter so key() forces a brand-new AndroidView
                            slotCounter++
                            adSlot = slotCounter to uiState.selectedFormat
                            viewModel.reportResult("${uiState.selectedFormat.title} 광고 요청 중…")
                        }
                    }
                }
            )
        }

        item {
            if (uiState.selectedFormat == SampleFormat.HybridWebView) {
                Text("웹뷰 하이브리드", fontWeight = FontWeight.SemiBold)
                Text("먼저 init, 그다음 광고 버튼")
                HybridWebViewScreen(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                )
            } else {
                val slot = adSlot
                // Only show the ad slot if it belongs to the currently selected format
                if (slot != null && slot.second == uiState.selectedFormat) {
                    // Light-grey background makes the slot visible even before ad content fills
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEAF0F6))
                    ) {
                        // key() recreates the AndroidView (and thus the SDK View) on every new slot
                        key(slot) {
                            AndroidView(
                                factory = { _ ->
                                    // View is created here, inside the Compose hierarchy
                                    when (slot.second) {
                                        SampleFormat.Banner -> NapSspSdkIntegration.bannerView(activity)
                                        SampleFormat.Native -> NapSspSdkIntegration.nativeView(activity)
                                        SampleFormat.Video  -> NapSspSdkIntegration.videoView(activity)
                                        else -> null
                                    } ?: android.view.View(activity)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 50.dp, max = 400.dp)
                            )
                        }
                    }
                } else {
                    AdDemoScreen(
                        title = uiState.selectedFormat.title,
                        subtitle = "광고 띄우기 버튼을 눌러주세요"
                    )
                }
            }
        }

        item {
            Text("포맷 목록", fontWeight = FontWeight.SemiBold)
        }

        items(SampleFormat.entries.toList()) { format ->
            Button(
                onClick = {
                    adSlot = null  // clear ad slot when switching format
                    viewModel.selectFormat(format)
                },
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
