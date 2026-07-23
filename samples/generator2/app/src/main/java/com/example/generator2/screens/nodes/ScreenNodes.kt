package com.example.generator2.screens.nodes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.generator2.screens.nodes.canvas.NodeCanvas
import com.example.generator2.screens.nodes.dialog.NodePickerSheet
import com.example.generator2.screens.nodes.vm.VMNodes

@Composable
fun ScreenNodes(vm: VMNodes) {

    var pickerOpen by remember { mutableStateOf(false) }
    val activeNode by vm.runner.activeNode.collectAsState(initial = null)

    Scaffold(
        floatingActionButton = {
            if (vm.linkFrom == null) {
                FloatingActionButton(onClick = { pickerOpen = true }) {
                    Text("+", fontSize = 24.sp)
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            NodeCanvas(
                graph = vm.graph,
                selected = vm.selected,
                activeNode = activeNode,
                linking = vm.linkFrom != null,
                canBeTarget = vm::canBeTarget,
                fitKey = vm.fitRequest,
                onSelect = { id ->
                    if (vm.linkFrom != null) vm.completeLink(id) else vm.selected = id
                },
                onTapEmpty = {
                    if (vm.linkFrom != null) vm.cancelLink() else vm.selected = null
                },
                onMove = vm::moveNode,
            )
        }
    }

    if (pickerOpen) {
        NodePickerSheet(
            onPick = {
                vm.addNode(it)
                pickerOpen = false
            },
            onDismiss = { pickerOpen = false },
        )
    }
}
