package com.example.generator2.screens.config.atom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.screens.config.DefScreenConfig

@Composable
fun ConfigLineText(text: String = "", text2: String = "") {

    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = text,
            color = DefScreenConfig.textColor,
            maxLines = 3,
            minLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            style = DefScreenConfig.textStyle
        )

        Text(
            text2,
            Modifier
                .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
                .width(DefScreenConfig.widthEdit)
                .height(DefScreenConfig.heightEdit)
            , style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.W600,
                color = Color.White,
                textAlign = TextAlign.Center,
                baselineShift = BaselineShift(-0.1f)
            )
        )

    }

}