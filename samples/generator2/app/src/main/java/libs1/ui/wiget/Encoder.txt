package com.example.generator2.ui.wiget

import android.graphics.Bitmap
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.roundToInt
import kotlin.math.sqrt


@Composable
fun DPtoPX(inDp: Float): Float {
    val pxValue = LocalDensity.current.run {
        inDp.dp.toPx()
    } //DP->px
    return pxValue
}


//
// Пример привязки картинки imageBG =  ImageBitmap.imageResource( id = R.drawable.knob8 )
//
@Composable
fun EncoderLine(
    modifier: Modifier,

    imageThump: ImageBitmap? = null,            //Картинка некой тоски которая крутится по кругу некого радиуса
    imageThumpSize: Dp,                  //Размеры точки если null, то используем
    imageThumbOffset: Offset = Offset(0f, 0f), //Смещение отрисовки точки, ибо ее кинет в центр

    //imageUp: ImageBitmap? = null,            //Фоновая верхняя картинка, неподвижная
    //offsetUp: Offset = Offset(0f, 0f), //Смещение

    imageBG: ImageBitmap? = null,  //Фоновая картинка, неподвижная
    imageBGSize: Dp,
    imageBGOffset: Offset = Offset(0f, 0f), //Смещение отрисовки фона

    //Measure
    drawMeasureLine: Boolean = false, //Рисовать справочные линии?
    drawMeasureDot: Boolean = false, //Рисовать справочную точку?
    drawMeasureCircle: Boolean = false, //Рисовать справочную кольцо

    //Ограничения
    offsetAngle: Float = 0f, //Угла
    rangeAngle: Float = 0f, //Какой диапазон градусов всего 360 это 1 оборот  0 - ограничения нет

    sensitivity : Float = 1.0f, //Чувствительность

    value: Float? = null, //Вывод icrementalAngle от 0 до rangeAngle при rangeAngle != 0, и от +- Float при rangeAngle = 0
    onValueChange: (Float) -> Unit

) {

    val onValueChangeState = rememberUpdatedState(onValueChange)

    val valueRemember =  remember{ mutableStateOf(value)}

    var icrementalAngle = remember { mutableStateOf(0f) }

    if (value != icrementalAngle.value) icrementalAngle.value = value!!

    if (value != null) {
        //icrementalAngle = valueRemember.value!!
    }

    Box(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize(), contentAlignment = Alignment.Center
    ) {

        var offX by remember { mutableStateOf(0f) }    //Смещени, координаты текущего начала Box
        var offY by remember { mutableStateOf(0f) }
        var ugol by remember { mutableStateOf(0f) }


////////////////////////
        //Нужная ширина в px
        val imageBGsizeZadanieWidthPx = DPtoPX(imageBGSize.value)
        val imageBGkoefScale = imageBGsizeZadanieWidthPx / (imageBG?.width ?: 0)
        val imageBGresized =
            imageBG?.let {
                Bitmap.createScaledBitmap(
                    it.asAndroidBitmap(),
                    imageBGsizeZadanieWidthPx.toInt(),
                    (imageBGkoefScale * imageBG.height).toInt(),
                    true
                )
            }
                ?.asImageBitmap()

        //Нужная ширина в px
        val imageThumpsizeZadanieWidthPx = DPtoPX(imageThumpSize.value)
        val imageThumpkoefScale = imageThumpsizeZadanieWidthPx / (imageThump?.width ?: 0)
        val imageThumpresized =
            imageThump?.let {
                Bitmap.createScaledBitmap(
                    it.asAndroidBitmap(),
                    imageThumpsizeZadanieWidthPx.toInt(),
                    (imageThumpkoefScale * imageThump.height).toInt(),
                    true
                )
            }
                ?.asImageBitmap()
////////////////////////
//Блок смещения Offset Flat -> DPtoPX ->
        val imageBGresizedOffsetX = DPtoPX(imageBGOffset.x)
        val imageBGresizedOffsetY = DPtoPX(imageBGOffset.y)

        val imageThumbresizedOffsetX = DPtoPX(imageThumbOffset.x)
        val imageThumbresizedOffsetY = DPtoPX(imageThumbOffset.y)
////////////////////////
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        )
        {

            //Рисовать справочную точку?
            if (drawMeasureDot) {
                drawLine(
                    color = Color.Black,
                    start = Offset(offX + size.width / 2, offY + size.height / 2),
                    end = Offset(size.width / 2, size.height / 2)
                )

                drawArc(
                    color = Color.Blue,
                    startAngle = 0f,
                    sweepAngle = ugol,
                    useCenter = false,
                    size = Size(60f, 60f),
                    style = Stroke(width = 2f, cap = StrokeCap.Round),
                    topLeft = Offset(size.width / 2 - 30f, size.height / 2 - 30f)
                )
            }

            if (drawMeasureCircle) //Рисовать справочную кольцо
            {
                drawArc(
                    color = Color.Black,
                    startAngle = 0f,
                    sweepAngle = 359f,
                    useCenter = false,
                    size = Size(200f, 200f),
                    style = Stroke(width = 10f, cap = StrokeCap.Round),
                    topLeft = Offset(size.width / 2 - 100f, size.height / 2 - 100f)
                )
                drawArc(
                    color = Color.Green,
                    startAngle = icrementalAngle.value,
                    sweepAngle = 10f,
                    useCenter = false,
                    size = Size(200f, 200f),
                    style = Stroke(width = 10f, cap = StrokeCap.Round),
                    topLeft = Offset(size.width / 2 - 100f, size.height / 2 - 100f)
                )
            }

            //Рисуем неподвижную часть фоновая
            if (imageBGresized != null) {
                drawImage(
                    image = imageBGresized,
                    topLeft = Offset(
                        x = size.width / 2 - imageBGresized.width / 2 + imageBGresizedOffsetX,
                        y = size.height / 2 - imageBGresized.height / 2 + imageBGresizedOffsetY
                    )
                )
            }

            //Поворачиваем картинку точки подвижня часть
            rotate(degrees = icrementalAngle.value + offsetAngle) {
                if (imageThumpresized != null) {
                    drawImage(
                        image = imageThumpresized,
                        topLeft = Offset(
                            x = size.width / 2 - imageThumpresized.width / 2 + imageThumbresizedOffsetX,
                            y = size.height / 2 - imageThumpresized.height / 2 + imageThumbresizedOffsetY
                        )
                    )
                }
            }

            //Если нужно рисовать справочные линии
            if (drawMeasureLine) {
                //Вкртикальная линия
                drawLine(
                    color = Color.Black,
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height)
                )

                //Горизонтальная линия
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offX.roundToInt(), offY.roundToInt()) }
                .pointerInput(Unit) {
                    detectTapGestures(
                        //onTap = {   print("onTap\n") },
                        //onPress = {
                        //},
                        //onDoubleTap = { print("onDoubleTap\n") },
                        //onLongPress = { print("onLongPress\n") },
                    )
                }
                .pointerInput(Unit) {

                    detectDragGestures(
                        onDragStart = {
                        },
                        onDrag =
                        { change, dragAmount ->
                            change.consumeAllChanges()
                            //icrementalAngle += dragAmount.x
                            icrementalAngle.value += dragAmount.y * sensitivity

                                if (rangeAngle != 0f) {
                                    if (icrementalAngle.value > rangeAngle) icrementalAngle.value =
                                        rangeAngle
                                    if (icrementalAngle.value < 0f) icrementalAngle.value = 0f
                                }

                            //onValueChange(icrementalAngle.value)

                            onValueChangeState.value.invoke(icrementalAngle.value)


                        },
                        onDragEnd = {
                        },
                        onDragCancel = {
                        }
                    )
                }
                .background(if (drawMeasureDot) Color.Yellow else Color.Transparent, CircleShape),

            contentAlignment = Alignment.Center
        )
        {
        }
    }
}


