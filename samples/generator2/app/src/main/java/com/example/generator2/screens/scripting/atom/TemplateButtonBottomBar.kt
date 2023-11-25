package com.example.generator2.screens.scripting.atom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.R
import com.example.generator2.theme.colorDarkBackground

@Composable
fun TemplateButtonBottomBar(
    modifier: Modifier = Modifier,
    str: String = "?",
    onClick: () -> Unit = {},
    backgroundColor: Color = colorDarkBackground,
    contentColor: Color = Color.White,
) {
    OutlinedButton(
        onClick = onClick, modifier = Modifier
            .fillMaxWidth()
            .then(modifier) //.weight(1f)
            .padding(start = 8.dp, end = 4.dp), colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = backgroundColor, contentColor = contentColor
        ), border = BorderStroke(1.dp, Color.LightGray), contentPadding = PaddingValues(2.dp)
    ) {
        Text(str, maxLines = 2, fontFamily = FontFamily(Font(R.font.jetbrains)), fontSize = 14.sp)
    }
}

@Composable
fun OutlinedButtonTextAndIcon(
    modifier: Modifier = Modifier,
    str: String = "?",
    onClick: () -> Unit = {},
    backgroundColor: Color = colorDarkBackground,
    contentColor: Color = Color.White,
    resId: Int, //Ресурс для анимации
    paddingStart: Dp = 4.dp,
    paddingEnd: Dp = 4.dp,
    paddingStartText: Dp = 8.dp,
    paddingStartIcon: Dp = 0.dp
) {

    OutlinedButton(
        onClick = {
            onClick()

        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(start = paddingStart, end = paddingEnd)
            .then(modifier),
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = backgroundColor, contentColor = contentColor
        ),
        border = BorderStroke(1.dp, Color.LightGray),
        contentPadding = PaddingValues(2.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {

            Icon(
                painter = painterResource(resId),
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.padding(start = paddingStartIcon)
            )

            Text(
                str,
                maxLines = 2,
                fontFamily = FontFamily(Font(R.font.jetbrains)),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = paddingStartText)
            )


        }
    }
}