package com.example.generator2.screens.mainscreen4.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.generator2.common.haptic.Haptic
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.threephase.patterns.ThreePhasePatterns
import com.example.generator2.screens.common.modifier.noRippleClickable
import com.example.generator2.screens.mainscreen4.textStyleButtonOnOff
import com.example.generator2.screens.mainscreen4.ui.MainscreenTextBoxAndDropdownMenu
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.theme.colorLightBackground
import com.example.generator2.theme.colorLightBackground2
import kotlinx.coroutines.flow.MutableStateFlow

/** Калибровка 3-фазы по умолчанию: A = B = C = 0 дБ, центр 13 % (как в restim). */
private const val CENTER_DEFAULT = 13f

/**
 * Карточка режима «3-фаза»: круг с точкой, выбор паттерна, параметры,
 * сворачиваемая калибровка. Заменяет карточки CHL/CHR, пока режим включён.
 */
@Composable
fun CardThreePhase(gen: Generator) {

    val d = gen.liveData
    val pattern by d.tp_Pattern.collectAsStateWithLifecycle()
    var calibrationOpen by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxWidth()
            .background(colorLightBackground)
    ) {
        Column(Modifier.padding(bottom = 8.dp)) {

            ThreePhaseCircle(
                gen.threePhase,
                fingerActive = pattern == ThreePhasePatterns.FINGER,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            PatternSelector(pattern) { name ->
                if (name != pattern) {
                    //Смена паттерна стирает нарисованную пальцем траекторию
                    gen.threePhase.patterns.finger.clear()
                    d.tp_Pattern.value = name
                }
                Haptic.confirm()
            }

            Row(Modifier.padding(top = 4.dp)) {
                Param("Скорость", d.tp_Velocity, "%.1f", listOf("0.1", "0.5", "1.0", "2.0", "5.0", "10.0"), 0.1f..10f, 0.01f)
                Param("Ампл.", d.tp_Amplitude, "%.2f", listOf("0.25", "0.5", "0.75", "1.0"), 0f..1f, 0.002f)
            }
            Row(Modifier.padding(top = 4.dp)) {
                Param("Гц", d.tp_Carrier_Fr, "%.0f", listOf("300", "500", "700", "1000", "1500", "2000"), 50f..10000f, 1f)
                Param("Громк.", d.tp_Volume, "%.2f", listOf("0.05", "0.1", "0.2", "0.5", "1.0"), 0f..1f, 0.002f)
            }

            ToggleButton("Калибровка", calibrationOpen, Modifier.padding(top = 8.dp, end = 8.dp)) {
                calibrationOpen = !calibrationOpen
                Haptic.confirm()
            }

            if (calibrationOpen) {
                val db = listOf("-20", "-10", "-6", "-3", "0")
                Row(Modifier.padding(top = 4.dp)) {
                    Param("A", d.tp_Calib_A, "%.2f", db, -40f..10f, 0.02f)
                    Param("B", d.tp_Calib_B, "%.2f", db, -40f..10f, 0.02f)
                    Param("C", d.tp_Calib_C, "%.2f", db, -40f..10f, 0.02f)
                }
                Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Param("Центр %", d.tp_Center, "%.1f", listOf("0", "5", "13", "20"), 0f..20f, 0.02f)
                    ToggleButton("Сброс", false, Modifier.weight(1f).padding(end = 8.dp)) {
                        d.tp_Calib_A.value = 0f
                        d.tp_Calib_B.value = 0f
                        d.tp_Calib_C.value = 0f
                        d.tp_Center.value = CENTER_DEFAULT
                        Haptic.confirm()
                    }
                }
            }
        }
    }
}

/** Числовой параметр: подпись + значение, тянется пальцем, пресеты в меню. */
@Composable
private fun RowScope.Param(
    label: String,
    state: MutableStateFlow<Float>,
    format: String,
    items: List<String>,
    range: ClosedFloatingPointRange<Float>,
    sensing: Float,
) {
    val value by state.collectAsStateWithLifecycle()
    MainscreenTextBoxAndDropdownMenu(
        str = label + " " + String.format(format, value),
        modifier = Modifier.weight(1f),
        items = items,
        value = value,
        onChange = { state.value = it.coerceIn(range) },
        sensing = sensing,
        range = range,
    )
}

@Composable
private fun PatternSelector(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        Modifier
            .padding(start = 8.dp, end = 8.dp, top = 4.dp)
            .fillMaxWidth()
            .height(36.dp)
            .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(colorDarkBackground)
            .noRippleClickable { expanded = true },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "$selected  ▾",
            color = Color.LightGray,
            modifier = Modifier.padding(start = 12.dp),
            style = textStyleButtonOnOff
        )

        DropdownMenu(
            offset = DpOffset(12.dp, 4.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .wrapContentWidth()
                .background(colorLightBackground2)
                .border(1.dp, color = Color.DarkGray, shape = RoundedCornerShape(16.dp))
        ) {
            ThreePhasePatterns.NAMES.forEach { name ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    onSelect(name)
                }) {
                    Text(text = name, color = if (name == selected) Color(0xFF01AE0F) else Color.White)
                }
            }
        }
    }
}

@Composable
private fun ToggleButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .padding(start = 8.dp)
            .height(32.dp)
            .border(2.dp, if (selected) Color(0xFF1B5E20) else Color.DarkGray, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF01AE0F) else colorDarkBackground)
            .noRippleClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = if (selected) colorDarkBackground else Color.LightGray,
            style = textStyleButtonOnOff
        )
    }
}
