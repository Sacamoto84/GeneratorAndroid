package com.example.generator2.mp3.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.generator2.mp3.PlayerMP3

@Composable
fun Mp3Library(exoplayer : PlayerMP3)
{
  Column(modifier = Modifier.fillMaxWidth()) {
      Mp3LibraryList()
      Mp3LibraryControl()
  }

}

@Composable
fun Mp3LibraryList()
{



}

@Composable
fun Mp3LibraryControl()
{



}