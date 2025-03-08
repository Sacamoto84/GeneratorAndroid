package com.example.generator2.screens.scripting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.generator2.screens.scripting.atom.ScriptItem
import com.example.generator2.screens.scripting.vm.VMScripting

@Composable
fun ScriptConsole(
    l: SnapshotStateList<String>,
    selectLine: Int,
    modifier: Modifier = Modifier,
    global: VMScripting,
) {

    println("ScriptConsole selectLine:$selectLine")
//
    var indexSelect by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectLine) {
        indexSelect = selectLine // Или передавать нужное значение
    }

//
   val lastIndex = l.lastIndex
//
    if (indexSelect > lastIndex) {
        indexSelect = maxOf(0, lastIndex)
    }

//    if (indexSelect == 0) {
//        indexSelect = 1
//        global.script.pc_ex.update { 1 }
//    }

    val lazyListState: LazyListState = rememberLazyListState()
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF283593))
            .then(modifier), contentAlignment = Alignment.CenterStart
    )
    {

        LazyColumn(
            modifier = Modifier.fillMaxSize(), state = lazyListState
        ) {
            itemsIndexed(l)
            { index, item ->
                val a = global.script.update.collectAsStateWithLifecycle().value
                a
                Row(horizontalArrangement = Arrangement.Start)
                {
                    Box(
                        modifier = Modifier.selectable(
                            selected = indexSelect == index,
                            onClick = {
                                global.script.pc.value = index
                            })
                    ) {
                        val select = indexSelect == index
                        ScriptItem().Draw(str = { item }, index = { index }, { select })
                    }
                }
            }

        }


    }

}

