package com.example.generator2.util

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

class Tost() {

    lateinit var context: Context

    fun initialized(context: Context) {
        this.context = context
    }

    fun show(text: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, text, duration).show()
    }
}

@SuppressLint("StaticFieldLeak")
val toast = Tost()