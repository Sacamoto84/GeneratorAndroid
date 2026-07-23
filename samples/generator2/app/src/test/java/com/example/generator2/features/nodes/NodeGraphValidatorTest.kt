package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeGraphValidatorTest {

    private fun errors(graph: NodeGraph) =
        validate(graph).filter { it.severity == Severity.ERROR }

    @Test
    fun `новый граф без ошибок`() {
        assertEquals(emptyList<Issue>(), errors(newGraph()))
    }

    @Test
    fun `граф без Старта это ошибка`() {
        val g = newGraph().withoutNode(NodeId(1))
        assertTrue(errors(g).any { it.text.contains("нет ноды Старт") })
    }

    @Test
    fun `два Старта это ошибка`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Старт 2", 0f, 0f, NodeBody.Start))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))
        assertTrue(errors(g).any { it.text.contains("Старт должен быть один") })
    }

    @Test
    fun `у Старта без связи ошибка`() {
        val g = newGraph().withoutEdge(NodeId(1), Port.OUT)
        assertTrue(errors(g).any { it.nodeId == NodeId(1) && it.text.contains("исходящей связи") })
    }

    @Test
    fun `у Шага без связи ошибка`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, step()))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
        assertTrue(errors(g).any { it.nodeId == NodeId(3) && it.text.contains("исходящей связи") })
    }

    @Test
    fun `у Условия пустой выход да это ошибка`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Если", 0f, 0f, condition()))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.NO, NodeId(2))
        assertTrue(errors(g).any { it.text.contains("«да»") })
    }

    @Test
    fun `недостижимая нода это ошибка`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Сирота", 0f, 0f, step()))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))
        assertTrue(errors(g).any { it.nodeId == NodeId(3) && it.text.contains("недостижима") })
    }

    @Test
    fun `связь в несуществующую ноду это ошибка`() {
        val g = newGraph().withEdge(NodeId(1), Port.OUT, NodeId(77))
        assertTrue(errors(g).any { it.text.contains("несуществующую") })
    }

    @Test
    fun `связь в Старт это ошибка`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, step()))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(1))
        assertTrue(errors(g).any { it.text.contains("нельзя вести связь") })
    }

    @Test
    fun `отрицательная задержка это ошибка`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, step(delayMs = -5L)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))
        assertTrue(errors(g).any { it.text.contains("Отрицательная задержка") })
    }

    @Test
    fun `регистр вне диапазона это ошибка`() {
        val g = newGraph()
            .withNode(
                GraphNode(NodeId(3), "Рег", 0f, 0f, NodeBody.Register(RegOp.LOAD, 42, Operand.Const(1f)))
            )
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))
        assertTrue(errors(g).any { it.text.contains("вне F0..F9") })
    }

    private fun step(delayMs: Long = 100L) =
        NodeBody.Step(StepParams(ChannelParams(), ChannelParams()), delayMs)

    private fun condition() =
        NodeBody.Condition(1, CompareOp.LESS, Operand.Const(5f))
}
