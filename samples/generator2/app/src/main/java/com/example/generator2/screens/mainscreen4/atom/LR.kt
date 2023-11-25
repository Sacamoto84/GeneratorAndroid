package com.example.generator2.screens.mainscreen4.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.R
import com.example.generator2.generator.Generator
import com.example.generator2.theme.colorDarkBackground

private val m: Modifier = Modifier
    .fillMaxHeight()
    .aspectRatio(1.5f)
    .clip(RoundedCornerShape(15.dp))
    .border(1.dp, Color.DarkGray, CircleShape)
    .background(colorDarkBackground)
    .offset(1.dp, 0.dp)

val fontSize = 20.sp

@Composable
fun LR(gen: Generator) {
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp)),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        val mono = gen.liveData.mono.collectAsState()
        val shuffle = gen.liveData.shuffle.collectAsState()
        val enL = gen.liveData.enL.collectAsState()
        val enR = gen.liveData.enR.collectAsState()

        val colorL = if (enL.value) {
            if (!shuffle.value) Color.White else {
                if (!mono.value) Color.Red else Color.White
            }
        } else Color.DarkGray
        val colorR = if (enR.value) {
            if (!shuffle.value) Color.White else {
                if (!mono.value) Color.Red else Color.White
            }
        } else Color.DarkGray

        Text(
            text = if (!mono.value) {
                if (!shuffle.value) "L" else "R"
            } else "L",
            modifier = Modifier
                .then(m)
                .clickable { gen.liveData.enL.value = !gen.liveData.enL.value },
            fontSize = fontSize,
            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
            color = colorL, fontFamily = FontFamily(Font(R.font.jetbrains))
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = if (!mono.value) {
                if (!shuffle.value) "R" else "L"
            } else "R",
            modifier = Modifier
                .then(m)
                .clickable { gen.liveData.enR.value = !gen.liveData.enR.value },
            fontSize = fontSize,
            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
            color = colorR, fontFamily = FontFamily(Font(R.font.jetbrains))
        )
    }
}