package com.example.generator2.screens.config

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.generator2.R

@Composable
fun Config_Green_button_refresh(
    modifier: Modifier, onClick: () -> Unit
) {
    Button(
        modifier = modifier, content = {

            Icon(
                tint = Color.White,
                painter = painterResource(id = R.drawable.refresh),
                contentDescription = null,
            )

        }, onClick = onClick, colors = ButtonDefaults.buttonColors(
            backgroundColor = DefScreenConfig.backgroundColorGreenButton,
            disabledBackgroundColor = DefScreenConfig.disabledBackgroundColorGreenButton
        )
    )
}

@Composable
fun ConfigGreenButton(
    modifier: Modifier, onClick: () -> Unit, label: String = ""
) {

    Button(
        modifier = modifier, content = {
            Text(
                text = label,
                color = Color.White,
                fontSize = DefScreenConfig.textSizeGreenButton
            )
        }, onClick = onClick, colors = ButtonDefaults.buttonColors(
            backgroundColor = DefScreenConfig.backgroundColorGreenButton,
            disabledBackgroundColor = DefScreenConfig.disabledBackgroundColorGreenButton
        )
    )

}
