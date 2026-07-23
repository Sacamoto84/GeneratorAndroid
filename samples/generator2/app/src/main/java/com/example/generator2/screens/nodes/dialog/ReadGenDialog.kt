package com.example.generator2.screens.nodes.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.script.GenBlock
import com.example.generator2.features.script.GenParam
import com.example.generator2.features.script.REGISTER_COUNT

@Composable
fun ReadGenDialog(
    body: NodeBody.ReadGen,
    onDone: (NodeBody.ReadGen) -> Unit,
    onDismiss: () -> Unit,
) {
    var dst by remember { mutableStateOf(body.dst) }
    var ch by remember { mutableStateOf(body.ch) }
    var block by remember { mutableStateOf(body.block) }
    var param by remember { mutableStateOf(body.param) }

    //FR у любого блока, BASE и DEV только у FM
    val params = if (block == GenBlock.FM) GenParam.entries else listOf(GenParam.FR)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .background(Color(0xFF2D2D2F), RoundedCornerShape(14.dp))
                .padding(16.dp),
        ) {
            Text("Чтение частоты", color = Color.White, fontSize = 16.sp)

            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Picker("F$dst", (0 until REGISTER_COUNT).map { "F$it" }) { dst = it }
                Text("=", color = Color.White, fontSize = 16.sp)
                Picker(block.name, GenBlock.entries.map { it.name }) {
                    block = GenBlock.entries[it]
                    //сменили блок на не-FM — BASE/DEV недоступны, откатываем на FR
                    if (block != GenBlock.FM) param = GenParam.FR
                }
                Picker("CH$ch", listOf("CH1", "CH2")) { ch = it + 1 }
                Picker(param.name, params.map { it.name }) { param = params[it] }
            }

            Text(
                "Читает текущую частоту блока генератора в регистр",
                color = Color(0xFF9A9AA0),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 10.dp),
            )

            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Отмена", color = Color(0xFF9A9AA0)) }
                TextButton(onClick = { onDone(NodeBody.ReadGen(dst, ch, block, param)) }) {
                    Text("Готово", color = Color.White)
                }
            }
        }
    }
}
