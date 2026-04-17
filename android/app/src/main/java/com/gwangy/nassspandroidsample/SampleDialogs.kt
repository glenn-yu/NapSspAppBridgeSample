package com.gwangy.nassspandroidsample

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

private data class TestPreset(
    val title: String,
    val description: String,
    val mediaKey: String? = null,
    val adUnits: Map<String, String> = emptyMap(),
)

private data class KeyDraft(
    val mediaKey: String,
    val adUnits: Map<String, String>,
)

private fun currentKeyDraft(context: Context): KeyDraft = KeyDraft(
    mediaKey = AppConfig.getMediaKey(context).orEmpty(),
    adUnits = mapOf(
        "banner_320x100" to AppConfig.getAdUnit(context, "banner_320x100").orEmpty(),
        "native" to AppConfig.getAdUnit(context, "native").orEmpty(),
        "outstream_video" to AppConfig.getAdUnit(context, "outstream_video").orEmpty(),
        "reward_video" to AppConfig.getAdUnit(context, "reward_video").orEmpty(),
        "interstitial_320x480" to AppConfig.getAdUnit(context, "interstitial_320x480").orEmpty(),
        "interstitial_320x480_f" to AppConfig.getAdUnit(context, "interstitial_320x480_f").orEmpty(),
    ),
)

private fun runtimeDefaults(context: Context): KeyDraft {
    val adUnitIds = NapSspConfig.adUnitIds(context)
    return KeyDraft(
        mediaKey = NapSspConfig.mediaKey(context),
        adUnits = mapOf(
            "banner_320x100" to adUnitIds["banner_320x100"].orEmpty(),
            "native" to adUnitIds["native"].orEmpty(),
            "outstream_video" to adUnitIds["outstream_video"].orEmpty(),
            "reward_video" to adUnitIds["reward_video"].orEmpty(),
            "interstitial_320x480" to adUnitIds["interstitial_320x480"].orEmpty(),
            "interstitial_320x480_f" to adUnitIds["interstitial_320x480_f"].orEmpty(),
        ),
    )
}

private fun testPresets(context: Context): List<TestPreset> {
    val defaults = runtimeDefaults(context)
    return listOf(
        TestPreset(
            title = "Full sample pack",
            description = "가장 무난한 시작값. Configure Keys를 처음 열었을 때 바로 채워 넣기 좋습니다.",
            mediaKey = defaults.mediaKey,
            adUnits = defaults.adUnits,
        ),
        TestPreset(
            title = "Banner only",
            description = "배너가 잘 붙는지 먼저 확인할 때 사용합니다.",
            mediaKey = defaults.mediaKey,
            adUnits = mapOf("banner_320x100" to defaults.adUnits["banner_320x100"].orEmpty()),
        ),
        TestPreset(
            title = "Native only",
            description = "네이티브 렌더링과 레이아웃 바인딩을 빠르게 확인할 때 사용합니다.",
            mediaKey = defaults.mediaKey,
            adUnits = mapOf("native" to defaults.adUnits["native"].orEmpty()),
        ),
        TestPreset(
            title = "Video only",
            description = "동영상 슬롯만 집중 테스트할 때 사용합니다.",
            mediaKey = defaults.mediaKey,
            adUnits = mapOf("outstream_video" to defaults.adUnits["outstream_video"].orEmpty()),
        ),
        TestPreset(
            title = "Reward + interstitial",
            description = "리워드, 전면 동영상, 전면 배너 흐름을 볼 때 사용합니다.",
            mediaKey = defaults.mediaKey,
            adUnits = mapOf(
                "reward_video" to defaults.adUnits["reward_video"].orEmpty(),
                "interstitial_320x480" to defaults.adUnits["interstitial_320x480"].orEmpty(),
                "interstitial_320x480_f" to defaults.adUnits["interstitial_320x480_f"].orEmpty(),
            ),
        ),
    )
}

@Composable
fun ConfigureKeysDialog(
    context: Context,
    onDismiss: () -> Unit,
    onSaved: (mediaKey: String, pairs: Map<String, String>) -> Unit,
) {
    var draft by remember { mutableStateOf(currentKeyDraft(context)) }
    val presets = remember(context) { testPresets(context) }

    fun fillSampleKeys() {
        draft = runtimeDefaults(context)
    }

    fun applyPreset(preset: TestPreset) {
        val updatedUnits = draft.adUnits.toMutableMap()
        preset.adUnits.forEach { (key, value) -> updatedUnits[key] = value }
        draft = draft.copy(
            mediaKey = preset.mediaKey ?: draft.mediaKey,
            adUnits = updatedUnits,
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text("Configure Keys") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "초보자용 빠른 입력",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Button(onClick = ::fillSampleKeys, modifier = Modifier.fillMaxWidth()) {
                    Text("Fill sample keys")
                }
                Text(
                    text = "Test presets",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                presets.forEach { preset ->
                    Button(
                        onClick = { applyPreset(preset) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(preset.title, fontWeight = FontWeight.Bold)
                            Text(preset.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Text(
                    text = "Manual values",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = draft.mediaKey,
                    onValueChange = { draft = draft.copy(mediaKey = it) },
                    label = { Text("MEDIA_KEY") },
                    modifier = Modifier.fillMaxWidth(),
                )
                listOf(
                    "banner_320x100",
                    "native",
                    "outstream_video",
                    "reward_video",
                    "interstitial_320x480",
                    "interstitial_320x480_f",
                ).forEach { key ->
                    OutlinedTextField(
                        value = draft.adUnits[key].orEmpty(),
                        onValueChange = { value ->
                            draft = draft.copy(adUnits = draft.adUnits.toMutableMap().apply { put(key, value) })
                        },
                        label = { Text(key) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSaved(draft.mediaKey, draft.adUnits)
                },
            ) { Text("저장") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}

@Composable
fun SampleSettingsDialog(
    onDismiss: () -> Unit,
    onOpenConfigureKeys: () -> Unit,
    onOpenLogViewer: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text("Sample settings") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("초보자용 바로가기")
                Button(onClick = onOpenConfigureKeys, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Configure Keys")
                }
                Button(onClick = onOpenLogViewer, modifier = Modifier.fillMaxWidth()) {
                    Text("View recent logs")
                }
                Text(
                    "Configure Keys에서 sample presets를 채우고, 로그 뷰어에서 최근 50줄의 이벤트를 확인할 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("닫기") }
        },
    )
}

@Composable
fun LogViewerDialog(
    lines: List<String>,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text("Recent logs") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("최근 50줄", style = MaterialTheme.typography.bodyMedium)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 440.dp),
                    tonalElevation = 1.dp,
                ) {
                    if (lines.isEmpty()) {
                        Text(
                            text = "No log entries yet.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(lines) { line ->
                                Text(
                                    text = line,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("닫기") }
        },
        dismissButton = {
            TextButton(onClick = onRefresh) { Text("Refresh") }
        },
    )
}
