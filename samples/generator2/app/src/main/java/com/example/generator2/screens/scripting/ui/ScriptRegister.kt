package com.example.generator2.screens.scripting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.screens.scripting.vm.VMScripting


//Блок регистров
@Composable
fun RegisterViewDraw(global: VMScripting) {
//    Box(
//        modifier = Modifier
//            .padding(start = 6.dp, end = 6.dp)
//            .fillMaxWidth()
//    ) {
//        Column(
//            Modifier.height(50.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//
//                ComposeBoxForF(
//                    0,
//                    modifier = Modifier.weight(1f),
//                    text = { global.script.f[0].toString() })
//                ComposeBoxForF(
//                    1,
//                    modifier = Modifier.weight(1f),
//                    text = { global.script.f[1].toString() })
//                ComposeBoxForF(
//                    2,
//                    modifier = Modifier.weight(1f),
//                    text = { global.script.f[2].toString() })
//                ComposeBoxForF(
//                    3,
//                    modifier = Modifier.weight(1f),
//                    text = { global.script.f[3].toString() })
//                ComposeBoxForF(
//                    4,
//                    modifier = Modifier.weight(1f),
//                    text = { global.script.f[4].toString() })
//
//
//            }
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//
//                ComposeBoxForF(
//                    5,
//                    modifier = Modifier.weight(1f),
//                    text = { global.script.f[5].toString() })
//                ComposeBoxForF(
//                    6,
//                    modifier = Modifier.weight(1f),
//                    text = { global.script.f[6].toString() })
//                ComposeBoxForF(
//                    7,
//                    modifier = Modifier.weight(1f),
//                    text = { global.script.f[7].toString() })
//                ComposeBoxForF(
//                    8,
//                    modifier = Modifier.weight(1f),
//                    text = { global.script.f[8].toString() })
//                ComposeBoxForF(
//                    9,
//                    modifier = Modifier.weight(1f),
//                    text = { global.script.f[9].toString() })
//
//
//            }
//
//        }
//    }
}

//Ячейка регистра
@Composable
private fun ComposeBoxForF(index: Int, text: () -> String, modifier: Modifier = Modifier) {

    Box(
        modifier = Modifier
            .padding(start = 1.dp, end = 1.dp)
            .height(25.dp)
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
            .then(modifier) //, contentAlignment = Alignment.CenterStart

    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height(25.dp)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }



            Text(
                text = text(),
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}