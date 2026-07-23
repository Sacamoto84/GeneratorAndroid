package com.example.generator2.screens.nodes.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.screens.nodes.vm.NodeKind

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodePickerSheet(onPick: (NodeKind) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 32.dp)) {
            listOf(
                NodeKind.STEP to "Шаг — параметры генератора и задержка",
                NodeKind.DELAY to "Задержка — только пауза",
                NodeKind.REGISTER to "Регистр — присвоить, прибавить, вычесть",
                NodeKind.READ to "Чтение — частота генератора в регистр",
                NodeKind.CONDITION to "Условие — два выхода: да и нет",
                NodeKind.STOP to "Стоп — конец прогона",
            ).forEach { (kind, text) ->
                Text(
                    text,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(kind) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                )
            }
        }
    }
}
