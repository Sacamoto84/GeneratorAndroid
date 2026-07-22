package com.example.generator2.features.script

/**
 * Количество регистров F0..F9
 */
const val REGISTER_COUNT = 10

/**
 * Разделитель токенов. Вынесен из функций: разбор идёт в горячем цикле движка,
 * компилировать регулярку на каждую инструкцию незачем.
 */
private val whitespace = Regex("\\s+")

/**
 * Ошибка разбора или исполнения строки скрипта.
 * Номер строки проставляется движком, если он известен.
 */
class ScriptException(val line: Int, message: String) : Exception(message)

/**
 * Токен похож на регистр: префикс F или R, дальше целое число.
 * Диапазон не проверяется — этим занимается вызывающий.
 */
fun looksLikeRegister(token: String): Boolean {
    val prefix = token.firstOrNull() ?: return false
    if (prefix != 'F' && prefix != 'R') return false
    return token.drop(1).toIntOrNull() != null
}

/**
 * Индекс регистра из токена F1 или R1.
 * null — токен не регистр либо номер вне F0..F[REGISTER_COUNT]-1.
 */
fun registerIndexOrNull(token: String): Int? {
    if (!looksLikeRegister(token)) return null
    val index = token.drop(1).toIntOrNull() ?: return null
    return if (index in 0 until REGISTER_COUNT) index else null
}

/**
 * Операнд из токена: "F1" -> Reg(1), "50" -> Const(50f).
 * null, если токен ни регистр, ни число.
 */
fun parseOperand(token: String): Operand? {
    registerIndexOrNull(token)?.let { return Operand.Reg(it) }
    return token.toFloatOrNull()?.let { Operand.Const(it) }
}

/**
 * Обратно в токен скрипта. Const(50f) -> "50.0", Reg(1) -> "F1".
 */
fun Operand.toToken(): String = when (this) {
    is Operand.Const -> value.toString()
    is Operand.Reg -> "F$index"
}

/**
 * Операнд команды: константа или регистр
 */
sealed interface Operand {
    data class Const(val value: Float) : Operand
    data class Reg(val index: Int) : Operand
}

/**
 * Блок генератора, к которому обращается команда
 */
enum class GenBlock { CR, AM, FM }

/**
 * Числовой параметр блока генератора
 */
enum class GenParam { FR, BASE, DEV }

enum class CompareOp(val text: String) {
    LESS("<"), LESS_EQ("<="), GREATER(">"), GREATER_EQ(">="), EQ("=="), NOT_EQ("!=");

    fun apply(left: Float, right: Float): Boolean = when (this) {
        LESS -> left < right
        LESS_EQ -> left <= right
        GREATER -> left > right
        GREATER_EQ -> left >= right
        EQ -> left == right
        NOT_EQ -> left != right
    }
}

/**
 * Разобранная команда скрипта
 */
sealed interface Cmd {
    data object Nop : Cmd
    data object End : Cmd
    data object Else : Cmd
    data object EndIf : Cmd
    data class Goto(val target: Int) : Cmd
    data class Delay(val ms: Long) : Cmd
    data class Load(val dst: Int, val src: Operand) : Cmd
    data class Arith(val isPlus: Boolean, val dst: Int, val src: Operand) : Cmd
    data class If(val left: Int, val op: CompareOp, val right: Operand) : Cmd

    /** CH[1 2] [CR AM FM] [ON OFF] */
    data class GenSwitch(val ch: Int, val block: GenBlock, val on: Boolean) : Cmd

    /** CR[1 2] FR 1000.0 | FM[1 2] DEV F1 | FM[1 2] BASE 1234.6 */
    data class GenValue(
        val ch: Int, val block: GenBlock, val param: GenParam, val value: Operand
    ) : Cmd

    /** CR[1 2] MOD 01_Sine */
    data class GenMod(val ch: Int, val block: GenBlock, val name: String) : Cmd
}

/**
 * Поиск парной строки блока IF с учётом вложенности.
 *
 * @param from строка IF или ELSE, поиск идёт со следующей
 * @param stopOnElse true для IF с ложным условием: остановиться и на ELSE тоже
 * @return строка, на которую нужно перейти
 * @throws ScriptException если парный ENDIF не найден
 */
