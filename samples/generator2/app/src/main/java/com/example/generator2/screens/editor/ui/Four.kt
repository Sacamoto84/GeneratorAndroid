
//@file:kotlin.jvm.JvmName("TuplesKt")

package com.example.generator2.screens.editor.ui

import java.io.Serializable

public data class Four<out A, out B, out C, out D>(
    public val first: A,
    public val second: B,
    public val third: C,
    public val four: D

) : Serializable {

    public override fun toString(): String = "($first, $second, $third, $four )"
}

public fun <T> Four<T, T, T, T>.toList(): List<T> = listOf(first, second, third, four)