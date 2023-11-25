package com.example.generator2.screens.mainscreen4

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.R


/**
 * Ширина переключателей
 */
val ms4SwitchWidth = 72.dp


val modifierInfinitySlider =
    Modifier
        .padding(start = 8.dp)
        .border(2.dp, Color.DarkGray, RoundedCornerShape(8.dp))
        .clip(RoundedCornerShape(8.dp))
        .background(Color(0xFF141414))
        .height(32.dp)
        .width(36.dp)

/**
 * Стиль для кнопок включения модуляций
 */
val textStyleButtonOnOff = TextStyle(
    fontSize = 24.sp,
    fontWeight = FontWeight.Bold,
    fontFamily = FontFamily(Font(R.font.bebas_neue_cyrillic)),
    letterSpacing = 2.sp
)


/**
 * Размер шрифта для установки частот
 */
val textStyleEditFontSize = 24.sp

/**
 * Семейство шрифта для установки частот
 */
val textStyleEditFontFamily = FontFamily(Font(R.font.nunito))