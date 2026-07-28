package com.example.generator2.screens.mainscreen4.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.generator2.common.haptic.Haptic
import com.example.generator2.features.generator.Generator
import com.example.generator2.screens.mainscreen4.textStyleButtonOnOff
import com.example.generator2.theme.colorDarkBackground
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MasterButton(gen: Generator) {
    val pressed by gen.liveData.masterButton.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (pressed) Color(0xFF01AE0F) else colorDarkBackground)
            .border(
                2.dp,
                if (pressed) Color(0xFF1B5E20) else Color.DarkGray,
                RoundedCornerShape(8.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    gen.liveData.masterButton.value = true
                    Haptic.confirm()
                    tryAwaitRelease()
                    gen.liveData.masterButton.value = false
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "MASTER",
            color = if (pressed) colorDarkBackground else Color.LightGray,
            style = textStyleButtonOnOff
        )
    }
}
