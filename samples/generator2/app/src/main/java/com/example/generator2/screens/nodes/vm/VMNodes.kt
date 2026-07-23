package com.example.generator2.screens.nodes.vm

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import com.example.generator2.common.snackbar.SnackBar
import com.example.generator2.element.Console2
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.nodes.Issue
import com.example.generator2.features.nodes.NodeGraphUtils
import com.example.generator2.features.nodes.NodeRunner
import com.example.generator2.features.nodes.Severity
import com.example.generator2.features.nodes.model.ChannelParams
import com.example.generator2.features.nodes.model.GraphFormatException
import com.example.generator2.features.nodes.model.GraphNode
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.NodeGraph
import com.example.generator2.features.nodes.model.NodeId
import com.example.generator2.features.nodes.model.Port
import com.example.generator2.features.nodes.model.RegOp
import com.example.generator2.features.nodes.model.StepParams
import com.example.generator2.features.nodes.model.newGraph
import com.example.generator2.features.nodes.model.nextId
import com.example.generator2.features.nodes.model.node
import com.example.generator2.features.nodes.model.withEdge
import com.example.generator2.features.nodes.model.withNode
import com.example.generator2.features.nodes.model.withoutEdge
import com.example.generator2.features.nodes.model.withoutNode
import com.example.generator2.features.nodes.validate
import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand
import javax.inject.Inject

/** Какую ноду создаёт «+» */
enum class NodeKind { STEP, REGISTER, CONDITION, STOP }

