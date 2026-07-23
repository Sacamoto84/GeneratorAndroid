package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.ChannelParams
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
): List<Issue> = structureErrors(graph) + warnings(graph, carrierNames, modNames)

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

        is NodeBody.Delay ->
            if (b.delayMs < 0) {
                add(Issue(node.id, Severity.ERROR, "Отрицательная задержка ${b.delayMs}"))
            }

        is NodeBody.Register ->
            if (b.dst !in range) {
                add(Issue(node.id, Severity.ERROR, "Регистр F${b.dst} $outOfRange"))
            }

        is NodeBody.Condition -> {
            if (b.left !in range) {
                add(Issue(node.id, Severity.ERROR, "Регистр F${b.left} $outOfRange"))
            }
            if (b.delayBeforeMs < 0 || b.delayAfterMs < 0) {
                add(Issue(node.id, Severity.ERROR, "Отрицательная задержка в условии"))
            }
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

//╭─ Предупреждения ──────────────────────────────────────────────────────╮

private fun warnings(
    graph: NodeGraph,
    carrierNames: Set<String>,
    modNames: Set<String>,
): List<Issue> = buildList {

    //Цикл без единой задержки. Устройство не повиснет — в Script есть
    //YIELD_EVERY, — но ядро будет греться, поэтому предупреждение, не ошибка
    stronglyConnected(graph).forEach { component ->
        val delaySum = component.sumOf { id ->
            when (val b = graph.node(id)?.body) {
                is NodeBody.Step -> b.delayMs
                is NodeBody.Delay -> b.delayMs
                is NodeBody.Condition -> b.delayBeforeMs + b.delayAfterMs
                else -> 0L
            }
        }
        if (delaySum == 0L) {
            component.forEach {
                add(Issue(it, Severity.WARNING, "Цикл без задержки: будет крутиться на полной скорости"))
            }
        }
    }

    graph.nodes.forEach { n ->
        val step = n.body as? NodeBody.Step ?: return@forEach

        if (step.params.checkedCount == 0 && step.delayMs == 0L) {
            add(Issue(n.id, Severity.WARNING, "Шаг ничего не делает: ни одного параметра, задержка ноль"))
        }

        listOf(1 to step.params.ch1, 2 to step.params.ch2).forEach { (ch, p) ->
            if (p.carrierFr != null && p.fmBase != null) {
                add(
                    Issue(
                        n.id, Severity.WARNING,
                        "CR$ch FR и FM$ch BASE пишут одно и то же поле, победит второе",
                    )
                )
            }
            unknownWaveforms(p, carrierNames, modNames).forEach { name ->
                add(Issue(n.id, Severity.WARNING, "Форма «$name» генератору неизвестна"))
            }
        }
    }
}

/**
 * Имена форм, которых генератор не знает.
 *
 * Несущая ищется в gen.itemlistCarrier, AM и FM — оба в gen.itemlistAM
 * (см. Spinner_Send_Buffer). Неизвестное имя движок молча игнорирует,
 * поэтому и предупреждение, а не ошибка.
 */
private fun unknownWaveforms(
    p: ChannelParams,
    carrierNames: Set<String>,
    modNames: Set<String>,
): List<String> = buildList {
    p.carrierMod?.let { if (carrierNames.isNotEmpty() && it !in carrierNames) add(it) }
    p.amMod?.let { if (modNames.isNotEmpty() && it !in modNames) add(it) }
    p.fmMod?.let { if (modNames.isNotEmpty() && it !in modNames) add(it) }
}

/**
 * Нетривиальные сильно связные компоненты по Тарьяну: больше одной ноды
 * либо петля на себя. Ровно они и есть циклы графа, без перебора самих циклов.
 */
internal fun stronglyConnected(graph: NodeGraph): List<Set<NodeId>> {
    var counter = 0
    val index = HashMap<NodeId, Int>()
    val low = HashMap<NodeId, Int>()
    val stack = ArrayDeque<NodeId>()
    val onStack = HashSet<NodeId>()
    val result = mutableListOf<Set<NodeId>>()

    fun successors(id: NodeId): List<NodeId> {
        val node = graph.node(id) ?: return emptyList()
        return node.body.ports().mapNotNull { graph.target(id, it) }
    }

    fun strongConnect(v: NodeId) {
        index[v] = counter
        low[v] = counter
        counter++
        stack.addLast(v)
        onStack.add(v)

        successors(v).forEach { w ->
            when {
                w !in index -> {
                    strongConnect(w)
                    low[v] = minOf(low.getValue(v), low.getValue(w))
                }

                w in onStack -> low[v] = minOf(low.getValue(v), index.getValue(w))
            }
        }

        if (low.getValue(v) == index.getValue(v)) {
            val component = linkedSetOf<NodeId>()
            while (true) {
                val w = stack.removeLast()
                onStack.remove(w)
                component.add(w)
                if (w == v) break
            }
            val isLoop = component.size == 1 && v in successors(v)
            if (component.size > 1 || isLoop) result.add(component)
        }
    }

    graph.nodes.forEach { if (it.id !in index) strongConnect(it.id) }
    return result
}
