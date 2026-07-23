package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.Operand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeGraphTest {

    @Test
    fun `новый граф это Старт со стрелкой в Стоп`() {
        val g = newGraph()
        assertEquals(2, g.nodes.size)
        assertTrue(g.startNode()!!.body is NodeBody.Start)
        val stop = g.target(g.startNode()!!.id, Port.OUT)
        assertTrue(g.node(stop!!)!!.body is NodeBody.Stop)
    }

    @Test
    fun `следующий id больше максимального`() {
        assertEquals(NodeId(3), newGraph().nextId())
    }

    @Test
    fun `id не переиспользуется после удаления`() {
        val g = newGraph()
        val afterDelete = g.withoutNode(NodeId(2))
        assertEquals(NodeId(3), afterDelete.nextId())
    }

    @Test
    fun `id не всплывает и после нескольких удалений`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, emptyStep()))
            .withNode(GraphNode(NodeId(4), "Шаг", 0f, 0f, emptyStep()))
            .withoutNode(NodeId(4))
            .withoutNode(NodeId(3))

        assertEquals(NodeId(5), g.nextId())
    }

    @Test
    fun `правка ноды не двигает счётчик id`() {
        val g = newGraph()
        val start = g.node(NodeId(1))!!
        val moved = g.withNode(start.copy(x = 500f))

        assertEquals(g.nextId(), moved.nextId())
    }

    @Test
    fun `новая связь из занятого порта заменяет старую`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, emptyStep()))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))

        assertEquals(1, g.edges.count { it.from == NodeId(1) && it.port == Port.OUT })
        assertEquals(NodeId(3), g.target(NodeId(1), Port.OUT))
    }

    @Test
    fun `удаление ноды уносит все её рёбра`() {
        val g = newGraph().withoutNode(NodeId(2))
        assertTrue(g.edges.isEmpty())
        assertNull(g.target(NodeId(1), Port.OUT))
    }

    @Test
    fun `порты зависят от типа ноды`() {
        assertEquals(listOf(Port.OUT), NodeBody.Start.ports())
        assertEquals(listOf(Port.OUT), emptyStep().ports())
        assertEquals(listOf(Port.YES, Port.NO), condition().ports())
        assertEquals(emptyList<Port>(), NodeBody.Stop.ports())
    }

    private fun emptyStep() = NodeBody.Step(StepParams(ChannelParams(), ChannelParams()), 0L)

    private fun condition() =
        NodeBody.Condition(1, com.example.generator2.features.script.CompareOp.LESS, Operand.Const(5f))
}
