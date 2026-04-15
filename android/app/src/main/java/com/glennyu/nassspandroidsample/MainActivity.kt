package com.glennyu.nassspandroidsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SampleScreen()
                }
            }
        }
    }
}

@Composable
private fun SampleScreen() {
    val viewModel = remember { SampleViewModel() }
    val uiState = viewModel.uiState

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("nap ssp Android Native Sample", fontWeight = FontWeight.Bold)
            Text("포맷을 고르고, 실제 nap ssp SDK 코드를 꽂을 수 있는 구조")
            Text(uiState.message)
        }

        item {
            FormatDetailScreen(
                format = uiState.selectedFormat,
                onRunSample = { viewModel.selectFormat(uiState.selectedFormat) },
                onHookSdk = { viewModel.markBridgeReady() }
            )
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
                    Text(format.title)
                    Text(format.description)
                }
            }
        }
    }
}
