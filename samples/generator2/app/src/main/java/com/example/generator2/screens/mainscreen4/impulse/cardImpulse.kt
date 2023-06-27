package com.example.generator2.screens.mainscreen4.impulse

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.R
import com.example.generator2.model.LiveData
import com.example.generator2.screens.mainscreen4.atom.ButtonChEn
import com.example.generator2.screens.mainscreen4.atom.ButtonIterator
import com.example.generator2.screens.mainscreen4.atom.VolumeControl
import com.example.generator2.screens.mainscreen4.ms4SwitchWidth
import com.example.generator2.theme.colorGreen
import com.example.generator2.theme.colorOrange
import kotlinx.coroutines.flow.update


@Composable
private fun LeftColumns(str: String = "CH0") {

    val colorSelectButton = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Yellow)
    val colorUnselectButton = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Black)

    val chEN: State<Boolean> = if (str == "CH0")
        LiveData.ch1_EN.collectAsState()
    else
        LiveData.ch2_EN.collectAsState()

    //Режим 0-normal 1-50Hz
    val mode = if (str == "CH0")
        LiveData.parameterInt4.collectAsState().value
    else
        LiveData.parameterInt5.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxHeight()
        //.background(Color.Gray)
        ,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Кнопка включения канала
        ButtonChEn(str)

        Text(text = "Режим", color = Color.Yellow)

        OutlinedButton(
            onClick = {
                if (str == "CH0")
                    LiveData.parameterInt4.value = 0
                else
                    LiveData.parameterInt5.value = 0
            }, modifier = Modifier
                .padding(start = 8.dp)
                .width(ms4SwitchWidth),
            colors = if (mode == 0) colorSelectButton else colorUnselectButton,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(text = "Normal", color = if (mode == 0) Color.Black else Color.Gray)
        }

        OutlinedButton(
            onClick = {

                if (str == "CH0")
                    LiveData.parameterInt4.value = 1
                else
                    LiveData.parameterInt5.value = 1

            },
            modifier = Modifier
                .padding(start = 8.dp)
                .width(ms4SwitchWidth),
            contentPadding = PaddingValues(0.dp),
            colors = if (mode == 1) colorSelectButton else colorUnselectButton,
        ) {
            Text(text = "50Гц", color = if (mode == 1) Color.Black else Color.Gray)
        }

        val v =
            if (str == "CH0")
                LiveData.currentVolume0.collectAsState()
            else
                LiveData.currentVolume1.collectAsState()

        VolumeControl(
            value = v.value,
            onValueChange = { it1 ->

                println("onValueChange $it1")

                if (str == "CH0") {

                    LiveData.currentVolume0.update { it1 }
                    LiveData.volume0.update { it1 * LiveData.maxVolume0.value }


                } else {

                    LiveData.currentVolume1.update { it1 }
                    LiveData.volume1.update { it1 * LiveData.maxVolume1.value }
                }

            })

    }


}

@Composable
private fun RightColumnsNormal(str: String = "CH0") {

    val w =
        if (str == "CH0") LiveData.impulse0timeImp.collectAsState() else LiveData.impulse1timeImp.collectAsState()

    val p =
        if (str == "CH0") LiveData.impulse0timeImpPause.collectAsState() else LiveData.impulse1timeImpPause.collectAsState()

    /**
     * ### Первая строка
     */
    Row(
        Modifier
            .padding(top = 8.dp)
            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {


        Row(
            Modifier
                .fillMaxWidth()
                .padding(end = 8.dp), horizontalArrangement = Arrangement.End
        ) {


            Image(
                painterResource(id = R.drawable.w1),
                contentDescription = "",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))
            //Text(text = "Ширина импульса", color = Color.LightGray)

            ButtonIterator(
                text = (w.value * 20.833).toInt().toString() + " us",
                value = w.value,
                onValueChange = {
                    if (str == "CH0") LiveData.impulse0timeImp.value =
                        it else LiveData.impulse1timeImp.value = it
                },
            )
        }


    }

    /**
     * ### Вторая строка
     */
    Row(
        Modifier
            .padding(top = 8.dp)
            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            Modifier
                .fillMaxWidth()
                .padding(end = 8.dp), horizontalArrangement = Arrangement.End
        ) {

            val all = ((w.value * 20.833) * 2 + p.value * 20.833).toInt()

            Text(
                text = "All $all us",
                color = Color.LightGray,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .offset(4.dp, 0.dp),
            )


            Image(
                painterResource(id = R.drawable.p),
                contentDescription = "",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))


            ButtonIterator(
                text = (p.value * 20.833).toInt().toString() + " us",
                value = p.value,
                onValueChange = {
                    var v = it
                    if (v <= 0) v = 0
                    if (str == "CH0") LiveData.impulse0timeImpPause.value =
                        v else LiveData.impulse1timeImpPause.value = v


                },
            )
        }


    }


}


@Composable
private fun RightColumns50(str: String = "CH0") {

    Column(
        modifier = Modifier
            .fillMaxSize()
        //.background(Color.Red)
    ) {

        Text(
            text = "Частота импульсов 1..50 Гц",
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "50 Гц",
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Slider(value = 5f, onValueChange = {}, valueRange = 1f..50f)

        Text(
            text = "Время импульса 0,5..5 Сек",
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "50 Гц",
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Slider(value = 5f, onValueChange = {}, valueRange = 1f..50f)

        Button(onClick = { /*TODO*/ }) {
            Text(text = "Fire")
        }


    }

}

@Composable
fun CardImpulse(str: String = "CH0") {

    //Режим 0-normal 1-50Hz
    val mode = if (str == "CH0")
        LiveData.parameterInt4.collectAsState().value
    else
        LiveData.parameterInt5.collectAsState().value


    Column {
        Box(
            modifier = Modifier
                .background(if (str == "CH0") colorGreen else colorOrange)
                .height(8.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {}

        Row {
            LeftColumns(str)

            AnimatedContent(
                targetState = mode,
                transitionSpec = {
                    val time = 400
                    //Появление
                    (fadeIn(animationSpec = tween(time / 2)) + expandVertically(
                        animationSpec = tween(
                            time
                        )
                    ))
                        .togetherWith(
                            (fadeOut(animationSpec = tween(time)) + shrinkVertically(
                                animationSpec = tween(time)
                            ))
                        ).using(
                            SizeTransform(
                                clip = true,
                                sizeAnimationSpec = { _, _ -> tween(time) })
                        )
                }, label = ""
            )
            {

                if (it == 0)
                    RightColumnsNormal(str)
                else
                    RightColumns50(str)

            }


        }

    }

}