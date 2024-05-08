package com.example.generator2.features.explorer.presenter.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.features.explorer.model.ExplorerItem

@Composable
fun ScreenExplorerDrawCount(item: ExplorerItem) {

    if ((item.node.value.isDirectory) && (item.counterItems > 0)) {
        Text(
            text = item.counterItems.toString(),
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.clip(RoundedCornerShape(50f)).background(Color.Red).padding(8.dp)
        )
    }
}