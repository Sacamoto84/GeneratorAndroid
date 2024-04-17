package com.example.generator2.features.update.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.generator2.R
import com.example.generator2.features.update.Update

//@Composable
//fun DialogNewVersion() {
//
//    val dialogOpen = Update.visibleDialogNew.collectAsState()
//
//    if (dialogOpen.value) {
//        Dialog(
//            onDismissRequest = { Update.visibleDialogNew.value = false },
//            properties = DialogProperties(
//                dismissOnClickOutside = true
//            )
//        )
//        {
//            Surface(
//                modifier = Modifier
//                    .fillMaxWidth()
//                //.wrapContentHeight()
//                ,
//                shape = RoundedCornerShape(size = 10.dp),
//                color = Color(0xFF343434)
//            ) {
//                Column(modifier = Modifier.padding(all = 4.dp)) {
//
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.background(Color.Transparent)
//                    )
//                    {
//                        Icon(
//                            modifier = Modifier
//                                .size(28.dp)
//                                .padding(start = 4.dp),
//                            painter = painterResource(R.drawable.download),
//                            contentDescription = "",
//                            tint = Color.White
//                        )
//
//                        Text(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 8.dp, start = 8.dp)
//                                .offset(0.dp, (-4).dp),
//                            text = "Была найдена новая версия", color = Color.White,
//                            fontSize = 18.sp
//                        )
//                    }
//
//                    Text(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 8.dp, start = 8.dp),
//                        text = "Обновить с версии ${Update.currentVersion} до ${Update.externalVersion}", color = Color.LightGray,
//                        fontSize = 16.sp
//                    )
//
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 8.dp),
//                        horizontalArrangement = Arrangement.SpaceAround
//                    ) {
//
//                        TextButton(
//                            //modifier = Modifier.weight(1f)
//                            //,
//                            onClick = { Update.visibleDialogNew.value = false }) {
//                            Text("Отмена", fontSize = 18.sp)
//                        }
//
//                        TextButton(
//                            //modifier = Modifier.weight(1f)
//                            //,
//                            onClick = {
//                                Update.visibleDialogNew.value = false
//                                Update.isDownloading.value = true
//                            }) {
//                            Text("Установить", fontSize = 18.sp)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//}

