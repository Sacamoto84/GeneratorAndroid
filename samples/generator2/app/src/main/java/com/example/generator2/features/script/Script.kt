package com.example.generator2.features.script

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.generator2.features.generator.Generator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

/*
 * ----------------- Логика -----------------
 * IF F1 = 2
 * ...
 * ELSE
 * ...
 * ENDIF
 *
 * DELAY 1000 - Задержка работы
 */

/*
 *╭─ Переход ─╮╭─ Задержка ─╮╭─ Завершение ─╮
 *│ GOTO 2    ││ DELAY 4000 ││ END          │
 *╰───────────╯╰────────────╯╰──────────────╯
 *╭─ Арифметика ────┬─────────────────────╮╭─ Загрузка константы в регистр ─╮
 *│ MINUS F1 5000.0 │ F1 - 5000.0 -> F1   ││ LOAD F1 2344.0  │ 2344.0 -> F1 │
 *│ MINUS F1 F2     │ F1 - F2 -> F1       ││ LOAD F1 F2      │ F2 -> F1     │
 *│ PLUS  F1 4555.5 │ F1 + 4555.5 -> F1   │╰─────────────────┴──────────────╯
 *│ PLUS  F1 F2     │ F1 + F2 -> F1       │
 *╰─────────────────┴─────────────────────╯
 *╭─ Генератор ─────────────────╮╭────────────┬──────────────╮
 *│ CH[1 2] [CR AM FM] [ON OFF] ││IF F1 < 450 │ IF F1 < 450  │
 *│                             ││...{true}   │ ...  {true}  │
 *│ CR[1 2] FR 1000.0     F[]   ││ELSE        │ ENDIF        │
 *│ CR[1 2] MOD 02_HWawe        ││...{false}  │              │
 *│                             ││ENDIF       │              │
 *│ AM[1 2] FR 1000.3     F[]   │├───┬────┬───┼────┬────┬────┤
 *│ AM[1 2] MOD 02_HWawe        ││ < │ <= │ > │ >= │ == │ != │
 *│                             │╰───┴────┴───┴────┴────┴────╯
 *│ FM[1 2] BASE 1234.6   F[]   │  BASE - частота несущей канала
 *│ FM[1 2] DEV  123.8    F[]   │
 *│ FM[1 2] MOD  02_HWawe       │  Регистры: F0..F9 (префикс R равнозначен F)
 *│ FM[1 2] FR   3.5      F[]   │  Вложенные IF поддерживаются
 *╰─────────────────────────────╯
 */

/**
 * Через сколько инструкций уступать процессор, чтобы петля без DELAY
 * не занимала ядро целиком
 */
private const val YIELD_EVERY = 256

/**
 * Как часто отдавать снимок регистров в UI. Сам массив движок правит на каждой
 * инструкции, а перерисовывать экран сотни раз в секунду незачем.
 */
private const val REGISTER_PUBLISH_MS = 50L

//Экраны для нижнего меню
enum class StateCommandScript {
    START, PAUSE, RESUME, STOP, EDIT, //Перевести в режим редактирования

    //Состояния
    ISRUNNING, ISPAUSE, ISTOPPING, ISEDITING, //Сейчас режим редактирования
}


class Script(val gen: Generator) {

    /**
     *  Имя файла
     */
    var name by mutableStateOf("New")

    //───────────────────────────────────────────────┐
    /**
     * ## Регистры Float 10 штук
     *
     * Обычный массив: правится на каждой инструкции, наблюдаемым делать дорого.
     * Для экрана есть [registers].
     */
    var register = FloatArray(REGISTER_COUNT)

    /**
     * Снимок регистров для UI, обновляется не чаще [REGISTER_PUBLISH_MS]
     * и обязательно в конце прогона
     */
    val registers = MutableStateFlow(List(REGISTER_COUNT) { 0f })
    //───────────────────────────────────────────────┘

    //───────────────────────────────────────────────┐
    val list = ScriptList()
    //───────────────────────────────────────────────┘
    val update = MutableStateFlow(0)

    var pc = MutableStateFlow(0)

    var state by mutableStateOf(StateCommandScript.ISTOPPING)

