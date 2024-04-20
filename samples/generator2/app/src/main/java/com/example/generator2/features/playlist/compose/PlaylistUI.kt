package com.example.generator2.features.playlist.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.generator2.features.playlist.VMPlayList

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PlaylistUI(vm: VMPlayList = hiltViewModel()) {

    Scaffold(
        topBar = {
            Text(
                text = "Плейлисты",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center, fontSize = 24.sp
            )
            Divider(color = Color.Black)
        },
        bottomBar = { BottomBar(vm) },
        modifier = Modifier.background(Color.DarkGray)
    ) {


        LazyColumn(
            modifier = Modifier.padding(
                top = it.calculateTopPadding(),
                bottom = it.calculateBottomPadding()
            )
        ) {

            items(vm.playlist.list) { playlist ->
                Text(text = playlist.playlistName, modifier = Modifier.background(Color.Magenta))
            }


            items(vm.playlist.list) { playlist ->
                Text(text = "Добавить", modifier = Modifier.background(Color.Magenta))
            }


        }

    }
}


@Composable
private fun BottomBar(vm: VMPlayList) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.Blue),
        verticalAlignment = Alignment.CenterVertically
    ) {


    }

}


