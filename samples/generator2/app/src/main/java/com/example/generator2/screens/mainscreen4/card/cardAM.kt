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
import com.example.generator2.theme.colorLightBackground2
import libs.modifier.noRippleClickable


@Composable
fun CardAM(str: String = "CH0") {

    val amEN: State<Boolean?> = if (str == "CH0") {
        LiveData.ch1_AM_EN.collectAsState()
    } else {
        LiveData.ch2_AM_EN.collectAsState()
    }

    Column {

        Box(
            modifier = Modifier
                .background(Color.DarkGray) //colorGreen else colorOrange)
                .height(1.dp)
                .fillMaxWidth()
        )

        Row(
            Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically
        ) {

            val amFr: State<Float?> = if (str == "CH0") {
                LiveData.ch1_AM_Fr.collectAsState()
            } else {
                LiveData.ch2_AM_Fr.collectAsState()
            }


            // Кнопка включения AM
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(48.dp)
                    .width(ms4SwitchWidth)
                    .border(
                        2.dp,
                        color = if (amEN.value!!) Color(0xFF1B5E20) else Color.DarkGray,
                        RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        color = if (amEN.value!!) Color(0xFF01AE0F) else colorDarkBackground
                    )
                    .noRippleClickable(onClick = {
                        if (str == "CH0") LiveData.ch1_AM_EN.value =
                            !LiveData.ch1_AM_EN.value
                        else LiveData.ch2_AM_EN.value = !LiveData.ch2_AM_EN.value
                    }) //.shadow(1.dp, shape = RoundedCornerShape(8.dp), ambientColor = Color.Blue)
                , contentAlignment = Alignment.Center
            ) {


                Text(
                    text = "AM",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (amEN.value!!) colorDarkBackground else Color.LightGray,
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
                    .noRippleClickable { expanded = true }) {

                //Частота модуляции
                MainScreenTextBoxGuest(
                    String.format("%.1f", amFr.value),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(48.dp)
                        .fillMaxSize(),
                    value = amFr.value!!,
                    sensing = if (amFr.value!! < 10.0F) LiveConstrain.sensetingSliderAmFm.value else LiveConstrain.sensetingSliderAmFm.value * 10f,
                    range = LiveConstrain.rangeSliderAmFm,
                    onValueChange = {
                        if (str == "CH0") LiveData.ch1_AM_Fr.value =
                            it else LiveData.ch2_AM_Fr.value = it
                    },
                    fontSize = textStyleEditFontSize,
                    fontFamily = textStyleEditFontFamily
                )

                val items = listOf("0.1", "1.0", "5.5", "10.0", "40.0", "100.0")

                DropdownMenu(
                    offset = DpOffset(8.dp, 4.dp),
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        //.width(80.dp)
                        .background(
                            colorLightBackground2
                        )
                        .border(1.dp, color = Color.DarkGray, shape = RoundedCornerShape(16.dp))
                ) {

                    items.forEachIndexed { index, s ->
                        DropdownMenuItem(onClick = {
                            selectedIndex = index
                            expanded = false

                            if (str == "CH0") {
                                LiveData.ch1_AM_Fr.value = s.toFloat()
                            } else {
                                LiveData.ch2_AM_Fr.value = s.toFloat()
                            }

                        })
                        {
                            Text(text = s, color = Color.White)
                        }
                    }
                }
            }
////////////////////////////////////////////////////////////////////////////////////////////////////

            val amDepth: State<Float?> = if (str == "CH0") {
                LiveData.ch1AmDepth.collectAsState()
            } else {
                LiveData.ch2AmDepth.collectAsState()
            }

            InfinitySlider(
                value = amDepth.value,
                sensing = 0.001f,
                range = 0f..1f,
                onValueChange = {
                    if (str == "CH0") LiveData.ch1AmDepth.value =
                        it else LiveData.ch2AmDepth.value = it
                },
                modifier = modifierInfinitySlider,
                vertical = true,
                invert = true,
                visibleText = false,
                text = (amDepth.value?.times(100F))?.toInt().toString()
            )
////////////////////////////////////////////////////////////////////////////////////////////////////

            UIspinner.Spinner(
                str,
                "AM",
                modifier = Modifier
                    .padding(top = 0.dp, start = 8.dp, end = 8.dp)
                    .wrapContentWidth()
                    .clip(shape = RoundedCornerShape(4.dp)),
                filename = if (str == "CH0") LiveData.ch1_AM_Filename.collectAsState()
                else LiveData.ch1_AM_Filename.collectAsState()

            )

        }

        Spacer(modifier = Modifier.height(8.dp))

    } //}
}