    /**
     * Куда писать сообщения движка. Слой UI подменяет на консоль скрипта.
     */
    var logger: (String) -> Unit = { Timber.i(it) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Job текущего прогона. Останов и пауза — это его отмена.
     */
    private var job: Job? = null

    /**
     * Номер прогона. Растёт при каждом старте и останове: отмена корутины
     * асинхронна, и по номеру отменённый прогон понимает, что его уже сменили.
     */
    @Volatile
    private var generation = 0

    init {
        Timber.i("!!! Script() init{}")
        command(StateCommandScript.STOP)
    }

    fun command(s: StateCommandScript) {

        when (s) {
            StateCommandScript.STOP -> {
                stop()
                state = StateCommandScript.ISTOPPING
            }

            StateCommandScript.PAUSE -> {
                cancelRun()
                state = StateCommandScript.ISPAUSE
            }

            StateCommandScript.RESUME -> {
                launchRun()
                state = StateCommandScript.ISRUNNING
            }

            StateCommandScript.START -> {
                cancelRun()
                register.fill(0f)
                publishRegisters()
                pc.value = 0
                launchRun()
                state = StateCommandScript.ISRUNNING
            }

            StateCommandScript.EDIT -> {
                stop()
                state = StateCommandScript.ISEDITING
            }

            else -> {}
        }
    }

    /**
     * ️⚡️Перевод состояния в строку ️
     */
    fun stateToString(): String = when (state) {
        StateCommandScript.START -> "START"
        StateCommandScript.PAUSE -> "PAUSE"
        StateCommandScript.RESUME -> "RESUME"
        StateCommandScript.STOP -> "STOP"
        StateCommandScript.EDIT -> "EDIT"
        StateCommandScript.ISRUNNING -> "isRUNNING"
        StateCommandScript.ISTOPPING -> "isSTOPPING"
        StateCommandScript.ISEDITING -> "isEDITING"
        StateCommandScript.ISPAUSE -> "isPAUSE"
    }

    fun log(str: String) = logger(str)

    private fun stop() {
        cancelRun()
        register.fill(0f)
        publishRegisters()
        pc.value = 0
    }

    /** Отдать текущее содержимое регистров на экран */
    private fun publishRegisters() {
        registers.value = register.toList()
    }

    private fun cancelRun() {
        //Отмена асинхронна: корутина может ещё доработать инструкцию.
        //Смена поколения запрещает ей писать pc и state после остановки.
        generation++
        job?.cancel()
        job = null
    }

    /**
     * Запустить исполнение с текущего pc
     */
    private fun launchRun() {
        cancelRun()
        val myGeneration = generation
        job = scope.launch {
            try {
                runLoop(myGeneration)
                //Дошли до END
                if (isCurrent(myGeneration)) {
                    publishRegisters()
                    pc.value = 0
                    state = StateCommandScript.ISTOPPING
                    log("Скрипт окончен")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ScriptException) {
                //pc оставляем на битой строке, регистры не чистим — для диагностики
                if (isCurrent(myGeneration)) {
                    publishRegisters()
                    state = StateCommandScript.ISTOPPING
                    log("Ошибка в строке ${e.line}: ${e.message}")
                }
                Timber.e(e, "Script: ошибка в строке ${e.line}")
            } catch (e: Exception) {
                if (isCurrent(myGeneration)) {
                    publishRegisters()
                    state = StateCommandScript.ISTOPPING
                    log("Ошибка в строке ${pc.value}: ${e.message}")
                }
                Timber.e(e, "Script: ошибка в строке ${pc.value}")
            }
        }
    }

    /**
     * Этот прогон всё ещё актуален, то есть его не отменили и не сменили новым
     */
    private fun isCurrent(myGeneration: Int) = myGeneration == generation

    /**
     * Цикл исполнения. Выходит нормально только по END.
     */
    private suspend fun CoroutineScope.runLoop(myGeneration: Int) {
        var steps = 0
        var lastPublish = 0L

        while (isActive && isCurrent(myGeneration)) {

            val snapshot = list.toList()
            val current = pc.value

            if (current !in snapshot.indices) {
                throw ScriptException(current, "PC вне списка (строк: ${snapshot.size})")
            }

            val cmd = parseCommand(snapshot[current], current)
            if (cmd == Cmd.End) return

            execute(cmd, snapshot, current, myGeneration)

            val now = System.currentTimeMillis()
            if (now - lastPublish >= REGISTER_PUBLISH_MS) {
                lastPublish = now
                if (isCurrent(myGeneration)) publishRegisters()
            }

            //Не занимать ядро, если в скрипте нет ни одного DELAY
            if (++steps % YIELD_EVERY == 0) delay(1)
        }
    }

    /**
     * Исполнить команду. Двигает pc сама, потому что переходы у каждой команды свои.
     */
    private suspend fun execute(cmd: Cmd, snapshot: List<String>, current: Int, myGeneration: Int) {

        //Писать состояние можно только пока прогон актуален: отмена асинхронна,
        //и остановленная корутина не должна затирать сброшенные pc и регистры
        fun setPc(value: Int) {
            if (isCurrent(myGeneration)) pc.value = value
        }

        fun setRegister(index: Int, value: Float) {
            if (isCurrent(myGeneration)) register[index] = value
        }

        when (cmd) {

            Cmd.End -> return //обрабатывается в runLoop

            Cmd.Nop -> setPc(current + 1)

            Cmd.EndIf -> setPc(current + 1)

            //Истинная ветка дошла до ELSE — прыгаем на парный ENDIF
            Cmd.Else -> setPc(findPairLine(snapshot, current, stopOnElse = false))

            is Cmd.Goto -> {
                if (cmd.target !in snapshot.indices) {
                    throw ScriptException(current, "GOTO ${cmd.target}: строки нет")
                }
                setPc(cmd.target)
            }

            is Cmd.Delay -> {
                //Сначала ждём, потом двигаем pc: пока идёт задержка, на экране
                //должна быть подсвечена сама строка DELAY, а не следующая
                delay(cmd.ms)
                setPc(current + 1)
            }

            is Cmd.Load -> {
                setRegister(cmd.dst, valueOf(cmd.src))
                setPc(current + 1)
            }

            is Cmd.Arith -> {
                val operand = valueOf(cmd.src)
                setRegister(
                    cmd.dst,
                    if (cmd.isPlus) register[cmd.dst] + operand else register[cmd.dst] - operand
                )
                setPc(current + 1)
            }

            is Cmd.If -> {
                val result = cmd.op.apply(register[cmd.left], valueOf(cmd.right))
                setPc(
                    if (result) {
                        current + 1
                    } else {
                        //Ложная ветка — на строку после ELSE либо на ENDIF
                        findPairLine(snapshot, current, stopOnElse = true)
                    }
                )
            }

            is Cmd.GenSwitch -> {
                genSwitch(cmd)
                setPc(current + 1)
            }

            is Cmd.GenValue -> {
                genValue(cmd, valueOf(cmd.value))
                setPc(current + 1)
            }

            is Cmd.GenMod -> {
                genMod(cmd)
                setPc(current + 1)
            }
        }
    }

    private fun valueOf(operand: Operand): Float = when (operand) {
        is Operand.Const -> operand.value
        is Operand.Reg -> register[operand.index]
    }

    //╭─ Генератор ───────────────────────────────────────────────────────────╮

    private fun genSwitch(cmd: Cmd.GenSwitch) {
        val first = cmd.ch == 1
        when (cmd.block) {
            GenBlock.CR ->
                if (first) gen.liveData.ch1_EN.update { cmd.on }
                else gen.liveData.ch2_EN.update { cmd.on }

            GenBlock.AM ->
                if (first) gen.liveData.ch1_AM_EN.update { cmd.on }
                else gen.liveData.ch2_AM_EN.update { cmd.on }

            GenBlock.FM ->
                if (first) gen.liveData.ch1_FM_EN.update { cmd.on }
                else gen.liveData.ch2_FM_EN.update { cmd.on }
        }
    }

    private fun genValue(cmd: Cmd.GenValue, value: Float) {
        val first = cmd.ch == 1
        when (cmd.block) {

            GenBlock.CR ->
                if (first) gen.liveData.ch1_Carrier_Fr.update { value }
                else gen.liveData.ch2_Carrier_Fr.update { value }

            GenBlock.AM ->
                if (first) gen.liveData.ch1_AM_Fr.update { value }
                else gen.liveData.ch2_AM_Fr.update { value }

            GenBlock.FM -> when (cmd.param) {
                //BASE — частота несущей, вокруг которой идёт девиация
                GenParam.BASE ->
                    if (first) gen.liveData.ch1_Carrier_Fr.update { value }
                    else gen.liveData.ch2_Carrier_Fr.update { value }

                GenParam.DEV ->
                    if (first) gen.liveData.ch1_FM_Dev.update { value }
                    else gen.liveData.ch2_FM_Dev.update { value }

                GenParam.FR ->
                    if (first) gen.liveData.ch1_FM_Fr.update { value }
                    else gen.liveData.ch2_FM_Fr.update { value }
            }
        }
    }

    private fun genMod(cmd: Cmd.GenMod) {
        val first = cmd.ch == 1
        when (cmd.block) {
            GenBlock.CR ->
                if (first) gen.liveData.ch1_Carrier_Filename.update { cmd.name }
                else gen.liveData.ch2_Carrier_Filename.update { cmd.name }

            GenBlock.AM ->
                if (first) gen.liveData.ch1_AM_Filename.update { cmd.name }
                else gen.liveData.ch2_AM_Filename.update { cmd.name }

            GenBlock.FM ->
                if (first) gen.liveData.ch1_FM_Filename.update { cmd.name }
                else gen.liveData.ch2_FM_Filename.update { cmd.name }
        }
    }

    //╰───────────────────────────────────────────────────────────────────────╯

}
