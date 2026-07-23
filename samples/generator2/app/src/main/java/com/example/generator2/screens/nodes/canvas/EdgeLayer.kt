package com.example.generator2.screens.nodes.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.generator2.features.nodes.model.GraphNode
import com.example.generator2.features.nodes.model.NodeGraph
import com.example.generator2.features.nodes.model.Port
import com.example.generator2.features.nodes.model.node
import kotlin.math.abs
import kotlin.math.max

private val EdgeColor = Color(0xFF86868B)
private val YesColor = Color(0xFF34C759)
private val NoColor = Color(0xFFE5553A)

/**
 * Рёбра рисуются под карточками в тех же мировых координатах.
 * Слой не обрезается по своим границам, поэтому связь, уходящая за экран,
 * рисуется целиком и не «обрубается» на краю.
 */
@Composable
fun EdgeLayer(graph: NodeGraph, modifier: Modifier = Modifier) {

    val density = LocalDensity.current
    val cardW = with(density) { CARD_W.toPx() }
    val cardH = with(density) { CARD_H.toPx() }

    Canvas(modifier.fillMaxSize()) {

        fun px(node: GraphNode): Offset =
            with(density) { Offset(node.x.dp.toPx(), node.y.dp.toPx()) }

        graph.edges.forEach { edge ->
            val from = graph.node(edge.from) ?: return@forEach
            val to = graph.node(edge.to) ?: return@forEach

            val a = px(from)
            val b = px(to)

            val start = Offset(
                x = a.x + cardW,
                y = a.y + when (edge.port) {
                    Port.OUT -> cardH / 2f
                    Port.YES -> cardH * 0.3f
                    Port.NO -> cardH * 0.7f
                },
            )
            val end = Offset(b.x, b.y + cardH / 2f)

            //Горизонтальные усы: провод выходит вбок, как в node-red,
            //и не превращается в прямую диагональ
            val bend = max(with(density) { 48.dp.toPx() }, abs(end.x - start.x) / 2f)

            val path = Path().apply {
                moveTo(start.x, start.y)
                cubicTo(start.x + bend, start.y, end.x - bend, end.y, end.x, end.y)
            }

            drawPath(
                path = path,
                color = when (edge.port) {
                    Port.OUT -> EdgeColor
                    Port.YES -> YesColor
                    Port.NO -> NoColor
                },
                style = Stroke(width = with(density) { 2.dp.toPx() }),
            )
        }
    }
}
