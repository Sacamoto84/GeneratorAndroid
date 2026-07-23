package com.example.generator2.screens.nodes.bottom

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.features.nodes.model.GraphNode
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.Port
import com.example.generator2.features.nodes.model.target
import com.example.generator2.features.nodes.model.NodeGraph

/**
 * Панель появляется, когда что-то выделено.
 *
 * Тапа по кривой в дизайне нет: правило «один порт — одна связь» означает,
 * что связь однозначно задаётся парой «нода и порт», поэтому и создаётся,
 * и переподключается, и удаляется она отсюда.
 */
@Composable
fun NodeActionBar(
    graph: NodeGraph,
    node: GraphNode,
    onParams: () -> Unit,
    onLink: (Port) -> Unit,
    onUnlink: (Port) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D2D2F))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        val hasParams = node.body !is NodeBody.Start && node.body !is NodeBody.Stop
        if (hasParams) Action("⚙ Параметры", onParams)

        when (node.body) {
            is NodeBody.Condition -> {
                LinkAction(graph, node, Port.YES, "да", onLink, onUnlink)
                LinkAction(graph, node, Port.NO, "нет", onLink, onUnlink)
            }

            is NodeBody.Stop -> Unit

            else -> LinkAction(graph, node, Port.OUT, null, onLink, onUnlink)
        }

        if (node.body !is NodeBody.Start) {
            Action("⧉ Дублировать", onDuplicate)
            Action("🗑 Удалить", onDelete, Color(0xFFE5553A))
        }
    }
}

@Composable
private fun LinkAction(
    graph: NodeGraph,
    node: GraphNode,
    port: Port,
    label: String?,
    onLink: (Port) -> Unit,
    onUnlink: (Port) -> Unit,
) {
    val connected = graph.target(node.id, port) != null
    val suffix = label?.let { " «$it»" }.orEmpty()

    Action(if (connected) "→ Переподключить$suffix" else "→ Связь$suffix", { onLink(port) })
    if (connected) Action("✕ Отвязать$suffix", { onUnlink(port) })
}

@Composable
private fun Action(text: String, onClick: () -> Unit, color: Color = Color.White) {
    TextButton(onClick = onClick) {
        Text(text, color = color, fontSize = 13.sp, maxLines = 1)
    }
}
