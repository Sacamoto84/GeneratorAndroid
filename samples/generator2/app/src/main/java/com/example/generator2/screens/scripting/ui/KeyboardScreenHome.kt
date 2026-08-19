package com.example.generator2.screens.scripting.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.generator2.theme.colorChL
import com.example.generator2.theme.colorChR

/**
 * Стартовый экран: выбор команды. Слово встаёт в начало строки, дальше
 * клавиатура уходит на экран, который умеет добрать аргументы.
 */
@Composable
internal fun KeyboardScreenHome(keyboard: ScriptKeyboard) {

    //Начать команду словом и уйти добирать аргумент на позицию 1
    fun start(
        word: String,
        route: RouteKeyboardEnum,
        noHomeRoute: RouteKeyboardEnum? = null
    ) {
        keyboard.startWith(word)
        keyboard.routeTo(RouteKeyboard(1, route, noHomeRoute))
    }

    //Команда целиком, аргументов у неё нет
    fun whole(word: String) = keyboard.list.update(keyboard.selectIndex, word)

    KeyboardGrid(
        k0 = {
            KeyX("CHL", color = colorChL) { start("CHL", RouteKeyboardEnum.CRAMFM) }
        },
        k1 = {
            KeyX("CRL", color = colorChL) { start("CRL", RouteKeyboardEnum.CRAMValue) }
        },
        k2 = {
            KeyX("AML", color = colorChL) { start("AML", RouteKeyboardEnum.CRAMValue) }
        },
        k3 = {
            KeyX("FML", color = colorChL) { start("FML", RouteKeyboardEnum.FMValue) }
        },
        k4 = {
            KeyX("CHR", color = colorChR) { start("CHR", RouteKeyboardEnum.CRAMFM) }
        },
        k5 = {
            KeyX("CRR", color = colorChR) { start("CRR", RouteKeyboardEnum.CRAMValue) }
        },
        k6 = {
            KeyX("AMR", color = colorChR) { start("AMR", RouteKeyboardEnum.CRAMValue) }
        },
        k7 = {
            KeyX("FMR", color = colorChR) { start("FMR", RouteKeyboardEnum.FMValue) }
        },
        k8 = {
            KeyX("GOTO", color = Color.White) { start("GOTO", RouteKeyboardEnum.NUMBER) }
        },
        k9 = {
            KeyX("IF", color = Color.White) {
                start("IF", RouteKeyboardEnum.F, RouteKeyboardEnum.Comparison)
            }
        },
        k10 = {
            KeyX("ELSE", color = Color.White) { whole("ELSE") }
        },
        k11 = {
            KeyX("PLUS", color = Color.White) {
                start("PLUS", RouteKeyboardEnum.F, RouteKeyboardEnum.IFValue)
            }
        },
        k12 = {
            KeyX("DELAY", color = Color.White) { start("DELAY", RouteKeyboardEnum.NUMBER) }
        },
        k13 = {
            KeyX("ENDIF", color = Color.White) { whole("ENDIF") }
        },
        k14 = {
            KeyX("LOAD", color = Color.White) {
                start("LOAD", RouteKeyboardEnum.F, RouteKeyboardEnum.IFValue)
            }
        },
        k15 = {
            KeyX("MINUS", color = Color.White) {
                start("MINUS", RouteKeyboardEnum.F, RouteKeyboardEnum.IFValue)
            }
        }
    )
}
