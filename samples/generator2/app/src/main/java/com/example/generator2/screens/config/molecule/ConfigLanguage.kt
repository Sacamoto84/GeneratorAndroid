package com.example.generator2.screens.config.molecule

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.MainRes
import com.example.generator2.screens.config.DefScreenConfig
import com.example.generator2.screens.config.vm.VMConfig
import com.example.generator2.strings.MainResStrings


@Composable
fun ConfigLanguage(vm: VMConfig) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = MainResStrings.languageSelection, color = Color.LightGray,
            maxLines = 3,
            minLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            style = DefScreenConfig.textStyle
        )
        Demo_ExposedDropdownMenuBox(vm)
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Demo_ExposedDropdownMenuBox(vm: VMConfig) {
    val context = LocalContext.current
    val coffeeDrinks = arrayOf("Русский", "English")
    var expanded by remember { mutableStateOf(false) }

    val leng = vm.readLanguage()



    var selectedText by remember {
        if (leng == "ru") {
            mutableStateOf(coffeeDrinks[0])
        }
        else
            mutableStateOf(coffeeDrinks[1])
    }

    Box(
        modifier = Modifier
            .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
            .width(DefScreenConfig.widthEdit)
            .height(DefScreenConfig.heightEdit)
    ) {

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {

            Box(contentAlignment = Alignment.Center) {

                BasicText(

                    text = selectedText,


                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(
                            color = Color(0xFF353838),
                            shape = DefScreenConfig.shapeEdit
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF696B6B),
                            shape = DefScreenConfig.shapeEdit
                        )
                        .menuAnchor()
                    ,

                    style =

                    TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W600,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        //background = Color.Gray,
                        baselineShift = BaselineShift(-0.1f)
                    )

                    ,

                    )


                Box(
                    contentAlignment = Alignment.CenterEnd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        null,
                        Modifier.rotate(if (expanded) 180f else 0f), tint = Color.White
                    )
                }


            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                coffeeDrinks.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            selectedText = item
                            expanded = false
                            Toast.makeText(context, item, Toast.LENGTH_SHORT).show()

                            vm.saveLanguage(item)
                            vm.recompose()
                        }
                    )
                }
            }
        }
    }
}