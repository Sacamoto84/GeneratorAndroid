package com.example.generator2.screens.mainscreen4.card

import CardAM
import CardCarrier
import CardFM
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.generator2.theme.colorLightBackground

@Composable
fun CardCard(str: String = "CH0") {

    Card(
        backgroundColor = colorLightBackground,
        modifier = Modifier
            .height(258.dp)
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp)
    )
    {

        Column {
            CardCarrier(str)
            Spacer(modifier = Modifier.height(8.dp))
            CardAM(str)
            CardFM(str)
        }

    }

}






