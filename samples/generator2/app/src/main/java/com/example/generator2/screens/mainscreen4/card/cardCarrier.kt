import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.generator2.model.LiveConstrain
import com.example.generator2.model.LiveData
import com.example.generator2.screens.mainscreen4.modifierInfinitySlider
import com.example.generator2.screens.mainscreen4.ms4SwitchWidth
import com.example.generator2.screens.mainscreen4.textStyleButtonOnOff
import com.example.generator2.screens.mainscreen4.textStyleEditFontFamily
import com.example.generator2.screens.mainscreen4.textStyleEditFontSize
import com.example.generator2.screens.mainscreen4.ui.InfinitySlider
import com.example.generator2.screens.mainscreen4.ui.MainScreenTextBoxGuest
import com.example.generator2.screens.mainscreen4.ui.UIspinner
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.theme.colorGreen
import com.example.generator2.theme.colorLightBackground2
import com.example.generator2.theme.colorOrange
import kotlinx.coroutines.flow.update
import libs.modifier.noRippleClickable
import timber.log.Timber


@Composable
fun CardCarrier(str: String = "CH0") {

    val chEN: State<Boolean> = if (str == "CH0") {
        LiveData.ch1_EN.collectAsState()
    } else {
        LiveData.ch2_EN.collectAsState()
    }

    val carrierFr: State<Float> = if (str == "CH0") {
        LiveData.ch1_Carrier_Fr.collectAsState()
    } else {
        LiveData.ch2_Carrier_Fr.collectAsState()
    }

    Column {

        Box(
            modifier = Modifier
                .background(if (str == "CH0") colorGreen else colorOrange)
                .height(8.dp)
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {}


        Row(
            Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically
        ) {

            // Кнопка включения канала
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(48.dp)
                    .width(ms4SwitchWidth)
                    .border(
                        2.dp,
                        color = if (chEN.value) Color(0xFF1B5E20) else Color.DarkGray,
                        RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        color = if (chEN.value) Color(0xFF4DD0E1) else colorDarkBackground
                    )
                    .noRippleClickable(onClick = {
                        if (str == "CH0") LiveData.ch1_EN.value =
                            !LiveData.ch1_EN.value
                        else LiveData.ch2_EN.value = !LiveData.ch2_EN.value
                        println("Кнопка")
                    }
                    ), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (chEN.value) "On" else "Off",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (chEN.value) colorDarkBackground else Color.LightGray,
                    style = textStyleButtonOnOff
                )
            }


//////////////////////////////////////////////////////////////////////////////////////////////////////
            var expanded by remember { mutableStateOf(false) }
            var selectedIndex by remember { mutableIntStateOf(0) }

            Box(
                Modifier
                    .padding(start = 0.dp)
                    .height(48.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .noRippleClickable { expanded = true }
            )
            {

                MainScreenTextBoxGuest(
                    String.format("%d", carrierFr.value.toInt()),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(48.dp)
                        .fillMaxSize(),

                    value = carrierFr.value,
                    sensing = LiveConstrain.sensetingSliderCr.value * 2,
                    range = LiveConstrain.rangeSliderCr,
                    onValueChange = { it1 ->

                        Timber.e(it1.toString())

                        if (str == "CH0")
                            LiveData.ch1_Carrier_Fr.update { it1 }
                        else
                            LiveData.ch2_Carrier_Fr.update { it1 }
                    },
                    fontSize = textStyleEditFontSize,
                    fontFamily = textStyleEditFontFamily
                    )


                val items = listOf(
                    "600",
                    "800",
                    "1000",
                    "1500",
                    "2000",
                    "2500",
                    "3000",
                    "3500",
                    "4000"
                )

                DropdownMenu(
                    offset = DpOffset(12.dp, 4.dp),
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        //.width(80.dp)
                        .background( colorLightBackground2 )
                        .border( 1.dp, color = Color.DarkGray, shape = RoundedCornerShape(16.dp) )
                ) {

                    items.forEachIndexed { index, s ->
                        DropdownMenuItem(onClick = {
                            selectedIndex = index
                            expanded = false

                            if (str == "CH0") {
                                LiveData.ch1_Carrier_Fr.value = s.toFloat()
                            } else {
                                LiveData.ch2_Carrier_Fr.value = s.toFloat()
                            }

                        })
                        {
                            Text(text = s, color = Color.White)
                        }
                    }
                }


            }


            InfinitySlider(
                value = carrierFr.value,
                sensing = LiveConstrain.sensetingSliderCr.value / 4,
                range = LiveConstrain.rangeSliderCr,
                onValueChange = {
                    if (str == "CH0") LiveData.ch1_Carrier_Fr.value =
                        it else LiveData.ch2_Carrier_Fr.value = it
                },
                modifier = modifierInfinitySlider,
                vertical = true,
                invert = true,
                visibleText = false
            )




            UIspinner.Spinner(
                CH = str,
                Mod = "CR",
                modifier = Modifier
                    .padding(top = 0.dp, start = 8.dp, end = 8.dp)
                    .wrapContentWidth()
                    .clip(shape = RoundedCornerShape(4.dp)),
                filename = if (str == "CH0") LiveData.ch1_Carrier_Filename.collectAsState()
                else LiveData.ch2_Carrier_Filename.collectAsState()
            )

        }

        //            Slider(
        //                valueRange = rangeSliderCr,
        //                value = carrierFr.value!!,
        //                onValueChange = {
        //                    if (str == "CH0") Global.ch1_Carrier_Fr.value =
        //                        it else Global.ch2_Carrier_Fr.value = it
        //                },
        //                modifier = Modifier
        //                    .fillMaxWidth()
        //                    .padding(start = 8.dp, end = 8.dp),
        //                colors = SliderDefaults.colors(thumbColor = Color.LightGray),
        //                steps = stepSliderCr
        //            )

        Spacer(modifier = Modifier.height(8.dp))

        CardAM(str)
        CardFM(str)

    }


}