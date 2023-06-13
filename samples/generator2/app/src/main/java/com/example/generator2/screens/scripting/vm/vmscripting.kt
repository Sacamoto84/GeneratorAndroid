package com.example.generator2.screens.scripting.vm

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.generator2.di.Hub
import com.example.generator2.vm.StateCommandScript
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject

@Stable
@SuppressLint("StaticFieldLeak")
@HiltViewModel
class VMScripting @Inject constructor(
     @ApplicationContext val contextActivity: Context,
     val hub: Hub
) : ViewModel() {

    val openDialogSaveAs       = mutableStateOf(false)
    val openDialogDeleteRename = mutableStateOf(false)

    fun bNewClick() {
        hub.script.command(StateCommandScript.STOP)
        hub.script.list.clear()
        hub.script.list.add("New")
        hub.script.list.add("?")
        hub.script.list.add("END")
        hub.script.command(StateCommandScript.EDIT)
    }

    fun bEditClick() {
        hub.script.command(StateCommandScript.EDIT)
    }

    fun bSaveClick() {
        if (hub.script.list[0] == "New")
            openDialogSaveAs.value = true
        else
            hub.utils.saveListToScriptFile(hub.script.list, hub.script.list[0])

    }

    fun bAddEndClick() {
        hub.script.list.add(hub.script.pc + 1, "END")
        hub.script.pc_ex = hub.script.pc
    }

    fun bDeleteClick() {
        if (hub.script.list.size > 1) {

            hub.script.list.removeAt(hub.script.pc)

            if (hub.script.pc > hub.script.list.lastIndex) {
                hub.script.pc = hub.script.list.lastIndex
            }

            hub.script.pc_ex = hub.script.pc
        }
    }

    fun bUpClick() {
        if (hub.script.pc > 1) {
            Collections.swap(
                hub.script.list,
                hub.script.pc - 1,
                hub.script.pc
            )
            hub.script.pc--
        }

        hub.script.pc_ex = hub.script.pc
    }

    fun bDownClick() {
        if ((hub.script.pc > 0) && (hub.script.pc < hub.script.list.lastIndex)) {
            Collections.swap(
                hub.script.list,
                hub.script.pc + 1,
                hub.script.pc
            )
            hub.script.pc++
        }
        hub.script.pc_ex = hub.script.pc
    }

    fun bAddClick() {
        hub.script.list.add(hub.script.pc + 1, "?")
        hub.script.pc_ex = hub.script.pc
    }

    /**
     * Сохранить текущий скрипт в файл
     */
    fun saveListToScript(name: String) {
        println("global saveListToScript()")
        hub.utils.saveListToScriptFile( hub.script.list, name)
    }

    //DialogSaveAs
    fun bDialogSaveAsDone( value : String ){
        hub.script.list[0] = value
        saveListToScript(value)
        openDialogSaveAs.value = false
        Toast.makeText(contextActivity, "Saved", Toast.LENGTH_LONG).show()
    }




}