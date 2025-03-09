package com.example.generator2.screens.scripting.ui

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.generator2.R
import com.example.generator2.screens.scripting.vm.VMScripting
import com.example.generator2.screens.scripting.atom.OutlinedButtonTextAndIcon
import com.example.generator2.screens.scripting.dialog.DialogDeleteRename
import com.example.generator2.screens.scripting.dialog.DialogSaveAs
import com.example.generator2.features.script.StateCommandScript
import com.example.generator2.theme.colorGreen
import timber.log.Timber
import java.io.FileNotFoundException

private val files: MutableList<String> = mutableListOf()

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScriptTable(vm: VMScripting) {

    Box(modifier = Modifier.fillMaxSize(1f)) {

        Column {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
            {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {

                    Text(vm.script.name, color = colorGreen)
                    Text(
                        "PC:" + vm.script.pc.collectAsStateWithLifecycle().value.toString(),
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f), contentAlignment = Alignment.BottomEnd
                    ) {

//                        if (vm.script.pc_ex.collectAsStateWithLifecycle().value > vm.script.list.lastIndex)
//                            vm.script.pc_ex.1update { vm.script.list.lastIndex }

                        val pc = vm.script.pc.collectAsStateWithLifecycle().value
                        vm.script.update.collectAsStateWithLifecycle().value
                        ScriptConsole(
                            l = vm.script.list.instance(),
                            selectLine = pc,
                            global = vm
                        )

                    }

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

                        if (vm.script.state != StateCommandScript.ISEDITING) {

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
                                    vm.utils.filesInScriptToList().map { it.dropLast(3) }
                                )
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

                                                    try {
                                                        vm.script.command(StateCommandScript.STOP)
                                                        val l =
                                                            vm.utils.readScriptFileToList(files[index])
                                                        vm.script.list.clear()
                                                        l.forEach {
                                                            vm.script.list.add(it)
                                                        }
                                                        vm.script.name = files[index]
                                                    } catch (e1: FileNotFoundException) {
                                                        Timber.e(e1.localizedMessage)
                                                    } catch (e: Exception) {
                                                        Timber.e(e.localizedMessage)
                                                    }
                                                }, onLongClick = {

                                                    vm.openDialogDeleteRename.value = true
                                                    vm.script.name = files[index]

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

                        if (vm.script.state == StateCommandScript.ISEDITING) {

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
                                resId = R.drawable.delete
                            )
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
                            Spacer(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                            )
                        }


                    }
                }
            }

            //Блок диалога сохранить как
            if (vm.openDialogSaveAs.value)
                DialogSaveAs(
                    onDismissRequest = { vm.openDialogSaveAs.value = false },
                    onDone = { vm.bDialogSaveAsDone(it) }, onScan = {
                        val a = vm.utils.filesInScriptToList()
                        a
                    }
                )

            val context = LocalContext.current

            //Блок диалога переименования
            if (vm.openDialogDeleteRename.value)
                DialogDeleteRename(
                    vm.script.name,
                    onDone = { itRenameDoneValue ->
                        vm.dialogRenameNewValue(itRenameDoneValue)
                        Toast.makeText(context, "Renamed", Toast.LENGTH_LONG).show()
                    }, onDismissRequest = {
                        vm.openDialogDeleteRename.value = false
                    }, onClickDelete = {
                        vm.dialogRenameClickDelete()
                    })


            if (vm.script.state == StateCommandScript.ISEDITING) {
                vm.keyboard.Core { vm.script.pc.value }
            }

        }
    }
}
