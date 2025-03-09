package com.example.generator2.screens.scripting.bottom

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.generator2.R
import com.example.generator2.features.script.StateCommandScript
import com.example.generator2.screens.scripting.vm.VMScripting
import com.example.generator2.theme.colorLightBackground

/**
 * Компонент нижней панели экрана скрипта
 */
@Composable
fun BottomAppBarScript(vm: VMScripting){

    val navigator = LocalNavigator.currentOrThrow

    BottomAppBar(
        backgroundColor = colorLightBackground,
        contentColor = Color.White,
    ) {

        //Кнопка назад
        IconButton(modifier = Modifier.testTag("buttonM4ScriptGoBack"),
            onClick = {navigator.pop()}) {
            Icon(painter = painterResource(R.drawable.back4), contentDescription = null)
        }

        Spacer(modifier = Modifier.weight(1f))

        if ((vm.script.state == StateCommandScript.ISRUNNING) || (vm.script.state == StateCommandScript.ISPAUSE)) {

            //Пауза
            IconButton(onClick = {

                if (vm.script.state != StateCommandScript.ISPAUSE) vm.script.command(
                    StateCommandScript.PAUSE
                )
                else {
                    vm.script.state = StateCommandScript.ISRUNNING
                    vm.script.end = false
                }

            }) {
                if (vm.script.state != StateCommandScript.ISPAUSE)
                    Icon(
                        painter = painterResource(
                            R.drawable.pause
                        ), contentDescription = null
                    )
                else
                    Icon(
                        painter = painterResource(
                            R.drawable.play
                        ), contentDescription = null
                    )
            }
        } else {
            //Старт
            IconButton(onClick = {
                vm.script.command(StateCommandScript.START)
            }) {
                Icon(painter = painterResource(R.drawable.play), contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.weight(0.1f))

        //Стоп
        IconButton(onClick = { vm.script.command(StateCommandScript.STOP) }) {
            Icon(painter = painterResource(R.drawable.stop), contentDescription = null)
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = {
            //navController.navigate("scriptinfo")
        }) {
            Icon(painter = painterResource(R.drawable.info4), contentDescription = null)
        }

    }

}