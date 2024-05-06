package com.example.generator2.features.explorer.presenter.compose

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.media3.common.util.UnstableApi
import com.example.generator2.R
import com.example.generator2.features.explorer.model.ExplorerItem
import com.example.generator2.features.explorer.presenter.ScreenExplorerViewModel





@OptIn(UnstableApi::class)
@Composable
fun ScreenExplorerDrawItem(item: ExplorerItem, vm: ScreenExplorerViewModel) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
            .clip(RoundedCornerShape(2.dp))
            .border(1.dp, Color.DarkGray, RoundedCornerShape(2.dp))
            .background(if (item.isDirectory) Color(0xFF006064) else Color(0xFF33313B))
    ) {

        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                vm.onClick_DrawItem(item)
            }
        )
        {

            //Иконка папка и формат текст
            ScreenExplorerDrawItemIcon(item)

            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {



                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = item.name//.substringBeforeLast('.')
                        ,
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    ScreenExplorerDrawCount(item)
                }



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