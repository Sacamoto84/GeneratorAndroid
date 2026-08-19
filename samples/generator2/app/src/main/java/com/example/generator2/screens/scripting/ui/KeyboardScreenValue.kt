package com.example.generator2.screens.scripting.ui

import androidx.compose.runtime.Composable

/**
 * Экраны выбора параметра. У каждого параметра две клавиши: взять значение
 * из регистра (Fx) или набрать числом — отсюда пары кнопок с одним словом.
 */

/** Что именно правим у FM: частоту модуляции, базу, девиацию или форму. */
@Composable
internal fun KeyboardScreenFmValue(keyboard: ScriptKeyboard, arg: Int) {

    fun pick(word: String, next: RouteKeyboardEnum) {
        keyboard.put(arg, word)
        keyboard.routeTo(RouteKeyboard(arg + 1, next))
    }

    KeyboardGrid(
        k0 = { KeyX("FR Fx") { pick("FR", RouteKeyboardEnum.F) } },
        k1 = { KeyX("FR xx") { pick("FR", RouteKeyboardEnum.NUMBER) } },
        k2 = { },
        k3 = { KeyBack(keyboard) },
        k4 = {
            KeyX("MOD") {
                keyboard.replace(arg, "MOD")
                keyboard.routeTo(RouteKeyboard(arg + 1, RouteKeyboardEnum.MODFM))
            }
        },
        k5 = { },
        k6 = { },
        k7 = { },
        k8 = { KeyX("BASE Fx") { pick("BASE", RouteKeyboardEnum.F) } },
        k9 = { KeyX("BASE xx") { pick("BASE", RouteKeyboardEnum.NUMBER) } },
        k10 = { },
        k11 = { },
        k12 = { KeyX("DEV Fx") { pick("DEV", RouteKeyboardEnum.F) } },
        k13 = { KeyX("DEV xx") { pick("DEV", RouteKeyboardEnum.NUMBER) } },
        k14 = { },
        k15 = { }
    )
}

/**
 * Правая часть сравнения в IF: слово сюда не пишется, экран только выбирает,
 * откуда придёт значение.
 */
@Composable
internal fun KeyboardScreenIfValue(keyboard: ScriptKeyboard, arg: Int) {

    fun source(next: RouteKeyboardEnum) = keyboard.routeTo(RouteKeyboard(arg, next))

    KeyboardGrid(
        k0 = { KeyX("Fx") { source(RouteKeyboardEnum.F) } },
        k1 = { KeyX("xxxx.x") { source(RouteKeyboardEnum.NUMBER) } },
        k2 = { },
        k3 = { KeyBack(keyboard) },
        k4 = { },
        k5 = { },
        k6 = { },
        k7 = { KeyBlank() },
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

/** Несущие с AM правятся одинаково: частота либо форма сигнала. */
@Composable
internal fun KeyboardScreenCrAmValue(keyboard: ScriptKeyboard, arg: Int) {

    fun pick(word: String, next: RouteKeyboardEnum) {
        keyboard.replace(arg, word)
        keyboard.routeTo(RouteKeyboard(arg + 1, next))
    }

    //Список форм зависит от того, чем команда начиналась: у несущей он свой
    fun modRoute(): RouteKeyboardEnum =
        if (keyboard.command[0] in listOf("CRL", "CRR", "CR1", "CR2")) RouteKeyboardEnum.MODCR
        else RouteKeyboardEnum.MODAM

    KeyboardGrid(
        k0 = { KeyX("FR Fx") { pick("FR", RouteKeyboardEnum.F) } },
        k1 = { KeyX("FR xx.x") { pick("FR", RouteKeyboardEnum.NUMBER) } },
        k2 = { },
        k3 = { KeyBack(keyboard) },
        k4 = {
            KeyX("MOD") {
                keyboard.replace(arg, "MOD")
                keyboard.routeTo(RouteKeyboard(arg + 1, modRoute()))
            }
        },
        k5 = { },
        k6 = { },
        k7 = { KeyBlank() },
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
