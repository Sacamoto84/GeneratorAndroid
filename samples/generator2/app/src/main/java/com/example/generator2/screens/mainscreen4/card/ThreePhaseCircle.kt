package com.example.generator2.screens.mainscreen4.card

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.features.threephase.ThreePhaseEngine
import com.example.generator2.features.threephase.ThreePhaseGeometry
import com.example.generator2.features.threephase.patterns.Trajectory
import kotlin.math.hypot

/** Доля половины ширины под круг — остальное под подписи электродов. */
private const val CIRCLE_FILL = 0.78f

private val colorElectrode = Color(0xFFE53935)
private val colorNeutral = Color(0xFF9E9E9E)
private val colorDot = Color(0xFF01AE0F)

/**
 * Круг трёхфазного режима: треугольник электродов, точка текущей позиции.
 * При [fingerActive] точку водят пальцем, и рисуется записанная траектория.
 */
@Composable
fun ThreePhaseCircle(engine: ThreePhaseEngine, fingerActive: Boolean, modifier: Modifier = Modifier) {

    val finger = engine.patterns.finger

    var alpha by remember { mutableFloatStateOf(0f) }
    var beta by remember { mutableFloatStateOf(0f) }
    var trajectory by remember { mutableStateOf<Trajectory?>(null) }

    //Позиция приходит из аудиопотока — перечитываем каждый кадр
    LaunchedEffect(engine) {
        while (true) withFrameNanos {
            alpha = engine.displayAlpha
            beta = engine.displayBeta
            trajectory = finger.trajectory()
        }
    }

    val textMeasurer = rememberTextMeasurer()

    val touch = if (fingerActive) Modifier.pointerInput(finger) {
        awaitEachGesture {
            fun ab(p: Offset): Pair<Float, Float> {
                val cx = size.width / 2f
                val cy = size.height / 2f
                return ThreePhaseGeometry.fromScreen(p.x, p.y, cx, cy, minOf(cx, cy) * CIRCLE_FILL)
            }

            val down = awaitFirstDown()
            down.consume()
            val (a0, b0) = ab(down.position)
            finger.touchDown(a0, b0, down.uptimeMillis / 1000.0)
            try {
                while (true) {
                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break
                    change.consume() //иначе жест уйдёт в прокрутку экрана
                    val (a, b) = ab(change.position)
                    finger.touchMove(a, b, change.uptimeMillis / 1000.0)
                }
            } finally {
                finger.touchUp()
            }
        }
    } else Modifier

    Canvas(
        modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .then(touch)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = minOf(cx, cy) * CIRCLE_FILL

        fun at(a: Float, b: Float): Offset {
            val (x, y) = ThreePhaseGeometry.toScreen(a, b, cx, cy, r)
            return Offset(x, y)
        }

        val neutral = at(1f, 0f)
        val left = at(-0.5f, 0.8660254f)
        val right = at(-0.5f, -0.8660254f)

        drawPath(
            Path().apply {
                moveTo(neutral.x, neutral.y)
                lineTo(left.x, left.y)
                lineTo(right.x, right.y)
                close()
            },
            color = Color.DarkGray,
            style = Stroke(1.dp.toPx())
        )
        drawCircle(Color.Gray, r, Offset(cx, cy), style = Stroke(2.dp.toPx()))

        val electrode = 8.dp.toPx()
        drawCircle(colorNeutral, electrode, neutral)
        drawCircle(colorElectrode, electrode, left)
        drawCircle(colorElectrode, electrode, right)

        label(textMeasurer, "Neutral", neutral + Offset(electrode * 1.4f, -electrode * 2.2f))
        label(textMeasurer, "L+", left + Offset(-electrode * 2.6f, electrode * 0.6f))
        label(textMeasurer, "R+", right + Offset(electrode * 1.0f, electrode * 0.6f))

        val path = trajectory
        if (fingerActive && path != null && path.alpha.size > 1) {
            val line = Path()
            for (i in path.alpha.indices) {
                val p = at(path.alpha[i], path.beta[i])
                if (i == 0) line.moveTo(p.x, p.y) else line.lineTo(p.x, p.y)
            }
            drawPath(line, Color(0x8001AE0F), style = Stroke(2.dp.toPx()))
        }

        //Звук нормализуется в круг (Orbiting Circles и Deep Throb выходят за 1) — точка тоже
        val norm = maxOf(1f, hypot(alpha, beta))
        drawCircle(colorDot, 7.dp.toPx(), at(alpha / norm, beta / norm))
    }
}

private fun DrawScope.label(textMeasurer: TextMeasurer, text: String, topLeft: Offset) {
    drawText(
        textMeasurer = textMeasurer,
        text = text,
        topLeft = topLeft,
        style = TextStyle(color = Color.LightGray, fontSize = 13.sp)
    )
}
