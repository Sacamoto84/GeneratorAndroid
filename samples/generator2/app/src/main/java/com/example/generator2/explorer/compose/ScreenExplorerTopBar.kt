package com.example.generator2.explorer.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.generator2.AppPath
import com.example.generator2.R
import com.example.generator2.explorer.ScreenExplorerViewModel

@Composable
fun ScreenExplorerTopBar(vm: ScreenExplorerViewModel) {


    Column {

        var s = vm.explorerCurrentDir.collectAsState().value.substringAfter(AppPath().sdcard)
        if (s == "") s = "/"
        Text(text = s)

        Row (
            Modifier
                .fillMaxWidth()
                .background(Color.Cyan)){

            IconButton(onClick = { vm.up() }) {
                Icon(painter = painterResource(R.drawable.back4), contentDescription = null)
            }

            IconButton(onClick = { vm.explorerCurrentDir.value = AppPath().music }) {
                Icon(painter = painterResource(R.drawable.info), contentDescription = null)
            }

            IconButton(onClick = { vm.explorerCurrentDir.value = AppPath().sdcard }) {
                Icon(painter = painterResource(R.drawable.line3_2), contentDescription = null)
            }

        }

    }



}