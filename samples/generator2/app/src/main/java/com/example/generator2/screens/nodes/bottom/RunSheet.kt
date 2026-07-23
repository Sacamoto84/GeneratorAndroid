package com.example.generator2.screens.nodes.bottom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.element.Console2
import com.example.generator2.screens.scripting.ui.RegisterViewDraw

/**
 * Свёрнутая шторка — одна строка с занятыми регистрами. Развёрнутая —
 * все регистры и консоль.
 *
 * Разворачивается тапом по полоске, а не свайпом: полоска высотой в одну
 * строку — маленькая цель для перетаскивания, и по той же причине, что
 * и связи в два тапа, надёжнее нажатие.
 */
@Composable
fun RunSheet(registers: List<Float>, console: Console2, modifier: Modifier = Modifier) {

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxWidth()
            .background(Color(0xFF2D2D2F)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val busy = registers.withIndex().filter { it.value != 0f }

            Row(Modifier.weight(1f).horizontalScroll(rememberScrollState())) {
                if (busy.isEmpty()) {
                    Text("регистры пусты", color = Color(0xFF6B6B70), fontSize = 12.sp)
                } else {
                    busy.forEach { (i, v) ->
                        Text(
                            "F$i $v   ",
                            color = Color(0xFFA06CD5),
                            fontSize = 12.sp,
                            maxLines = 1,
                        )
                    }
                }
            }

            Text(
                if (expanded) "консоль ▾" else "консоль ▴",
                color = Color(0xFF9A9AA0),
                fontSize = 12.sp,
            )
        }

        AnimatedVisibility(expanded) {
            Column {
                RegisterViewDraw(registers)
                Box(Modifier.height(180.dp).fillMaxWidth().padding(horizontal = 8.dp)) {
                    console.Draw(Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
