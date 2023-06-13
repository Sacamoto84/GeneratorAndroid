package com.example.generator2.screens.editor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.generator2.screens.editor.EditorMatModel

@Composable
fun EditorPreviewCarrier(model: EditorMatModel) {
    Box(
        modifier = Modifier.padding(8.dp).fillMaxWidth().aspectRatio(4f).background(Color.Black)
    ) {


        val points = remember { mutableListOf<Offset>() }
        val dispose = remember {  mutableStateOf(true)}

        Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {

            if(dispose.value) {
                dispose.value = false

                model.signal[model.signal.lastIndex] =  model.signal[model.signal.lastIndex - 1]

                val sizeW = size.width
                points.clear()
                for (x in 0 until size.width.toInt() / 2 ) {
                    val mapX: Int =
                        model.map(x.toFloat(), 0f, sizeW / 2 - 1, 0f, model.editWight.toFloat())
                            .toInt()
                    val y = model.map(
                        model.signal[mapX].toFloat(),
                        0f,
                        model.editMax.toFloat(),
                        0f,
                        size.height - 1
                    ) / 2
                    points.add(Offset(x.toFloat(), y))
                    points.add(Offset(x.toFloat(), size.height - y))
                }

                for (x in size.width.toInt() / 2 until size.width.toInt()) {
                    val mapX: Int = model.map(
                        x.toFloat(),
                        sizeW / 2,
                        sizeW - 1,
                        0f,
                        model.editWight.toFloat()
                    ).toInt()
                    val y = model.map(
                        model.signal[mapX].toFloat(),
                        0f,
                        model.editMax.toFloat(),
                        0f,
                        size.height - 1
                    ) / 2
                    points.add(Offset(x.toFloat(), y))
                    points.add(Offset(x.toFloat(), size.height - y))
                }
            }

            model.refsresh.value

            model.signal[model.signal.lastIndex] =  model.signal[model.signal.lastIndex - 1]

            if (model.refsreshButton.value == 1) {
                val sizeW = size.width
                points.clear()
                for (x in 0 until size.width.toInt() / 2) {
                    val mapX: Int =
                        model.map(x.toFloat(), 0f, sizeW / 2 - 1, 0f, model.editWight.toFloat())
                            .toInt()
                    val y = model.map(
                        model.signal[mapX].toFloat(),
                        0f,
                        model.editMax.toFloat(),
                        0f,
                        size.height - 1
                    ) / 2
                    points.add(Offset(x.toFloat(), y))
                    points.add(Offset(x.toFloat(), size.height - y))
                }

                for (x in size.width.toInt() / 2 until size.width.toInt()) {
                    val mapX: Int = model.map(
                        x.toFloat(),
                        sizeW / 2,
                        sizeW - 1,
                        0f,
                        model.editWight.toFloat()
                    ).toInt()
                    val y = model.map(
                        model.signal[mapX].toFloat(),
                        0f,
                        model.editMax.toFloat(),
                        0f,
                        size.height - 1
                    ) / 2
                    points.add(Offset(x.toFloat(), y))
                    points.add(Offset(x.toFloat(), size.height - y))
                }
            }

            println("recompose EditorPreviewCarrier Canvas")

            //Вертикальная линия
            drawLine(
                color = Color.DarkGray,
                start = Offset(size.width / 2, 0f),
                end = (Offset(size.width / 2, size.height - 1))
            )

            //Горизонтальная линия
            drawLine(
                color = Color.DarkGray,
                start = Offset(0f, size.height / 2),
                end = (Offset(size.width - 1, size.height / 2))
            )

            drawPoints(
                color = Color.Yellow,
                points = points,
                cap = StrokeCap.Round,
                pointMode = PointMode.Lines,
                strokeWidth = 1f
            )

        }
    }
}

@Composable
fun EditorPreviewFM(model: EditorMatModel) {

    Box(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp).fillMaxWidth()
            .aspectRatio(4f).background(Color.Black)
    ) {
        val points = remember { mutableListOf<Offset>() }

        val dispose = remember {  mutableStateOf(true)}

        Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {

            if(dispose.value)
            {
                dispose.value = false
                points.clear()
                
                model.signal[model.signal.lastIndex] =  model.signal[model.signal.lastIndex - 1]

                val sizeW = size.width

                for (x in 0 until size.width.toInt() / 2) {
                    val mapX: Int =
                        model.map(x.toFloat(), 0f, sizeW / 2 - 1, 0f, model.editWight.toFloat())
                            .toInt()
                    val y = model.map(
                        model.signal[mapX].toFloat(), 0f, model.editMax.toFloat(), 0f, size.height - 1
                    ).toFloat()
                    points.add(Offset(x.toFloat(), y))

                }
                for (x in size.width.toInt() / 2 until size.width.toInt()) {
                    val mapX: Int =
                        model.map(x.toFloat(), sizeW / 2, sizeW - 1, 0f, model.editWight.toFloat())
                            .toInt()
                    val y = model.map(
                        model.signal[mapX].toFloat(), 0f, model.editMax.toFloat(), 0f, size.height - 1
                    ).toFloat()
                    points.add(Offset(x.toFloat(), y))
                }
            }

            model.refsresh.value

            model.signal[model.signal.lastIndex] =  model.signal[model.signal.lastIndex - 1]

            if (model.refsreshButton.value == 1) {
                points.clear()
                val sizeW = size.width
                for (x in 0 until size.width.toInt() / 2) {
                    val mapX: Int =
                        model.map(x.toFloat(), 0f, sizeW / 2 - 1, 0f, model.editWight.toFloat())
                            .toInt()
                    val y = model.map(
                        model.signal[mapX].toFloat(), 0f, model.editMax.toFloat(), 0f, size.height - 1
                    ).toFloat()
                    points.add(Offset(x.toFloat(), y))

                }
                for (x in size.width.toInt() / 2 until size.width.toInt()) {
                    val mapX: Int =
                        model.map(x.toFloat(), sizeW / 2, sizeW - 1, 0f, model.editWight.toFloat())
                            .toInt()
                    val y = model.map(
                        model.signal[mapX].toFloat(), 0f, model.editMax.toFloat(), 0f, size.height - 1
                    ).toFloat()
                    points.add(Offset(x.toFloat(), y))
                }
            }

            //Вертикальная линия
            drawLine(
                color = Color.DarkGray,
                start = Offset(size.width / 2, 0f),
                end = (Offset(size.width / 2, size.height - 1))
            )

            //Горизонтальная линия
            drawLine(
                color = Color.DarkGray,
                start = Offset(0f, size.height / 2),
                end = (Offset(size.width - 1, size.height / 2))
            )


            drawPoints(
                color = Color.Magenta,
                points = points,
                cap = StrokeCap.Round,
                pointMode = PointMode.Polygon,
                strokeWidth = 1f
            )


        }
    }
}