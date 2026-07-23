package com.example.generator2.features.nodes.model

import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand
import com.example.generator2.features.script.parseOperand
import com.example.generator2.features.script.toToken

/** Файл .ng не прочитан: не тот json, неизвестный тип, битый операнд, чужая версия */
class GraphFormatException(message: String) : Exception(message)

const val GRAPH_FORMAT_VERSION = 1

/**
 * Представление графа на диске.
 *
 * Все поля nullable намеренно: Gson создаёт объект в обход конструктора,
 * значения по умолчанию не срабатывают, и любое отсутствующее поле пришло бы
 * нулём или null мимо системы типов Kotlin. Проверки живут в мапперах.
 *
 * Операнд хранится тем же токеном, что уходит в скрипт: "1000.0" или "F1".
 */
data class GraphDto(
    val version: Int? = null,
    val nodes: List<NodeDto>? = null,
    val edges: List<EdgeDto>? = null,
    /** Счётчик выданных id. Нет в файле — берём максимум по нодам. */
    val lastId: Int? = null,
)

data class NodeDto(
    val id: Int? = null,
    val type: String? = null,
    val title: String? = null,
    val x: Float? = null,
    val y: Float? = null,
    // STEP
    val delayMs: Long? = null,
    val ch1: ChannelDto? = null,
    val ch2: ChannelDto? = null,
    // REGISTER
    val regOp: String? = null,
    val regDst: Int? = null,
    val regSrc: String? = null,
    // CONDITION
    val condLeft: Int? = null,
    val condOp: String? = null,
    val condRight: String? = null,
)

data class ChannelDto(
    val carrierEnabled: Boolean? = null,
    val carrierFr: String? = null,
    val carrierMod: String? = null,
    val amEnabled: Boolean? = null,
    val amFr: String? = null,
    val amMod: String? = null,
    val fmEnabled: Boolean? = null,
    val fmBase: String? = null,
    val fmDev: String? = null,
    val fmFr: String? = null,
    val fmMod: String? = null,
)

data class EdgeDto(val from: Int? = null, val port: String? = null, val to: Int? = null)

//╭─ Диск -> домен ───────────────────────────────────────────────────────╮

fun GraphDto.toDomain(): NodeGraph {
    val v = version ?: throw GraphFormatException("в файле нет поля version")
    if (v > GRAPH_FORMAT_VERSION) {
        throw GraphFormatException("файл версии $v, приложение понимает до $GRAPH_FORMAT_VERSION")
    }
    val domainNodes = (nodes ?: emptyList()).map { it.toDomain() }
    val maxNodeId = domainNodes.maxOfOrNull { it.id.value } ?: 0
    return NodeGraph(
        nodes = domainNodes,
        edges = (edges ?: emptyList()).map { it.toDomain() },
        //Старый файл без lastId — берём максимум по нодам; заодно защищаемся
        //от битого файла, где счётчик меньше номера какой-то ноды
        lastId = maxOf(lastId ?: 0, maxNodeId),
    )
}

private fun NodeDto.toDomain(): GraphNode {
    val nodeId = id ?: throw GraphFormatException("у ноды нет id")
    return GraphNode(
        id = NodeId(nodeId),
        title = title.orEmpty(),
        x = x ?: 0f,
        y = y ?: 0f,
        body = when (type) {
            "START" -> NodeBody.Start
            "STOP" -> NodeBody.Stop

            "STEP" -> NodeBody.Step(
                params = StepParams(
                    ch1 = (ch1 ?: ChannelDto()).toDomain(nodeId),
                    ch2 = (ch2 ?: ChannelDto()).toDomain(nodeId),
                ),
                delayMs = delayMs ?: 0L,
            )

            "REGISTER" -> NodeBody.Register(
                op = RegOp.entries.firstOrNull { it.name == regOp }
                    ?: throw GraphFormatException("нода $nodeId: неизвестная операция regOp=$regOp"),
                dst = regDst ?: throw GraphFormatException("нода $nodeId: нет regDst"),
                src = operand(regSrc, nodeId, "regSrc"),
            )

            "CONDITION" -> NodeBody.Condition(
                left = condLeft ?: throw GraphFormatException("нода $nodeId: нет condLeft"),
                op = CompareOp.entries.firstOrNull { it.text == condOp }
                    ?: throw GraphFormatException("нода $nodeId: неизвестное сравнение condOp=$condOp"),
                right = operand(condRight, nodeId, "condRight"),
            )

            else -> throw GraphFormatException("нода $nodeId: неизвестный тип $type")
        },
    )
}

private fun ChannelDto.toDomain(nodeId: Int) = ChannelParams(
    carrierEnabled = carrierEnabled,
    carrierFr = carrierFr?.let { operand(it, nodeId, "carrierFr") },
    carrierMod = carrierMod,
    amEnabled = amEnabled,
    amFr = amFr?.let { operand(it, nodeId, "amFr") },
    amMod = amMod,
    fmEnabled = fmEnabled,
    fmBase = fmBase?.let { operand(it, nodeId, "fmBase") },
    fmDev = fmDev?.let { operand(it, nodeId, "fmDev") },
    fmFr = fmFr?.let { operand(it, nodeId, "fmFr") },
    fmMod = fmMod,
)

private fun operand(token: String?, nodeId: Int, field: String): Operand {
    val text = token ?: throw GraphFormatException("нода $nodeId: нет поля $field")
    return parseOperand(text)
        ?: throw GraphFormatException("нода $nodeId: поле $field не число и не регистр: $text")
}

private fun EdgeDto.toDomain(): GraphEdge = GraphEdge(
    from = NodeId(from ?: throw GraphFormatException("у связи нет from")),
    port = Port.entries.firstOrNull { it.name == port }
        ?: throw GraphFormatException("у связи неизвестный порт: $port"),
    to = NodeId(to ?: throw GraphFormatException("у связи нет to")),
)

//╭─ Домен -> диск ───────────────────────────────────────────────────────╮

fun NodeGraph.toDto(): GraphDto = GraphDto(
    version = GRAPH_FORMAT_VERSION,
    nodes = nodes.map { it.toDto() },
    edges = edges.map { EdgeDto(it.from.value, it.port.name, it.to.value) },
    lastId = lastId,
)

private fun GraphNode.toDto(): NodeDto {
    val base = NodeDto(id = id.value, title = title, x = x, y = y)
    return when (val b = body) {
        is NodeBody.Start -> base.copy(type = "START")
        is NodeBody.Stop -> base.copy(type = "STOP")

        is NodeBody.Step -> base.copy(
            type = "STEP",
            delayMs = b.delayMs,
            ch1 = b.params.ch1.toDto(),
            ch2 = b.params.ch2.toDto(),
        )

        is NodeBody.Register -> base.copy(
            type = "REGISTER",
            regOp = b.op.name,
            regDst = b.dst,
            regSrc = b.src.toToken(),
        )

        is NodeBody.Condition -> base.copy(
            type = "CONDITION",
            condLeft = b.left,
            condOp = b.op.text,
            condRight = b.right.toToken(),
        )
    }
}

private fun ChannelParams.toDto() = ChannelDto(
    carrierEnabled = carrierEnabled,
    carrierFr = carrierFr?.toToken(),
    carrierMod = carrierMod,
    amEnabled = amEnabled,
    amFr = amFr?.toToken(),
    amMod = amMod,
    fmEnabled = fmEnabled,
    fmBase = fmBase?.toToken(),
    fmDev = fmDev?.toToken(),
    fmFr = fmFr?.toToken(),
    fmMod = fmMod,
)
