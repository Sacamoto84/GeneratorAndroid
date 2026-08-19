package com.example.generator2.screens.scripting.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.script.Script
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.util.Stack

/**
 * Клавиатурка редактора скрипта.
 *
 * Держит редактируемую строку в виде списка слов и стек экранов. Сами экраны
 * лежат в KeyboardScreen*.kt: класс отвечает за состояние и маршрутизацию,
 * разметку рисуют они.
 */
class ScriptKeyboard(private val s: Script, val gen: Generator) {

    internal var selectIndex = s.pc.value
    internal val list = s.list

    internal val route: MutableState<RouteKeyboard> =
        mutableStateOf(RouteKeyboard(0, RouteKeyboardEnum.HOME))

    private val routeStack = Stack<RouteKeyboard>() //Стек для отработки назад

    /**
     * Слова редактируемой команды. Экраны читают и правят его напрямую там,
     * где готовых операций ниже не хватает.
     */
    internal var command: MutableList<String> = mutableListOf()

    init {
        Timber.i("ScriptKeyboard() init{}")
    }

    /*
     *╭─────────────────────────────────────╮
     *│    Преобразование текста в список   │
     *╰─────────────────────────────────────╯
     */
    private fun textToListCommand(str: String) {
        command.clear()
        command = str.split(" ").toMutableList()
    }

    /*
    *╭─────────────────────────────────────╮
    *│                                     │
    *╰─────────────────────────────────────╯
    */
    private fun listCommandToText(): String {
        return command.joinToString(" ")
    }

    /*
    *╭─────────────────────────────────────╮
    *│    Добавить в список по индексу     │
    *╰─────────────────────────────────────╯
    */
    private fun listCommandAddToIndex(index: Int, text: String) {
        if (command.isEmpty()) command.add("!")

        while (command.lastIndex < index) {
            command.add("?")
        }

        command[index] = text
    }

    /*
    *╭─────────────────────────────────────╮
    *│    Удалить запись по индексу        │
    *╰─────────────────────────────────────╯
    */
    internal fun removeAt(index: Int) {
        if ((index >= 0) && (index <= command.lastIndex)) command.removeAt(index)
    }

    /** Записать текущий список слов в редактируемую строку скрипта. */
    internal fun commit() = list.update(selectIndex, listCommandToText())

    /** Слово на позицию index, всё разом: правка списка плюс запись в скрипт. */
    internal fun put(index: Int, word: String) {
        listCommandAddToIndex(index, word)
        commit()
    }

    /** То же, но старое слово на этой позиции сначала выбрасывается. */
    internal fun replace(index: Int, word: String) {
        removeAt(index)
        put(index, word)
    }

    /** Начать команду заново с одного слова — так работают кнопки экрана HOME. */
    internal fun startWith(word: String) {
        command.clear()
        put(0, word)
    }

    internal fun routeTo(r: RouteKeyboard) {
        routeStack.push(route.value)
        route.value = r
    }

    /** Уйти на HOME с чистым стеком: команда набрана, возвращаться некуда. */
    internal fun goHome() {
        routeTo(RouteKeyboard(0, RouteKeyboardEnum.HOME))
        routeStack.clear()
    }

    //На кнопку назад, вытянуть из стека экран
    internal fun backRoute() {

        if (routeStack.empty()) {
            route.value = RouteKeyboard(0, RouteKeyboardEnum.HOME)
            return
        }

        removeAt(route.value.argument)
        commit()

        route.value = routeStack.pop()

        removeAt(route.value.argument)
        commit()

    }

    @Composable
    fun Core(pc: () -> Int) {

        if (selectIndex < 0) selectIndex = 0

        if (s.list.size() == 0) return

        if (pc() < 0)
            s.pc.update { 0 }

        if (selectIndex > list.lastIndex()) selectIndex = list.lastIndex()

        val pc1 = s.pc.collectAsStateWithLifecycle().value
        if (pc1 > list.lastIndex())
            s.pc.update { list.lastIndex() }

        textToListCommand(list.get(selectIndex))
        selectIndex = pc1

        Timber.tag("script").i("Keyboard Core() PC:${s.pc}")

        when (route.value.route) {
            RouteKeyboardEnum.HOME -> KeyboardScreenHome(this)
            RouteKeyboardEnum.NUMBER -> KeyboardScreenNumberPad(this) { route.value.argument }
            RouteKeyboardEnum.F -> KeyboardScreenRegisterPad(this) { route.value.argument }
            RouteKeyboardEnum.ONOFF -> KeyboardScreenOnOff(this, route.value.argument)
            RouteKeyboardEnum.CRAMFM -> KeyboardScreenCrAmFm(this, route.value.argument)
            RouteKeyboardEnum.CRAMValue -> KeyboardScreenCrAmValue(this, route.value.argument)
            RouteKeyboardEnum.FMValue -> KeyboardScreenFmValue(this, route.value.argument)
            RouteKeyboardEnum.Comparison -> KeyboardScreenComparison(this, route.value.argument)
            RouteKeyboardEnum.IFValue -> KeyboardScreenIfValue(this, route.value.argument)
            RouteKeyboardEnum.MODCR -> KeyboardScreenModulation(this, route.value.argument, "CR")
            RouteKeyboardEnum.MODAM -> KeyboardScreenModulation(this, route.value.argument, "AM")
            RouteKeyboardEnum.MODFM -> KeyboardScreenModulation(this, route.value.argument, "FM")
        }

    }

}
