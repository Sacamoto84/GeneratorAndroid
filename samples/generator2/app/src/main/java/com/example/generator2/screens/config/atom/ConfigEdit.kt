package com.example.generator2.screens.config.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//@Composable
//public fun TextFieldDefaults.colorsEdit(
//    textColor: Color = Color.White,
//    focusedBorderColor: Color = Color.LightGray,
//    focusedLabelColor: Color = Color.White,
//): TextFieldColors =
//    outlinedTextFieldColors(
//        textColor = textColor,
//        focusedBorderColor = focusedBorderColor,
//        focusedLabelColor = focusedLabelColor
//    )

@Composable
fun editConfig(
    modifier: Modifier = Modifier,
    label: String,
    value: State<Float>,
    onDone: (Float) -> Unit = {},
    min: Float,
    max: Float,
    toInt: Boolean = false

) {

    var text by remember {
        mutableStateOf(
            TextFieldValue(
                if (toInt) value.value.toInt().toString() else value.value.toString()
            )
        )
    }
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    BasicTextField(
        value = text,
        onValueChange = {
            text = it
        },

        modifier = Modifier
            .padding(8.dp)
            .then(modifier)
            .focusRequester(focusRequester)
            .background(color = Color(0xFF353838), shape = RoundedCornerShape(size = 16.dp))
            .border(
                width = 1.dp,
                color = Color(0xFF696B6B),
                shape = RoundedCornerShape(size = 16.dp)
            )

        ,

        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Decimal
        ),

        keyboardActions = KeyboardActions(
            onDone = {

                if (text.text == "")
                    text = TextFieldValue(text = min.toString())

                var s = text.text.replace("-", "").replace(",", "").trim()
                if (s.isEmpty()) s = min.toString()

                try {
                    var f = s.toFloat()
                    if (f > max) f = max
                    if (f < min) f = min
                    text = TextFieldValue(text = if (toInt) f.toInt().toString() else f.toString())
                    onDone(f)
                } catch (e: Exception) {
                    onDone(min)
                }
                localFocusManager.clearFocus()
            }
        ),


        textStyle = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        ),


        )


}



