package com.example.generator2.features.mp3.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.generator2.features.mp3.PlayerMP3

@Composable
fun Mp3Library(exoplayer: PlayerMP3) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(1.dp, Color.LightGray), verticalArrangement = Arrangement.SpaceBetween
    ) {
        Mp3LibraryList(Modifier.weight(1f))
        Mp3LibraryControl()
    }

}

@Composable
fun Mp3LibraryList(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .then(modifier)
            .background(Color.Gray)
    ) {

    }
}

@Composable
fun Mp3LibraryControl() {
    Row(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(32.dp)
            .border(1.dp, Color.Magenta)
    ) {

    }
}