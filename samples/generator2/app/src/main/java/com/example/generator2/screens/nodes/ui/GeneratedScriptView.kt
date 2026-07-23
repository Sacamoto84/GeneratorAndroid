package com.example.generator2.screens.nodes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.generator2.features.nodes.CompileResult
import com.example.generator2.features.nodes.Issue

/**
 * Скомпилированный скрипт только для чтения, с подсветкой текущей строки.
 * Если граф не компилируется — вместо текста список ошибок.
 */
@Composable
fun GeneratedScriptView(
    result: CompileResult,
    pc: Int,
    nodeTitleOfLine: (Int) -> String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF1D1D1F))
                .padding(12.dp),
        ) {
            Row(Modifier.fillMaxWidth()) {
                Text("Сгенерированный скрипт", color = Color.White, fontSize = 16.sp)
                Column(Modifier.weight(1f)) {}
                TextButton(onClick = onDismiss) { Text("Закрыть", color = Color.White) }
            }

            when (result) {
                is CompileResult.Failed -> Errors(result.errors + result.warnings)

                is CompileResult.Ok -> LazyColumn(Modifier.fillMaxSize()) {
                    itemsIndexed(result.lines) { i, line ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(if (i == pc) Color(0xFF2E4A2E) else Color.Transparent)
                                .padding(vertical = 1.dp),
                        ) {
                            Text(
                                i.toString().padStart(3),
                                color = Color(0xFF6B6B70),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                            )
                            Text(
                                "  $line",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                nodeTitleOfLine(i),
                                color = Color(0xFF6B6B70),
                                fontSize = 11.sp,
                                maxLines = 1,
                                modifier = Modifier.width(96.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Errors(issues: List<Issue>) {
    LazyColumn {
        itemsIndexed(issues) { _, issue ->
            Text(
                issue.text,
                color = Color(0xFFE5553A),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
    }
}
