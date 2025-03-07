package com.example.generator2.features.script

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

class ScriptList {

    val list = mutableStateListOf<MutableState<String>>()

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

    fun toList() = list.map { it.value }.toList()
    fun removeAt(index: Int): MutableState<String> = list.removeAt(index)
    fun size() = list.size

    fun lastIndex() = list.lastIndex

    fun swap( i : Int, j : Int){
        val tmp = list[i].value
        list[i].value = list[j].value
        list[j].value = tmp
    }

}
