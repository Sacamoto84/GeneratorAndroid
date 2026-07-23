package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.GraphNode
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.NodeGraph
import com.example.generator2.features.nodes.model.NodeId
import com.example.generator2.features.nodes.model.Port
import com.example.generator2.features.nodes.model.node
import com.example.generator2.features.nodes.model.ports
import com.example.generator2.features.nodes.model.target
import com.example.generator2.features.script.REGISTER_COUNT

enum class Severity { ERROR, WARNING }

/**
 * Замечание к графу. nodeId == null — замечание про граф целиком,
 * центрировать холст не на чем.
 */
data class Issue(val nodeId: NodeId?, val severity: Severity, val text: String)

/**
 * Проверка графа. Чистая функция: имена форм сигнала приходят параметром,
 * потому что живут они в памяти генератора (gen.itemlistCarrier и
 * gen.itemlistAM), а тянуть сюда Generator значило бы потерять тестируемость.
 *
 * @param carrierNames имена, известные генератору для несущей
 * @param modNames имена, известные генератору для AM и FM — список у них общий
 */
fun validate(
    graph: NodeGraph,
    carrierNames: Set<String> = emptySet(),
    modNames: Set<String> = emptySet(),
): List<Issue> = structureErrors(graph)

//╭─ Ошибки ──────────────────────────────────────────────────────────────╮

private fun structureErrors(graph: NodeGraph): List<Issue> = buildList {

    val starts = graph.nodes.filter { it.body is NodeBody.Start }
    when {
        starts.isEmpty() -> add(Issue(null, Severity.ERROR, "В графе нет ноды Старт"))
        starts.size > 1 -> starts.forEach {
            add(Issue(it.id, Severity.ERROR, "Старт должен быть один, их ${starts.size}"))
        }
    }

    graph.edges.forEach { e ->
        if (graph.node(e.to) == null) {
            add(Issue(e.from, Severity.ERROR, "Связь ведёт в несуществующую ноду ${e.to.value}"))
        }
    }

    graph.nodes.forEach { n ->
        n.body.ports().forEach { port ->
            if (graph.target(n.id, port) == null) {
                add(Issue(n.id, Severity.ERROR, emptyPortText(port)))
            }
        }
    }

    val startIds = starts.map { it.id }.toSet()
    graph.edges.filter { it.to in startIds }.forEach {
        add(Issue(it.from, Severity.ERROR, "В ноду Старт нельзя вести связь"))
    }

    graph.nodes.forEach { n -> registerAndDelayErrors(n).forEach(::add) }

    //Недостижимость считаем только при единственном Старте: иначе
    //непонятно, от какого мерить, и сообщение будет врать
    if (starts.size == 1) {
        val reachable = reachableFrom(graph, starts.first().id)
        graph.nodes.filterNot { it.id in reachable }.forEach {
            add(Issue(it.id, Severity.ERROR, "Нода недостижима от Старта"))
        }
    }
}

private fun emptyPortText(port: Port): String = when (port) {
    Port.OUT -> "У ноды нет исходящей связи"
    Port.YES -> "У Условия не заполнен выход «да»"
    Port.NO -> "У Условия не заполнен выход «нет»"
}

private fun registerAndDelayErrors(node: GraphNode): List<Issue> = buildList {
    val range = 0 until REGISTER_COUNT
    val outOfRange = "вне F0..F${REGISTER_COUNT - 1}"

    when (val b = node.body) {
        is NodeBody.Step ->
            if (b.delayMs < 0) {
                add(Issue(node.id, Severity.ERROR, "Отрицательная задержка ${b.delayMs}"))
            }

        is NodeBody.Register ->
            if (b.dst !in range) {
                add(Issue(node.id, Severity.ERROR, "Регистр F${b.dst} $outOfRange"))
            }

        is NodeBody.Condition ->
            if (b.left !in range) {
                add(Issue(node.id, Severity.ERROR, "Регистр F${b.left} $outOfRange"))
            }

        else -> Unit
    }
}

/** Обход в ширину: до кого можно добраться от ноды start */
internal fun reachableFrom(graph: NodeGraph, start: NodeId): Set<NodeId> {
    val seen = linkedSetOf(start)
    val queue = ArrayDeque(listOf(start))
    while (queue.isNotEmpty()) {
        val id = queue.removeFirst()
        val node = graph.node(id) ?: continue
        node.body.ports().forEach { port ->
            graph.target(id, port)?.let { if (seen.add(it)) queue.addLast(it) }
        }
    }
    return seen
}
