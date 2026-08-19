package com.example.generator2.screens.scripting.ui

import androidx.compose.runtime.Composable

/**
 * Числовая клавиатура: набирает одно слово-число на позиции arg.
 *
 * Слово копится в локальной строке и после каждой клавиши целиком уезжает
 * в скрипт — так же ведёт себя и DEL, отрезая последний символ.
 */
@Composable
internal fun KeyboardScreenNumberPad(keyboard: ScriptKeyboard, arg: () -> Int) {

    while (keyboard.command.lastIndex < arg()) {
        keyboard.command.add("")
    }

    var typed: String = keyboard.command[arg()]

    keyboard.commit()

    fun type(symbol: String) {
        typed += symbol
        keyboard.put(arg(), typed)
    }

    fun backspace() {
        typed = typed.dropLast(1)
        keyboard.put(arg(), typed)
    }

    KeyboardGrid(
        k0 = { KeyX("1") { type("1") } },
        k1 = { KeyX("2") { type("2") } },
        k2 = { KeyX("3") { type("3") } },
        k3 = { KeyX("DEL") { backspace() } },
        k4 = { KeyX("4") { type("4") } },
        k5 = { KeyX("5") { type("5") } },
        k6 = { KeyX("6") { type("6") } },
        k7 = { KeyBack(keyboard) },
        k8 = { KeyX("7") { type("7") } },
        k9 = { KeyX("8") { type("8") } },
        k10 = { KeyX("9") { type("9") } },
        k11 = { },
        k12 = { KeyX(".") { type(".") } },
        k13 = { KeyX("0") { type("0") } },
        k14 = { },
        k15 = { KeyEnter(keyboard) }
    )
}
