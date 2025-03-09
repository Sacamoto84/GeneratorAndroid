package com.example.generator2.screens.scripting.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.generator2.R
import com.example.generator2.screens.scripting.vm.VMScripting
import com.example.generator2.theme.colorLightBackground2
import kotlinx.coroutines.delay

private val Corner = 8.dp

@Preview
@Composable
private fun Preview() {
    DialogSaveAs(onDismissRequest = {}, onDone = {}, onScan = { listOf("ewew", "ererwe") })
}

@Composable
fun DialogSaveAs(
    onDismissRequest: () -> Unit,
    onDone: (String) -> Unit,
    onScan: () -> List<String>
) {

    //var value by remember { mutableStateOf("") }
    //var value by remember { mutableStateOf(TextFieldValue("")) }
    var value by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    val files = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        files.clear()
        files.addAll(onScan.invoke())
    }

    Dialog(onDismissRequest = onDismissRequest) {

        Card(
            Modifier
                .height(400.dp)
                .width(220.dp),
            elevation = 8.dp,
            border = BorderStroke(1.dp, Color.Gray),
            shape = RoundedCornerShape(Corner),
            backgroundColor = colorLightBackground2
        )
        {
            LaunchedEffect(Unit) {
                delay(500)
                focusRequester.requestFocus()
            }

            Column {

                Text(
                    text = "Save As",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 0.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.jetbrains)),
                    color = Color.LightGray
                )

                Box(
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {

                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        modifier = Modifier
                            .focusRequester(focusRequester),
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = Color.LightGray, leadingIconColor = Color.LightGray,
                            backgroundColor = Color.Black, focusedIndicatorColor = Color.Transparent
                        ),
                        placeholder = { Text(text = "File Name", color = Color.Gray) },
                        singleLine = true,
                        shape = RoundedCornerShape(Corner),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onDone.invoke(value.text)
                        }),
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = FontFamily(Font(R.font.jetbrains))
                        )
                    )

//                    Box(
//                        modifier = Modifier.padding(end = 4.dp)
//                            .fillMaxWidth(),
//                        contentAlignment = Alignment.CenterEnd
//                    ) {
//                        Box(modifier = Modifier
//                            .size(32.dp)
//                            , contentAlignment = Alignment.Center) {
//                            Text("+", fontSize = 28.sp, color = Color.LightGray)
//                        }
//
//                    }


                }

                Column(
                    Modifier
                        .fillMaxSize()
                        .weight(1f)
                        //.padding(4.dp)
                        .background(Color(0x8B1D1C1C))
                    //.clip(RoundedCornerShape(8.dp))
                    //.verticalScroll(rememberScrollState())
                )
                {
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn {

                        items(files) {

                            val str = it.dropLast(3)
                            Text(
                                text = str,
                                color = Color.DarkGray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .padding(start = 8.dp, top = 4.dp, end = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray)
                                    .clickable {
                                        value = TextFieldValue(
                                            text = str,
                                            selection = TextRange(str.length)
                                        )
                                    },
                                fontFamily = FontFamily(Font(R.font.jetbrains)),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                            )

                        }

                    }
                }

            }
        }
    }
}