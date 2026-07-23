package com.example.generator2.screens.nodes.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.generator2.features.nodes.model.NodeGraph
import com.example.generator2.features.nodes.model.NodeId
import kotlin.math.roundToInt

private const val MIN_SCALE = 0.4f
private const val MAX_SCALE = 2.5f

/**
 * Холст графа.
 *
 * Ноды — обычные composable внутри одного слоя с graphicsLayer.
 * Начало трансформации сдвинуто в левый верхний угол (TransformOrigin(0,0)),
 * поэтому мировая точка p оказывается на экране в p * scale + offset —
 * без этого зум считался бы от центра и вся арифметика была бы вдвое сложнее.
 *
 * @param fitKey смена значения вписывает граф в экран: подаётся имя графа
 * @param onMove дельты перемещения ноды в dp
 */
@Composable
fun NodeCanvas(
    graph: NodeGraph,
    selected: NodeId?,
    activeNode: NodeId?,
    linking: Boolean,
    canBeTarget: (NodeId) -> Boolean,
    orientation: Orientation,
    fitKey: Any,
    focusNode: NodeId?,
    focusKey: Int,
    onSelect: (NodeId) -> Unit,
    onTapEmpty: () -> Unit,
    onMove: (NodeId, Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }

    //Вписать граф при открытии: масштаб и смещение в файле не хранятся
    LaunchedEffect(fitKey, viewport) {
        if (viewport.width == 0 || graph.nodes.isEmpty()) return@LaunchedEffect

        val minX = graph.nodes.minOf { it.x }
        val minY = graph.nodes.minOf { it.y }
        val maxX = graph.nodes.maxOf { it.x } + CARD_W.value
        val maxY = graph.nodes.maxOf { it.y } + CARD_H.value

        val margin = with(density) { 24.dp.toPx() }
        val w = with(density) { (maxX - minX).dp.toPx() } + margin * 2
        val h = with(density) { (maxY - minY).dp.toPx() } + margin * 2

        scale = minOf(viewport.width / w, viewport.height / h).coerceIn(MIN_SCALE, 1f)
        offset = Offset(
            x = margin - with(density) { minX.dp.toPx() } * scale,
            y = margin - with(density) { minY.dp.toPx() } * scale,
        )
    }

    //Навести холст на конкретную ноду: тап по проблеме в списке
    LaunchedEffect(focusKey) {
        val n = focusNode?.let { id -> graph.nodes.firstOrNull { it.id == id } } ?: return@LaunchedEffect
        if (viewport.width == 0) return@LaunchedEffect

        val cx = with(density) { n.x.dp.toPx() } + with(density) { CARD_W.toPx() } / 2f
        val cy = with(density) { n.y.dp.toPx() } + with(density) { CARD_H.toPx() } / 2f

        offset = Offset(viewport.width / 2f - cx * scale, viewport.height / 2f - cy * scale)
    }

    Box(
        modifier
            .fillMaxSize()
            .background(Color(0xFF1D1D1F))
            .onSizeChanged { viewport = it }
            .pointerInput(Unit) {
                detectTapGestures { onTapEmpty() }
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val next = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                    //Держим точку под пальцами на месте: она не должна уезжать при зуме
                    offset = centroid + pan - (centroid - offset) * (next / scale)
                    scale = next
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
                    transformOrigin = TransformOrigin(0f, 0f),
                )
        ) {
            EdgeLayer(graph, orientation)

            graph.nodes.forEach { node ->
                val dimmed = linking && !canBeTarget(node.id)

                NodeCard(
                    node = node,
                    isSelected = node.id == selected,
                    isActive = node.id == activeNode,
                    isDimmed = dimmed,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                with(density) { node.x.dp.toPx() }.roundToInt(),
                                with(density) { node.y.dp.toPx() }.roundToInt(),
                            )
                        }
                        .pointerInput(node.id, linking) {
                            detectTapGestures { onSelect(node.id) }
                        }
                        .pointerInput(node.id, linking) {
                            if (linking) return@pointerInput
                            detectDragGestures { change, drag ->
                                change.consume()
                                //Дельта приходит в локальных координатах слоя,
                                //то есть уже без масштаба: делить на scale не надо.
                                //Если на устройстве нода при зуме отстаёт от пальца
                                //или обгоняет его — поделите на scale здесь.
                                onMove(
                                    node.id,
                                    drag.x / density.density,
                                    drag.y / density.density,
                                )
                            }
                        },
                )
            }
        }
    }
}
