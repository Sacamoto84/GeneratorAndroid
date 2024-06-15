package com.example.generator2.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class MeasureMicroAvg {

    var sum = 0L
    var count = 0

    val avgUs : Float
        get() = sum/count.toFloat()

    //fun add(time : Long) { sum += time }
    fun clear() { sum = 0L; count = 0 }

    @OptIn(ExperimentalContracts::class)
    inline fun measureNanoTime(block: () -> Unit) : Long{
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        val start = System.nanoTime()
        block()
        val t = (System.nanoTime() - start)/1000
        sum += t
        count++
        if (count > 10000) clear()
        return t
    }

}