package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.Operand
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeGraphWarningsTest {

    private fun warnings(
        graph: NodeGraph,
        carrier: Set<String> = emptySet(),
        mod: Set<String> = emptySet(),
    ) = validate(graph, carrier, mod).filter { it.severity == Severity.WARNING }

    /** Старт -> Шаг -> сам себя. Задержку задаём параметром. */
    private fun selfLoop(delayMs: Long, params: StepParams = empty()): NodeGraph =
        newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, NodeBody.Step(params, delayMs)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(3))
            .withoutNode(NodeId(2))

    @Test
    fun `цикл без задержки это предупреждение`() {
        assertTrue(warnings(selfLoop(0L)).any { it.text.contains("без задержки") })
    }

    @Test
    fun `цикл с задержкой не ругается`() {
        assertTrue(warnings(selfLoop(100L)).none { it.text.contains("без задержки") })
    }

    @Test
    fun `линейный граф без циклов не ругается`() {
        assertTrue(warnings(newGraph()).none { it.text.contains("без задержки") })
    }

    @Test
    fun `пустой шаг с нулевой задержкой это предупреждение`() {
        assertTrue(warnings(selfLoop(0L)).any { it.text.contains("ничего не делает") })
    }

    @Test
    fun `CR FR и FM BASE вместе это предупреждение`() {
        val params = StepParams(
            ch1 = ChannelParams(carrierFr = Operand.Const(1000f), fmBase = Operand.Const(2000f)),
            ch2 = ChannelParams(),
        )
        assertTrue(warnings(selfLoop(100L, params)).any { it.text.contains("одно и то же поле") })
    }

    @Test
    fun `неизвестное имя формы это предупреждение`() {
        val params = StepParams(
            ch1 = ChannelParams(carrierMod = "Небывалая", amMod = "02_HWave"),
            ch2 = ChannelParams(),
        )
        val w = warnings(selfLoop(100L, params), carrier = setOf("Sine"), mod = setOf("02_HWave"))
        assertTrue(w.any { it.text.contains("Небывалая") })
        assertTrue(w.none { it.text.contains("02_HWave") })
    }

    private fun empty() = StepParams(ChannelParams(), ChannelParams())
}
