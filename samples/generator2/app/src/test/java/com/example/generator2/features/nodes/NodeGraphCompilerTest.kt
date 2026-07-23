package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.Operand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeGraphCompilerTest {

    private fun ok(graph: NodeGraph): CompileResult.Ok {
        val result = compile(graph)
        assertTrue("ожидался Ok, получено $result", result is CompileResult.Ok)
        return result as CompileResult.Ok
    }

    @Test
    fun `новый граф это одна строка END`() {
        val r = ok(newGraph())
        assertEquals(listOf("END"), r.lines)
        assertEquals(listOf(NodeId(2)), r.lineToNode)
    }

    @Test
    fun `битый граф не компилируется`() {
        val result = compile(newGraph().withoutEdge(NodeId(1), Port.OUT))
        assertTrue(result is CompileResult.Failed)
    }

    @Test
    fun `шаг печатает значения потом переключатели потом задержку и переход`() {
        val params = StepParams(
            ch1 = ChannelParams(
                carrierEnabled = true,
                carrierFr = Operand.Const(1000f),
                carrierMod = "Sine",
            ),
            ch2 = ChannelParams(),
        )
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, NodeBody.Step(params, 500L)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))

        val r = ok(g)
        assertEquals(
            listOf(
                "CR1 MOD Sine",
                "CR1 FR 1000.0",
                "CH1 CR ON",
                "DELAY 500",
                "GOTO 5",
                "END",
            ),
            r.lines,
        )
        assertEquals(
            listOf(NodeId(3), NodeId(3), NodeId(3), NodeId(3), NodeId(3), NodeId(2)),
            r.lineToNode,
        )
    }

    @Test
    fun `нулевая задержка не печатается`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, NodeBody.Step(empty(), 0L)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))

        assertEquals(listOf("GOTO 1", "END"), ok(g).lines)
    }

    @Test
    fun `второй канал печатается после первого`() {
        val params = StepParams(
            ch1 = ChannelParams(carrierFr = Operand.Const(100f)),
            ch2 = ChannelParams(amFr = Operand.Reg(3)),
        )
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, NodeBody.Step(params, 0L)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))

        assertEquals(
            listOf("CR1 FR 100.0", "AM2 FR F3", "GOTO 3", "END"),
            ok(g).lines,
        )
    }

    private fun empty() = StepParams(ChannelParams(), ChannelParams())
}
