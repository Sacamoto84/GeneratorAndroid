package com.example.generator2.screens.mainscreen4.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.R
import com.example.generator2.audio.audioOutBT
import com.example.generator2.audio.audioOutSpeaker
import com.example.generator2.audio.audioOutWired
import com.example.generator2.screens.mainscreen4.VMMain4
import com.talhafaki.composablesweettoast.util.SweetToastUtil
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * Заполняем Drawer Списка устройств
 */
@OptIn(DelicateCoroutinesApi::class)
@Composable
fun DrawerContentBottom(
    global: VMMain4
) {
    var work by remember { mutableStateOf(false) }
    var openDialogSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (openDialogSuccess) {
        openDialogSuccess = false

        SweetToastUtil.SweetSuccess(
            message = "Audio device changed",
            duration = Toast.LENGTH_SHORT,
            padding = PaddingValues(top = 0.dp),
            contentAlignment = Alignment.BottomCenter
        )
    }

    Column(
        modifier = Modifier //.fillMaxHeight(0.7f)
            .fillMaxWidth()
    ) {

        Text(
            "Audio Devices",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp),
            color = Color.White
        )

        DrawerButton(isSelect = false,
            icon = painterResource(R.drawable.speaker3),
            label = "Динамик", action = { audioOutSpeaker(context) })

        DrawerButton(isSelect = false,
            icon = painterResource(R.drawable.headphones),
            label = "Наушник", action = { audioOutWired(context) })


        DrawerButton(isSelect = false,
            icon = painterResource(R.drawable.bluetooth),
            label = "Блютус", action = { audioOutBT(context) })

        Spacer(modifier = Modifier.height(8.dp))
    }


}

