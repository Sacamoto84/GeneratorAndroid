package com.example.generator2.screens.mainscreen4.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.generator2.model.LiveConstrain
import com.example.generator2.screens.mainscreen4.textStyleEditFontFamily
import com.example.generator2.screens.mainscreen4.textStyleEditFontSize
import com.example.generator2.theme.colorLightBackground2
import libs.modifier.noRippleClickable
import timber.log.Timber

@Composable
fun MainscreenTextBoxAndDropdownMenu(
        str: String,
        modifier: Modifier = Modifier,
        enable: Boolean = true,
        items: List<String>,
        value: Float,
        onChange: (Float) -> Unit,
        sensing: Float = LiveConstrain.sensetingSliderCr.floatValue * 2,
        range: ClosedFloatingPointRange<Float> = LiveConstrain.rangeSliderCr
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(0) }

    Box(
            Modifier
                    .padding(start = 0.dp)
                    .height(48.dp)
                    .fillMaxWidth()
                    //.weight(1f)
                    .then(modifier)
                    .noRippleClickable {
                        if (enable)
                            expanded = true
                    }
    )
    {

        MainScreenTextBoxGuest(
                str = str,
                modifier = Modifier
                        .padding(start = 8.dp)
                        .height(48.dp)
                        .fillMaxSize(),

                value = value,
                sensing = sensing,
                range = range,
                onValueChange = { it1 ->

                    Timber.e(it1.toString())

                    if (enable)
                        onChange(it1)


                },
                fontSize = textStyleEditFontSize,
                fontFamily = textStyleEditFontFamily,
                color = if (enable) Color.LightGray else Color.DarkGray
        )

        DropdownMenu(
                offset = DpOffset(12.dp, 4.dp),
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                        //.width(80.dp)
                        .background(colorLightBackground2)
                        .border(1.dp, color = Color.DarkGray, shape = RoundedCornerShape(16.dp))
        ) {

            items.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    selectedIndex = index
                    expanded = false


                    if (enable)
                        onChange(s.toFloat())


                })
                {
                    Text(text = s, color = Color.White)
                }
            }
        }


    }
}