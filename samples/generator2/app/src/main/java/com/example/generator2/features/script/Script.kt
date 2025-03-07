package com.example.generator2.features.script

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.generator2.features.generator.Generator
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/*
 * ----------------- Логика -----------------
 * IF R1 = 2
 * ...
 * ELSE
 * ...
 * ENDIF
 *
 * DELAY 1000 - Задержка работы
 */

/*
 *╭─ Выйти ─╮╭─ Переход ─╮╭─ Задержка ─╮╭─ Завершение ─╮╭─ Текст ────╮
 *│ YIELD   ││ GOTO 2    ││ DELAY 4000 ││ END          ││ TEXT Текст │
 *╰─────────╯╰───────────╯╰────────────╯╰──────────────╯╰────────────╯
 *╭─ Арифметика ────┬─────────────────────╮╭─ Загрузка константы в регистр ─╮
 *│ MINUS R1 5000   │ R1 - 5000-> R1      ││ LOAD R1 2344    │ 2344 -> R1   │
 *│ MINUS F1 5000.0 │ F1 - 5.1 -> F1      ││ LOAD F1 2344.0  │ 2344 -> F1   │
 *│                 │                     │╰─────────────────┴──────────────╯
 *│ MINUS R1 R2     │ R1-R2->R1           │╭─ Отобразить дамп регистров ──╮
 *│ MINUS F1 F2     │ F1-F2->F1           ││ PRINTF - Дамп регистров F    │
 *│                 │                     │╰──────────────────────────────╯
 *│ PLUS R1 4555    │ R1 + 4555 -> 4555   │
 *│ PLUS F1 4555.5  │ F1 + 4555.5 -> 4555 │
 *│                 │                     │
 *│ PLUS R1 R2      │ R1+R2->R1           │
 *│ PLUS F1 F2      │ F1+F2->F1           │
 *╰─────────────────┴─────────────────────╯
 *╭─ Генератор ─────────────────╮╭─╮╭────────────┬──────────────╮
 *│ CH[1 2] [CR AM FM] [ON OFF] ││✓││IF F1 < 450 │ IF F1 < 450  │
 *│                             ││ ││...{true}   │ ...  {true}  │
 *│ CR[1 2] FR 1000.0     F[]   ││✓││ELSE        │ ENDIF        │
 *│ CR[1 2] MOD 02_HWawe        ││✓││...{false}  │ ...  {false} │
 *│                             ││ ││ENDIF       │              │
 *│ AM[1 2] FR 1000.3     F[]   ││✓│├───┬────┬───┼────┬────┬────┤
 *│ AM[1 2] MOD 02_HWawe        ││✓││ < │ <= │ > │ >= │ == │ != │
 *│                             ││ │╰───┴────┴───┴────┴────┴────╯
 *│ FM[1 2] BASE 1234.6   F[]   ││✓│
 *│ FM[1 2] DEV  123.8    F[]   ││✓│
 *│ FM[1 2] MOD  02_HWawe       ││✓│
 *│ FM[1 2] FR   3.5      F[]   ││✓│
 *╰─────────────────────────────╯╰─╯
 * ┌ ┐ └ ┘├ ┤ ┬ ┴ ┼ ─ │╭╮╯╰│ ─ ✓
 *╭─ Копирование регистров ───╮ Не готово
 *│ COPY R1 R2       │ R2->R1 │
 *│ COPY F1 F2       │ F2->F1 │
 *╰──────────────────┴────────┘
 *
 *
 * Переход если равно и не равно
 *
 *
 */

