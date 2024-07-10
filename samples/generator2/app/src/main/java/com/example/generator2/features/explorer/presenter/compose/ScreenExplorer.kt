package com.example.generator2.features.explorer.presenter.compose

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.generator2.features.explorer.presenter.ScreenExplorerViewModel

@Suppress("NonSkippableComposable")
@androidx.media3.common.util.UnstableApi
@Composable
fun ScreenExplorer(vm: ScreenExplorerViewModel) {

    LaunchedEffect(key1 = vm.currentNode.collectAsState().value) {
        vm.scanNode()
    }

    Scaffold(
        backgroundColor = Color.Black,
        bottomBar = {
            ScreenExplorerBottomBar(vm)
        }
    ) {

        Column(
            Modifier
                .padding(
                    bottom = it.calculateBottomPadding() + 0.dp,
                    top = 1.dp,
                    start = 0.dp,
                    end = 0.dp
                )
                .fillMaxSize()
                .border(1.dp, Color(0xFF313A42), RoundedCornerShape(4.dp))
                //.clip(RoundedCornerShape(4.dp))
                //.padding(8.dp)
                //.background(Color(0xFF1D2428))
                .verticalScroll(rememberScrollState())
        )
        {
            Spacer(modifier = Modifier.height(2.dp))
            vm.listItems.forEach { item ->
                ScreenExplorerDrawItem(item, vm)
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        vm.update
    }

}



