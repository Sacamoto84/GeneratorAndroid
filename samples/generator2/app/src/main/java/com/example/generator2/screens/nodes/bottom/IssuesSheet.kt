package com.example.generator2.screens.nodes.bottom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.features.nodes.Issue
import com.example.generator2.features.nodes.Severity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssuesSheet(issues: List<Issue>, onPick: (Issue) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 32.dp)) {
            Text(
                "Что мешает запуску",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            LazyColumn {
                //Сначала ошибки: они блокируют пуск, предупреждения нет
                items(issues.sortedBy { it.severity != Severity.ERROR }) { issue ->
                    Text(
                        (if (issue.severity == Severity.ERROR) "⛔ " else "⚠ ") + issue.text,
                        color = if (issue.severity == Severity.ERROR) Color(0xFFE5553A) else Color(0xFFFF9F0A),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(issue) }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}