const val PC_MAX = 128 //Максимальный размер скрипта..При динамическом списке не имеет смысла

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


    //╭─ Генератор ───────────────────────╮
    var end = true
    private var endTime = 0L              //Время > которого можно продолжать работу


    //───────────────────────────────────────────────┐
    /**
     * ## Регистры Float 10 штук
     */
    var register = FloatArray(10)
    //───────────────────────────────────────────────┘

    var str: String = ""

    //───────────────────────────────────────────────┐
    val list = ScriptList()
    //───────────────────────────────────────────────┘
    val update = MutableStateFlow(0)

    var pc = MutableStateFlow(0)

    var state by mutableStateOf(StateCommandScript.ISTOPPING)

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
                pause()
                state = StateCommandScript.ISPAUSE
            }

            StateCommandScript.RESUME -> {
                resume()
                state = StateCommandScript.ISRUNNING
            }

            StateCommandScript.START -> {
                start()
                state = StateCommandScript.ISRUNNING
            }

            StateCommandScript.EDIT -> {
                stop()
                state = StateCommandScript.ISEDITING
            }

            else -> {}
        }
    }

    //╰─────────────────────────────╯

    fun stateToString(): String {
        val s = when (state) {
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
        return s
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun log(
        str: String
    ) { //
        GlobalScope.launch(Dispatchers.Main) {
            //consoleLog.println(str)
        }
    }

    //Тесты
    //val unit5 = listOf<String>("New", "LOAD F1 1000", "IF F1 < 10000","CR1 FR F1","PLUS F1 100","GOTO 2", "ENDIF", "END")


    suspend fun run() {
        if (end) return
        end = false
        if (System.currentTimeMillis() <= endTime) return
        endTime = 0
        while (!end) {
            try {
                val list1 = list.toList()
                if ((pc.value >= 0) && (pc.value <= list1.lastIndex)) {
                    val s = list1[pc.value]
                    println("!!! S=$s pc:$pc")
                    cmdExecute(s)
                } else {
                    println("Ошибка индекса pc:$pc")
                    println(list1.joinToString(","))
                    pc.value = 0
                }
            } catch (e: Exception) {
                println("Exception Ошибка " + e.message + " pc:" + pc)
            }
            if (System.currentTimeMillis() <= endTime) return
            delay(1)
        }
    }


    private fun start() {
        pc.value = 0
        end = false
    }

    private fun stop() {
        for (i in register.indices) {
            register[i] = 0f
        }
        pc.value = 0
        end = true
        println("Script stop()")
    }

    private fun pause() {
        end = true
    }

    private fun resume() {
        end = false
    }


    //Выполнить команду по строке
    private suspend fun cmdExecute(comand: String) {

        println("Script: $pc $comand")

        //log("${pc.value} $comand")

        //Разобрать строку на список команд
        val listCMD = comand.split(" ")
        if (listCMD.isEmpty()) {
            println("Script: Error размер listCMD == 0")
            return
        }

        when (listCMD[0]) {

            "ELSE" -> { //Ищем первое ENDIF
                var currentPC = pc.value

                while (true) {
                    if (list.get(currentPC) == "ENDIF") {
                        pc.value = currentPC
                        break
                    }
                    currentPC++
                    if (currentPC > (PC_MAX - 1)) break
                }
            }

            "ENDIF" -> {
                pc.value ++
            }

            "END" -> {
                println("Скрипт окончен")
                end = true
                command(StateCommandScript.STOP)
            }

            "IF" -> ifCommand(comand)

            "CH1", "CH2", "CR1", "CR2", "AM1", "AM2", "FM1", "FM2" -> {
                generatorComand(comand)
                pc.value++
            }

            "MINUS", "PLUS" -> {
                comandPlusMinus(comand)
                pc.value++
            }

            "GOTO" -> {
                pc.value = listCMD[1].toInt()
            }

            "DELAY" -> {
                val d = listCMD[1].toLong()
                endTime = System.currentTimeMillis() + d
                pc.value++
            }

            "LOAD" -> { //LOAD F1 2344.0  │ 2344.0 -> F1
                load(comand)
                pc.value++
            }

            else -> {
                println("Script:? pc:$pc :$comand")
                pc.value++
                if (pc.value >= PC_MAX) end = true
            }

        }

    }

    fun load(comand: String) { //LOAD F1 2344.0  │ 2344.0 -> F1
        //Разобрать строку на список команд
        val listCMD = comand.split(" ")
        if (listCMD.isEmpty()) {
            println("Script: Error Load размер listCMD == 0")
            return
        }
        val index = listCMD[1].drop(1).toInt()
        register[index] = if ((listCMD[2].first() == 'F')) register[listCMD[2].drop(1).toInt()]
        else listCMD[2].toFloat()
    }

    // IF R1 < 5500
    private fun ifCommand(command: String) {

        //Разобрать строку на список команд
        val listCMD = command.split(" ")
        if (listCMD.isEmpty()) {
            println("Script: Error ifComand размер listCMD == 0")
            return
        }

        //IF Rxx Первый всегда R регистр
        val f1value = register[listCMD[1].drop(1).toInt()]

        val f2value = if ((listCMD[3].first() == 'F')) register[listCMD[3].drop(1).toInt()]
        else listCMD[3].toFloat()

        // имеем f1value и f2value
        var boolResult = false //Результат сравнения true or false чтобы решить куда дальше

        if ((listCMD[2] == "<") && (f1value < f2value)) boolResult = true
        if ((listCMD[2] == ">") && (f1value > f2value)) boolResult = true
        if ((listCMD[2] == "<=") && (f1value <= f2value)) boolResult = true
        if ((listCMD[2] == ">=") && (f1value >= f2value)) boolResult = true
        if ((listCMD[2] == "!=") && (f1value != f2value)) boolResult = true
        if ((listCMD[2] == "==") && (f1value == f2value)) boolResult = true

        if (boolResult) {
            pc.value++ //Переход на следующую строку, ибо условие выполнено
        } else { //Ищем первое ELSE или ENDIF, ибо условие не выполнено
            var currentPC = pc.value

            while (true) {
                if (list.get(currentPC) == "ELSE") //+1 к ELSE
                {
                    pc.value = currentPC + 1
                    break
                }

                if (list.get(currentPC) == "ENDIF") {
                    pc.value = currentPC
                    break
                }
                currentPC++
                if (currentPC > (PC_MAX - 1)) break
            }
        }
    }

    private suspend fun generatorComand(command: String) {

        //Разобрать строку на список команд
        val listCMD = command.split(" ")
        if (listCMD.isEmpty()) {
            println("Script: Error generatorComand размер listCMD == 0")
            return
        }

        val chanel = listCMD[0].drop(2).toInt() //Номер канала

        //╭─ CH1 CH2 ─────────────────────────────────────────────────────────────────╮
        if ((listCMD[0] == "CH1") || (listCMD[0] == "CH2")) {                       //│                                                                       //│
            val onoff =                                                             //│
                listCMD[2] == "ON"                                                  //│
            // ──────────────────────────────────────────┬────────────────────────────┤
            if (listCMD[1] == "CR")                    //│  CH1 CR ON   CH2 CR OFF  //│
            {                                          //╰────────────────────────────┤
                if (chanel == 1)                                                    //│
                    gen.liveData.ch1_EN.update { onoff }                            //│
                else                                                                //│
                    gen.liveData.ch2_EN.update { onoff }                            //│
            }                                                                       //│
            //────────────────────────────────────────────┬───────────────────────────┤
            if (listCMD[1] == "AM")                     //│  CH1 AM ON   CH2 AM OFF //│
            {                                           //╰───────────────────────────┤
                if (chanel == 1)                                                    //│
                    gen.liveData.ch1_AM_EN.update { onoff }                         //│
                else                                                                //│
                    gen.liveData.ch2_AM_EN.update { onoff }                         //│
            }                                                                       //│
            //────────────────────────────────────────────────────────────────────────┤
            if (listCMD[1] == "FM")                                                 //│
            {                                                                       //│
                if (chanel == 1)                                                    //│
                    gen.liveData.ch1_FM_EN.update { onoff }                         //│
                else                                                                //│
                    gen.liveData.ch2_FM_EN.update { onoff }                         //│
            }                                                                       //│
            //────────────────────────────────────────────────────────────────────────┤
            return                                                                  //│
        }                                                                           //│
        //╰───────────────────────────────────────────────────────────────────────────╯

        //╭─ CR1 CR2 ─────────────────────────────────────────────────────────────────╮
        if ((listCMD[0] == "CR1") || (listCMD[0] == "CR2"))                         //│
        {                                                                           //│
            //CR[1 2] MOD 01_Sine_12b                                               //│
            if (listCMD[1] == "MOD")                                                //│
            {                                                                       //│
                println(listCMD[2])                                                 //│
                if (chanel == 1) gen.liveData.ch1_Carrier_Filename.update { listCMD[2] }    //│
                else                                                                //│
                    gen.liveData.ch2_Carrier_Filename.update { listCMD[2] }             //│
            }                                                                       //│

            //CR[1 2] FR 1000.3                                                     //│
            if (listCMD[1] == "FR")                                                 //│
            {

                val value = if (listCMD[2].first() == 'F') {
                    register[listCMD[2].drop(1).toInt()]
                } else {
                    listCMD[2].toFloat()
                }

                if (chanel == 1) {
                    gen.liveData.ch1_Carrier_Fr.update { value }
                    //channel.send(value)
                } else {                                                            //│
                    gen.liveData.ch2_Carrier_Fr.update { value }
                }                                                                   //│
            }


            return                                                                  //│
        }                                                                           //│
        //╰───────────────────────────────────────────────────────────────────────────╯

        //╭─ AM1 AM2 ─────────────────────────────────────────────────────────────────╮
        if ((listCMD[0] == "AM1") || (listCMD[0] == "AM2"))                         //│
        {                                                                           //│
            //SEGGER_RTT_printf(0, "╭─ AM1 AM2 ─╮\n");                              //│
            //│
            //AM[1 2] FR 1000.3                                                     //│
            if (listCMD[1] == "FR")                                                 //│
            {                                                                       //│
                val value = if (listCMD[2].first() == 'F') register[listCMD[2].drop(1)
                    .toInt()]                                                       //│
                else                                                                //│
                    listCMD[2].toFloat()                                            //│

                if (chanel == 1)                                                    //│
                    gen.liveData.ch1_AM_Fr.update { value }                         //│
                else                                                                //│
                    gen.liveData.ch2_AM_Fr.update { value }                         //│
            }                                                                       //│

            //AM[1 2] MOD 02_HWawe { 1.9ms }                                        //│
            if (listCMD[1] == "MOD")                                                //│
            {                                                                       //│
                if (chanel == 1) {                                                  //│
                    gen.liveData.ch1_AM_Filename.update { listCMD[2] }                  //│
                }                                                                   //│
                else                                                                //│
                    gen.liveData.ch2_AM_Filename.update { listCMD[2] }                  //│
            }                                                                       //│
            return                                                                  //│
        }                                                                           //│
        //╰───────────────────────────────────────────────────────────────────────────╯
//        //╭─ FM1 FM2 ─────────────────────────────────────────────────────────────────╮
//        if ((listCMD[0] == "FM1") || (listCMD[0] == "FM2"))                         //│
//        {                                                                           //│
//            //SEGGER_RTT_printf(0, "╭─ FM1 FM2 ─╮\n");                              //│
//
//            //FM[1 2] BASE 1234.6                                                   //│
//            if (listCMD[1] == "BASE")                                               //│
//            {                                                                       //│
//                val value = if (listCMD[2].first() == 'F') {                            //│
//                    f[listCMD[2].drop(1).toInt()]                                //│
//                } else                                                              //│
//                    listCMD[2].toFloat()                                            //│
//
//              if (chanel == 1)                                                    //│
//                  LiveData.ch1_FM_Base.update { value }                           //│
//              else                                                                //│
//                  LiveData.ch2_FM_Base.update { value }                           //│
//            }                                                                       //│
//        }                                                                           //│
        //│
        //FM[1 2] DEV  123.8                                                        //│
        if (listCMD[1] == "DEV")                                                    //│
        {
            val value = if (listCMD[2].first() == 'F') {
                register[listCMD[2].drop(1).toInt()]
            } else listCMD[2].toFloat()

            if (chanel == 1) gen.liveData.ch1_FM_Dev.update { value }
            else                                                                     //│
                gen.liveData.ch2_FM_Dev.update { value }                                    //│
        }                                                                            //│

        //FM[1 2] MOD 02_HWawe                                                       //│
        if (listCMD[1] == "MOD")                                                     //│
        {                                                                            //│
            if (chanel == 1) gen.liveData.ch1_FM_Filename.update { listCMD[2] }
            else                                                                     //│
                gen.liveData.ch2_FM_Filename.update { listCMD[2] }                          //│
        }                                                                            //│

        //FM[1 2] FR   3.5                                                           //│
        if (listCMD[1] == "FR")                                                      //│
        {                                                                            //│
            val value = if (listCMD[2].first() == 'F') {
                register[listCMD[2].drop(1).toInt()]
            } else listCMD[2].toFloat()

            if (chanel == 1) gen.liveData.ch1_FM_Fr.update { value }
            else                                                                      //│
                gen.liveData.ch2_FM_Fr.update { value }                                      //│
        }                                                                             //│
        return                                                                        //│
    }                                                                                 //│ // ╰────────────────────────────────────────────────────────────────────────────────╯

    private fun comandPlusMinus(comand: String) { //MINUS F1 5000.0
        //MINUS F1 F2
        val listCMD = comand.split(" ")
        if (listCMD.isEmpty()) {
            println("Script: Error comandPlusMinus размер listCMD == 0")
            return
        }
        val index = listCMD[1].drop(1).toInt() //Индекс первой ячейки 0..9 //MINUS F1 F2
        if (listCMD[2].first() == 'F') { //Второй операнд это регистор
            val secondIndex = listCMD[2].drop(1)
                .toInt() //Индекс второго регистра

            // ┌── MINUS ────────────────────────────────────┐
            if (listCMD[0] == "MINUS") {
                register[index] = register[index] - register[secondIndex]  //MINUS F* F*
            }
            //└────────────────────────────────────────────┘

            //┌── PLUS ────────────────────────────────────┐
            if (listCMD[0] == "PLUS") {
                register[index] = register[index] + register[secondIndex]          //PLUS F* F*
            } //└────────────────────────────────────────────┘
        } else { //Второй операнд это константа
            //MINUS F1 5000.0
            val fvalue = listCMD[2].toFloat() //┌── MINUS ───────────────────────────────────┐
            if (listCMD[0] == "MINUS") {
                register[index] = register[index] - fvalue //MINUS F* F*
            } //└────────────────────────────────────────────┘
            //┌── PLUS ────────────────────────────────────┐
            if (listCMD[0] == "PLUS") {
                register[index] = register[index] + fvalue //MINUS F* F*
            } //└────────────────────────────────────────────┘
        }
    }

    //Тесты
//    fun unit5Load() {
//        list.value.clear()
//        list.value.add("New")
//        list.add("LOAD F1 1000")
//        list.add("IF F1 < 10000")
//        list.add("CR1 FR F1")
//        list.add("PLUS F1 100")
//        list.add("DELAY 50")
//        list.add("GOTO 2")
//        list.add("ENDIF")
//        list.add("END")
//    }

}