@Stable
class VMNodes @Inject constructor(
    private val utils: NodeGraphUtils,
    val runner: NodeRunner,
    private val gen: Generator,
) : ScreenModel {

    var graph by mutableStateOf(newGraph())
        private set

    var name by mutableStateOf(NEW_NAME)
        internal set

    /** Есть несохранённые правки. Взводится и перемещением ноды: координаты в файле. */
    var dirty by mutableStateOf(false)
        internal set

    var selected by mutableStateOf<NodeId?>(null)

    /** Не null — идёт выбор цели для связи из этого порта */
    var linkFrom by mutableStateOf<Pair<NodeId, Port>?>(null)
        private set

    /** Своя консоль: consoleLog экрана скриптов глобальный, логи бы смешались */
    val console = Console2()

    private val issuesState = derivedStateOf { validate(graph, carrierNames(), modNames()) }

    val issues: List<Issue> get() = issuesState.value

    val errors: List<Issue> get() = issues.filter { it.severity == Severity.ERROR }

    val canRun: Boolean get() = errors.isEmpty()

    init {
        runner.logger = { console.println(it) }
    }

    /** Имена форм несущей, известные генератору */
    fun carrierNames(): Set<String> = gen.itemlistCarrier.map { it.name }.toSet()

    /** Имена форм модуляции: у AM и FM список общий */
    fun modNames(): Set<String> = gen.itemlistAM.map { it.name }.toSet()

    /**
     * Текущее состояние генератора в параметры Шага: накрутил на главном
     * экране — зашёл в ноду — нажал. Ставятся все галочки сразу.
     */
    fun snapshotFromGenerator(): StepParams {
        val d = gen.liveData
        return StepParams(
            ch1 = ChannelParams(
                carrierEnabled = d.ch1_EN.value,
                carrierFr = Operand.Const(d.ch1_Carrier_Fr.value),
                carrierMod = d.ch1_Carrier_Filename.value,
                amEnabled = d.ch1_AM_EN.value,
                amFr = Operand.Const(d.ch1_AM_Fr.value),
                amMod = d.ch1_AM_Filename.value,
                fmEnabled = d.ch1_FM_EN.value,
                //BASE и есть частота несущей: движок пишет их в одно поле,
                //поэтому в снимке её не дублируем
                fmBase = null,
                fmDev = Operand.Const(d.ch1_FM_Dev.value),
                fmFr = Operand.Const(d.ch1_FM_Fr.value),
                fmMod = d.ch1_FM_Filename.value,
            ),
            ch2 = ChannelParams(
                carrierEnabled = d.ch2_EN.value,
                carrierFr = Operand.Const(d.ch2_Carrier_Fr.value),
                carrierMod = d.ch2_Carrier_Filename.value,
                amEnabled = d.ch2_AM_EN.value,
                amFr = Operand.Const(d.ch2_AM_Fr.value),
                amMod = d.ch2_AM_Filename.value,
                fmEnabled = d.ch2_FM_EN.value,
                fmBase = null,
                fmDev = Operand.Const(d.ch2_FM_Dev.value),
                fmFr = Operand.Const(d.ch2_FM_Fr.value),
                fmMod = d.ch2_FM_Filename.value,
            ),
        )
    }

    //╭─ Правка графа ────────────────────────────────────────────────────╮

    private fun edit(mutate: (NodeGraph) -> NodeGraph) {
        graph = mutate(graph)
        dirty = true
    }

    /**
     * Новая нода встаёт правее выделенной, а если ничего не выделено —
     * правее всех. Холст своих координат наружу не отдаёт, поэтому «в центр
     * экрана» положить нечем, да и предсказуемое место удобнее случайного.
     */
    fun addNode(kind: NodeKind) {
        val anchor = selected?.let { graph.node(it) }
        val x = (anchor?.x ?: graph.nodes.maxOfOrNull { it.x } ?: 0f) + 220f
        val y = anchor?.y ?: graph.nodes.minOfOrNull { it.y } ?: 0f

        val id = graph.nextId()
        edit { it.withNode(GraphNode(id, defaultTitle(kind, id), x, y, defaultBody(kind))) }
        selected = id
    }

    /** Счётчик просьб вписать граф в экран. Холст следит за его сменой. */
    var fitRequest by mutableStateOf(0)
        private set

    fun requestFit() {
        fitRequest++
    }

    fun moveNode(id: NodeId, dx: Float, dy: Float) {
        val node = graph.node(id) ?: return
        edit { it.withNode(node.copy(x = node.x + dx, y = node.y + dy)) }
    }

    fun replaceBody(id: NodeId, body: NodeBody) {
        val node = graph.node(id) ?: return
        edit { it.withNode(node.copy(body = body)) }
    }

    fun rename(id: NodeId, title: String) {
        val node = graph.node(id) ?: return
        edit { it.withNode(node.copy(title = title)) }
    }

    fun deleteSelected() {
        val id = selected ?: return
        edit { it.withoutNode(id) }
        selected = null
    }

    /** Копия тела без связей, со смещением, чтобы не легла ровно поверх оригинала */
    fun duplicateSelected() {
        val source = selected?.let { graph.node(it) } ?: return
        val id = graph.nextId()
        edit {
            it.withNode(source.copy(id = id, x = source.x + 24f, y = source.y + 24f))
        }
        selected = id
    }

    //╭─ Диалог параметров ───────────────────────────────────────────────╮

    /** Какой ноде открыт диалог параметров */
    var paramsFor by mutableStateOf<NodeId?>(null)
        private set

    fun openParams(id: NodeId) {
        paramsFor = id
    }

    fun closeParams() {
        paramsFor = null
    }

    //╭─ Связи ───────────────────────────────────────────────────────────╮

    fun startLink(port: Port) {
        val from = selected ?: return
        linkFrom = from to port
    }

    fun cancelLink() {
        linkFrom = null
    }

    /** В Старт входить нельзя — он и не предлагается как цель */
    fun canBeTarget(id: NodeId): Boolean = graph.node(id)?.body !is NodeBody.Start

    fun completeLink(to: NodeId) {
        val (from, port) = linkFrom ?: return
        if (canBeTarget(to)) edit { it.withEdge(from, port, to) }
        linkFrom = null
    }

    fun unlink(port: Port) {
        val from = selected ?: return
        edit { it.withoutEdge(from, port) }
    }

    //╭─ Файлы ───────────────────────────────────────────────────────────╮

    var openDialogSaveAs by mutableStateOf(false)
    var openDialogDeleteRename by mutableStateOf(false)
    var openDialogOpen by mutableStateOf(false)

    /** Действие, которое ждёт ответа «а несохранённое куда?» */
    var pendingDiscard by mutableStateOf<(() -> Unit)?>(null)
        private set

    fun graphNames(): List<String> = utils.list()

    /** Всё, что теряет текущий граф, проходит через это */
    private fun guard(action: () -> Unit) {
        if (dirty) pendingDiscard = action else action()
    }

    fun discardAndRun() {
        val action = pendingDiscard ?: return
        pendingDiscard = null
        action()
    }

    fun cancelDiscard() {
        pendingDiscard = null
    }

    fun newFile() = guard { replaceGraph(newGraph(), NEW_NAME) }

    fun openFile(fileName: String) = guard {
        try {
            replaceGraph(utils.read(fileName), fileName)
        } catch (e: GraphFormatException) {
            //Текущий граф не трогаем: неудачное открытие не должно стирать работу
            SnackBar.error(e.message ?: "Файл $fileName не читается")
        } catch (e: Exception) {
            SnackBar.error("Не удалось открыть $fileName: ${e.message}")
        }
    }

    fun save() {
        if (name == NEW_NAME) {
            openDialogSaveAs = true
            return
        }
        saveAs(name)
    }

    fun saveAs(fileName: String) {
        try {
            utils.save(graph, fileName)
            name = fileName
            dirty = false
            openDialogSaveAs = false
            SnackBar.success("Сохранено")
        } catch (e: Exception) {
            //dirty остаётся взведён: правки не в файле
            SnackBar.error("Не удалось сохранить: ${e.message}")
        }
    }

    fun renameFile(fileName: String) {
        utils.rename(name, fileName)
        name = fileName
        openDialogDeleteRename = false
    }

    fun deleteFile() {
        utils.delete(name)
        openDialogDeleteRename = false
        replaceGraph(newGraph(), NEW_NAME)
    }

    //╭─ Загрузка графа целиком ──────────────────────────────────────────╮

    internal fun replaceGraph(value: NodeGraph, graphName: String) {
        graph = value
        name = graphName
        selected = null
        linkFrom = null
        dirty = false
        requestFit()
    }

    private fun defaultBody(kind: NodeKind): NodeBody = when (kind) {
        NodeKind.STEP -> NodeBody.Step(StepParams(ChannelParams(), ChannelParams()), 0L)
        NodeKind.REGISTER -> NodeBody.Register(RegOp.LOAD, 0, Operand.Const(0f))
        NodeKind.CONDITION -> NodeBody.Condition(0, CompareOp.LESS, Operand.Const(0f))
        NodeKind.STOP -> NodeBody.Stop
    }

    private fun defaultTitle(kind: NodeKind, id: NodeId): String = when (kind) {
        NodeKind.STEP -> "Шаг ${id.value}"
        NodeKind.REGISTER -> "Регистр ${id.value}"
        NodeKind.CONDITION -> "Условие ${id.value}"
        NodeKind.STOP -> "Стоп"
    }

    companion object {
        const val NEW_NAME = "New"
    }
}
