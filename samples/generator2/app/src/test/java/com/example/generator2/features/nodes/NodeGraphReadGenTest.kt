package com.example.generator2.features.nodes

import com.example.generator2.features.generator.Generator
import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.GenBlock
import com.example.generator2.features.script.GenParam
import com.example.generator2.features.script.Operand
import com.example.generator2.features.script.Script
import com.example.generator2.features.script.StateCommandScript
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeGraphReadGenTest {

    private fun ok(graph: NodeGraph): CompileResult.Ok {
        val result = compile(graph)
        assertTrue("ожидался Ok, получено $result", result is CompileResult.Ok)
        return result as CompileResult.Ok
    }

    @Test
    fun `чтение компилируется в READ и переход`() {
        val g = newGraph()
            .withNode(
                GraphNode(NodeId(3), "Читаем", 0f, 0f, NodeBody.ReadGen(1, 1, GenBlock.CR, GenParam.FR))
            )
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))

        val r = ok(g)
        assertEquals(listOf("READ F1 CR1 FR", "GOTO 2", "END"), r.lines)
    }

    @Test
    fun `регистр вне диапазона это ошибка`() {
        val g = newGraph()
            .withNode(
                GraphNode(NodeId(3), "Читаем", 0f, 0f, NodeBody.ReadGen(42, 1, GenBlock.CR, GenParam.FR))
            )
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))

        assertTrue(validate(g).any { it.severity == Severity.ERROR && it.text.contains("вне F0..F9") })
    }

    @Test
    fun `граф читает частоту несущей в регистр и сравнивает`() = runBlocking {
        //Старт -> Чтение(F1 = CR1 FR) -> Условие(F1 > 500) —да→ Стоп, —нет→ Стоп
        //Несущую заранее ставим 1000, значит F1 станет 1000 и условие истинно
        val g = newGraph()
            .withNode(
                GraphNode(NodeId(3), "Читаем", 0f, 0f, NodeBody.ReadGen(1, 1, GenBlock.CR, GenParam.FR))
            )
            .withNode(
                GraphNode(
                    NodeId(4), "Проверка", 0f, 0f,
                    //пауза после, чтобы регистр успел попасть в снимок для UI и теста
                    NodeBody.Condition(1, CompareOp.GREATER, Operand.Const(500f), 0L, 50L),
                )
            )
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(4))
            .withEdge(NodeId(4), Port.YES, NodeId(2))
            .withEdge(NodeId(4), Port.NO, NodeId(2))

        val gen = Generator()
        gen.liveData.ch1_Carrier_Fr.value = 1000f
        val script = Script(gen)
        val compiled = ok(g)

        script.list.clear()
        compiled.lines.forEach { script.list.add(it) }

        script.command(StateCommandScript.START)
        withTimeout(3_000) {
            while (script.state != StateCommandScript.ISTOPPING) delay(10)
        }

        //Регистр F1 получил частоту несущей
        assertEquals(1000f, script.register[1], 0.001f)
    }
}
