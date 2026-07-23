package com.example.generator2.screens.nodes.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.features.nodes.model.GraphNode
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.RegOp
import com.example.generator2.features.script.toToken

/**
 * Размер карточки фиксирован намеренно: якоря рёбер тогда считаются
 * арифметикой, без onSizeChanged, без карты размеров и без дёрганья
 * рёбер на первом кадре.
 */
val CARD_W = 168.dp
val CARD_H = 72.dp

private val ColorStep = Color(0xFF3A7BD5)
private val ColorRegister = Color(0xFFA06CD5)
private val ColorCondition = Color(0xFFFF9F0A)
private val ColorStart = Color(0xFF34C759)
private val ColorStop = Color(0xFFE5553A)
private val ColorDelay = Color(0xFF5AC8FA)

fun NodeBody.accent(): Color = when (this) {
    is NodeBody.Start -> ColorStart
    is NodeBody.Stop -> ColorStop
    is NodeBody.Step -> ColorStep
    is NodeBody.Delay -> ColorDelay
    is NodeBody.Register -> ColorRegister
    is NodeBody.Condition -> ColorCondition
}

@Composable
fun NodeCard(
    node: GraphNode,
    isSelected: Boolean,
    isActive: Boolean,
    isDimmed: Boolean,
    modifier: Modifier = Modifier,
) {
    val border = when {
        isActive -> ColorStart
        isSelected -> ColorStep
        else -> Color(0xFF424245)
    }

    Row(
        modifier
            .size(CARD_W, CARD_H)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2D2D2F).copy(alpha = if (isDimmed) 0.35f else 1f))
            .border(if (isSelected || isActive) 2.dp else 1.dp, border, RoundedCornerShape(10.dp))
    ) {
        Box(
            Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(node.body.accent().copy(alpha = if (isDimmed) 0.35f else 1f))
        )

        Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp).fillMaxWidth()) {
            Text(
                node.title,
                color = Color.White.copy(alpha = if (isDimmed) 0.4f else 1f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            summary(node).forEach {
                Text(
                    it,
                    color = Color(0xFF9A9AA0),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Две строки под именем: что нода сделает. Больше не влезает в 72 dp. */
private fun summary(node: GraphNode): List<String> = when (val b = node.body) {
    is NodeBody.Start -> listOf("вход в граф")
    is NodeBody.Stop -> listOf("конец")

    is NodeBody.Step -> buildList {
        val n = b.params.checkedCount
        add(if (n == 0) "параметры не заданы" else "параметров: $n")
        if (b.delayMs > 0) add("задержка ${b.delayMs} мс")
    }

    is NodeBody.Delay -> listOf("пауза ${b.delayMs} мс")

    is NodeBody.Register -> listOf(
        when (b.op) {
            RegOp.LOAD -> "F${b.dst} = ${b.src.toToken()}"
            RegOp.PLUS -> "F${b.dst} += ${b.src.toToken()}"
            RegOp.MINUS -> "F${b.dst} -= ${b.src.toToken()}"
        }
    )

    is NodeBody.Condition -> buildList {
        add("F${b.left} ${b.op.text} ${b.right.toToken()}")
        val delays = buildList {
            if (b.delayBeforeMs > 0) add("до ${b.delayBeforeMs}")
            if (b.delayAfterMs > 0) add("после ${b.delayAfterMs}")
        }
        add(if (delays.isEmpty()) "да ↗   нет ↘" else "⏱ " + delays.joinToString("  "))
    }
}
