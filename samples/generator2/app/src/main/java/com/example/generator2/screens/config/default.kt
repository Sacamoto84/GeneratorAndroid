package com.example.generator2.screens.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.generator2.R
import com.siddroid.holi.colors.MaterialColor

object DefScreenConfig {

    var backgroundColorGreenButton = MaterialColor.GREEN_500

    //val backgroundColorGreenButton = Color(0xFF4CAF50) //MaterialColor.GREEN_500

    val disabledBackgroundColorGreenButton = Color(0xFF262726)
    val textSizeGreenButton = 16.sp

    //Стиль для строчек с информацийе
    val caption: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.4.sp,
        fontFamily = FontFamily(Font(R.font.jetbrains))
    )
}



