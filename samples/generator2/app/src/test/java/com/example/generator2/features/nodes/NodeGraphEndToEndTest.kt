package com.example.generator2.features.nodes

import com.example.generator2.features.generator.Generator
import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand
import com.example.generator2.features.script.Script
import com.example.generator2.features.script.StateCommandScript
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeGraphEndToEndTest {

    /**
     * Старт -> Регистр(LOAD F1 1000) -> Шаг(CR1 FR F1, 100 мс)
     *       -> Регистр(F1 += 50) -> Условие(F1 < 1200)
     * «да» обратно в Шаг, «нет» в Стоп.
     *
     * Частота несущей должна пройти 1000, 1050, 1100, 1150 и встать:
     * на пятом круге F1 = 1200, условие ложно, идём в Стоп.
     */
    private fun sweep(): NodeGraph {
        val params = StepParams(
            ch1 = ChannelParams(carrierFr = Operand.Reg(1)),
            ch2 = ChannelParams(),
        )
        return newGraph()
            .withNode(
                GraphNode(NodeId(6), "старт F1", 0f, 0f, NodeBody.Register(RegOp.LOAD, 1, Operand.Const(1000f)))
            )
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
            .withEdge(NodeId(1), Port.OUT, NodeId(6))
            .withEdge(NodeId(6), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(4))
            .withEdge(NodeId(4), Port.OUT, NodeId(5))
            .withEdge(NodeId(5), Port.YES, NodeId(3))
            .withEdge(NodeId(5), Port.NO, NodeId(2))
    }

    @Test
    fun `свип компилируется в ожидаемые адреса`() {
        val r = compile(sweep()) as CompileResult.Ok
        assertEquals(
            listOf(
                "LOAD F1 1000.0",
                "GOTO 2",
                "CR1 FR F1",
                "DELAY 100",
                "GOTO 5",
                "PLUS F1 50.0",
                "GOTO 7",
                "IF F1 < 1200.0",
                "GOTO 2",
                "ELSE",
                "GOTO 12",
                "ENDIF",
                "END",
            ),
            r.lines,
        )
    }

    @Test
    fun `граф свипа проводит несущую по ступеням и останавливается`() = runBlocking {

        val gen = Generator()
        val script = Script(gen)
        val compiled = compile(sweep()) as CompileResult.Ok

        script.list.clear()
        compiled.lines.forEach { script.list.add(it) }

        val seen = mutableListOf<Float>()
        val collector = launch(Dispatchers.Default) {
            gen.liveData.ch1_Carrier_Fr.collect { seen.add(it) }
        }

        script.command(StateCommandScript.START)

        //Движок крутится на своём Dispatchers.Default, виртуального времени
        //ему не подсунуть — ждём по-настоящему, но с потолком
        withTimeout(5_000) {
            while (script.state != StateCommandScript.ISTOPPING) delay(10)
        }
        collector.cancel()

        //400 — значение по умолчанию, оно приходит подписчику первым
        assertEquals(listOf(400f, 1000f, 1050f, 1100f, 1150f), seen.distinct())
        assertEquals(0, script.pc.value)
    }

    @Test
    fun `стоп посреди прогона обрывает движение частоты`() = runBlocking {

        val gen = Generator()
        val script = Script(gen)
        val compiled = compile(sweep()) as CompileResult.Ok

        script.list.clear()
        compiled.lines.forEach { script.list.add(it) }

        script.command(StateCommandScript.START)
        withTimeout(2_000) {
            while (gen.liveData.ch1_Carrier_Fr.value < 1000f) delay(5)
        }
        script.command(StateCommandScript.STOP)

        val frozen = gen.liveData.ch1_Carrier_Fr.value
        delay(400)
        assertEquals(frozen, gen.liveData.ch1_Carrier_Fr.value)
        assertTrue(script.state == StateCommandScript.ISTOPPING)
    }
}
