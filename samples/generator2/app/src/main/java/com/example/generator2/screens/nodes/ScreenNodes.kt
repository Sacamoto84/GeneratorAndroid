package com.example.generator2.screens.nodes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.node
import com.example.generator2.screens.common.dialog.DialogDeleteRename
import com.example.generator2.screens.common.dialog.DialogSaveAs
import com.example.generator2.screens.nodes.bottom.IssuesSheet
import com.example.generator2.screens.nodes.bottom.NodeActionBar
import com.example.generator2.screens.nodes.bottom.RunSheet
import com.example.generator2.screens.nodes.canvas.NodeCanvas
import com.example.generator2.screens.nodes.dialog.ConditionDialog
import com.example.generator2.screens.nodes.dialog.DelayDialog
import com.example.generator2.screens.nodes.dialog.DialogOpenGraph
import com.example.generator2.screens.nodes.dialog.NodePickerSheet
import com.example.generator2.screens.nodes.dialog.RegisterDialog
import com.example.generator2.screens.nodes.dialog.StepDialog
import com.example.generator2.screens.nodes.top.NodesTopBar
import com.example.generator2.screens.nodes.ui.GeneratedScriptView
import com.example.generator2.screens.nodes.vm.VMNodes

@Composable
fun ScreenNodes(vm: VMNodes) {

    var pickerOpen by remember { mutableStateOf(false) }
    var issuesOpen by remember { mutableStateOf(false) }
    var scriptOpen by remember { mutableStateOf(false) }
    val activeNode by vm.runner.activeNode.collectAsState(initial = null)

    Scaffold(
        //Тёмный фон под цвет холста: иначе при появлении и исчезании
        //нижней панели на миг просвечивает светлый фон темы — мигание
        containerColor = Color(0xFF1D1D1F),
        topBar = {
            NodesTopBar(
                name = vm.name,
                dirty = vm.dirty,
                isRunning = vm.isRunning,
                isPaused = vm.isPaused,
                errorCount = vm.errors.size,
                warningCount = vm.issues.size - vm.errors.size,
                onRun = { vm.run() },
                onPauseResume = { vm.pauseOrResume() },
                onStop = { vm.stop() },
                onShowIssues = { issuesOpen = true },
                onShowScript = { scriptOpen = true },
                onNew = { vm.newFile() },
                onOpen = { vm.openDialogOpen = true },
                onSave = { vm.save() },
                onSaveAs = { vm.openDialogSaveAs = true },
                onDeleteRename = { vm.openDialogDeleteRename = true },
                onFit = { vm.requestFit() },
            )
        },
        floatingActionButton = {
            if (vm.linkFrom == null) {
                FloatingActionButton(onClick = { pickerOpen = true }) {
                    Text("+", fontSize = 24.sp)
                }
            }
        },
        bottomBar = {
            Column {
                val registers by vm.runner.registers.collectAsState()
                RunSheet(registers = registers, console = vm.console)

                val node = vm.selected?.let { vm.graph.node(it) }
                if (node != null && vm.linkFrom == null) {
                    NodeActionBar(
                        graph = vm.graph,
                        node = node,
                        onParams = { vm.openParams(node.id) },
                        onLink = { vm.startLink(it) },
                        onUnlink = { vm.unlink(it) },
                        onDuplicate = { vm.duplicateSelected() },
                        onDelete = { vm.deleteSelected() },
                    )
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
                focusNode = vm.focusNode,
                focusKey = vm.focusRequest,
                onSelect = { id ->
                    if (vm.linkFrom != null) vm.completeLink(id) else vm.selected = id
                },
                onTapEmpty = {
                    if (vm.linkFrom != null) vm.cancelLink() else vm.selected = null
                },
                onMove = vm::moveNode,
            )

            if (vm.linkFrom != null) {
                Text(
                    "Выберите ноду, куда ведёт связь. Тап по пустому месту — отмена",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                        .background(Color(0xCC2D2D2F))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
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

    val editing = vm.paramsFor?.let { vm.graph.node(it) }
    if (editing != null && editing.body is NodeBody.Step) {
        StepDialog(
            title = editing.title,
            step = editing.body as NodeBody.Step,
            carrierNames = vm.carrierNames().sorted(),
            modNames = vm.modNames().sorted(),
            onSnapshot = { vm.snapshotFromGenerator() },
            onDone = { newTitle, newStep ->
                vm.rename(editing.id, newTitle)
                vm.replaceBody(editing.id, newStep)
                vm.closeParams()
            },
            onDismiss = { vm.closeParams() },
        )
    }

    if (editing != null && editing.body is NodeBody.Delay) {
        DelayDialog(
            body = editing.body as NodeBody.Delay,
            onDone = {
                vm.replaceBody(editing.id, it)
                vm.closeParams()
            },
            onDismiss = { vm.closeParams() },
        )
    }

    if (editing != null && editing.body is NodeBody.Register) {
        RegisterDialog(
            body = editing.body as NodeBody.Register,
            onDone = {
                vm.replaceBody(editing.id, it)
                vm.closeParams()
            },
            onDismiss = { vm.closeParams() },
        )
    }

    if (editing != null && editing.body is NodeBody.Condition) {
        ConditionDialog(
            body = editing.body as NodeBody.Condition,
            onDone = {
                vm.replaceBody(editing.id, it)
                vm.closeParams()
            },
            onDismiss = { vm.closeParams() },
        )
    }

    if (vm.openDialogOpen) {
        DialogOpenGraph(
            names = vm.graphNames(),
            onPick = {
                vm.openDialogOpen = false
                vm.openFile(it)
            },
            onDismiss = { vm.openDialogOpen = false },
        )
    }

    if (vm.openDialogSaveAs) {
        DialogSaveAs(
            onDismissRequest = { vm.openDialogSaveAs = false },
            onDone = { vm.saveAs(it) },
            onScan = { vm.graphNames() },
        )
    }

    if (vm.openDialogDeleteRename) {
        DialogDeleteRename(
            name = vm.name,
            onDone = { vm.renameFile(it) },
            onDismissRequest = { vm.openDialogDeleteRename = false },
            onClickDelete = { vm.deleteFile() },
        )
    }

    if (vm.pendingDiscard != null) {
        AlertDialog(
            onDismissRequest = { vm.cancelDiscard() },
            title = { Text("Граф не сохранён") },
            text = { Text("Правки в «${vm.name}» потеряются") },
            confirmButton = {
                TextButton(onClick = { vm.discardAndRun() }) { Text("Не сохранять") }
            },
            dismissButton = {
                TextButton(onClick = { vm.cancelDiscard() }) { Text("Отмена") }
            },
        )
    }

    if (issuesOpen) {
        IssuesSheet(
            issues = vm.issues,
            onPick = { issue ->
                issuesOpen = false
                issue.nodeId?.let { vm.focusOn(it) }
            },
            onDismiss = { issuesOpen = false },
        )
    }

    if (scriptOpen) {
        val pc by vm.runner.pc.collectAsState()
        GeneratedScriptView(
            //Показываем свежую компиляцию: граф мог измениться после прогона
            result = vm.compileNow(),
            pc = pc,
            nodeTitleOfLine = { vm.nodeTitleOfLine(it) },
            onDismiss = { scriptOpen = false },
        )
    }
}
