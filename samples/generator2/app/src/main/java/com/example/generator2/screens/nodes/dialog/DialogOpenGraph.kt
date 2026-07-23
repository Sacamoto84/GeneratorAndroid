package com.example.generator2.screens.nodes.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun DialogOpenGraph(names: List<String>, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .background(Color(0xFF2D2D2F), RoundedCornerShape(14.dp))
                .padding(12.dp)
                .heightIn(max = 420.dp),
        ) {
            Text("Открыть граф", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(8.dp))

            if (names.isEmpty()) {
                Text("Сохранённых графов пока нет", color = Color(0xFF9A9AA0), fontSize = 13.sp,
                    modifier = Modifier.padding(8.dp))
            }

            LazyColumn {
                items(names) { name ->
                    Text(
                        name,
                        color = Color.White,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(name) }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}