fun findPairLine(lines: List<String>, from: Int, stopOnElse: Boolean): Int {
    var depth = 0
    var i = from + 1

    while (i <= lines.lastIndex) {
        when (lines[i].trim().split(whitespace).firstOrNull()) {
            "IF" -> depth++

            "ELSE" -> if (depth == 0 && stopOnElse) return i + 1

            "ENDIF" -> {
                if (depth == 0) return i
                depth--
            }
        }
        i++
    }

    throw ScriptException(from, "не найден парный ENDIF")
}

/**
 * Разобрать строку скрипта в команду.
 *
 * @param line номер строки, попадает в текст ошибки
 * @throws ScriptException если строка не разобрана
 */
fun parseCommand(source: String, line: Int = -1): Cmd {

    val t = source.trim().split(whitespace).filter { it.isNotEmpty() }

    if (t.isEmpty() || t[0] == "?") return Cmd.Nop

    fun fail(message: String): Nothing = throw ScriptException(line, message)

    fun arg(index: Int): String =
        t.getOrNull(index) ?: fail("${t[0]}: мало аргументов, ожидался ${index + 1}-й")

    //F1 R1 -> индекс регистра, иначе null
    fun registerIndex(token: String): Int? {
        if (!looksLikeRegister(token)) return null
        return registerIndexOrNull(token)
            ?: fail("регистр $token вне диапазона F0..${REGISTER_COUNT - 1}")
    }

    fun register(token: String): Int =
        registerIndex(token) ?: fail("ожидался регистр F0..F${REGISTER_COUNT - 1}, получено $token")

    fun operand(token: String): Operand {
        registerIndex(token)?.let { return Operand.Reg(it) }
        return token.toFloatOrNull()?.let { Operand.Const(it) }
            ?: fail("не число и не регистр: $token")
    }

    //CH1 CR1 AM1 FM1 -> номер канала
    fun channel(token: String): Int = when (token.last()) {
        '1' -> 1
        '2' -> 2
        else -> fail("не разобран номер канала: $token")
    }

    fun block(token: String): GenBlock = when (token) {
        "CR" -> GenBlock.CR
        "AM" -> GenBlock.AM
        "FM" -> GenBlock.FM
        else -> fail("ожидался блок CR AM FM, получено $token")
    }

    return when (val head = t[0]) {

        "END" -> Cmd.End
        "ELSE" -> Cmd.Else
        "ENDIF" -> Cmd.EndIf

        "GOTO" -> Cmd.Goto(
            arg(1).toIntOrNull() ?: fail("GOTO: номер строки не число: ${arg(1)}")
        )

        "DELAY" -> {
            val ms = arg(1).toLongOrNull() ?: fail("DELAY: не число: ${arg(1)}")
            if (ms < 0) fail("DELAY: отрицательная задержка $ms")
            Cmd.Delay(ms)
        }

        "LOAD" -> Cmd.Load(register(arg(1)), operand(arg(2)))

        "PLUS", "MINUS" -> Cmd.Arith(head == "PLUS", register(arg(1)), operand(arg(2)))

        "IF" -> {
            val op = CompareOp.entries.firstOrNull { it.text == arg(2) }
                ?: fail("IF: неизвестное сравнение ${arg(2)}")
            Cmd.If(register(arg(1)), op, operand(arg(3)))
        }

        //CH[1 2] [CR AM FM] [ON OFF]
        "CH1", "CH2" -> {
            val onoff = arg(2)
            if (onoff != "ON" && onoff != "OFF") fail("$head: ожидалось ON или OFF, получено $onoff")
            Cmd.GenSwitch(channel(head), block(arg(1)), onoff == "ON")
        }

        //CR[1 2] AM[1 2] FM[1 2]
        "CR1", "CR2", "AM1", "AM2", "FM1", "FM2" -> {
            val genBlock = block(head.dropLast(1))
            val ch = channel(head)
            when (val param = arg(1)) {
                "MOD" -> Cmd.GenMod(ch, genBlock, arg(2))
                "FR" -> Cmd.GenValue(ch, genBlock, GenParam.FR, operand(arg(2)))
                "BASE" -> {
                    if (genBlock != GenBlock.FM) fail("$head: BASE есть только у FM")
                    Cmd.GenValue(ch, genBlock, GenParam.BASE, operand(arg(2)))
                }

                "DEV" -> {
                    if (genBlock != GenBlock.FM) fail("$head: DEV есть только у FM")
                    Cmd.GenValue(ch, genBlock, GenParam.DEV, operand(arg(2)))
                }

                else -> fail("$head: неизвестный параметр $param")
            }
        }

        else -> fail("неизвестная команда $head")
    }
}
