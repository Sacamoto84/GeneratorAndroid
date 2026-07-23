package com.example.generator2.features.nodes.model

import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand

/** Идентификатор ноды. Не переиспользуется: новый всегда больше всех бывших. */
@JvmInline
value class NodeId(val value: Int)

/** Выход ноды. У Старта, Шага и Регистра один, у Условия два, у Стопа ни одного. */
enum class Port { OUT, YES, NO }

/** Операция ноды Регистр. Ложится на LOAD, PLUS, MINUS языка скрипта. */
enum class RegOp { LOAD, PLUS, MINUS }

data class GraphEdge(val from: NodeId, val port: Port, val to: NodeId)

/**
 * Параметры одного канала. null означает снятую галочку: поле не трогаем.
 *
 * carrierEnabled ложится на "CH1 CR ON|OFF" — в движке это включение
 * всего канала (gen.liveData.ch1_EN), а не только несущей.
 */
data class ChannelParams(
    val carrierEnabled: Boolean? = null,
    val carrierFr: Operand? = null,
    val carrierMod: String? = null,
    val amEnabled: Boolean? = null,
    val amFr: Operand? = null,
    val amMod: String? = null,
    val fmEnabled: Boolean? = null,
    val fmBase: Operand? = null,
    val fmDev: Operand? = null,
    val fmFr: Operand? = null,
    val fmMod: String? = null,
) {
    /** Сколько галочек отмечено — для счётчика в заголовке группы */
    val checkedCount: Int
        get() = listOf(
            carrierEnabled, carrierFr, carrierMod,
            amEnabled, amFr, amMod,
            fmEnabled, fmBase, fmDev, fmFr, fmMod,
        ).count { it != null }
}

data class StepParams(val ch1: ChannelParams, val ch2: ChannelParams) {
    val checkedCount: Int get() = ch1.checkedCount + ch2.checkedCount
}

sealed interface NodeBody {
    data object Start : NodeBody
    data object Stop : NodeBody
    data class Step(val params: StepParams, val delayMs: Long) : NodeBody

    /** Отдельная нода-пауза: только задержка, без параметров генератора */
    data class Delay(val delayMs: Long) : NodeBody

    data class Register(val op: RegOp, val dst: Int, val src: Operand) : NodeBody

    /**
     * @param delayBeforeMs пауза перед проверкой условия
     * @param delayAfterMs пауза после проверки, в обеих ветках перед переходом
     */
    data class Condition(
        val left: Int,
        val op: CompareOp,
        val right: Operand,
        val delayBeforeMs: Long = 0L,
        val delayAfterMs: Long = 0L,
    ) : NodeBody
}

/** Выходы ноды в порядке обхода компилятором */
fun NodeBody.ports(): List<Port> = when (this) {
    is NodeBody.Start -> listOf(Port.OUT)
    is NodeBody.Step -> listOf(Port.OUT)
    is NodeBody.Delay -> listOf(Port.OUT)
    is NodeBody.Register -> listOf(Port.OUT)
    is NodeBody.Condition -> listOf(Port.YES, Port.NO)
    is NodeBody.Stop -> emptyList()
}

data class GraphNode(
    val id: NodeId,
    val title: String,
    val x: Float,
    val y: Float,
    val body: NodeBody,
)

data class NodeGraph(
    val nodes: List<GraphNode> = emptyList(),
    val edges: List<GraphEdge> = emptyList(),
    /**
     * Максимальный когда-либо выданный id. Удаление ноды его не опускает,
     * поэтому освободившийся номер второй раз не выдаётся и новая нода
     * не наследует связи мёртвой.
     */
    val lastId: Int = nodes.maxOfOrNull { it.id.value } ?: 0,
)

//╭─ Чтение ──────────────────────────────────────────────────────────────╮

fun NodeGraph.node(id: NodeId): GraphNode? = nodes.firstOrNull { it.id == id }

fun NodeGraph.startNode(): GraphNode? = nodes.firstOrNull { it.body is NodeBody.Start }

fun NodeGraph.target(from: NodeId, port: Port): NodeId? =
    edges.firstOrNull { it.from == from && it.port == port }?.to

/**
 * Следующий свободный id. Считается от [NodeGraph.lastId], а не от текущего
 * списка нод, поэтому после удаления ноды её номер не всплывает заново
 * и связи не прилипают к чужой ноде.
 */
fun NodeGraph.nextId(): NodeId = NodeId(lastId + 1)

//╭─ Правка ──────────────────────────────────────────────────────────────╮

/**
 * Добавляет новую ноду или правит существующую (по совпадению id).
 *
 * [NodeGraph.lastId] берётся через maxOf, а не инкремент: правка уже
 * существующей ноды (перетаскивание, редактирование тела) не должна
 * двигать счётчик id вперёд.
 */
fun NodeGraph.withNode(node: GraphNode): NodeGraph = copy(
    nodes = nodes.filterNot { it.id == node.id } + node,
    lastId = maxOf(lastId, node.id.value),
)

/** Один порт — одна связь: новая молча заменяет прежнюю */
fun NodeGraph.withEdge(from: NodeId, port: Port, to: NodeId): NodeGraph =
    copy(edges = edges.filterNot { it.from == from && it.port == port } + GraphEdge(from, port, to))

fun NodeGraph.withoutEdge(from: NodeId, port: Port): NodeGraph =
    copy(edges = edges.filterNot { it.from == from && it.port == port })

/** Удаление ноды уносит и входящие, и исходящие рёбра */
fun NodeGraph.withoutNode(id: NodeId): NodeGraph = copy(
    nodes = nodes.filterNot { it.id == id },
    edges = edges.filterNot { it.from == id || it.to == id },
)

/** Новый граф: Старт со стрелкой в Стоп — сразу валиден, можно жать «Пуск» */
fun newGraph(): NodeGraph = NodeGraph(
    nodes = listOf(
        GraphNode(NodeId(1), "Старт", 60f, 80f, NodeBody.Start),
        GraphNode(NodeId(2), "Стоп", 60f, 300f, NodeBody.Stop),
    ),
    edges = listOf(GraphEdge(NodeId(1), Port.OUT, NodeId(2))),
)
