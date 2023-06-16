package com.example.generator2.presets.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.generator2.AppPath
import com.example.generator2.R
import com.example.generator2.presets.Presets
import com.example.generator2.presets.presetsGetListName
import com.example.generator2.screens.scripting.ui.refresh
import com.example.generator2.screens.scripting.vm.VMScripting
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.theme.colorLightBackground
import com.example.generator2.util.toast
import java.io.File

private val Corner = 8.dp

@Composable
fun DialogPresetsDeleteRename(name: String) {

    val context = LocalContext.current

    println("DialogDeleteRename name:$name")

    var value by remember { mutableStateOf("") }

    value = name

    //var valueDelete by remember { mutableStateOf("") }
    Dialog(onDismissRequest = { Presets.isOpenDialogDeleteRename.value = false }) {
        Card(
            Modifier.width(220.dp), elevation = 8.dp, border = BorderStroke(
                1.dp, Color.Gray
            ), shape = RoundedCornerShape(Corner), backgroundColor = colorDarkBackground
        ) {

            Column {

                Text(
                    text = "Rename",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 0.dp)
                    //.clip(RoundedCornerShape(Corner)).background(Color.DarkGray)
                    ,
                    textAlign = TextAlign.Center, fontSize = 16.sp, fontFamily = FontFamily(Font(R.font.jetbrains)), color = Color.LightGray
                )

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.height(58.dp).padding(start = 16.dp, end = 16.dp, bottom = 0.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.LightGray, leadingIconColor = Color.LightGray,
                        backgroundColor = colorLightBackground, focusedIndicatorColor = Color.Transparent ),
                    placeholder = { Text(text = "File Name", color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(Corner),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {

                        val oldFile = File(AppPath().presets+"/${name}.txt")
                        val newFile = File(AppPath().presets+"/${value}.txt")

                        if (oldFile.renameTo(newFile)) {
                            println("Файл успешно переименован.")
                        } else {
                            println("Не удалось переименовать файл.")
                        }

                        Presets.presetList.clear()
                        Presets.presetList = presetsGetListName()

                        PresetsDialogRecompose.intValue++

                        Presets.isOpenDialogDeleteRename.value = false

                        toast.show("Renamed")

                    }),
                    textStyle = TextStyle(
                        fontSize = 18.sp, fontFamily = FontFamily(Font(R.font.jetbrains)),
                    ),
                )

                Text(
                    text = "or",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 0.dp)
                    //.clip(RoundedCornerShape(Corner)).background(Color.DarkGray)
                    ,
                    textAlign = TextAlign.Center, fontSize = 16.sp, fontFamily = FontFamily(Font(R.font.jetbrains)), color = Color.LightGray
                )

                Button(
                    onClick = {

                        val pathDocuments = AppPath().presets+"/${name}.txt"
                        File(pathDocuments).delete()

                        Presets.presetList.clear()
                        Presets.presetList = presetsGetListName()

                        PresetsDialogRecompose.intValue++

                        Presets.isOpenDialogDeleteRename.value = false

                    },
                    modifier = Modifier.fillMaxWidth().height(72.dp)
                        .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(Corner),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text(
                        "Delete",
                        fontSize = 28.sp,
                        color = Color.White,
                        modifier = Modifier.offset(0.dp, (0).dp)
                    )
                }

                //Divider(color = Color.Gray, thickness = 2.dp)

                //Spacer(modifier = Modifier.height(16.dp))




            }


        }


    }

}
