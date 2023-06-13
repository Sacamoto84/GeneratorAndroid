package com.example.generator2.screens.editor.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ComboBox(val items: List<String>, private val width: Dp = 100.dp) {

    var expanded by mutableStateOf(false)
    var selectedIndex by mutableStateOf(0)

    @Composable
    fun Draw() {

        Box(modifier = Modifier.width(width), contentAlignment = Alignment.Center) {

            Text(
                items[selectedIndex],
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = { expanded = true })
                    .background(Color.White),
                color = Color.Black, fontSize = 24.sp, textAlign = TextAlign.Center
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(80.dp).background(
                    Color.DarkGray
                )
            ) {

                items.forEachIndexed { index, s ->
                    DropdownMenuItem(onClick = {
                        selectedIndex = index
                        expanded = false
                    }) {
                        Text(text = s, color = Color.White)
                    }
                }
            }
        }

    }

}