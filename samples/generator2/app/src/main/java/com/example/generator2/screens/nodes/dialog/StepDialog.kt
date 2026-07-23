package com.example.generator2.screens.nodes.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.generator2.features.nodes.model.ChannelParams
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.StepParams
import com.example.generator2.features.script.Operand

/**
 * Диалог ноды Шаг: 22 поля, у каждого галочка «менять».
 * Снятая галочка означает null — параметр не попадёт в скрипт вовсе.
 */
@Composable
fun StepDialog(
    title: String,
    step: NodeBody.Step,
    carrierNames: List<String>,
    modNames: List<String>,
    onSnapshot: () -> StepParams,
    onDone: (title: String, step: NodeBody.Step) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(title) }
    var params by remember { mutableStateOf(step.params) }
    var delay by remember { mutableStateOf(step.delayMs.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .background(Color(0xFF2D2D2F), RoundedCornerShape(14.dp))
                .padding(12.dp)
                .heightIn(max = 560.dp),
        ) {
            BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3D3D3F), RoundedCornerShape(6.dp))
                    .padding(8.dp),
            )

            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
            ) {
                listOf(1 to params.ch1, 2 to params.ch2).forEach { (ch, p) ->

                    val update: (ChannelParams) -> Unit = { updated ->
                        params = if (ch == 1) params.copy(ch1 = updated) else params.copy(ch2 = updated)
                    }

                    Group("CH$ch · Несущая", listOf(p.carrierEnabled, p.carrierFr, p.carrierMod)) {
                        BoolRow("Канал включён", p.carrierEnabled) { update(p.copy(carrierEnabled = it)) }
                        OperandRow("Частота, Гц", p.carrierFr) { update(p.copy(carrierFr = it)) }
                        ModRow("Форма", p.carrierMod, carrierNames) { update(p.copy(carrierMod = it)) }
                    }

                    Group("CH$ch · AM", listOf(p.amEnabled, p.amFr, p.amMod)) {
                        BoolRow("AM включена", p.amEnabled) { update(p.copy(amEnabled = it)) }
                        OperandRow("Частота, Гц", p.amFr) { update(p.copy(amFr = it)) }
                        ModRow("Форма", p.amMod, modNames) { update(p.copy(amMod = it)) }
                    }

                    Group("CH$ch · FM", listOf(p.fmEnabled, p.fmBase, p.fmDev, p.fmFr, p.fmMod)) {
                        BoolRow("FM включена", p.fmEnabled) { update(p.copy(fmEnabled = it)) }
                        OperandRow("Несущая (BASE), Гц", p.fmBase) { update(p.copy(fmBase = it)) }
                        OperandRow("Девиация, Гц", p.fmDev) { update(p.copy(fmDev = it)) }
                        OperandRow("Частота, Гц", p.fmFr) { update(p.copy(fmFr = it)) }
                        ModRow("Форма", p.fmMod, modNames) { update(p.copy(fmMod = it)) }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Задержка после шага", color = Color.White, fontSize = 14.sp)
                Box(Modifier.weight(1f))
                BasicTextField(
                    value = delay,
                    onValueChange = { delay = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp, textAlign = TextAlign.End),
                    cursorBrush = SolidColor(Color.White),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(72.dp)
                        .background(Color(0xFF3D3D3F), RoundedCornerShape(5.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
                Text(" мс", color = Color(0xFF9A9AA0), fontSize = 13.sp)
            }

            TextButton(onClick = { params = onSnapshot() }, Modifier.fillMaxWidth()) {
                Text("↓ Снять с генератора", color = Color(0xFF34C759), fontSize = 14.sp)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Отмена", color = Color(0xFF9A9AA0)) }
                TextButton(
                    onClick = {
                        onDone(name, NodeBody.Step(params, delay.toLongOrNull() ?: 0L))
                    }
                ) { Text("Готово", color = Color.White) }
            }
        }
    }
}

/** Заголовок группы со счётчиком отмеченных полей; свёрнута по умолчанию */
@Composable
private fun Group(title: String, fields: List<Any?>, content: @Composable () -> Unit) {
    val checked = fields.count { it != null }
    var expanded by remember { mutableStateOf(checked > 0) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .background(Color(0xFF3D3D3F), RoundedCornerShape(6.dp))
            .clickable { expanded = !expanded }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (expanded) "▾" else "▸", color = Color(0xFF9A9AA0), fontSize = 12.sp)
        Text("  $title", color = Color(0xFF3A7BD5), fontSize = 13.sp)
        Box(Modifier.weight(1f))
        Text("$checked из ${fields.size}", color = Color(0xFF9A9AA0), fontSize = 11.sp)
    }

    if (expanded) content()
}

@Composable
private fun FieldRow(label: String, on: Boolean, onToggle: (Boolean) -> Unit, value: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = on,
            onCheckedChange = onToggle,
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3A7BD5)),
        )
        Text(
            label,
            color = if (on) Color.White else Color(0xFF6B6B70),
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )
        if (on) value() else Text("—", color = Color(0xFF6B6B70), fontSize = 13.sp)
    }
}

@Composable
private fun BoolRow(label: String, value: Boolean?, onChange: (Boolean?) -> Unit) {
    FieldRow(label, value != null, { onChange(if (it) false else null) }) {
        val on = value == true
        Text(
            if (on) "ON" else "OFF",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier
                .background(
                    if (on) Color(0xFF34C759) else Color(0xFF6B6B70),
                    RoundedCornerShape(5.dp),
                )
                .clickable { onChange(!on) }
                .padding(horizontal = 10.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun OperandRow(label: String, value: Operand?, onChange: (Operand?) -> Unit) {
    FieldRow(label, value != null, { onChange(if (it) Operand.Const(0f) else null) }) {
        OperandField(value = value!!, onChange = onChange)
    }
}

@Composable
private fun ModRow(label: String, value: String?, names: List<String>, onChange: (String?) -> Unit) {
    FieldRow(label, value != null, { onChange(if (it) names.firstOrNull().orEmpty() else null) }) {
        WaveformPicker(value = value!!, names = names, onPick = onChange)
    }
}
