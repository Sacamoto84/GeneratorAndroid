package com.example.generator2.screens.scripting.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.generator2.R
import com.example.generator2.strings.MainResStrings
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.theme.colorLightBackground

private val Corner = 8.dp


@Preview
@Composable
private fun PreviewDialogDeleteRename() {
    DialogDeleteRename(
        name = "test",
        onDone = {},
        onDismissRequest = {},
        onClickDelete = {}
    )
}

@Composable
fun DialogDeleteRename(
    name: String,
    onDone: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onClickDelete: () -> Unit
) {

    println("DialogDeleteRename name:$name")

    var value by remember { mutableStateOf(name) }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {

        Card(
            Modifier.width(220.dp), elevation = 8.dp, border = BorderStroke(1.dp, Color.Gray),
            shape = RoundedCornerShape(Corner), backgroundColor = colorDarkBackground
        ) {

            Column {
                
                Text(
                    text = MainResStrings.screenScriptDialogRenameRename,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.jetbrains)),
                    color = Color.LightGray
                )

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier
                        .height(58.dp)
                        .padding(start = 16.dp, end = 16.dp, bottom = 0.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.LightGray,
                        leadingIconColor = Color.LightGray,
                        backgroundColor = colorLightBackground,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    placeholder = {
                        Text(
                            text = MainResStrings.screenScriptDialogRenameFileName,
                            color = Color.Gray
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(Corner),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onDone(value) }
                    ),
                    textStyle = TextStyle(
                        fontSize = 18.sp, fontFamily = FontFamily(Font(R.font.jetbrains)),
                    ),
                )

                Text(
                    text = MainResStrings.screenScriptDialogRenameOr,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 4.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.jetbrains)),
                    color = Color.LightGray
                )

                Button(
                    onClick = onClickDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(Corner),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text(
                        MainResStrings.screenScriptDialogRenameDelete,
                        fontSize = 28.sp,
                        color = Color.White,
                        modifier = Modifier.offset(0.dp, (0).dp)
                    )
                }

            }
        }
    }
}
