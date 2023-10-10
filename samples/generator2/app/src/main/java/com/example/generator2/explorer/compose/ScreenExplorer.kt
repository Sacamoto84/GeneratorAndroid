package com.example.generator2.explorer.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.generator2.explorer.model.ExplorerItem
import com.example.generator2.explorer.viewmodel.ScreenExplorerViewModel

@androidx.media3.common.util.UnstableApi
@Composable
fun ScreenExplorer(vm: ScreenExplorerViewModel = hiltViewModel()) {

    LaunchedEffect(key1 = vm.currentDir.collectAsState().value) {
        vm.scan()
    }

    Scaffold(
        backgroundColor = Color.Black,
        bottomBar = {
            ScreenExplorerTopBar(vm)
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
                DrawItem(item, vm)
            }
            Spacer(modifier = Modifier.height(2.dp))
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
            .padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
            .clip(RoundedCornerShape(2.dp))
            .border(1.dp, Color.DarkGray, RoundedCornerShape(2.dp))
            .background(if (item.isDirectory) Color(0xFF2D353D) else Color(0xFF33313B))
    ) {

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {

            if (item.isDirectory) {
                if (item.name == """...""")
                    vm.up()
                else
                    vm.currentDir.value += "/" + item.name
            } else {
                val s = vm.currentDir.value + "/" + item.name
                vm.play(s)
            }

        }
        )
        {


            //Иконка
            if (item.isDirectory) {

                if (item.name != """...""")
                    Icon(
                        painter = painterResource(R.drawable.folder_open2),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .size(32.dp),
                        tint = Color.White
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
                        modifier = Modifier.padding(start = 4.dp)
                        //.border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                        ,
                        fontFamily = FontFamily(Font(R.font.bayon_regular)),
                        fontSize = 20.sp, textAlign = TextAlign.Center, color = Color.White
                    )

                }

            }

            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                Text(
                    text = item.name.substringBeforeLast('.'),
                    fontSize = 18.sp,
                    color = Color.White
                )

                if (item.isFormat.isNotEmpty()) {

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth()
                    ) {

                        //Время файла
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Icon(
                                painter = painterResource(R.drawable.player_clock),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.LightGray
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = item.lengthInSeconds,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(64.dp), color = Color(0xFFCDDC39)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.channelMode == "Mono")
                                Icon(
                                    painter = painterResource(R.drawable.player_mono),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color.LightGray
                                )
                            else
                                Icon(
                                    painter = painterResource(R.drawable.player_stereo),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color.LightGray
                                )

                            Text(text = item.channelMode, color = Color.White)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.player_samplerate2),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.LightGray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = item.sampleRate, color = Color.White)
                        }
                    }
                }
            }

        }


    }

}


