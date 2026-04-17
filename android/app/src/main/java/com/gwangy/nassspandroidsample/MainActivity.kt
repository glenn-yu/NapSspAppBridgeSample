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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.gwangy.nassspandroidsample.bridge.NapSspSdkIntegration

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
                    SampleScreen(this)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SampleScreen(activity: MainActivity) {
    val viewModel = remember { SampleViewModel() }
    var showConfig by remember { mutableStateOf(false) }

    // Wire SDK events
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

    if (showConfig) {
        ConfigureKeysDialog(onDismiss = { showConfig = false }, onSaved = { mediaKey, pairs ->
            // save media key and ad units
            AppConfig.setMediaKey(activity, mediaKey)
            pairs.forEach { (k,v) -> AppConfig.setAdUnit(activity, k, v) }
            viewModel.reportResult("설정 저장됨")
            showConfig = false
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NapSsp Hybrid Bridge", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { showConfig = true }) {
                        Text("Configure Keys")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    ) { innerPadding ->
        HybridWebViewScreen(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}
