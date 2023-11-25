package com.example.generator2.screens.editor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.dp
import com.example.generator2.screens.editor.EditorMatModel
import com.example.generator2.screens.editor.PaintingState
import com.smarttoolfactory.gesture.pointerMotionEvents

enum class MotionEvent {
    Idle, Down, Move, Up
}

val model = EditorMatModel()

@Composable
fun EditorCanvas() {

    //var currentPosition by remember { mutableStateOf(Offset.Unspecified) } // This is previous motion event before next touch is saved into this current position

    // color and text are for debugging and observing state changes and position
    val gestureColor by remember { mutableStateOf(Color.Black) } //Цвет фона

    val drawModifier = Modifier
        .padding(16.dp) //.shadow(1.dp)
        //.fillMaxWidth()
        //.height(300.dp)
        //.clipToBounds()
        .fillMaxSize()
        .background(gestureColor)
        .pointerMotionEvents(onDown = { pointerInputChange: PointerInputChange ->
            model.currentPosition.value = pointerInputChange.position
            model.motionEvent.value = MotionEvent.Down //gestureColor = Color.Blue
            model.refsresh.value++
            pointerInputChange.consume()
        }, onMove = { pointerInputChange: PointerInputChange ->
            model.currentPosition.value = pointerInputChange.position
            model.motionEvent.value = MotionEvent.Move // gestureColor = Color.Green
            model.refsresh.value++
            pointerInputChange.consume()
        }, onUp = { pointerInputChange: PointerInputChange ->
            model.motionEvent.value = MotionEvent.Up // gestureColor = Color.White
            model.refsresh.value++
            pointerInputChange.consume()
        }, delayAfterDownInMillis = 25L
        )

    val path1 = remember { Path() }

    var disposable by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier //.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            .fillMaxWidth()
            .aspectRatio(2f) //.clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
    ) {


        Canvas(modifier = drawModifier) {

            model.sizeCanvas = size //Передали размер канвы

            val mouseOffset = 150f

            if (disposable) {
                disposable = false
                println("disposable")

                //Позиция на редакторе
                model.currentPosition.value = Offset(size.width / 2, size.height / 2 + mouseOffset)

                //Для loop
                model.setOnlyPosition(
                    Offset(
                        size.width / 2, size.height / 2
                    )
                )
                model.lastPosition = model.position
                model.refsresh.value++
            }

            when (model.motionEvent.value) {
                MotionEvent.Down -> {

                    //println(model.motionEvent)

                    model.setOnlyPosition(
                        Offset(
                            model.currentPosition.value.x,
                            model.currentPosition.value.y - mouseOffset
                        )
                    )
                    model.lastPosition = model.position
                }

                MotionEvent.Move -> {

                    //println(model.motionEvent)

                    when (model.state) {
                        PaintingState.Show -> {
                            model.setPositionAndLast(
                                Offset(
                                    model.currentPosition.value.x,
                                    model.currentPosition.value.y - mouseOffset
                                )
                            )
                        }

                        PaintingState.PaintLine -> {
                            model.setOnlyPosition(
                                Offset(
                                    model.currentPosition.value.x,
                                    model.currentPosition.value.y - mouseOffset
                                )
                            ) //println("..PaintLine")
                        }

                        PaintingState.PaintPoint -> {
                            model.setPositionAndLast(
                                Offset(
                                    model.currentPosition.value.x,
                                    model.currentPosition.value.y - mouseOffset
                                )
                            )
                        }
                    }
                }

                MotionEvent.Up -> { //path.lineTo(currentPosition.x, currentPosition.y)
                    model.motionEvent.value = MotionEvent.Idle //println(model.motionEvent)
                }

                else -> {}
            }

            //Центральная вертикальная
            drawLine(
                color = Color.Gray,
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height - 1)
            )
            drawLine(
                color = Color.Gray,
                start = Offset(size.width / 4, 0f),
                end = Offset(size.width / 4, size.height - 1)
            )
            drawLine(
                color = Color.Gray,
                start = Offset(size.width * 3 / 4, 0f),
                end = Offset(size.width * 3 / 4, size.height - 1)
            )

            //Горизонтальная линия
            drawLine(
                color = Color.Gray,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width - 1, size.height / 2)
            )
            drawLine(
                color = Color.Gray,
                start = Offset(0f, size.height / 4),
                end = Offset(size.width - 1, size.height / 4)
            )
            drawLine(
                color = Color.Gray,
                start = Offset(0f, size.height * 3 / 4),
                end = Offset(size.width - 1, size.height * 3 / 4)
            )

            drawRect(
                color = Color.DarkGray,
                topLeft = Offset(0f, 0f),
                size = size,
                style = Stroke(width = 1.dp.toPx())
            )

            if (model.currentPosition.value != Offset.Unspecified) {

                //println("current position ${model.currentPosition.value.x}, ${model.currentPosition.value.y}")

                if (model.state == PaintingState.PaintPoint) {
                    model.line() //Расчет нового сигнала для точки
                }

                if (model.state == PaintingState.PaintLine) {
                    model.line() //Расчет нового сигнала для точки
                }

                //Палец
                drawCircle(
                    color = Color.Red,
                    center = model.currentPosition.value,
                    radius = 60f,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        join = StrokeJoin.Bevel,
                        cap = StrokeCap.Square, //pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 15f))
                    )
                )

                drawCircle(
                    color = Color.Gray, center = Offset(
                        model.currentPosition.value.x, model.currentPosition.value.y - mouseOffset
                    ), radius = 10f, alpha = 0.6f
                )


                //                val textPaint = Paint().asFrameworkPaint().apply {
                //                    isAntiAlias = true
                //                    textSize = 24.sp.toPx()
                //                    color = android.graphics.Color.BLUE
                //                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                //                }
                //                //Текст
                //                val paint = android.graphics.Paint()
                //                paint.textSize = 28f
                //                paint.color = 0xffff0000.toInt()
                //                drawIntoCanvas {
                //                    it.nativeCanvas.drawText(
                //                        textConstrain(model.position.x.toInt(), model.position.y.toInt()),
                //                        model.currentPosition.value.x + 50f,
                //                        model.currentPosition.value.y - 250f,
                //                        paint
                //                    )
                //                }


            }

            //Рисуем сам сигнал
            val points3 = model.createPoint()
            drawPoints( //                brush = Brush.linearGradient(
                //                    colors = listOf(Color.Red, Color.Yellow)
                //                ),
                color = Color.Red,
                points = points3,
                cap = StrokeCap.Round,
                pointMode = PointMode.Polygon,
                strokeWidth = 3f
            )

        }
    }
}

fun textConstrain(X: Int, Y: Int): String {
    var x = X
    var y = Y
    if (x < 0) x = 0

    if (x > 1023) x = 1023

    if (y < 0) y = 0

    if (y > 4095) y = 4095

    y = 4095 - y


    return "$x,$y"

}



