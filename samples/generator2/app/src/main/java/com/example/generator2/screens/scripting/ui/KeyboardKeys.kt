package com.example.generator2.screens.scripting.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.generator2.screens.scripting.atom.TemplateButtonBottomBar
import com.example.generator2.theme.colorDarkBackground

/** Обычная клавиша: подпись плюс действие. */
@Composable
internal fun KeyX(label: String, color: Color = Color.White, onClick: () -> Unit) {
    TemplateButtonBottomBar(str = label, contentColor = color, onClick = onClick)
}

/** Команда набрана — закрыть её и вернуться на HOME. */
@Composable
internal fun KeyEnter(keyboard: ScriptKeyboard) {
    KeyX("DONE", onClick = { keyboard.goHome() })
}

/** Шаг назад по стеку экранов. */
@Composable
internal fun KeyBack(keyboard: ScriptKeyboard) {
    KeyX("<-", onClick = { keyboard.backRoute() })
}

/** Пустое место в сетке, но с габаритами клавиши — держит разметку. */
@Composable
internal fun KeyBlank() {
    OutlinedButton(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 4.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = Color.Transparent, contentColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.Transparent),
        contentPadding = PaddingValues(2.dp)
    ) {
    }
}

/**
 * Сетка 4x4. null означает, что места под клавишу нет вовсе: соседи в этом
 * ряду разъезжаются на освободившуюся ширину.
 */
@Composable
internal fun KeyboardGrid(
    k0: (@Composable () -> Unit)? = null,
    k1: (@Composable () -> Unit)? = null,
    k2: (@Composable () -> Unit)? = null,
    k3: (@Composable () -> Unit)? = null,
    k4: (@Composable () -> Unit)? = null,
    k5: (@Composable () -> Unit)? = null,
    k6: (@Composable () -> Unit)? = null,
    k7: (@Composable () -> Unit)? = null,
    k8: (@Composable () -> Unit)? = null,
    k9: (@Composable () -> Unit)? = null,
    k10: (@Composable () -> Unit)? = null,
    k11: (@Composable () -> Unit)? = null,
    k12: (@Composable () -> Unit)? = null,
    k13: (@Composable () -> Unit)? = null,
    k14: (@Composable () -> Unit)? = null,
    k15: (@Composable () -> Unit)? = null,
) {
    val keys = listOf(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9, k10, k11, k12, k13, k14, k15)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorDarkBackground)
    ) {
        keys.chunked(4).forEach { row ->
            Row {
                row.forEach { key ->
                    if (key != null) Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        key()
                    }
                }
            }
        }
    }
}
