package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.ChannelParams
import com.example.generator2.features.nodes.model.GraphNode
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.NodeGraph
import com.example.generator2.features.nodes.model.NodeId
import com.example.generator2.features.nodes.model.Port
import com.example.generator2.features.nodes.model.RegOp
import com.example.generator2.features.nodes.model.StepParams
import com.example.generator2.features.nodes.model.node
import com.example.generator2.features.nodes.model.ports
import com.example.generator2.features.nodes.model.startNode
import com.example.generator2.features.nodes.model.target
import com.example.generator2.features.script.Cmd
import com.example.generator2.features.script.ScriptException
import com.example.generator2.features.script.parseCommand
import com.example.generator2.features.script.toToken

sealed interface CompileResult {

    /**
     * @param lines строки для Script.list
     * @param lineToNode владелец каждой строки, индексы совпадают с lines
     */
    data class Ok(
        val lines: List<String>,
        val lineToNode: List<NodeId>,
        val warnings: List<Issue>,
    ) : CompileResult

    data class Failed(val errors: List<Issue>, val warnings: List<Issue>) : CompileResult
}

/**
 * Граф в строки скрипта.
 *
 * Обход в глубину от преемника Старта, преемники в порядке портов.
 * Порядок влияет только на читаемость: все переходы — явные GOTO,
 * семантика от него не зависит. Зато он детерминирован, и golden-тесты
 * не разъезжаются от прогона к прогону.
 */
fun compile(
    graph: NodeGraph,
    carrierNames: Set<String> = emptySet(),
    modNames: Set<String> = emptySet(),
): CompileResult {

    val issues = validate(graph, carrierNames, modNames)
    val errors = issues.filter { it.severity == Severity.ERROR }
    val warnings = issues.filter { it.severity == Severity.WARNING }
    if (errors.isNotEmpty()) return CompileResult.Failed(errors, warnings)

    val order = emissionOrder(graph)

    //Проход 1: размер блока каждой ноды даёт её адрес
    val address = HashMap<NodeId, Int>()
    var cursor = 0
    order.forEach { node ->
        address[node.id] = cursor
        cursor += node.lineCount()
    }

    //Проход 2: строки с уже известными номерами переходов
    val lines = ArrayList<String>(cursor)
    val owners = ArrayList<NodeId>(cursor)
    order.forEach { node ->
        val block = emit(node, graph, address)
        lines.addAll(block)
        repeat(block.size) { owners.add(node.id) }
    }

    selfCheck(lines, owners)?.let { return CompileResult.Failed(listOf(it), warnings) }

    return CompileResult.Ok(lines, owners, warnings)
}

/**
 * Ноды в порядке печати. Старт не попадает: он печатает ноль строк,
 * а вход в граф — это первая строка его преемника, то есть pc = 0.
 */
private fun emissionOrder(graph: NodeGraph): List<GraphNode> {
    val start = graph.startNode() ?: return emptyList()
    val first = graph.target(start.id, Port.OUT) ?: return emptyList()

    val visited = LinkedHashSet<NodeId>()

    fun walk(id: NodeId) {
        if (!visited.add(id)) return
        val node = graph.node(id) ?: return
        node.body.ports().forEach { port -> graph.target(id, port)?.let { walk(it) } }
    }

    walk(first)
    return visited.mapNotNull { graph.node(it) }
}

private fun GraphNode.lineCount(): Int = when (val b = body) {
    is NodeBody.Start -> 0
    is NodeBody.Stop -> 1
    is NodeBody.Register -> 2
    is NodeBody.ReadGen -> 2
    //DELAY? + GOTO
    is NodeBody.Delay -> (if (b.delayMs > 0) 1 else 0) + 1
    //(DELAY before?) + IF + (DELAY after?) + GOTO + ELSE + (DELAY after?) + GOTO + ENDIF
    is NodeBody.Condition ->
        (if (b.delayBeforeMs > 0) 1 else 0) + 5 + 2 * (if (b.delayAfterMs > 0) 1 else 0)
    is NodeBody.Step -> b.params.assignmentLines().size + (if (b.delayMs > 0) 1 else 0) + 1
}

