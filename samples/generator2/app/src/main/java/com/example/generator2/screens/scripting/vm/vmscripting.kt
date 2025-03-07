package com.example.generator2.screens.scripting.vm

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.generator2.features.script.Script
import com.example.generator2.features.script.StateCommandScript
import com.example.generator2.screens.scripting.ui.ScriptKeyboard
import com.example.generator2.util.UtilsKT
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject

@Stable
@SuppressLint("StaticFieldLeak")
@HiltViewModel
class VMScripting @Inject constructor(
    @ApplicationContext val contextActivity: Context,
    val script: Script,
    val utils: UtilsKT,
    val keyboard: ScriptKeyboard
    //val keyboard: ScriptKeyboard
) : ViewModel() {

    val openDialogSaveAs = mutableStateOf(false)
    val openDialogDeleteRename = mutableStateOf(false)

    fun bNewClick() {
        script.command(StateCommandScript.STOP)
        script.list.clear()
        script.list.add("---")
        script.list.add("?")
        script.list.add("END")
        script.command(StateCommandScript.EDIT)
    }

    fun bEditClick() {
       script.command(StateCommandScript.EDIT)
    }

    fun bSaveClick() {
        if (script.name == "New")
            openDialogSaveAs.value = true
        else
            utils.saveListToScriptFile(script.list.toList(), script.name)
    }

    /**
     * Добавить в конец списка END
     */
    fun bAddEndClick() {
        script.list.add(script.pc.value + 1, "END")
        script.update.value++
    }

    fun bDeleteClick() {
        if (script.list.size() > 0) {
            script.list.removeAt( script.pc.value )
            if ( script.pc.value > script.list.lastIndex() ) {
                script.pc.value = script.list.lastIndex()
            }
        }
    }

    fun bUpClick() {

        if (script.pc.value > 0) {
            script.list.swap(script.pc.value - 1, script.pc.value)
            script.pc.value--
        }

    }

    fun bDownClick() {
        if ((script.pc.value >= 0) && ( script.pc.value < script.list.lastIndex())
        ) {
            script.list.swap(script.pc.value+1, script.pc.value)
            script.pc.value++
        }
    }

    fun bAddClick() {
        script.list.add(script.pc.value, "?")
    }

    /**
     * Сохранить текущий скрипт в файл
     */
    fun saveListToScript(name: String) {
        println("global saveListToScript()")

        utils.saveListToScriptFile(
            script.list.toList(), name
        )
    }

    //DialogSaveAs
    fun bDialogSaveAsDone(value: String) {
        script.name = value
        saveListToScript(value)
        openDialogSaveAs.value = false
        Toast.makeText(contextActivity, "Saved", Toast.LENGTH_LONG).show()
    }


}