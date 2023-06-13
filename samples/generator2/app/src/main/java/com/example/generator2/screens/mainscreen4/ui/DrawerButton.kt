package com.example.generator2.screens.mainscreen4.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DrawerButton(
    modifier: Modifier = Modifier,
    isSelect: Boolean = false,
    icon: Painter,
    label: String,
    action: () -> Unit,
) {
    val surfaceModifier =
        Modifier.then(modifier).padding(start = 8.dp, top = 8.dp, end = 8.dp).fillMaxWidth()

    Surface(
        modifier = surfaceModifier,
        color = if (isSelect) Color.Gray else Color.DarkGray,
        shape = MaterialTheme.shapes.small
    ) {
        TextButton(
            onClick = action, modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    fontWeight = FontWeight.Bold,
                    text = label,
                    style = MaterialTheme.typography.body2,
                    color = Color.White
                )
            }
        }
    }
}