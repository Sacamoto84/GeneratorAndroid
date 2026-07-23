package com.example.generator2.screens.nodes

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Одноразовый спайк: проверяем, что касания доходят до карточек внутри
 * трансформированного слоя после зума и панорамы. Удаляется в Task 15.
 */
@Composable
fun SpikeCanvas() {

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var lastTapped by remember { mutableStateOf("нет") }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF202020))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.4f, 2.5f)
                    offset += pan
                }
            }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                )
        ) {
            listOf("A" to 40, "B" to 160, "C" to 280).forEach { (name, top) ->
                Box(
                    Modifier
                        .offset { IntOffset(60, top) }
                        .size(168.dp, 72.dp)
                        .background(Color(0xFF3A7BD5))
                        .clickable { lastTapped = name }
                ) {
                    Text(name, color = Color.White)
                }
            }
        }

        Text(
            "нажата: $lastTapped   зум: ${(scale * 100).roundToInt()}%",
            color = Color.White,
        )
    }
}
