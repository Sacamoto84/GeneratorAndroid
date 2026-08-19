package com.example.generator2.screens.scripting.ui

import androidx.compose.runtime.Composable

/**
 * Экраны выбора одного слова из короткого набора: они только подставляют
 * слово на позицию arg и передают ход следующему экрану.
 */

/** Знак сравнения для IF, дальше идёт значение правой части. */
@Composable
internal fun KeyboardScreenComparison(keyboard: ScriptKeyboard, arg: Int) {

    fun pick(sign: String) {
        keyboard.replace(arg, sign)
        keyboard.routeTo(RouteKeyboard(arg + 1, RouteKeyboardEnum.IFValue))
    }

    KeyboardGrid(
        k0 = { KeyX("<") { pick("<") } },
        k1 = { KeyX(">") { pick(">") } },
        k2 = { },
        k3 = { KeyBack(keyboard) },
        k4 = { KeyX("<=") { pick("<=") } },
        k5 = { KeyX(">=") { pick(">=") } },
        k6 = { },
        k7 = { },
        k8 = { KeyX("==") { pick("==") } },
        k9 = { KeyX("!=") { pick("!=") } },
        k10 = { },
        k11 = { KeyBlank() },
        k12 = { },
        k13 = { },
        k14 = { },
        k15 = { KeyBlank() }
    )
}

/** Включить или выключить — на этом команда закончена. */
@Composable
internal fun KeyboardScreenOnOff(keyboard: ScriptKeyboard, arg: Int) {

    fun pick(state: String) {
        keyboard.put(arg, state)
        keyboard.goHome()
    }

    KeyboardGrid(
        k0 = { KeyX("ON") { pick("ON") } },
        k1 = { KeyX("OFF") { pick("OFF") } },
        k2 = { },
        k3 = { KeyBack(keyboard) },
        k4 = { KeyBlank() },
        k5 = { },
        k6 = { },
        k7 = { },
        k8 = { },
        k9 = { },
        k10 = { },
        k11 = { KeyBlank() },
        k12 = { },
        k13 = { },
        k14 = { },
        k15 = { KeyBlank() }
    )
}

/** Какой блок канала трогаем: несущая, AM или FM. Дальше — ON/OFF. */
@Composable
internal fun KeyboardScreenCrAmFm(keyboard: ScriptKeyboard, arg: Int) {

    fun pick(block: String) {
        keyboard.put(arg, block)
        keyboard.routeTo(RouteKeyboard(arg + 1, RouteKeyboardEnum.ONOFF))
    }

    KeyboardGrid(
        k0 = { KeyX("CR") { pick("CR") } },
        k1 = { KeyX("AM") { pick("AM") } },
        k2 = { KeyX("FM") { pick("FM") } },
        k3 = { KeyBack(keyboard) },
        k4 = { },
        k5 = { KeyBlank() },
        k6 = { },
        k7 = { },
        k8 = { },
        k9 = { },
        k10 = { },
        k11 = { KeyBlank() },
        k12 = { },
        k13 = { },
        k14 = { },
        k15 = { KeyBlank() }
    )
}
