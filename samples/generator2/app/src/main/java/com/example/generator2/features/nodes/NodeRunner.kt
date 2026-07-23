package com.example.generator2.features.nodes

import com.example.generator2.features.generator.Generator
import com.example.generator2.features.nodes.model.NodeId
import com.example.generator2.features.script.Script
import com.example.generator2.features.script.StateCommandScript
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Прогон графа.
 *
 * Держит собственный экземпляр Script: у синглтона в list и name лежит
 * текстовый скрипт, открытый пользователем на соседнем экране, и затирать
 * его запуском графа нельзя.
 */
class NodeRunner(gen: Generator, private val arbiter: GeneratorArbiter) {

    private val script = Script(gen)

    private var lineToNode: List<NodeId> = emptyList()

    val state: StateCommandScript get() = script.state

    val registers: StateFlow<List<Float>> get() = script.registers

    val pc: StateFlow<Int> get() = script.pc

    /**
     * Нода, чья строка сейчас под pc.
     *
     * На DELAY движок держит pc на самой строке задержки, а она принадлежит
     * своему Шагу — значит нода светится ровно столько, сколько длится пауза.
     */
    val activeNode: Flow<NodeId?> = script.pc.map { lineToNode.getOrNull(it) }

    /** Куда движок пишет сообщения. Экран подставляет сюда свою консоль. */
    var logger: (String) -> Unit
        get() = script.logger
        set(value) {
            script.logger = value
        }

    init {
        arbiter.register(RunOwner.NODES) { script.command(StateCommandScript.STOP) }
    }

    fun start(compiled: CompileResult.Ok) {
        arbiter.acquire(RunOwner.NODES)
        lineToNode = compiled.lineToNode
        script.list.clear()
        compiled.lines.forEach { script.list.add(it) }
        script.command(StateCommandScript.START)
    }

    fun pause() {
        script.command(StateCommandScript.PAUSE)
    }

    fun resume() {
        arbiter.acquire(RunOwner.NODES)
        script.command(StateCommandScript.RESUME)
    }

    fun stop() {
        script.command(StateCommandScript.STOP)
        arbiter.release(RunOwner.NODES)
    }

    /** Нода, которой принадлежит строка — для сообщений об ошибках движка */
    fun nodeOfLine(line: Int): NodeId? = lineToNode.getOrNull(line)
}
