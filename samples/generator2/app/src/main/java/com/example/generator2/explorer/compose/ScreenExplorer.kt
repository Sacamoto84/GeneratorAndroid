package com.example.generator2.explorer.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.generator2.explorer.ScreenExplorerViewModel

@Composable
fun ScreenExplorer(vm: ScreenExplorerViewModel = hiltViewModel() )
{
    Column(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {




    }
}