//
// Пример привязки картинки imageBG =  ImageBitmap.imageResource( id = R.drawable.knob8 )
//
@Composable
fun Encoder(
    modifier: Modifier,

    imageThump: ImageBitmap? = null,            //Картинка некой тоски которая крутится по кругу некого радиуса
    imageThumpSize: Dp,                  //Размеры точки если null, то используем
    imageThumbOffset: Offset = Offset(0f, 0f), //Смещение отрисовки точки, ибо ее кинет в центр

    //imageUp: ImageBitmap? = null,            //Фоновая верхняя картинка, неподвижная
    //offsetUp: Offset = Offset(0f, 0f), //Смещение

    imageBG: ImageBitmap? = null,  //Фоновая картинка, неподвижная
    imageBGSize: Dp,
    imageBGOffset: Offset = Offset(0f, 0f), //Смещение отрисовки фона

    //Measure
    drawMeasureLine: Boolean = false, //Рисовать справочные линии?
    drawMeasureDot: Boolean = false, //Рисовать справочную точку?
    drawMeasureCircle: Boolean = false, //Рисовать справочную кольцо

    //Ограничения
    offsetAngle: Float = 0f, //Угла
    rangeAngle: Float = 0f, //Какой диапазон градусов всего 360 это 1 оборот  0 - ограничения нет

    value: Float? = null, //Вывод icrementalAngle от 0 до rangeAngle при rangeAngle != 0, и от +- Float при rangeAngle = 0
    onValueChange: (Float) -> Unit

) {

    val onValueChangeState = rememberUpdatedState(onValueChange)

    val ValueRemember = rememberUpdatedState(value)


    var icrementalAngle by remember { mutableStateOf(0f) }

    if (value != null) {
        icrementalAngle = ValueRemember.value!!
    }


    Box(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize(), contentAlignment = Alignment.Center
    ) {

        var areaCurrent: Int = 0                             //Текущая область
        var areaLast: Int = 0                                //Прошлая область

        var offX by remember { mutableStateOf(0f) }    //Смещени, координаты текущего начала Box
        var offY by remember { mutableStateOf(0f) }

        var ugol by remember { mutableStateOf(0f) }


        var lastAngle = 0f //При старте устанавливаем стартовый угол

////////////////////////
        //Нужная ширина в px
        val imageBGsizeZadanieWidthPx = DPtoPX(imageBGSize.value)
        val imageBGkoefScale = imageBGsizeZadanieWidthPx / (imageBG?.width ?: 0)
        val imageBGresized =
            imageBG?.let {
                Bitmap.createScaledBitmap(
                    it.asAndroidBitmap(),
                    imageBGsizeZadanieWidthPx.toInt(),
                    (imageBGkoefScale * imageBG.height).toInt(),
                    true
                )
            }
                ?.asImageBitmap()

        //Нужная ширина в px
        val imageThumpsizeZadanieWidthPx = DPtoPX(imageThumpSize.value)
        val imageThumpkoefScale = imageThumpsizeZadanieWidthPx / (imageThump?.width ?: 0)
        val imageThumpresized =
            imageThump?.let {
                Bitmap.createScaledBitmap(
                    it.asAndroidBitmap(),
                    imageThumpsizeZadanieWidthPx.toInt(),
                    (imageThumpkoefScale * imageThump.height).toInt(),
                    true
                )
            }
                ?.asImageBitmap()
////////////////////////
//Блок смещения Offset Flat -> DPtoPX ->
        val imageBGresizedOffsetX = DPtoPX(imageBGOffset.x)
        val imageBGresizedOffsetY = DPtoPX(imageBGOffset.y)

        val imageThumbresizedOffsetX = DPtoPX(imageThumbOffset.x)
        val imageThumbresizedOffsetY = DPtoPX(imageThumbOffset.y)
////////////////////////
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        )
        {

            //Рисовать справочную точку?
            if (drawMeasureDot) {
                drawLine(
                    color = Color.Black,
                    start = Offset(offX + size.width / 2, offY + size.height / 2),
                    end = Offset(size.width / 2, size.height / 2)
                )

                drawArc(
                    color = Color.Blue,
                    startAngle = 0f,
                    sweepAngle = ugol,
                    useCenter = false,
                    size = Size(60f, 60f),
                    style = Stroke(width = 2f, cap = StrokeCap.Round),
                    topLeft = Offset(size.width / 2 - 30f, size.height / 2 - 30f)
                )
            }

            if (drawMeasureCircle) //Рисовать справочную кольцо
            {
                drawArc(
                    color = Color.Black,
                    startAngle = 0f,
                    sweepAngle = 359f,
                    useCenter = false,
                    size = Size(200f, 200f),
                    style = Stroke(width = 10f, cap = StrokeCap.Round),
                    topLeft = Offset(size.width / 2 - 100f, size.height / 2 - 100f)
                )
                drawArc(
                    color = Color.Green,
                    startAngle = icrementalAngle,
                    sweepAngle = 10f,
                    useCenter = false,
                    size = Size(200f, 200f),
                    style = Stroke(width = 10f, cap = StrokeCap.Round),
                    topLeft = Offset(size.width / 2 - 100f, size.height / 2 - 100f)
                )
            }

            //Рисуем неподвижную часть фоновая
            if (imageBGresized != null) {
                drawImage(
                    image = imageBGresized,
                    topLeft = Offset(
                        x = size.width / 2 - imageBGresized.width / 2 + imageBGresizedOffsetX,
                        y = size.height / 2 - imageBGresized.height / 2 + imageBGresizedOffsetY
                    )
                )
            }

            //Поворачиваем картинку точки подвижня часть
            rotate(degrees = icrementalAngle + offsetAngle) {
                if (imageThumpresized != null) {
                    drawImage(
                        image = imageThumpresized,
                        topLeft = Offset(
                            x = size.width / 2 - imageThumpresized.width / 2 + imageThumbresizedOffsetX,
                            y = size.height / 2 - imageThumpresized.height / 2 + imageThumbresizedOffsetY
                        )
                    )
                }
            }

            //Рисуем неподвижную верхняя фоновая
//            if (imageUp != null) {
//                drawImage(
//                    image = imageUp,
//                    topLeft = Offset(
//                        x = size.width / 2 - imageUp.width / 2 + offsetUp.x,
//                        y = size.height / 2 - imageUp.height / 2 + offsetUp.y
//                    )
//                )
//            }

            //Если нужно рисовать справочные линии
            if (drawMeasureLine) {
                //Вкртикальная линия
                drawLine(
                    color = Color.Black,
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height)
                )

                //Горизонтальная линия
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2)
                )
            }
        }

        Box(
            modifier = Modifier
                //.padding(start = 300.dp)
                //.clip(CircleShape)
                //.size(sizeObj.dp, sizeObj.dp)
                .fillMaxSize()
                .offset { IntOffset(offX.roundToInt(), offY.roundToInt()) }
                .pointerInput(Unit) {
                    detectTapGestures(
                        //onTap = {   print("onTap\n") },
                        onPress = {
                            //offX = it.x - sizeObj / 2
                            //offY = it.y - sizeObj / 2
                            //print("onPress\n")
                        },
                        //onDoubleTap = { print("onDoubleTap\n") },
                        //onLongPress = { print("onLongPress\n") },
                    )
                }
                .pointerInput(Unit) {

                    detectDragGestures(
                        onDragStart = {
                            lastAngle = 99999f //при старте
                            areaCurrent = identifierArea(it.x, it.y)
                            areaLast = areaCurrent
                        },
                        onDrag =
                        { change, dragAmount ->
                            change.consumeAllChanges()
                            offX += dragAmount.x
                            offY += dragAmount.y
                            print("offX=${offX} offY=${offY} dragAmount.x=${dragAmount.x} dragAmount.y=${dragAmount.y}\n")

                            if (lastAngle == 99999f)
                                lastAngle = measure_angle(offX, offY, 80f, 0f)
                            else {
                                ugol = measure_angle(offX, offY, 80f, 0f)
                                areaCurrent = identifierArea(offX, offY)
                                if (areaCurrent == areaLast) {

                                    if ((areaCurrent == 0) || (areaCurrent == 1))
                                        icrementalAngle += ugol - lastAngle
                                    else
                                        icrementalAngle -= ugol - lastAngle


                                    if (rangeAngle != 0f) {
                                        if (icrementalAngle > rangeAngle) icrementalAngle =
                                            rangeAngle
                                        if (icrementalAngle < 0f) icrementalAngle = 0f
                                    }

                                }



                                onValueChangeState.value.invoke(icrementalAngle)




                                lastAngle = ugol
                                areaLast = areaCurrent
                            }
                        },
                        onDragEnd = {
                            offX = 0f
                            offY = 0f
                        },
                        onDragCancel = {
                            offX = 0f
                            offY = 0f
                        }
                    )
                }
                .background(if (drawMeasureDot) Color.Yellow else Color.Transparent, CircleShape),

            contentAlignment = Alignment.Center
        )
        {
        }
    }
}


//Функция определяет к какой области относится X Y
private fun identifierArea(x: Float, y: Float): Int {
    var res = 0
    if (x >= 0) //0 3
    {
        if (y >= 0)
            res = 0
        else
            res = 3
    } else // 1 2
    {
        if (y >= 0)
            res = 1
        else
            res = 2
    }
    return res
}

private fun measure_angle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val norma_x = sqrt(x1 * x1 + y1 * y1)
    val norma_y = sqrt(x2 * x2 + y2 * y2)
    var cosfi = (x1 * x2 + y1 * y2) / (norma_x * norma_y)

    //print("\n>x1:${x1} y1:${y1} x2:${x2} y2:${y2}\n")
    //print("cosfi:${cosfi}\n")
    val fi = acos(cosfi) * 180f / PI.toFloat()
    //print("fi:${fi}\n")
    return fi
}