private fun emit(node: GraphNode, graph: NodeGraph, address: Map<NodeId, Int>): List<String> {

    fun jump(port: Port): String {
        val to = requireNotNull(graph.target(node.id, port)) {
            "нода ${node.id.value}: порт $port пуст, валидатор должен был это поймать"
        }
        return "GOTO ${requireNotNull(address[to]) { "нет адреса ноды ${to.value}" }}"
    }

    return when (val b = node.body) {
        is NodeBody.Start -> emptyList()
        is NodeBody.Stop -> listOf("END")

        is NodeBody.Step -> buildList {
            addAll(b.params.assignmentLines())
            if (b.delayMs > 0) add("DELAY ${b.delayMs}")
            add(jump(Port.OUT))
        }

        is NodeBody.Delay -> buildList {
            if (b.delayMs > 0) add("DELAY ${b.delayMs}")
            add(jump(Port.OUT))
        }

        is NodeBody.Register -> listOf(
            when (b.op) {
                RegOp.LOAD -> "LOAD F${b.dst} ${b.src.toToken()}"
                RegOp.PLUS -> "PLUS F${b.dst} ${b.src.toToken()}"
                RegOp.MINUS -> "MINUS F${b.dst} ${b.src.toToken()}"
            },
            jump(Port.OUT),
        )

        is NodeBody.ReadGen -> listOf(
            "READ F${b.dst} ${b.block}${b.ch} ${b.param}",
            jump(Port.OUT),
        )

        //Пять строк не от щедрости: на истинном условии движок ставит
        //pc = current + 1 и попадает на переход ветки «да», на ложном
        //через findPairLine попадает на строку после ELSE. ENDIF не
        //исполняется никогда, но без него findPairLine бросит исключение.
        //
        //Задержка «после» стоит в обеих ветках: на истинном pc = current+1
        //попадает на DELAY сразу за IF, на ложном findPairLine отдаёт строку
        //за ELSE — тоже DELAY. findPairLine считает вложенность только по
        //IF/ELSE/ENDIF, поэтому DELAY между ними поиск пары не ломает.
        is NodeBody.Condition -> buildList {
            if (b.delayBeforeMs > 0) add("DELAY ${b.delayBeforeMs}")
            add("IF F${b.left} ${b.op.text} ${b.right.toToken()}")
            if (b.delayAfterMs > 0) add("DELAY ${b.delayAfterMs}")
            add(jump(Port.YES))
            add("ELSE")
            if (b.delayAfterMs > 0) add("DELAY ${b.delayAfterMs}")
            add(jump(Port.NO))
            add("ENDIF")
        }
    }
}

/**
 * Строки Шага в каноническом порядке: сначала значения, потом переключатели,
 * сначала первый канал, потом второй.
 *
 * Значения раньше включения не случайны: блок оживает уже на нужной частоте,
 * а не на прежней с последующим скачком.
 */
internal fun StepParams.assignmentLines(): List<String> = ch1.lines(1) + ch2.lines(2)

private fun ChannelParams.lines(ch: Int): List<String> = buildList {
    carrierMod?.let { add("CR$ch MOD $it") }
    carrierFr?.let { add("CR$ch FR ${it.toToken()}") }
    amMod?.let { add("AM$ch MOD $it") }
    amFr?.let { add("AM$ch FR ${it.toToken()}") }
    fmMod?.let { add("FM$ch MOD $it") }
    fmBase?.let { add("FM$ch BASE ${it.toToken()}") }
    fmDev?.let { add("FM$ch DEV ${it.toToken()}") }
    fmFr?.let { add("FM$ch FR ${it.toToken()}") }

    carrierEnabled?.let { add("CH$ch CR ${if (it) "ON" else "OFF"}") }
    amEnabled?.let { add("CH$ch AM ${if (it) "ON" else "OFF"}") }
    fmEnabled?.let { add("CH$ch FM ${if (it) "ON" else "OFF"}") }
}

/**
 * Строки печатал компилятор, поэтому любая ошибка разбора в рантайме —
 * его баг, а не пользователя. Дешёвая проверка на выходе ловит регрессию
 * на устройстве, а в тестах становится сильным инвариантом.
 *
 * @return замечание, если что-то не сошлось; null, если всё в порядке
 */
private fun selfCheck(lines: List<String>, owners: List<NodeId>): Issue? {

    fun internal(text: String) =
        Issue(null, Severity.ERROR, "Внутренняя ошибка компилятора: $text")

    if (lines.size != owners.size) {
        return internal("строк ${lines.size}, владельцев ${owners.size}")
    }

    lines.forEachIndexed { i, line ->
        val cmd = try {
            parseCommand(line, i)
        } catch (e: ScriptException) {
            return internal("строка $i не разбирается: $line (${e.message})")
        }
        if (cmd is Cmd.Goto && cmd.target !in lines.indices) {
            return internal("GOTO ${cmd.target} вне диапазона 0..${lines.lastIndex}")
        }
    }

    return null
}
