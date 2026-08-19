package com.example.generator2.features.scope

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.generator2.R
import com.example.generator2.theme.colorChL
import com.example.generator2.theme.colorChR

//Габарит кнопки панели — квадрат в высоту строки
private val m = Modifier
    .height(40.dp)
    .width(40.dp)

private val colorEnabled = Color.Black
private val colorTextEnabled = Color.Green
private val colorTextDisabled = Color.Gray

/** Квадратная кнопка-переключатель с подписью. */
@Composable
private fun ToggleBox(
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = m
            .clickable(onClick = onClick)
            .border(1.dp, Color.Gray)
            .background(if (active) colorEnabled else Color.Black)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/** Панель под осциллографом: каналы, пауза, развёртка, лиссажу. */
@Suppress("NonSkippableComposable")
@Composable
fun PanelButton(scope: Scope) {

    val fontSize = 24.sp

    val stateIsVisibleL = scope.isVisibleL.collectAsStateWithLifecycle().value
    val stateIsVisibleR = scope.isVisibleR.collectAsStateWithLifecycle().value
    val stateIsOneTwo = scope.isOneTwo.collectAsStateWithLifecycle().value

    Row(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth()
            .background(Color.Cyan),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row {
            ToggleBox(
                active = stateIsVisibleL,
                onClick = { scope.isVisibleL.value = scope.isVisibleL.value.not() }
            ) {
                Text(
                    text = "L",
                    color = if (stateIsVisibleL) colorChL else colorTextDisabled,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
            }

            ToggleBox(
                active = stateIsVisibleR,
                onClick = { scope.isVisibleR.value = scope.isVisibleR.value.not() }
            ) {
                Text(
                    text = "R",
                    color = if (stateIsVisibleR) colorChR else colorTextDisabled,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
            }

            ToggleBox(
                active = stateIsOneTwo,
                onClick = { scope.isOneTwo.value = scope.isOneTwo.value.not() },
                modifier = Modifier.rotate(90f)
            ) {
                Text(
                    text = if (stateIsOneTwo) "•" else "••",
                    color = Color.White,
                    fontSize = fontSize
                )
            }
        }

        Box(
            modifier = Modifier
                .height(40.dp)
                .width(64.dp)
                .border(1.dp, Color.Gray)
                .background(Color.Black)
                .clickable {
                    scope.isPause.value = scope.isPause.value.not()
                },
            contentAlignment = Alignment.Center
        ) {
            Text("Pause", color = Color.White)
        }

        Row {

            //Знак Плюс
            Box(
                modifier = m
                    .clickable(onClick = { scope.compressorCountUp() })
                    .border(1.dp, Color.Gray)
                    .background(Color.Black)
                    .drawBehind {
                        drawLine(
                            Color.White,
                            start = Offset(size.width * 1 / 3f, size.height / 2f),
                            end = Offset(size.width * 2 / 3f, size.height / 2f),
                            strokeWidth = 3.dp.toPx()
                        )

                        drawLine(
                            Color.White,
                            start = Offset(size.width * 1 / 2f, size.height / 3f),
                            end = Offset(size.width * 1 / 2f, size.height * 2f / 3f),
                            strokeWidth = 3.dp.toPx()
                        )

                    })

            Text(
                text = scope.sweepLabel(scope.compressorCount.floatValue),
                modifier = Modifier
                    .width(64.dp)
                    .height(40.dp)
                    .wrapContentHeight(Alignment.CenterVertically)
                    .background(Color.Black),
                color = Color.White,
                fontSize = 24.sp,
                textAlign = TextAlign.Center, fontFamily = FontFamily(Font(R.font.nunito))
            )

            //Знак минус
            Box(
                modifier = m
                    .clickable(onClick = { scope.compressorCountDown() })
                    .border(1.dp, Color.Gray)
                    .background(Color.Black)
                    .drawBehind {
                        drawLine(
                            Color.White,
                            start = Offset(size.width * 1 / 3f, size.height / 2f),
                            end = Offset(size.width * 2 / 3f, size.height / 2f),
                            strokeWidth = 3.dp.toPx()
                        )
                    })
        }

/////////////////////////////////////// Кнопка лиссажу ///////////////////////////////////////
        Box(
            modifier = m
                .clickable(onClick = { scope.isUseLissagu.value = scope.isUseLissagu.value.not() })
                .border(1.dp, Color.Green)
                .background(Color.Black)
                .drawBehind {
                    // Размеры овала
                    val ovalWidth = size.height * 0.75f
                    val ovalHeight = ovalWidth * 0.45f

                    // Центр канвы
                    val canvasCenter = Offset(x = size.width / 2, y = size.height / 2)

                    // Верхний левый угол для центрирования овала
                    val topLeft = Offset(
                        x = canvasCenter.x - ovalWidth / 2,
                        y = canvasCenter.y - ovalHeight / 2
                    )

                    // Поворачиваем канву
                    rotate(degrees = -45f, pivot = canvasCenter) {
                        drawOval(
                            color = Color.White,
                            topLeft = topLeft,
                            size = Size(width = ovalWidth, height = ovalHeight),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    drawLine(
                        Color.White,
                        start = Offset(size.width * 0.1f, size.height / 2f),
                        end = Offset(size.width * 0.9f, size.height / 2f),
                        strokeWidth = 1.dp.toPx()
                    )

                    drawLine(
                        Color.White,
                        start = Offset(size.width * 1 / 2f, size.height * 0.2f),
                        end = Offset(size.width * 1 / 2f, size.height * 0.8f),
                        strokeWidth = 1.dp.toPx()
                    )

                }
        )
//////////////////////////////////////////////////////////////////////////////////////////////

    }

}

/**
 * Кнопка выбора синхронизации.
 *
 * Толщина рамки, а не только цвет: активная кнопка различима без
 * цветовосприятия.
 */
@Composable
private fun SyncButton(
    active: Boolean,
    activeColor: Color,
    label: String,
    shape: Shape?,
    onClick: () -> Unit
) {
    val base = if (shape == null) m else m.clip(shape)
    val bordered =
        if (shape == null) base.border(if (active) 2.dp else 1.dp, if (active) activeColor else Color.Gray)
        else base.border(if (active) 2.dp else 1.dp, if (active) activeColor else Color.Gray, shape)

    Box(
        modifier = bordered
            .clickable(onClick = onClick)
            .background(if (active) colorEnabled else Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = if (active) activeColor else colorTextDisabled)
    }
}

/** Выбор канала синхронизации развёртки: нет, левый, правый. */
@Suppress("NonSkippableComposable")
@Composable
fun OscilloscopeControl(scope: Scope) {

    val a = 8.dp

    Row {

        SyncButton(
            active = scope.oscillSync.value == OSCILLSYNC.NONE,
            activeColor = colorTextEnabled,
            label = "N",
            shape = RoundedCornerShape(topStart = a, bottomStart = a),
            onClick = { scope.oscillSync.value = OSCILLSYNC.NONE }
        )

        SyncButton(
            active = scope.oscillSync.value == OSCILLSYNC.L,
            activeColor = colorChL,
            label = "L",
            shape = null,
            onClick = { scope.oscillSync.value = OSCILLSYNC.L }
        )

        SyncButton(
            active = scope.oscillSync.value == OSCILLSYNC.R,
            activeColor = colorChR,
            label = "R",
            shape = RoundedCornerShape(topEnd = a, bottomEnd = a),
            onClick = { scope.oscillSync.value = OSCILLSYNC.R }
        )

    }
}
