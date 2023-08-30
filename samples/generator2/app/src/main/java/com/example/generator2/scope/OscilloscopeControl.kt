package com.example.generator2.scope

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.generator2.mp3.OSCILLSYNC
import com.example.generator2.mp3.oscillSync


@Composable
fun OscilloscopeControl() {

    //val context = LocalContext.current
    val h = 32.dp
    val w = 64.dp

    Row {

        Box(modifier = Modifier
            .height(32.dp)
            .width(32.dp)
            .border(1.dp, Color.Gray)
            .background(Color.Black)
            .clickable(onClick = { compressorCount.floatValue *= 2 }),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "+", color = Color.Gray)
        }

        Text(text = "${compressorCount.floatValue}", color = Color.White)
        Button(onClick = { compressorCount.floatValue /= 2 }) {

        }




        Button(onClick = { oscillSync.value = OSCILLSYNC.NONE }) {
            Text(text = "N")
        }
        Button(onClick = { oscillSync.value = OSCILLSYNC.R }) {
            Text(text = "R")
        }
        Button(onClick = { oscillSync.value = OSCILLSYNC.L }) {
            Text(text = "L")
        }


    }
}