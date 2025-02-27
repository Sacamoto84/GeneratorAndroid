package com.example.generator2.screens.scripting

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.generator2.screens.scripting.vm.VMScripting


//Основной экран для скриптов
@Composable
fun ScreenScriptCommon(global: VMScripting = hiltViewModel()) {
    Column( //Modifier
        //  .recomposeHighlighter()
        //.background(Color.Cyan)
    ) {

//        Box(Modifier.weight(1f)) {
//            ScriptTable(vm = global)
//        }
//
//        //Блок регистров
//        if (global.script.state != StateCommandScript.ISEDITTING) {
//            Spacer(modifier = Modifier.height(8.dp))
//
//            RegisterViewDraw(global = global)
//        }
//        Spacer(modifier = Modifier.height(8.dp))
//
//        BottomAppBar(
//            backgroundColor = colorLightBackground,
//            contentColor = Color.White,
//        ) {
//
//
//            //Кнопка назад
//            IconButton(modifier = Modifier.testTag("buttonM4ScriptGoBack"),
//                onClick = { navController.popBackStack() }) {
//                Icon(painter = painterResource(R.drawable.back4), contentDescription = null)
//            }
//
//            Spacer(modifier = Modifier.weight(1f))
//
//            if ((global.script.state == StateCommandScript.ISRUNNING) || (global.script.state == StateCommandScript.ISPAUSE)) {
//
//                //Пауза
//                IconButton(onClick = {
//                    if (global.script.state != StateCommandScript.ISPAUSE) global.script.command(
//                        StateCommandScript.PAUSE
//                    )
//                    else {
//                        global.script.state = StateCommandScript.ISRUNNING
//                        global.script.end = false
//                    }
//                }) {
//                    if (global.script.state != StateCommandScript.ISPAUSE)
//                        Icon(
//                            painter = painterResource(
//                                R.drawable.pause
//                            ), contentDescription = null
//                        )
//                    else
//                        Icon(
//                            painter = painterResource(
//                                R.drawable.play
//                            ), contentDescription = null
//                        )
//                }
//            } else {
//                //Старт
//                IconButton(onClick = {
//                    global.script.command(StateCommandScript.START)
//                }) {
//                    Icon(painter = painterResource(R.drawable.play), contentDescription = null)
//                }
//            }
//
//            Spacer(modifier = Modifier.weight(0.1f))
//
//            //Стоп
//            IconButton(onClick = {
//                global.script.command(StateCommandScript.STOP)
//            }) {
//                Icon(painter = painterResource(R.drawable.stop), contentDescription = null)
//            }
//
//            Spacer(modifier = Modifier.weight(1f))
//            IconButton(onClick = { navController.navigate("scriptinfo") }) {
//                Icon(painter = painterResource(R.drawable.info4), contentDescription = null)
//            }
//
//        }
//
//    }

    }
}








