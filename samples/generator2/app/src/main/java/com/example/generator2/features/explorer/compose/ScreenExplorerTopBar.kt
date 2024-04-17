package com.example.generator2.features.update.explorer.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.generator2.AppPath
import com.example.generator2.R
import com.example.generator2.features.explorer.viewmodel.ScreenExplorerViewModel
import com.example.generator2.navController



@androidx.media3.common.util.UnstableApi
@Composable
fun ScreenExplorerTopBar(vm: ScreenExplorerViewModel) {

    Column {

        var s = vm.currentDir.collectAsState().value.substringAfter(vm.appPath.sdcard)
        if (s == "") s = "/"

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 1.dp)
                .border(1.dp, Color(0xFF313A42), RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1D2428))
                .padding(start = 4.dp),
            text = s, color = Color(0xFF9BA7B8)
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 1.dp)
                .border(1.dp, Color(0xFF313A42), RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF2D353D))
        ) {

            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    painter = painterResource(R.drawable.back4),
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }

            IconButton(onClick = { vm.up() }) {
                Icon(
                    painter = painterResource(R.drawable.player_up),
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }

            IconButton(onClick = { vm.currentDir.value = vm.appPath.music }) {
                Icon(
                    painter = painterResource(R.drawable.player_home),
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }

            IconButton(onClick = { vm.currentDir.value = vm.appPath.sdcard }) {
                Icon(
                    painter = painterResource(R.drawable.player_memory1),
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }

        }

    }


}