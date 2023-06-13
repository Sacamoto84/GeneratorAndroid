package com.example.generator2.screens.mainscreen4.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.generator2.util.format

@Composable
fun InfinitySlider(
    modifier: Modifier = Modifier,
    image: ImageBitmap? = null,  //Фоновая картинка, неподвижная
    sensing: Float = 1.0f, //Чувствительность
    value: Float? = null, //Вывод icrementalAngle от 0 до rangeAngle при rangeAngle != 0, и от +- Float при rangeAngle = 0
    onValueChange: (Float) -> Unit,
    vertical: Boolean = false,    //Если вертикальный вариант
    range: ClosedFloatingPointRange<Float> = 0f..1000f,
    invert: Boolean = false,
    visibleText: Boolean = true,
    text : String = "",
    fontSize : TextUnit = 14.sp
) {

    val onValueChangeState = rememberUpdatedState(onValueChange)
    val ValueRemember = rememberUpdatedState(value)

    var icrementalAngle by remember { mutableFloatStateOf(0f) }

    if (value != null) {
        icrementalAngle = ValueRemember.value!!
    }

    //var lastAngle = 0f //При старте устанавливаем стартовый угол

    var offX by remember { mutableStateOf(0f) }    //Смещени, координаты текущего начала Box

    val _sensing = rememberUpdatedState(sensing)

    Box(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDrag =
                    { change, dragAmount ->
                        change.consumeAllChanges()
                        icrementalAngle -= dragAmount.y * _sensing.value - dragAmount.x * _sensing.value
                        if (icrementalAngle > range.endInclusive) icrementalAngle =
                            range.endInclusive
                        if (icrementalAngle < range.start) icrementalAngle = range.start
                        onValueChangeState.value.invoke(icrementalAngle)
                    },
                    onDragEnd = {
                        offX = 0f
                    },
                    onDragCancel = {
                        offX = 0f
                    }
                )
            },
        contentAlignment = Alignment.Center
    )
    {

        if (image != null) {
            Image(
                image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
        if (visibleText)
            Text(text = "${icrementalAngle.format(2)}", color = Color.White)

        if (text.isNotEmpty())
            Text(
                text = text, color = Color.White, fontSize = fontSize)
    }


}

fun Modifier.vertical() =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.height, placeable.width) {
            placeable.place(
                x = -(placeable.width / 2 - placeable.height / 2),
                y = -(placeable.height / 2 - placeable.width / 2)
            )
        }
    }