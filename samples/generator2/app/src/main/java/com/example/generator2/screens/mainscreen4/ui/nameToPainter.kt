package com.example.generator2.screens.mainscreen4.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.generator2.R

@Composable
fun nameToPainter(str: String): Painter {
    var imageVector: Painter = painterResource(R.drawable.info)
    if (str.indexOf("telephony") != -1) imageVector = painterResource(R.drawable.telephone_200)
    if (str.indexOf("earphone") != -1) imageVector = painterResource(R.drawable.telephone3)
    if (str.indexOf("built-in speaker") != -1) imageVector = painterResource(R.drawable.speaker3)
    if (str.indexOf("headphones") != -1) imageVector = painterResource(R.drawable.headphones)
    if (str.indexOf("Bluetooth") != -1) imageVector = painterResource(R.drawable.headset2)
    if (str.indexOf("A2DP") != -1) imageVector = painterResource(R.drawable.bluetooth3)
    if (str.indexOf("Auto select") != -1) imageVector = painterResource(R.drawable.auto2)
    return imageVector
}