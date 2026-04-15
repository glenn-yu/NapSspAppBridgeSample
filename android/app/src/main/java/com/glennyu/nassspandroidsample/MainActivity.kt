package com.glennyu.nassspandroidsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    var message by remember { mutableStateOf("포맷을 선택해 주세요") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("nap ssp Android Native Sample")
        Text(message)

        Button(onClick = { message = "배너 샘플을 여는 자리" }) {
            Text("배너")
        }
        Button(onClick = { message = "네이티브 샘플을 여는 자리" }) {
            Text("네이티브")
        }
        Button(onClick = { message = "동영상 샘플을 여는 자리" }) {
            Text("동영상")
        }
        Button(onClick = { message = "리워드 동영상 샘플을 여는 자리" }) {
            Text("리워드 동영상")
        }
        Button(onClick = { message = "전면 동영상 샘플을 여는 자리" }) {
            Text("전면 동영상")
        }
    }
}
