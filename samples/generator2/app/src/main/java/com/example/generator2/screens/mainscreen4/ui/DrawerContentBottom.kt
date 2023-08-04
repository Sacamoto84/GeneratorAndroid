package com.example.generator2.screens.mainscreen4.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.screens.mainscreen4.VMMain4
import com.talhafaki.composablesweettoast.util.SweetToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Заполняем Drawer Списка устройств
 */
@Composable
fun DrawerContentBottom(
    global: VMMain4
) {
    var work by remember { mutableStateOf(false) }
    var openDialogSuccess by remember { mutableStateOf(false) }

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
            modifier = Modifier.padding(8.dp)
        )

        //Получить список устройств
        global.hub.audioDevice.mDeviceAdapter.forEachIndexed { index, pair ->
            val label = pair.id.toString() + " " + pair.name.toString()
            val imageVector = nameToPainter(pair.name.toString())
            DrawerButton(isSelect = pair.id == global.hub.audioDevice.mDeviceId,
                icon = imageVector,
                label = label,
                action = {
                    if (!work) {
                        GlobalScope.launch(Dispatchers.Main) {
                            val numDeferred1 = async{
                                work = true
                                //global.hub.audioDevice.playbackEngine.stop()
                                //global.hub.audioDevice.playbackEngine.delete()
                                //global.hub.audioDevice.playbackEngine.create()
                                global.hub.audioDevice.OnItemSelectedListener(index)
                                //global.hub.audioDevice.playbackEngine.start()
                                //global.audioDevice.getDeviceId()
                                //delay(2000)
                                //global.sendAlltoGen()
                                work = false
                                //Toast.makeText(mContext, "Audio device changed",Toast.LENGTH_SHORT).show()
                                openDialogSuccess = true
                                global.hub.audioDevice.getDeviceId()}
                            numDeferred1.await()
                        }
                    }
                })
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
