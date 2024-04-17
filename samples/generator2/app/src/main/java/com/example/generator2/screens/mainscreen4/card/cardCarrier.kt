import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.generator2.generator.Generator
import com.example.generator2.model.LiveConstrain
import com.example.generator2.screens.mainscreen4.modifierInfinitySlider
import com.example.generator2.screens.mainscreen4.ms4SwitchWidth
import com.example.generator2.screens.mainscreen4.textStyleButtonOnOff
import com.example.generator2.screens.mainscreen4.ui.InfinitySlider
import com.example.generator2.screens.mainscreen4.ui.MainscreenTextBoxAndDropdownMenu
import com.example.generator2.screens.mainscreen4.ui.UIspinner
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.theme.colorGreen
import com.example.generator2.theme.colorOrange
import com.example.generator2.modifier.noRippleClickable


@Composable
fun CardCarrier(str: String = "CH0", gen: Generator) {

    val chEN: State<Boolean> =
        if (str == "CH0") gen.liveData.ch1_EN.collectAsState() else gen.liveData.ch2_EN.collectAsState()

    val carrierFr: State<Float> =
        if (str == "CH0") gen.liveData.ch1_Carrier_Fr.collectAsState() else gen.liveData.ch2_Carrier_Fr.collectAsState()

    val fmSelectMode: State<Int?> = if (str == "CH0")
        gen.liveData.parameterInt0.collectAsState() //CH1 режим выбора частот FM модуляции 0-обычный 1-минимум макс
    else
        gen.liveData.parameterInt1.collectAsState() //CH2 режим выбора частот FM модуляции 0-обычный 1-минимум макс

    Column {

        Box(
            modifier = Modifier
                .background(if (str == "CH0") colorGreen else colorOrange)
                .height(8.dp)
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {}


        Row(
            Modifier.padding(top = 0.dp), verticalAlignment = Alignment.CenterVertically
        ) {

            // Кнопка включения канала
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(32.dp)
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
                        if (str == "CH0") gen.liveData.ch1_EN.value = !gen.liveData.ch1_EN.value
                        else gen.liveData.ch2_EN.value = !gen.liveData.ch2_EN.value
                        println("Кнопка")
                    }), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (chEN.value) "On" else "Off",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (chEN.value) colorDarkBackground else Color.LightGray,
                    style = textStyleButtonOnOff
                )
            }



            MainscreenTextBoxAndDropdownMenu(
                str = String.format("%d", carrierFr.value.toInt()),
                modifier = Modifier.weight(1f),
                enable = fmSelectMode.value == 0,
                items = listOf(
                    "100", "600", "800", "1000", "1500", "2000", "2500", "3000", "3500", "4000"
                ),
                value = carrierFr.value,
                onChange = {

                    if (fmSelectMode.value == 0)
                        if (str == "CH0") gen.liveData.ch1_Carrier_Fr.value =
                            it else gen.liveData.ch2_Carrier_Fr.value = it
                },
                range = 50f..10000f
            )



            InfinitySlider(
                value = carrierFr.value,
                sensing = LiveConstrain.sensetingSliderCr.floatValue / 4,
                range = 50f..10000f,
                onValueChange = {
                    if (fmSelectMode.value == 0) if (str == "CH0") gen.liveData.ch1_Carrier_Fr.value =
                        it else gen.liveData.ch2_Carrier_Fr.value = it
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
                    .padding(start = 8.dp, end = 8.dp)
                    .wrapContentWidth()
                    .clip(shape = RoundedCornerShape(4.dp)),
                filename = if (str == "CH0") gen.liveData.ch1_Carrier_Filename.collectAsState()
                else gen.liveData.ch2_Carrier_Filename.collectAsState(), gen = gen
            )

        }

    }


}
