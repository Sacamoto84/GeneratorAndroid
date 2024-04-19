package com.example.generator2.screens.scripting.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import com.example.generator2.screens.ConsoleLogDraw
import com.example.generator2.screens.scripting.vm.VMScripting
import com.example.generator2.screens.scripting.atom.OutlinedButtonTextAndIcon
import com.example.generator2.screens.scripting.dialog.DialogDeleteRename
import com.example.generator2.screens.scripting.dialog.DialogSaveAs
import com.example.generator2.features.script.StateCommandScript

val refresh = mutableStateOf(0)

private val files: MutableList<String> = mutableListOf()

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScriptTable(vm: VMScripting) {

    var filename by remember { mutableStateOf("") }  //Имя выбранного файла в списке

    Box(modifier = Modifier.fillMaxSize(1f)) {
        Column() {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
            {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f), contentAlignment = Alignment.BottomEnd
                ) {
                    if (vm.script.pc_ex > vm.script.list.lastIndex) vm.script.pc_ex =
                        vm.script.list.lastIndex

                    ScriptConsole(vm.script.list, vm.script.pc_ex, global = vm)

                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(160.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.TopCenter
                )
                {

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(Color.DarkGray),
                        verticalArrangement = Arrangement.SpaceEvenly
                    )
                    {

                        if (vm.script.state != StateCommandScript.ISEDITTING) {

                            Spacer(modifier = Modifier.height(8.dp))
                            //Кнопка New
                            OutlinedButtonTextAndIcon(
                                str = "New",
                                onClick = { vm.bNewClick() },
                                resId = R.drawable.page2,
                                paddingStartText = 10.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButtonTextAndIcon(
                                str = "Edit",
                                onClick = { vm.bEditClick() },
                                resId = R.drawable.edit,
                                paddingStartIcon = 3.dp,
                                paddingStartText = 9.dp
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            // Создать список названий файлов из папки /Script
                            if (vm.script.state == StateCommandScript.ISTOPPING) {
                                println("Читаем файлы")
                                files.clear()
                                files.addAll(
                                    vm.utils.filesInDirToList("/Script").map { it.dropLast(3) }) //
                            }

                            //Отображение списка названия скриптов
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                                    .padding(4.dp)
                                    .background(Color(0x8B1D1C1C))
                                    .border(1.dp, Color.DarkGray)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Spacer(modifier = Modifier.height(4.dp))
                                for (index in files.indices) {
                                    Text(
                                        text = files[index],
                                        color = Color.DarkGray,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                            .padding(start = 8.dp, top = 4.dp, end = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.LightGray)
                                            .combinedClickable(
                                                onClick = {

                                                    vm.script.command(StateCommandScript.STOP)
                                                    val l =
                                                        vm.utils.readScriptFileToList(files[index])
                                                    vm.script.list.clear()
                                                    vm.script.list.addAll(l)

                                                }, onLongClick = {

                                                    vm.openDialogDeleteRename.value = true
                                                    filename = files[index]

                                                })
                                            .offset(0.dp, (0).dp),
                                        fontFamily = FontFamily(Font(R.font.jetbrains)),
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 18.sp
                                    )
                                }
                            } //

                            //Текущее состояние
                            Text(
                                text = vm.script.stateToString(),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = Color.LightGray
                            )
                            //Консоль Логов
                            ConsoleLogDraw(Modifier.weight(0.4f))
                        }



                        if (vm.script.state == StateCommandScript.ISEDITTING) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButtonTextAndIcon(str = "Back", onClick = {
                                vm.script.command(
                                    StateCommandScript.STOP
                                )
                            }, resId = R.drawable.left)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButtonTextAndIcon(
                                str = "Save",
                                onClick = { vm.bSaveClick() },
                                resId = R.drawable.save
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButtonTextAndIcon(
                                str = "Save As",
                                onClick = { vm.openDialogSaveAs.value = true },
                                resId = R.drawable.save_as
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButtonTextAndIcon(
                                str = "Add",
                                onClick = { vm.bAddClick() },
                                resId = R.drawable.add
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButtonTextAndIcon(
                                str = "Add END",
                                onClick = { vm.bAddEndClick() },
                                resId = R.drawable.end,
                                paddingStartIcon = 4.dp,
                                paddingStartText = 12.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButtonTextAndIcon(
                                str = "Delete",
                                onClick = { vm.bDeleteClick() },
                                resId = R.drawable.delete)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButtonTextAndIcon(
                                str = "Up",
                                onClick = { vm.bUpClick() },
                                resId = R.drawable.up
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButtonTextAndIcon(
                                str = "Down",
                                onClick = { vm.bDownClick() },
                                resId = R.drawable.down
                            )
                            Spacer(modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f))
                        }


                    }
                }
            }

            if (vm.openDialogSaveAs.value) DialogSaveAs(vm)
            if (vm.openDialogDeleteRename.value) DialogDeleteRename(filename, vm)

            if (vm.script.state == StateCommandScript.ISEDITTING) {
                vm.keyboard.Core { vm.script.pc_ex }
            }
        }
    }
}
