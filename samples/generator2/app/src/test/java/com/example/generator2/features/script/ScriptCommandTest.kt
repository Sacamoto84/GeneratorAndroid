package com.example.generator2.features.script

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScriptCommandTest {

    //╭─ Разбор ──────────────────────────────────────────────────────────────╮

    @Test
    fun `пустая строка и вопрос дают Nop`() {
        assertEquals(Cmd.Nop, parseCommand(""))
        assertEquals(Cmd.Nop, parseCommand("   "))
        assertEquals(Cmd.Nop, parseCommand("?"))
    }

    @Test
    fun `лишние пробелы не мешают`() {
        assertEquals(Cmd.Goto(2), parseCommand("  GOTO   2  "))
    }

    @Test
    fun `LOAD с константой и с регистром`() {
        assertEquals(Cmd.Load(1, Operand.Const(2344f)), parseCommand("LOAD F1 2344.0"))
        assertEquals(Cmd.Load(1, Operand.Reg(2)), parseCommand("LOAD F1 F2"))
    }

    @Test
    fun `префикс R равнозначен F`() {
        assertEquals(parseCommand("LOAD F1 5"), parseCommand("LOAD R1 5"))
    }

    @Test
    fun `арифметика`() {
        assertEquals(Cmd.Arith(true, 1, Operand.Const(100f)), parseCommand("PLUS F1 100"))
        assertEquals(Cmd.Arith(false, 3, Operand.Reg(4)), parseCommand("MINUS F3 F4"))
    }

    @Test
    fun `сравнение IF`() {
        assertEquals(
            Cmd.If(1, CompareOp.LESS, Operand.Const(10000f)), parseCommand("IF F1 < 10000")
        )
        assertEquals(
            Cmd.If(0, CompareOp.NOT_EQ, Operand.Reg(9)), parseCommand("IF F0 != F9")
        )
    }

    @Test
    fun `команды генератора`() {
        assertEquals(
            Cmd.GenSwitch(1, GenBlock.FM, true), parseCommand("CH1 FM ON")
        )
        assertEquals(
            Cmd.GenSwitch(2, GenBlock.CR, false), parseCommand("CH2 CR OFF")
        )
        assertEquals(
            Cmd.GenValue(1, GenBlock.CR, GenParam.FR, Operand.Reg(1)), parseCommand("CR1 FR F1")
        )
        assertEquals(
            Cmd.GenValue(2, GenBlock.FM, GenParam.BASE, Operand.Const(1234.6f)),
            parseCommand("FM2 BASE 1234.6")
        )
        assertEquals(
            Cmd.GenValue(1, GenBlock.FM, GenParam.DEV, Operand.Const(123.8f)),
            parseCommand("FM1 DEV 123.8")
        )
        assertEquals(
            Cmd.GenMod(1, GenBlock.AM, "02_HWawe"), parseCommand("AM1 MOD 02_HWawe")
        )
    }

    //╰───────────────────────────────────────────────────────────────────────╯

    //╭─ Ошибки разбора ──────────────────────────────────────────────────────╮

    private fun assertFails(source: String) {
        try {
            parseCommand(source, line = 7)
            throw AssertionError("ожидалась ScriptException для: $source")
        } catch (e: ScriptException) {
            assertEquals(7, e.line)
            assertTrue("пустое сообщение", !e.message.isNullOrBlank())
        }
    }

    @Test
    fun `регистр вне диапазона это ошибка а не падение массива`() {
        assertFails("LOAD F10 5")
        assertFails("PLUS F42 1")
    }

    @Test
    fun `недостаток аргументов это ошибка`() {
        assertFails("LOAD")
        assertFails("LOAD F1")
        assertFails("IF F1 <")
        assertFails("GOTO")
        assertFails("DELAY")
    }

    @Test
    fun `мусорные аргументы это ошибка`() {
        assertFails("GOTO два")
        assertFails("DELAY -5")
        assertFails("LOAD F1 abc")
        assertFails("IF F1 =< 5")
        assertFails("CH1 FM MAYBE")
        assertFails("CR1 BASE 100")
        assertFails("FM1 WAT 100")
        assertFails("LOAD 5 F1")
    }

    @Test
    fun `неизвестная команда это ошибка`() {
        assertFails("PRINTF")
        assertFails("!")
    }

    /**
     * Клавиатура пишет команду в список по мере набора, так что в скрипте
     * может остаться незавершённая строка. Она обязана давать понятную ошибку,
     * а не падение по индексу.
     */
    @Test
    fun `недобранная с клавиатуры команда это ошибка`() {
        assertFails("CH1")
        assertFails("CH1 CR")
        assertFails("FM1 DEV")
        assertFails("CR1 MOD")
    }

    //╰───────────────────────────────────────────────────────────────────────╯

    //╭─ Поиск парной строки ─────────────────────────────────────────────────╮

    @Test
    fun `ложное условие без ELSE прыгает на ENDIF`() {
        val lines = listOf("IF F1 < 5", "PLUS F1 1", "ENDIF", "END")
        assertEquals(2, findPairLine(lines, 0, stopOnElse = true))
    }

    @Test
    fun `ложное условие с ELSE прыгает за ELSE`() {
        val lines = listOf("IF F1 < 5", "PLUS F1 1", "ELSE", "MINUS F1 1", "ENDIF", "END")
        assertEquals(3, findPairLine(lines, 0, stopOnElse = true))
    }

    @Test
    fun `истинная ветка от ELSE идёт на ENDIF`() {
        val lines = listOf("IF F1 < 5", "PLUS F1 1", "ELSE", "MINUS F1 1", "ENDIF", "END")
        assertEquals(4, findPairLine(lines, 2, stopOnElse = false))
    }

    @Test
    fun `вложенный IF не сбивает поиск`() {
        val lines = listOf(
            "IF F1 < 5",     // 0
            "IF F2 < 5",     // 1
            "PLUS F2 1",     // 2
            "ELSE",          // 3 — принадлежит вложенному IF
            "MINUS F2 1",    // 4
            "ENDIF",         // 5 — закрывает вложенный
            "ELSE",          // 6 — принадлежит внешнему
            "PLUS F1 1",     // 7
            "ENDIF",         // 8
            "END"            // 9
        )
        assertEquals(7, findPairLine(lines, 0, stopOnElse = true))
        assertEquals(8, findPairLine(lines, 6, stopOnElse = false))
        assertEquals(4, findPairLine(lines, 1, stopOnElse = true))
        assertEquals(5, findPairLine(lines, 3, stopOnElse = false))
    }

    @Test(expected = ScriptException::class)
    fun `отсутствие ENDIF это ошибка а не выход за границы`() {
        findPairLine(listOf("IF F1 < 5", "PLUS F1 1", "END"), 0, stopOnElse = true)
    }

    //╰───────────────────────────────────────────────────────────────────────╯
}
