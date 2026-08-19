package com.example.generator2.screens.scripting.ui

import androidx.compose.runtime.Composable

/**
 * Выбор регистра F0..F9 на позицию arg.
 *
 * Куда уходить дальше, решает NoHomeRoute текущего маршрута: у команд вроде
 * IF за регистром идёт ещё сравнение, у остальных команда на этом кончается.
 */
@Composable
internal fun KeyboardScreenRegisterPad(keyboard: ScriptKeyboard, arg: () -> Int) {

    fun pick(register: String) {
        keyboard.put(arg(), register)

        val next = keyboard.route.value.NoHomeRoute
        if (next == null) keyboard.goHome()
        else keyboard.routeTo(RouteKeyboard(arg() + 1, next))
    }

    KeyboardGrid(
        k0 = { KeyX("F1") { pick("F1") } },
        k1 = { KeyX("F2") { pick("F2") } },
        k2 = { KeyX("F3") { pick("F3") } },
        k3 = { KeyBack(keyboard) },
        k4 = { KeyX("F4") { pick("F4") } },
        k5 = { KeyX("F5") { pick("F5") } },
        k6 = { KeyX("F6") { pick("F6") } },
        k7 = { },
        k8 = { KeyX("F7") { pick("F7") } },
        k9 = { KeyX("F8") { pick("F8") } },
        k10 = { KeyX("F9") { pick("F9") } },
        k11 = { },
        k12 = { },
        k13 = { KeyX("F0") { pick("F0") } },
        k14 = { },
        k15 = { }
    )
}
