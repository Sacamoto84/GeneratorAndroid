package com.example.generator2.explorer.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.generator2.R
import com.example.generator2.explorer.ExplorerItem
import com.example.generator2.explorer.ScreenExplorerViewModel


@Composable
fun ScreenExplorer(vm: ScreenExplorerViewModel = hiltViewModel()) {

    LaunchedEffect(key1 = vm.explorerCurrentDir.collectAsState().value) {

        vm.scan()


    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B1B25))
    ) {

        ScreenExplorerTopBar(vm)


        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            vm.listItems.forEach {
                DrawItem(it, vm)
            }
        }

        vm.update


    }
}
@androidx.media3.common.util.UnstableApi
@Composable
private fun DrawItem(item: ExplorerItem, vm: ScreenExplorerViewModel) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 4.dp, end = 4.dp)
            //.border(1.dp, Color.Black, RoundedCornerShape(4.dp))
            .background(if (item.isDirectory) Color(0xFF4B6F7D) else Color(0xFF33313B))
    ) {

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {

            if (item.isDirectory)
                vm.explorerCurrentDir.value += "/" + item.name
else
            {
                val s = vm.explorerCurrentDir.value + "/" + item.name
                vm.play(s)
            }

        }


        )
        {


            if (item.isDirectory) {

                Icon(
                    painter = painterResource(R.drawable.folder_open2),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )

            } else {

                Box(
                    modifier = Modifier
                        //.size(48.dp)
                        //.background(Color(0xFF8BB7F0))
                    ,
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = item.isFormat,
                        modifier = Modifier.padding(start=4.dp)
                            //.border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                        ,
                        fontFamily = FontFamily(Font(R.font.bayon_regular)),
                        fontSize = 20.sp, textAlign = TextAlign.Center, color = Color.White
                    )

                }

            }

            Column(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .weight(1f))
            {
                Text(text = item.name.substringBeforeLast('.'), fontSize = 18.sp, color = Color.White)

                if (item.isFormat.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = item.lengthInSeconds,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(64.dp), color = Color.White
                        )
                        Text(text = item.channelMode, color = Color.White)
                        Text(text = item.sampleRate, color = Color.White)
                    }
                }
            }

//            Box(
//                modifier = Modifier
//                    .size(24.dp)
//                    .background(Color(0xFF8BB7F0)),
//                contentAlignment = Alignment.Center
//            ) {
//
//
//            }
//
//            Box(
//                modifier = Modifier
//                    .size(24.dp)
//                    .background(Color(0xFF8BB7F0)),
//                contentAlignment = Alignment.Center
//            ) {
//
//
//            }



        }


    }

}


