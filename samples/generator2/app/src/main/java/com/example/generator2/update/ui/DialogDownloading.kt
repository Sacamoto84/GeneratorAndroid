package com.example.generator2.update.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.generator2.update.Update

//@Preview(device = "spec:width=411dp,height=891dp", apiLevel = 33)
//@Composable
//fun previewDialogDownloading()
//{
//    DialogDownloading()
//}
//
//
//@Composable
//fun DialogDownloading() {
//
//    val dialogOpen = Update.isDownloading.collectAsState()
//
//    if (dialogOpen.value) {
//        Dialog(
//            onDismissRequest = { Update.isDownloading.value = false },
//            properties = DialogProperties(
//                dismissOnClickOutside = false
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
//                    Text(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 8.dp, start = 0.dp),
//                        text = "Закачка", color = Color.White,
//                        fontSize = 18.sp, textAlign = TextAlign.Center
//
//                    )
//
//                    val p = Update.percent.collectAsState()
//
//                    Text(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 8.dp, start = 0.dp),
//                        text = String.format("%.0f",p.value * 100) + "% ${Update.downloadedByte}/${Update.downloadSize}", color = Color.White,
//                        fontSize = 18.sp, textAlign = TextAlign.Center
//
//                    )
//
//                    LinearProgressIndicator(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(start = 16.dp, end = 16.dp, top = 16.dp), progress = p.value
//                    )
//
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 8.dp),
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//
//                        TextButton(
//                            //modifier = Modifier.weight(1f)
//                            //,
//                            onClick = { Update.isDownloading.value =  false }) {
//                            Text("Отмена", fontSize = 18.sp)
//                        }
//
//                    }
//                }
//            }
//        }
//    }
//}