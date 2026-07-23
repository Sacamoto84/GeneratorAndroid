package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class GraphDtoTest {

    private val gson = Gson()

    private val sample = NodeGraph(
        nodes = listOf(
            GraphNode(NodeId(1), "Старт", 40f, 120f, NodeBody.Start),
            GraphNode(
                NodeId(2), "Разгон", 220f, 100f,
                NodeBody.Step(
                    StepParams(
                        ch1 = ChannelParams(
                            carrierEnabled = true,
                            carrierFr = Operand.Reg(1),
                            amMod = "02_HWave",
                        ),
                        ch2 = ChannelParams(),
                    ),
                    delayMs = 100L,
                ),
            ),
            GraphNode(
                NodeId(3), "шаг +50", 220f, 240f,
                NodeBody.Register(RegOp.PLUS, 1, Operand.Const(50f)),
            ),
            GraphNode(
                NodeId(4), "до 5 кГц", 220f, 350f,
                NodeBody.Condition(1, CompareOp.LESS, Operand.Const(5000f)),
            ),
            GraphNode(NodeId(5), "Стоп", 40f, 460f, NodeBody.Stop),
        ),
        edges = listOf(
            GraphEdge(NodeId(1), Port.OUT, NodeId(2)),
            GraphEdge(NodeId(2), Port.OUT, NodeId(3)),
            GraphEdge(NodeId(3), Port.OUT, NodeId(4)),
            GraphEdge(NodeId(4), Port.YES, NodeId(2)),
            GraphEdge(NodeId(4), Port.NO, NodeId(5)),
        ),
    )

    @Test
    fun `граф переживает поездку через json`() {
        val json = gson.toJson(sample.toDto())
        val back = gson.fromJson(json, GraphDto::class.java).toDomain()
        assertEquals(sample, back)
    }

    @Test
    fun `счётчик id переживает поездку даже когда больше максимального номера`() {
        //После удаления ноды lastId выше любого живого id — он обязан
        //сохраниться, иначе удалённый номер выдастся заново после перезагрузки
        val afterDelete = sample.withoutNode(NodeId(5))
        val back = gson.fromJson(gson.toJson(afterDelete.toDto()), GraphDto::class.java).toDomain()
        assertEquals(5, back.lastId)
        assertEquals(NodeId(6), back.nextId())
    }

    @Test
    fun `снятая галочка не попадает в json`() {
        val json = gson.toJson(sample.toDto())
        assertFalse(json.contains("fmDev"))
    }

    @Test
    fun `отсутствующий ключ читается как снятая галочка`() {
        val json = "{\"version\":1," +
            "\"nodes\":[{\"id\":1,\"type\":\"STEP\",\"title\":\"x\",\"x\":0,\"y\":0,\"delayMs\":0}]," +
            "\"edges\":[]}"
        val node = gson.fromJson(json, GraphDto::class.java).toDomain().node(NodeId(1))!!
        val step = node.body as NodeBody.Step
        assertNull(step.params.ch1.carrierFr)
        assertEquals(0, step.params.checkedCount)
    }

    @Test
    fun `неизвестный тип ноды это ошибка формата`() {
        val json = "{\"version\":1,\"nodes\":[{\"id\":1,\"type\":\"ТАНЕЦ\",\"x\":0,\"y\":0}],\"edges\":[]}"
        assertThrows(GraphFormatException::class.java) {
            gson.fromJson(json, GraphDto::class.java).toDomain()
        }
    }

    @Test
    fun `версия новее нашей это ошибка формата`() {
        val json = "{\"version\":99,\"nodes\":[],\"edges\":[]}"
        assertThrows(GraphFormatException::class.java) {
            gson.fromJson(json, GraphDto::class.java).toDomain()
        }
    }

    @Test
    fun `нечитаемый операнд это ошибка формата`() {
        val json = "{\"version\":1,\"nodes\":[{\"id\":1,\"type\":\"REGISTER\",\"x\":0,\"y\":0," +
            "\"regOp\":\"PLUS\",\"regDst\":1,\"regSrc\":\"хрень\"}],\"edges\":[]}"
        assertThrows(GraphFormatException::class.java) {
            gson.fromJson(json, GraphDto::class.java).toDomain()
        }
    }
}
