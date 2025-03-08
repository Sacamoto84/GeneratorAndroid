package com.example.generator2.features.script

import androidx.compose.runtime.mutableStateListOf

// ✅ Работает
// ❌ UI не перерисуется! 👍⚠️⚡️🔰🔴⛔️⚙️☁️✨🚫📖🔸❓🌌◍◉▶∎∷⋮⋯⋰⋱✏✔☑☐⌨⓿❶❷❸❹░▒▓▌▧▧▧▧▦▦▦◇½

class ScriptList {

    private val list = mutableStateListOf<String>()

    fun instance() = list

    fun update(index: Int, value: String) {
        if ((index < 0) || (index > list.lastIndex))
            return
        list[index] = value
    }

    fun add(index: Int, value: String) {
        list.add(index, value)
    }

    fun add(value: String) {
        list.add(value)
    }

    /**
     * Очистка буфера
     */
    fun clear() {
        list.clear()
    }

    fun get(index: Int): String {
        return list[index]
    }

    fun toList() = list.map { it }.toList()

    /**
     * ⚡️Удалить елемент по индексу
     */
    fun removeAt(index: Int) = list.removeAt(index)

    fun size() = list.size

    fun lastIndex() = list.lastIndex

    fun swap(i: Int, j: Int) {
        val tmp = list[i]
        list[i] = list[j]
        list[j] = tmp
    }

}
