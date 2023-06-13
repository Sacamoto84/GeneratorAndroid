package com.example.generator2.screens.mainscreen4.impulse

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.R
import com.example.generator2.model.LiveData
import com.example.generator2.screens.mainscreen4.atom.ButtonChEn
import com.example.generator2.screens.mainscreen4.atom.ButtonIterator
import com.example.generator2.theme.colorGreen
import com.example.generator2.theme.colorOrange


@Composable
fun CardImpulse(str: String = "CH0") {

//    Image(
//        painterResource(id = R.drawable.practice),
//        contentDescription = "",
//        contentScale = ContentScale.Fit,
//    )

    val chEN: State<Boolean> = if (str == "CH0") {
        LiveData.ch1_EN.collectAsState()
    } else {
        LiveData.ch2_EN.collectAsState()
    }

    Column {

        Box(
            modifier = Modifier
                .background(if (str == "CH0") colorGreen else colorOrange)
                .height(8.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {}

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

            // Кнопка включения канала
            ButtonChEn(str)

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
                    modifier = Modifier.padding(end = 8.dp).offset(4.dp, 0.dp),
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


}