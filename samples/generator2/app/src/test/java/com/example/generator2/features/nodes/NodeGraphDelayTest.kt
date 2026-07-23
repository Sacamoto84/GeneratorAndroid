package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeGraphDelayTest {

    private fun ok(graph: NodeGraph): CompileResult.Ok {
        val result = compile(graph)
        assertTrue("ожидался Ok, получено $result", result is CompileResult.Ok)
        return result as CompileResult.Ok
    }

    //╭─ Нода Задержка ───────────────────────────────────────────────────────╮

    @Test
    fun `нода Задержка это DELAY и переход`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Пауза", 0f, 0f, NodeBody.Delay(500L)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))

        val r = ok(g)
        assertEquals(listOf("DELAY 500", "GOTO 2", "END"), r.lines)
        assertEquals(listOf(NodeId(3), NodeId(3), NodeId(2)), r.lineToNode)
    }

    @Test
    fun `нулевая задержка ноды не печатает DELAY`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Пауза", 0f, 0f, NodeBody.Delay(0L)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))

        assertEquals(listOf("GOTO 1", "END"), ok(g).lines)
    }

    @Test
    fun `отрицательная задержка ноды это ошибка`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Пауза", 0f, 0f, NodeBody.Delay(-1L)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))

        val errors = validate(g).filter { it.severity == Severity.ERROR }
        assertTrue(errors.any { it.text.contains("Отрицательная задержка") })
    }

    @Test
    fun `цикл только из ноды Задержки не ругается на отсутствие задержки`() {
        //Старт -> Задержка(100) -> сам себя
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Пауза", 0f, 0f, NodeBody.Delay(100L)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(3))
            .withoutNode(NodeId(2))

        val w = validate(g).filter { it.severity == Severity.WARNING }
        assertTrue(w.none { it.text.contains("без задержки") })
    }

    //╭─ Задержки в Условии ──────────────────────────────────────────────────╮

    /** Старт -> Условие(F1<5, before=B, after=A) -> да в Стоп, нет в Стоп */
    private fun conditionGraph(before: Long, after: Long): NodeGraph =
        newGraph()
            .withNode(
                GraphNode(
                    NodeId(3), "Если", 0f, 0f,
                    NodeBody.Condition(1, CompareOp.LESS, Operand.Const(5f), before, after),
                )
            )
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.YES, NodeId(2))
            .withEdge(NodeId(3), Port.NO, NodeId(2))

    @Test
    fun `условие без задержек по-прежнему пять строк`() {
        val r = ok(conditionGraph(before = 0L, after = 0L))
        assertEquals(
            listOf("IF F1 < 5.0", "GOTO 5", "ELSE", "GOTO 5", "ENDIF", "END"),
            r.lines,
        )
    }

    @Test
    fun `задержка до стоит перед IF`() {
        val r = ok(conditionGraph(before = 200L, after = 0L))
        assertEquals(
            listOf("DELAY 200", "IF F1 < 5.0", "GOTO 6", "ELSE", "GOTO 6", "ENDIF", "END"),
            r.lines,
        )
    }

    @Test
    fun `задержка после стоит в обеих ветках`() {
        val r = ok(conditionGraph(before = 0L, after = 300L))
        assertEquals(
            listOf(
                "IF F1 < 5.0",
                "DELAY 300",
                "GOTO 7",
                "ELSE",
                "DELAY 300",
                "GOTO 7",
                "ENDIF",
                "END",
            ),
            r.lines,
        )
    }

    @Test
    fun `движок с задержкой после уходит в ветку да через DELAY`() {
        //На истинном условии движок ставит pc = current+1 — там DELAY, потом GOTO
        val lines = ok(conditionGraph(before = 0L, after = 300L)).lines
        val ifLine = lines.indexOfFirst { it.startsWith("IF") }
        assertEquals("DELAY 300", lines[ifLine + 1])
    }

    @Test
    fun `все строки условия с задержками разбираются и переходы в диапазоне`() {
        val r = ok(conditionGraph(before = 200L, after = 300L))
        r.lines.forEachIndexed { i, line ->
            val cmd = com.example.generator2.features.script.parseCommand(line, i)
            if (cmd is com.example.generator2.features.script.Cmd.Goto) {
                assertTrue("GOTO ${cmd.target} вне диапазона", cmd.target in r.lines.indices)
            }
        }
        assertEquals(r.lines.size, r.lineToNode.size)
    }
}
