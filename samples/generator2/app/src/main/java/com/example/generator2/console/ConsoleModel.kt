package com.example.generator2.console

import androidx.compose.ui.graphics.Color

data class PairTextAndColor(
    var text: String,
    var colorText: Color,
    var colorBg: Color,
    var bold: Boolean = false,
    var italic: Boolean = false,
    var underline: Boolean = false,
    var flash: Boolean = false
)

data class LineTextAndColor(
    var text: String, //Строка вообще
    var pairList: List<PairTextAndColor> //То что будет определено в этой строке
)