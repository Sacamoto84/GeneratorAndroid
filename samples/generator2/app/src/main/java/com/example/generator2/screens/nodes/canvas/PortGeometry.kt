package com.example.generator2.screens.nodes.canvas

import androidx.compose.ui.geometry.Offset
import com.example.generator2.features.nodes.model.Port

/**
 * Куда смотрят вход и выход ноды на холсте. Влияет только на картинку:
 * компилятор ходит по связям, а не по геометрии, поэтому ориентация
 * ничего в скрипте не меняет.
 *
 * LR — вход слева, выход справа (как было). RL — зеркально. TB — вход
 * сверху, выход снизу. BT — наоборот.
 */
enum class Orientation(val label: String) {
    LR("Вход слева → выход справа"),
    RL("Вход справа → выход слева"),
    TB("Вход сверху → выход снизу"),
    BT("Вход снизу → выход сверху"),
}

/**
 * Точка входа ноды в её локальных координатах (0..w, 0..h).
 * Вход всегда один, лежит на стороне, противоположной выходам.
 */
fun Orientation.entryAnchor(w: Float, h: Float): Offset = when (this) {
    Orientation.LR -> Offset(0f, h / 2f)
    Orientation.RL -> Offset(w, h / 2f)
    Orientation.TB -> Offset(w / 2f, 0f)
    Orientation.BT -> Offset(w / 2f, h)
}

/**
 * Точка выхода для конкретного порта. У OUT — центр выходной стороны.
 * У Условия два порта: YES ближе к началу оси, NO — дальше, чтобы провода
 * «да» и «нет» не сливались.
 */
fun Orientation.exitAnchor(port: Port, w: Float, h: Float): Offset {
    //Доля вдоль выходной стороны: OUT посередине, YES/NO разведены
    val frac = when (port) {
        Port.OUT -> 0.5f
        Port.YES -> 0.3f
        Port.NO -> 0.7f
    }
    return when (this) {
        Orientation.LR -> Offset(w, h * frac)
        Orientation.RL -> Offset(0f, h * frac)
        Orientation.TB -> Offset(w * frac, h)
        Orientation.BT -> Offset(w * frac, 0f)
    }
}

/** true — ось раскладки горизонтальна (LR/RL); провода гнём по x, иначе по y */
val Orientation.isHorizontal: Boolean
    get() = this == Orientation.LR || this == Orientation.RL
