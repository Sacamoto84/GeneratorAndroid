package com.example.generator2.screens.nodes.top

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
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

@Composable
fun NodesTopBar(
    name: String,
    dirty: Boolean,
    isRunning: Boolean,
    isPaused: Boolean,
    errorCount: Int,
    warningCount: Int,
    onRun: () -> Unit,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onShowIssues: () -> Unit,
    onShowScript: () -> Unit,
    onNew: () -> Unit,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    onSaveAs: () -> Unit,
    onDeleteRename: () -> Unit,
    onFit: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D2D2F))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (dirty) "$name •" else name,
            color = Color.White,
            fontSize = 16.sp,
        )

        Box(Modifier.width(12.dp))

        if (errorCount + warningCount > 0) {
            Text(
                if (errorCount > 0) "⛔ $errorCount" else "⚠ $warningCount",
                color = if (errorCount > 0) Color(0xFFE5553A) else Color(0xFFFF9F0A),
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onShowIssues).padding(6.dp),
            )
        }

        //Пуск неактивен, пока есть ошибки: гонять генератор по половине графа
        //не стоит
        Text(
            "▶",
            color = if (errorCount == 0 && !isRunning) Color(0xFF34C759) else Color(0xFF6B6B70),
            fontSize = 18.sp,
            modifier = Modifier
                .clickable(enabled = errorCount == 0 && !isRunning, onClick = onRun)
                .padding(8.dp),
        )

        Text(
            if (isPaused) "▷" else "❚❚",
            color = if (isRunning || isPaused) Color(0xFFFF9F0A) else Color(0xFF6B6B70),
            fontSize = 16.sp,
            modifier = Modifier
                .clickable(enabled = isRunning || isPaused, onClick = onPauseResume)
                .padding(8.dp),
        )

        Text(
            "■",
            color = if (isRunning || isPaused) Color(0xFFE5553A) else Color(0xFF6B6B70),
            fontSize = 16.sp,
            modifier = Modifier
                .clickable(enabled = isRunning || isPaused, onClick = onStop)
                .padding(8.dp),
        )

        Text(
            "{ }",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.clickable(onClick = onShowScript).padding(8.dp),
        )

        Box(Modifier.weight(1f))

        Text("⤢", color = Color.White, fontSize = 18.sp, modifier = Modifier.clickable(onClick = onFit).padding(8.dp))

        Box {
            Text("☰", color = Color.White, fontSize = 18.sp,
                modifier = Modifier.clickable { menu = true }.padding(8.dp))

            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                listOf(
                    "Новый" to onNew,
                    "Открыть" to onOpen,
                    "Сохранить" to onSave,
                    "Сохранить как" to onSaveAs,
                    "Переименовать или удалить" to onDeleteRename,
                ).forEach { (text, action) ->
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = {
                            menu = false
                            action()
                        },
                    )
                }
            }
        }
    }
}
