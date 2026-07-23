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
import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.REGISTER_COUNT

@Composable
fun ConditionDialog(
    body: NodeBody.Condition,
    onDone: (NodeBody.Condition) -> Unit,
    onDismiss: () -> Unit,
) {
    var left by remember { mutableStateOf(body.left) }
    var op by remember { mutableStateOf(body.op) }
    var right by remember { mutableStateOf(body.right) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .background(Color(0xFF2D2D2F), RoundedCornerShape(14.dp))
                .padding(16.dp),
        ) {
            Text("Условие", color = Color.White, fontSize = 16.sp)

            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Picker("F$left", (0 until REGISTER_COUNT).map { "F$it" }) { left = it }
                Picker(op.text, CompareOp.entries.map { it.text }) { op = CompareOp.entries[it] }
                OperandField(value = right, onChange = { right = it })
            }

            Text(
                "Выход «да» — условие верно, «нет» — неверно",
                color = Color(0xFF9A9AA0),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 10.dp),
            )

            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Отмена", color = Color(0xFF9A9AA0)) }
                TextButton(onClick = { onDone(NodeBody.Condition(left, op, right)) }) {
                    Text("Готово", color = Color.White)
                }
            }
        }
    }
}
