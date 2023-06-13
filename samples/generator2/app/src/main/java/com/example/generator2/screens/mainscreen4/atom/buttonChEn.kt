package com.example.generator2.screens.mainscreen4.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.generator2.model.LiveData
import com.example.generator2.screens.mainscreen4.ms4SwitchWidth
import com.example.generator2.theme.colorDarkBackground
import libs.modifier.noRippleClickable

@Composable
fun ButtonChEn(str: String = "CH0") {

    val chEN: State<Boolean> = if (str == "CH0") {
        LiveData.ch1_EN.collectAsState()
    } else {
        LiveData.ch2_EN.collectAsState()
    }

// Кнопка включения канала
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .height(24.dp)
            .width(ms4SwitchWidth)
            .border(
                2.dp,
                color = if (chEN.value) Color(0xFF1B5E20) else Color.DarkGray,
                RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (chEN.value) Color(0xFF4DD0E1) else colorDarkBackground
            )
            .noRippleClickable(onClick = {
                if (str == "CH0") LiveData.ch1_EN.value =
                    !LiveData.ch1_EN.value
                else LiveData.ch2_EN.value = !LiveData.ch2_EN.value

                println("Кнопка")

            })
    ) {
        Text(
            text = if (chEN.value) "On" else "Off",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}