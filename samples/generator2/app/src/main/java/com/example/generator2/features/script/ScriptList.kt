package com.example.generator2.features.script

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.util.concurrent.CopyOnWriteArrayList

class ScriptList {

    private val list = CopyOnWriteArrayList<MutableState<String>>()

    fun add( index: Int, value : String ) {
       list.add(index, mutableStateOf(value))
    }

    fun add( value : String ) {
        list.add(mutableStateOf(value))
    }

    /**
     * Очистка буфера
     */
    fun clear() {
        list.clear()
    }

    fun get(index: Int): String {
        return list[index].value
    }

    fun toList() = list.toList()
}