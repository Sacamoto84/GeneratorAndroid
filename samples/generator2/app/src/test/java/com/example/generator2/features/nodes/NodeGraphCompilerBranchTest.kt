package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.Cmd
import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand
import com.example.generator2.features.script.findPairLine
import com.example.generator2.features.script.parseCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeGraphCompilerBranchTest {

    /**
     * Старт -> Шаг(CR1 FR F1, 100 мс) -> Регистр(F1 += 50) -> Условие(F1 < 1200)
     * «да» обратно в Шаг, «нет» в Стоп.
     */
    private fun sweep(): NodeGraph {
        val params = StepParams(
            ch1 = ChannelParams(carrierFr = Operand.Reg(1)),
            ch2 = ChannelParams(),
        )
        return newGraph()
            .withNode(GraphNode(NodeId(3), "Разгон", 0f, 0f, NodeBody.Step(params, 100L)))
            .withNode(
                GraphNode(NodeId(4), "шаг", 0f, 0f, NodeBody.Register(RegOp.PLUS, 1, Operand.Const(50f)))
            )
            .withNode(
                GraphNode(
                    NodeId(5), "предел", 0f, 0f,
                    NodeBody.Condition(1, CompareOp.LESS, Operand.Const(1200f)),
                )
            )
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(4))
            .withEdge(NodeId(4), Port.OUT, NodeId(5))
            .withEdge(NodeId(5), Port.YES, NodeId(3))
            .withEdge(NodeId(5), Port.NO, NodeId(2))
    }

    private fun ok(graph: NodeGraph): CompileResult.Ok {
        val result = compile(graph)
        assertTrue("ожидался Ok, получено $result", result is CompileResult.Ok)
        return result as CompileResult.Ok
    }

    @Test
    fun `свип компилируется в ожидаемые строки`() {
        assertEquals(
            listOf(
                "CR1 FR F1",
                "DELAY 100",
                "GOTO 3",
                "PLUS F1 50.0",
                "GOTO 5",
                "IF F1 < 1200.0",
                "GOTO 0",
                "ELSE",
                "GOTO 10",
                "ENDIF",
                "END",
            ),
            ok(sweep()).lines,
        )
    }

    @Test
    fun `каждая строка знает свою ноду`() {
        assertEquals(
            listOf(3, 3, 3, 4, 4, 5, 5, 5, 5, 5, 2).map { NodeId(it) },
            ok(sweep()).lineToNode,
        )
    }

    @Test
    fun `движок на истинном условии уходит в ветку да`() {
        val lines = ok(sweep()).lines
        val ifLine = lines.indexOfFirst { it.startsWith("IF") }
        //Движок ставит pc = current + 1, там стоит переход ветки «да»
        val cmd = parseCommand(lines[ifLine + 1], ifLine + 1)
        assertEquals(Cmd.Goto(0), cmd)
    }

    @Test
    fun `движок на ложном условии уходит в ветку нет`() {
        val lines = ok(sweep()).lines
        val ifLine = lines.indexOfFirst { it.startsWith("IF") }
        //Ровно так движок ищет ложную ветку
        val target = findPairLine(lines, ifLine, stopOnElse = true)
        assertEquals(Cmd.Goto(10), parseCommand(lines[target], target))
    }

    @Test
    fun `все строки разбираются и переходы попадают в диапазон`() {
        val r = ok(sweep())
        r.lines.forEachIndexed { i, line ->
            val cmd = parseCommand(line, i)
            if (cmd is Cmd.Goto) assertTrue("GOTO ${cmd.target} вне диапазона", cmd.target in r.lines.indices)
        }
        assertEquals(r.lines.size, r.lineToNode.size)
    }

    @Test
    fun `самопроверка ловит строку, которую движок не разбирает`() {
        //Пустое имя формы даёт "CR1 MOD " — движку не хватает третьего
        //аргумента, и он бросает ScriptException. Через UI такое имя не
        //набрать, но самопроверка обязана поймать любой битый вывод компилятора.
        val params = StepParams(
            ch1 = ChannelParams(carrierMod = ""),
            ch2 = ChannelParams(),
        )
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, NodeBody.Step(params, 0L)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))

        val result = compile(g)
        assertTrue("ожидался Failed, получено $result", result is CompileResult.Failed)
        assertTrue(
            (result as CompileResult.Failed).errors.any { it.text.contains("Внутренняя ошибка") }
        )
    }
}
