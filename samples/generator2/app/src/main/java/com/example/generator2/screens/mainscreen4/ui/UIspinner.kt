package com.example.generator2.screens.mainscreen4.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.example.generator2.generator.gen
import com.example.generator2.model.itemList

object UIspinner {

    @SuppressLint("ModifierParameter", "StateFlowValueCalledInComposition")
    @Composable
    fun Spinner(
        CH: String,
        Mod: String,
        transparent: Boolean = false,
        modifier: Modifier = Modifier,
        filename: State<String>
    ) {

        val expanded = remember { mutableStateOf(false) }

        //Выбор с каким списком работать
        var itemlist: ArrayList<itemList> = gen.itemlistCarrier
        when (Mod) {
            "CR" -> itemlist = gen.itemlistCarrier
            "AM" -> itemlist = gen.itemlistAM
            "FM" -> itemlist = gen.itemlistFM
        }

        //Текущий текст
        var currentValue = "---"

        if (CH == "CH0") {
            when (Mod) {
                "CR" -> currentValue = gen.liveData.ch1_Carrier_Filename.value
                "AM" -> currentValue = gen.liveData.ch1_AM_Filename.value
                "FM" -> currentValue = gen.liveData.ch1_FM_Filename.value
            }
        } else {
            when (Mod) {
                "CR" -> currentValue = gen.liveData.ch2_Carrier_Filename.value
                "AM" -> currentValue = gen.liveData.ch2_AM_Filename.value
                "FM" -> currentValue = gen.liveData.ch2_FM_Filename.value
            }
        }

        //var currentValue = filename.value

        //Индекс текущего битмапа
        val indexBitmapCurrent = remember { mutableStateOf(0) }
        itemlist.forEachIndexed { index, element ->
            if (element.name == currentValue) indexBitmapCurrent.value = index
        }

        if (itemlist.isEmpty()) return

        Box(
            modifier = Modifier
                //.fillMaxSize()
                .background(Color.Transparent)
                .then(modifier), contentAlignment = Alignment.Center
        )
        {
            Row(modifier = Modifier
                .clickable {
                    expanded.value = !expanded.value
                }
                .background(color = if (transparent) Color(0x00000000) else Color(0xFF13161B)),
                //horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
                //,
                //.align(Alignment.Center)
            )
            {

                itemlist[indexBitmapCurrent.value].bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .width(104.dp)
                            .height(48.dp)
                            .padding(start = 4.dp, end = 4.dp) //128 64
                    )
                }






                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                    modifier = Modifier.background(
                        color = if (transparent) Color(0) else Color(
                            0xFF454954
                        )
                    ),
                    properties = PopupProperties()

                )
                {

                    itemlist.forEach {
                        DropdownMenuItem(
                            modifier = Modifier.padding(2.dp)
                                .background(Color(0xFF454954))
                                .width(340.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            ,
                            onClick = {
                                currentValue = it.name
                                expanded.value = false
                                if (CH == "CH0") {
                                    when (Mod) {
                                        "CR" -> gen.liveData.ch1_Carrier_Filename.value = currentValue
                                        "AM" -> gen.liveData.ch1_AM_Filename.value = currentValue
                                        "FM" -> gen.liveData.ch1_FM_Filename.value = currentValue
                                    }
                                } else {
                                    when (Mod) {
                                        "CR" -> gen.liveData.ch2_Carrier_Filename.value = currentValue
                                        "AM" -> gen.liveData.ch2_AM_Filename.value = currentValue
                                        "FM" -> gen.liveData.ch2_FM_Filename.value = currentValue

                                    }
                                }

                            }
                        ) {
                            it.bitmap?.let { it1 ->
                                Image(
                                    bitmap = it1.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(top = 4.dp, bottom = 4.dp, end = 4.dp)
                                        .height(64.dp)
                                )
                            }
                            Text(
                                text = it.name,
                                color = Color(0xFFE7E1D5),
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        //}
    }

}