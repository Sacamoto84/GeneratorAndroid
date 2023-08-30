package com.example.generator2.screens.mainscreen4.atom

import android.graphics.Typeface
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.theme.colorLightBackground2
import com.example.generator2.util.format
import com.siddroid.holi.colors.MaterialColor
import libs.modifier.noRippleClickable

lateinit var customTypeface: Typeface


@Composable
fun VolumeControl(value: Float, onValueChange: (Float) -> Unit) {

    val H = 32.dp//48.dp
    val W = 72.dp//72.dp

    val onValueChangeState = rememberUpdatedState(onValueChange)

    //var volume by remember { mutableFloatStateOf(value) }

    var volume = value

    //val pxValue = with(LocalDensity.current) { 16.dp.toPx() }

    val pxValue = LocalDensity.current.run { 30.dp.toPx() - 28.dp.toPx() * value }

    var position by remember { mutableFloatStateOf(pxValue) }

    val density = LocalDensity.current

    position = (1f-volume)*(H.value * density.density - 20.dp.value * density.density) + 2.dp.value * density.density

    position = (position).coerceIn(
        2.dp.value * density.density,
        H.value * density.density - 16.dp.value * density.density - 2.dp.value * density.density
    )

    //Показать инфо
    var showInfo by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (showInfo) 1f else 0f,
        animationSpec = tween(durationMillis = 200), label = ""
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .padding(start = 8.dp, top = 0.dp)
            .height(H)
            .width(W)


            .noRippleClickable { expanded = true }


            .pointerInput(Unit) {

                detectVerticalDragGestures(
                    onDragStart = {
                        showInfo = true
                    },

                    onDragEnd = {
                        showInfo = false
                    },

                    onVerticalDrag = { _, dragAmount ->
                        //println("p.position.y " + p.position.y.toString())
                        //position = p.position.y.coerceIn(0f, 104.dp.toPx() - 16.dp.toPx())

                        position = (position + dragAmount).coerceIn(
                            2.dp.toPx(),
                            H.toPx() - 16.dp.toPx() - 2.dp.toPx()
                        )
                        //println("position $position")
                        volume =
                            (1f - (position - 2.dp.toPx()) / (H.toPx() - 16.dp.toPx() - 4.dp.toPx()))

                        onValueChangeState.value.invoke(volume)
                    }
                )

            }, contentAlignment = Alignment.Center
    ) {


        val items = listOf(
            "0",
            "20",
            "40",
            "60",
            "80",
            "100",
        ).reversed()

        DropdownMenu(
            offset = DpOffset(12.dp, 4.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                //.width(80.dp)
                .background(colorLightBackground2)
                .border(1.dp, color = Color.DarkGray, shape = RoundedCornerShape(16.dp))
        ) {


            items.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    selectedIndex = index
                    expanded = false

                    onValueChangeState.value.invoke(s.toFloat() / 100.0F)

                })
                {
                    Text(text = s, color = Color.White)
                }
            }

        }






























        Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
            //val h = size.height
            //println("h px:$h")
            //println("h dp:${h.toDp()}")

            value

            //Фон
            drawRoundRect(
                color = colorDarkBackground,
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(8.dp.toPx(), (8.dp.toPx()))
            )

            //Зеленый
            drawRoundRect(
                color = MaterialColor.GREEN_700,
                topLeft = Offset(2.dp.toPx(), position),
                size = Size(size.width - 4.dp.toPx(), 16.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx(), (6.dp.toPx()))
            )

            val paint = android.graphics.Paint()

            paint.apply {
                textSize = 14.sp.toPx()
                color = Color.White.toArgb()//.copy(alpha = alphaAnim).toArgb()//0xffff0000.toInt()
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER

                typeface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    // customTypeface
                    Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)


                } else {
                    Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                }
            }

            drawIntoCanvas {
                it.nativeCanvas.drawText(
                    (volume * 100f).format(0),
                    size.width / 2,
                    position + 13.dp.toPx(),
                    paint
                )
            }

            paint.alpha = (alphaAnim * 255f).toInt()

            drawRoundRect(
                color = MaterialColor.LIGHT_BLUE_700,
                topLeft = Offset(0f, position - 70.dp.toPx()),
                alpha = alphaAnim,
                size = Size(size.width, 16.dp.toPx()),
                cornerRadius = CornerRadius(16.dp.toPx(), (16.dp.toPx()))
            )
            drawIntoCanvas {
                it.nativeCanvas.drawText(
                    (volume * 100f).format(0),
                    size.width / 2,
                    position + 13.dp.toPx() - 70.dp.toPx(),
                    paint
                )
            }
        })
    }
}

