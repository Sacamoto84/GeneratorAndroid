package com.example.generator2.screens.nodes.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.generator2.features.nodes.model.GraphNode
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.NodeGraph
import com.example.generator2.features.nodes.model.Port
import com.example.generator2.features.nodes.model.node
import com.example.generator2.features.nodes.model.ports
import kotlin.math.hypot
import kotlin.math.max

private val EdgeColor = Color(0xFF86868B)
private val YesColor = Color(0xFF34C759)
private val NoColor = Color(0xFFE5553A)
private val EntryColor = Color(0xFF9A9AA0)
private val CanvasBg = Color(0xFF1D1D1F)

/**
 * Рёбра и значки портов рисуются под карточками в тех же мировых координатах.
 * Слой не обрезается по своим границам, поэтому связь, уходящая за экран,
 * рисуется целиком и не «обрубается» на краю.
 */
@Composable
fun EdgeLayer(graph: NodeGraph, orientation: Orientation, modifier: Modifier = Modifier) {

    val density = LocalDensity.current
    val cardW = with(density) { CARD_W.toPx() }
    val cardH = with(density) { CARD_H.toPx() }
    val minBend = with(density) { 48.dp.toPx() }
    val stroke = with(density) { 2.dp.toPx() }
    val portR = with(density) { 5.dp.toPx() }

    //Наружные нормали выходной и входной сторон: вдоль них гнутся провода
    val (outN, inN) = when (orientation) {
        Orientation.LR -> Offset(1f, 0f) to Offset(-1f, 0f)
        Orientation.RL -> Offset(-1f, 0f) to Offset(1f, 0f)
        Orientation.TB -> Offset(0f, 1f) to Offset(0f, -1f)
        Orientation.BT -> Offset(0f, -1f) to Offset(0f, 1f)
    }

    Canvas(modifier.fillMaxSize()) {

        fun topLeft(node: GraphNode): Offset =
            with(density) { Offset(node.x.dp.toPx(), node.y.dp.toPx()) }

        //───── рёбра ─────
        graph.edges.forEach { edge ->
            val from = graph.node(edge.from) ?: return@forEach
            val to = graph.node(edge.to) ?: return@forEach

            val start = topLeft(from) + orientation.exitAnchor(edge.port, cardW, cardH)
            val end = topLeft(to) + orientation.entryAnchor(cardW, cardH)

            val bend = max(minBend, hypot(end.x - start.x, end.y - start.y) / 2f)
            val c1 = start + outN * bend
            val c2 = end + inN * bend

            val path = Path().apply {
                moveTo(start.x, start.y)
                cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)
            }

            drawPath(
                path = path,
                color = when (edge.port) {
                    Port.OUT -> EdgeColor
                    Port.YES -> YesColor
                    Port.NO -> NoColor
                },
                style = Stroke(width = stroke),
            )
        }

        //───── значки портов ─────
        //Все треугольники смотрят вдоль потока: наружу выхода = внутрь входа
        //следующей ноды, поэтому направление у входа и выхода одно.
        val flow = when (orientation) {
            Orientation.LR -> Offset(1f, 0f)
            Orientation.RL -> Offset(-1f, 0f)
            Orientation.TB -> Offset(0f, 1f)
            Orientation.BT -> Offset(0f, -1f)
        }

        graph.nodes.forEach { node ->
            val tl = topLeft(node)

            //вход: есть у всех, кроме Старта. Контурный треугольник
            if (node.body !is NodeBody.Start) {
                val p = tl + orientation.entryAnchor(cardW, cardH)
                drawTriangle(p, flow, portR, EntryColor, filled = false, stroke)
            }

            //выходы: залитые треугольники цвета порта
            node.body.ports().forEach { port ->
                val p = tl + orientation.exitAnchor(port, cardW, cardH)
                val color = when (port) {
                    Port.OUT -> EntryColor
                    Port.YES -> YesColor
                    Port.NO -> NoColor
                }
                //подложка цвета холста, чтобы провод не просвечивал сквозь значок
                drawTriangle(p, flow, portR + stroke, CanvasBg, filled = true, stroke)
                drawTriangle(p, flow, portR, color, filled = true, stroke)
            }
        }
    }
}

/** Треугольник с вершиной в направлении dir, центром в center */
private fun DrawScope.drawTriangle(
    center: Offset,
    dir: Offset,
    size: Float,
    color: Color,
    filled: Boolean,
    stroke: Float,
) {
    val forward = dir * size
    //перпендикуляр к dir той же длины — боковые вершины основания
    val perp = Offset(-dir.y, dir.x) * size

    val tip = center + forward
    val a = center - forward + perp
    val b = center - forward - perp

    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(a.x, a.y)
        lineTo(b.x, b.y)
        close()
    }

    drawPath(path, color, style = if (filled) Fill else Stroke(width = stroke))
}
