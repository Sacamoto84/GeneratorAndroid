package com.example.generator2.screens.nodes.top

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
