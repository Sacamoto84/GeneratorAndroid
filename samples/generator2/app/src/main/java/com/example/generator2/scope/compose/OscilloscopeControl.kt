package com.example.generator2.scope.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.generator2.mp3.OSCILLSYNC
import com.example.generator2.mp3.oscillSync


private val m = Modifier
    .height(32.dp)
    .width(32.dp)
    //.border(1.dp, Color.Gray)
    //.background(Color.Black)

private val colorEnabled = Color.Black
private val colorTextEnabled = Color.Green
private val colorTextDisabled = Color.Gray

@Composable
fun OscilloscopeControl() {

    val a = 8.dp

    Row {

        Box(
            modifier = m.clip(RoundedCornerShape(topStart = a, bottomStart = a)).border(1.dp, Color.Gray, RoundedCornerShape(topStart = a, bottomStart = a))
                .clickable(onClick = { oscillSync.value = OSCILLSYNC.NONE })
                .background(if (oscillSync.value == OSCILLSYNC.NONE) colorEnabled else Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "N",
                color = if (oscillSync.value == OSCILLSYNC.NONE) colorTextEnabled else colorTextDisabled
            )
        }

        Box(
            modifier = m.border(1.dp, Color.Gray)
                .clickable(onClick = { oscillSync.value = OSCILLSYNC.L })
                .background(if (oscillSync.value == OSCILLSYNC.L) colorEnabled else Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "L",
                color = if (oscillSync.value == OSCILLSYNC.L) colorTextEnabled else colorTextDisabled
            )
        }

        Box(
            modifier = m.clip(RoundedCornerShape(topEnd = a, bottomEnd = a)).border(1.dp, Color.Gray, RoundedCornerShape(topEnd = a, bottomEnd = a))
                .clickable(onClick = { oscillSync.value = OSCILLSYNC.R })
                .background(if (oscillSync.value == OSCILLSYNC.R) colorEnabled else Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "R",
                color = if (oscillSync.value == OSCILLSYNC.R) colorTextEnabled else colorTextDisabled
            )
        }

    }
}