package com.example.generator2.screens.nodes.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.generator2.features.nodes.model.RegOp
import com.example.generator2.features.script.REGISTER_COUNT

@Composable
fun RegisterDialog(
    body: NodeBody.Register,
    onDone: (NodeBody.Register) -> Unit,
    onDismiss: () -> Unit,
) {
    var op by remember { mutableStateOf(body.op) }
    var dst by remember { mutableStateOf(body.dst) }
    var src by remember { mutableStateOf(body.src) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .background(Color(0xFF2D2D2F), RoundedCornerShape(14.dp))
                .padding(16.dp),
        ) {
            Text("Регистр", color = Color.White, fontSize = 16.sp)

            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Picker("F$dst", (0 until REGISTER_COUNT).map { "F$it" }) { dst = it }

                Picker(
                    when (op) {
                        RegOp.LOAD -> "="
                        RegOp.PLUS -> "+="
                        RegOp.MINUS -> "-="
                    },
                    listOf("=", "+=", "-="),
                ) { op = RegOp.entries[it] }

                OperandField(value = src, onChange = { src = it })
            }

            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Отмена", color = Color(0xFF9A9AA0)) }
                TextButton(onClick = { onDone(NodeBody.Register(op, dst, src)) }) {
                    Text("Готово", color = Color.White)
                }
            }
        }
    }
}

/** Список коротких вариантов; наружу отдаёт индекс выбранного */
@Composable
internal fun Picker(current: String, options: List<String>, onPick: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }

    Text(
        "$current ▾",
        color = Color.White,
        fontSize = 14.sp,
        modifier = Modifier
            .background(Color(0xFF3D3D3F), RoundedCornerShape(5.dp))
            .clickable { open = true }
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )

    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        options.forEachIndexed { i, text ->
            DropdownMenuItem(
                text = { Text(text) },
                onClick = {
                    onPick(i)
                    open = false
                },
            )
        }
    }
}
