package com.example.generator2.screens.scripting.ui

//Экраны для нижнего меню
enum class RouteKeyboardEnum {
    HOME, NUMBER, F, ONOFF, CRAMFM, CRAMValue, FMValue, Comparison, IFValue, MODCR, MODAM, MODFM
}

//Если есть NoHomeRoute то мы идем по нему а не на Home используем для F для создания альтернативного маршрута
data class RouteKeyboard(
    var argument: Int = 0, var route: RouteKeyboardEnum, var NoHomeRoute: RouteKeyboardEnum? = null
)
