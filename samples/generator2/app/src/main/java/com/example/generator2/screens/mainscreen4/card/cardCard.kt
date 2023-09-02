package com.example.generator2.screens.mainscreen4.card

import CardAM
import CardCarrier
import CardFM
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.generator2.generator.Generator
import com.example.generator2.theme.colorLightBackground

@Composable
fun CardCard(str: String = "CH0", gen: Generator) {
    Box(modifier = Modifier.fillMaxWidth().background(colorLightBackground))
    {
        Column {
            CardCarrier(str, gen = gen)
            CardAM(str, gen = gen)
            CardFM(str, gen = gen)
        }
    }
}






