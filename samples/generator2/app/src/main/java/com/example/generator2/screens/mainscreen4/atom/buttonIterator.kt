package com.example.generator2.screens.mainscreen4.atom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.R
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.util.format


private val buttonH = 32.dp
private val buttonW = 42.dp


@Composable
fun ButtonIterator(
    text: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    val v = remember {
        mutableIntStateOf(value)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {


        OutlinedButton(
            contentPadding = PaddingValues(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
            shape = RoundedCornerShape(
                bottomEnd = 0.dp,
                bottomStart = 8.dp,
                topEnd = 0.dp,
                topStart = 8.dp
            ),
            border = BorderStroke(1.dp, Color.LightGray),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorDarkBackground),


            modifier = Modifier
                .height(buttonH)
                .width(buttonW)
                .offset(1.dp, 0.dp),


            onClick = {
                v.intValue = v.intValue - 1
                if (v.intValue <= 0) v.intValue = 0
                onValueChange(v.intValue)
            }) {


            Icon(
                painter = painterResource(R.drawable.minus_small),
                contentDescription = null, tint = Color.LightGray
            )


        }

        Text(
            text = text, //v.floatValue.format(0),
            modifier = Modifier
                .width(80.dp)
                .height(buttonH)
                .background(
                    colorDarkBackground
                )
                .border(1.dp, Color.LightGray)
                .offset(0.dp, 3.dp),
            textAlign = TextAlign.Center, fontSize = 18.sp

        )

        OutlinedButton(
            contentPadding = PaddingValues(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
            shape = RoundedCornerShape(
                bottomEnd = 8.dp,
                bottomStart = 0.dp,
                topEnd = 8.dp,
                topStart = 0.dp
            ),
            border = BorderStroke(1.dp, Color.LightGray),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorDarkBackground),

            modifier = Modifier
                .height(buttonH)
                .width(buttonW)
                .offset((-1).dp, 0.dp),

            onClick = {
                v.intValue = v.intValue + 1
                onValueChange(v.intValue)
            })
        {

            Icon(
                painter = painterResource(R.drawable.plus_small),
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(32.dp)
            )

        }

    }
}
