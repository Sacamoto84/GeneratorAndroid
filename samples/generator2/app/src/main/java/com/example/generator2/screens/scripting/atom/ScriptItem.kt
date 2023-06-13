package com.example.generator2.screens.scripting.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.R

class ScriptItem {

    @Composable
    fun Draw(str: () -> String, index: () -> Int, select: () -> Boolean) {

        println("Draw  ${index()}")

        val x = convertStringToAnnotatedString({ str() }, { index() })
        Text(x,
            modifier = Modifier.fillMaxWidth().padding(top = 0.dp)
                .background(if (select()) Color.Cyan else Color.Transparent),
            fontSize = 20.sp
        )

    }

    private fun convertStringToAnnotatedString(str: () -> String, index: () -> Int): AnnotatedString {
        //lateinit var x : AnnotatedString
        var x = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Blue, background = Color.White)) {
                append("${index()}")
            }
        }

        //Разобрать строку на список команд
        val listCMD = str().split(" ")
        if (listCMD.isEmpty()) {
            println("convertStringToPairTextAndColor: Error listCMD == 0")
            return x
        }

        var color: Color = Color.White
        var background: Color = Color.Black

        /////////////////////
        when (listCMD[0]) {

            "CH1", "CH2" -> {
                color = Color(0xFFFFDF30)
                background = Color(0xFF012F50)
            }

            "CR1", "CR2" -> {
                color = Color(0xFF00FFFF)
            }

            "AM1", "AM2" -> {
                color = Color.Green
            }

            "FM1", "FM2" -> {
                color = Color(0xFFFF7A21)
            }

            "LOAD" -> {
                color = Color(0xFFB2EBF2)
            }

            "GOTO" -> {
                color = Color.White
                background = Color(0xFF0D8A71)
            }

            "DELAY" -> {
                color = Color(0xFFF5FFFF)
                background = Color(0xFF10467C)
            }

            "END" -> {
                color = Color(0xFF000000)
                background = Color(0xFFFFEB3B)
            }

            "ENDIF", "IF", "ELSE" -> {
                color = Color(0xFFFFFFFF)
                background = Color(0xFF318792)
            }

            else -> {
                color = Color.LightGray
            }


        } /////////////////////////////////////

        x += buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = color,
                    background = background,
                    fontFamily = FontFamily(Font(R.font.jetbrains))
                )
            )
            { append(" " + listCMD[0] + " ") }
        }

        if (listCMD.size >= 2) {
            x += buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = color,
                        background = background,
                        fontFamily = FontFamily(Font(R.font.jetbrains))
                    )
                ) { append(listCMD[1] + " ") }
            }
        } /////////////////////////////////////////////////////

        if (listCMD.size >= 3) {
            x += buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = color,
                        background = background,
                        fontFamily = FontFamily(Font(R.font.jetbrains))
                    )
                ) { append(listCMD[2] + " ") }
            }
        }


        ///////////////////////////////
        if (listCMD.size >= 4) {

            x += buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = color,
                        background = background,
                        fontFamily = FontFamily(Font(R.font.jetbrains))
                    )
                ) { append(listCMD[3] + " ") }
            }
        }
        ////////////////////////////


        return x
    }

}
