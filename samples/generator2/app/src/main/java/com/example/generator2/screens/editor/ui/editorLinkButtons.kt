package com.example.generator2.screens.editor.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EditorLinkButtons(modifier: Modifier = Modifier) {

    Row(
        Modifier
            .height(28.dp)
            .fillMaxWidth()
            .then(modifier)
        //.clip(RoundedCornerShape(8.dp))
        //.border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
        , horizontalArrangement = Arrangement.SpaceAround
    ) {

        repeat(6)
        {
            OutlinedButton(onClick = { /*TODO*/ }) {

            }

        }


    }

}