package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.ChannelParams
import com.example.generator2.features.nodes.model.GraphNode
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.NodeId
import com.example.generator2.features.nodes.model.Port
import com.example.generator2.features.nodes.model.StepParams
import com.example.generator2.features.nodes.model.newGraph
import com.example.generator2.features.nodes.model.node
import com.example.generator2.features.nodes.model.target
import com.example.generator2.features.nodes.model.withEdge
import com.example.generator2.features.nodes.model.withNode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Порядок нод в графе — часть контракта: правленая нода уезжает в конец списка,
 * то есть рисуется поверх остальных.
 *
 * Из-за этого холст обязан раскладывать карточки с key(node.id): без ключа
 * Compose опознаёт их по позиции, перестановка меняет позиции прямо во время
 * перетаскивания и обрывает жест. Если правила порядка здесь меняются —
 * проверить NodeCanvas.
 */
class NodeGraphWithNodeTest {

    private fun step() = NodeBody.Step(StepParams(ChannelParams(), ChannelParams()), 0L)

    @Test
    fun `правка ноды не плодит дубликаты`() {
        val g = newGraph()
        val moved = g.withNode(g.node(NodeId(1))!!.copy(x = 100f))

        assertEquals(g.nodes.size, moved.nodes.size)
        assertEquals(1, moved.nodes.count { it.id == NodeId(1) })
        assertEquals(100f, moved.node(NodeId(1))!!.x, 0f)
    }

    @Test
    fun `правленая нода встаёт последней`() {
        val g = newGraph() //Старт(1), Стоп(2)
        val moved = g.withNode(g.node(NodeId(1))!!.copy(x = 100f))

        assertEquals(NodeId(1), moved.nodes.last().id)
        assertEquals(listOf(NodeId(2), NodeId(1)), moved.nodes.map { it.id })
    }

    @Test
    fun `повторная правка той же ноды порядок уже не меняет`() {
        val g = newGraph().withNode(newGraph().node(NodeId(1))!!.copy(x = 100f))
        val order = g.nodes.map { it.id }

        val again = g.withNode(g.node(NodeId(1))!!.copy(x = 200f))

        assertEquals(order, again.nodes.map { it.id })
    }

    @Test
    fun `новая нода дописывается в конец`() {
        val g = newGraph().withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, step()))

        assertEquals(3, g.nodes.size)
        assertEquals(NodeId(3), g.nodes.last().id)
    }

    @Test
    fun `правка ноды не трогает рёбра`() {
        val g = newGraph().withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, step()))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))

        val moved = g.withNode(g.node(NodeId(3))!!.copy(y = 50f))

        assertEquals(g.edges, moved.edges)
        assertEquals(NodeId(3), moved.target(NodeId(1), Port.OUT))
    }
}
