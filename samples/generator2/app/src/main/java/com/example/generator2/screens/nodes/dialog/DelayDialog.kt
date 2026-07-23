package com.example.generator2.screens.nodes.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.generator2.features.nodes.model.NodeBody

@Composable
fun DelayDialog(
    body: NodeBody.Delay,
    onDone: (NodeBody.Delay) -> Unit,
    onDismiss: () -> Unit,
) {
    var ms by remember { mutableStateOf(TextFieldValue(body.delayMs.toString())) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .background(Color(0xFF2D2D2F), RoundedCornerShape(14.dp))
                .padding(16.dp),
        ) {
            Text("Задержка", color = Color.White, fontSize = 16.sp)

            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Пауза", color = Color.White, fontSize = 14.sp)
                Box(Modifier.weight(1f))
                MsField(ms) { ms = it }
                Text(" мс", color = Color(0xFF9A9AA0), fontSize = 13.sp)
            }

            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Отмена", color = Color(0xFF9A9AA0)) }
                TextButton(onClick = { onDone(NodeBody.Delay(ms.text.toLongOrNull() ?: 0L)) }) {
                    Text("Готово", color = Color.White)
                }
            }
        }
    }
}

/** Поле для миллисекунд: TextFieldValue, чтобы курсор не прыгал */
@Composable
internal fun MsField(value: TextFieldValue, onChange: (TextFieldValue) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = TextStyle(color = Color.White, fontSize = 13.sp, textAlign = TextAlign.End),
        cursorBrush = SolidColor(Color.White),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .width(80.dp)
            .background(Color(0xFF3D3D3F), RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
    )
}
