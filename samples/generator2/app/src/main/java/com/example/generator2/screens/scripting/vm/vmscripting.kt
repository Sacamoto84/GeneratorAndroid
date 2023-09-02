package com.example.generator2.screens.scripting.vm

import android.annotation.SuppressLint
import android.content.Context
import android.inputmethodservice.Keyboard
import android.widget.Toast
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.generator2.element.Script
import com.example.generator2.element.StateCommandScript
import com.example.generator2.screens.scripting.ui.ScriptKeyboard
import com.example.generator2.util.UtilsKT
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject

@Stable
@SuppressLint("StaticFieldLeak")
@HiltViewModel
class VMScripting @Inject constructor(
    @ApplicationContext val contextActivity: Context,
    val script: Script,
    val utils : UtilsKT,
    val keyboard : ScriptKeyboard
) : ViewModel() {

    val openDialogSaveAs = mutableStateOf(false)
    val openDialogDeleteRename = mutableStateOf(false)

    fun bNewClick() {
        script.command(StateCommandScript.STOP)
        script.list.clear()
        script.list.add("New")
        script.list.add("?")
        script.list.add("END")
        script.command(StateCommandScript.EDIT)
    }

    fun bEditClick() {
        script.command(StateCommandScript.EDIT)
    }

    fun bSaveClick() {
        if (script.list[0] == "New")
            openDialogSaveAs.value = true
        else
            utils.saveListToScriptFile(script.list, script.list[0])

    }

    fun bAddEndClick() {
        script.list.add(
            script.pc + 1, "END"
        )
        script.pc_ex = script.pc
    }

    fun bDeleteClick() {
        if (script.list.size > 1) {


            script.list.removeAt(
                script.pc
            )

            if (
                script.pc >
                script.list.lastIndex
            ) {

                script.pc =
                    script.list.lastIndex
            }


            script.pc_ex =
                script.pc
        }
    }

    fun bUpClick() {
        if (
            script.pc > 1) {
            Collections.swap(

                script.list,

                script.pc - 1,

                script.pc
            )

            script.pc--
        }


        script.pc_ex =
            script.pc
    }

    fun bDownClick() {
        if ((
                    script.pc > 0) && (
                    script.pc <
                            script.list.lastIndex)
        ) {
            Collections.swap(

                script.list,

                script.pc + 1,

                script.pc
            )

            script.pc++
        }

        script.pc_ex =
            script.pc
    }

    fun bAddClick() {

        script.list.add(
            script.pc + 1, "?"
        )

        script.pc_ex =
            script.pc
    }

    /**
     * Сохранить текущий скрипт в файл
     */
    fun saveListToScript(name: String) {
        println("global saveListToScript()")

        utils.saveListToScriptFile(
            script.list, name
        )
    }

    //DialogSaveAs
    fun bDialogSaveAsDone(value: String) {

        script.list[0] = value
        saveListToScript(value)
        openDialogSaveAs.value = false
        Toast.makeText(contextActivity, "Saved", Toast.LENGTH_LONG).show()
    }


}