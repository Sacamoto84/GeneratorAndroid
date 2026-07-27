package com.example.generator2.screens.mainscreen4.card

import CardAM
import CardCarrier
import CardFM
import CardMaster
import CardMorph
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.generator.GeneratorCH
import com.example.generator2.theme.colorLightBackground

@Composable
fun CardCard(ch: GeneratorCH, gen: Generator) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .background(colorLightBackground))
    {
        Column {
            CardCarrier(ch, gen = gen)
            CardMorph(ch, gen = gen)
            CardAM(ch, gen = gen)
            CardFM(ch, gen = gen)
            CardMaster(ch, gen = gen)
        }
    }
}






