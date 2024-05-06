package com.example.generator2.features.explorer.presenter.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.R
import com.example.generator2.features.explorer.model.ExplorerItem
import com.example.generator2.features.explorer.presenter.NODE_UP

@Composable
fun ScreenExplorerDrawItemIcon(item: ExplorerItem) {

    if (item.isDirectory) {

        if (item.name != NODE_UP) {
            Icon(
                painter = painterResource(R.drawable.folder_open2),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(32.dp),
                tint = Color.Yellow
            )
        }
    }
    else {

        Box(
            modifier = Modifier
            //.size(48.dp)
            //.background(Color(0xFF8BB7F0))
            ,
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = item.isFormat,
                modifier = Modifier.padding(start = 4.dp)
                //.border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                ,
                fontFamily = FontFamily(Font(R.font.bayon_regular)),
                fontSize = 20.sp, textAlign = TextAlign.Center, color = Color.White
            )

        }

    }

}