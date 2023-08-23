package com.example.generator2.explorer.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.generator2.explorer.ExplorerItem
import com.example.generator2.explorer.ScreenExplorerViewModel
import com.example.generator2.theme.colorDarkBackground


@Composable
fun ScreenExplorer(vm: ScreenExplorerViewModel = hiltViewModel() )
{

    LaunchedEffect(key1 = vm.explorerCurrentDir.collectAsState().value){

        vm.scan()



    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.LightGray)) {

        ScreenExplorerTopBar(vm)


        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ){
            vm.listItems.forEach{
                drawItem(it)
            }
        }

        vm.update



    }
}

@Composable
private fun drawItem( item : ExplorerItem)
{
    Text(text = item.name)
}


