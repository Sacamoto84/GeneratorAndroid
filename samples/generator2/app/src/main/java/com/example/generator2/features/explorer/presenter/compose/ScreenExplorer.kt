package com.example.generator2.features.explorer.presenter.compose

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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

    LaunchedEffect(key1 = vm.dataRepository.currentNode) {
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
        )
        {
            Spacer(modifier = Modifier.height(2.dp))
            LazyColumn(modifier = Modifier.fillMaxSize(), state = rememberLazyListState()) {
                items(vm.listItems){ item ->
                    ScreenExplorerDrawItem(item) { vm.onClick_DrawItem(item) }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}



