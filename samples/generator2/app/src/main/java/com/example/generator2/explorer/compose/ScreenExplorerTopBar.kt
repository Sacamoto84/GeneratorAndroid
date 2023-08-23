package com.example.generator2.explorer.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.generator2.R
import com.example.generator2.explorer.ScreenExplorerViewModel
import com.example.generator2.navController

@Composable
fun ScreenExplorerTopBar(vm: ScreenExplorerViewModel) {

    Row (Modifier.fillMaxWidth().background(Color.Cyan)){

        IconButton(onClick = { navController.navigate("config") }) {
            Icon(painter = painterResource(R.drawable.line3_2), contentDescription = null)
        }

        IconButton(onClick = { navController.navigate("config") }) {
            Icon(painter = painterResource(R.drawable.line3_2), contentDescription = null)
        }

        IconButton(onClick = { navController.navigate("config") }) {
            Icon(painter = painterResource(R.drawable.line3_2), contentDescription = null)
        }

    }

}