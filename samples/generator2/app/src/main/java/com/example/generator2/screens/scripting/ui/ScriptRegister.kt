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

/**
 * Блок регистров
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterViewDraw(register: FloatArray) {
    FlowRow(
        modifier = Modifier.padding(start = 6.dp, end = 6.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.Center,
        maxItemsInEachRow = 5
    ) {
        register.forEachIndexed { index, it ->
            ComposeBoxForF(
                index,
                modifier = Modifier.weight(1f),
                text = { it.toString() })
        }
    }
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
            .then(modifier)
    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .width(14.dp)
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
