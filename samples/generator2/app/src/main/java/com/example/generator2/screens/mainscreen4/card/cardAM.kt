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
import com.example.generator2.gen
import com.example.generator2.model.LiveConstrain
import com.example.generator2.screens.mainscreen4.modifierInfinitySlider
import com.example.generator2.screens.mainscreen4.ms4SwitchWidth
import com.example.generator2.screens.mainscreen4.textStyleButtonOnOff
import com.example.generator2.screens.mainscreen4.ui.InfinitySlider
import com.example.generator2.screens.mainscreen4.ui.MainscreenTextBoxAndDropdownMenu
import com.example.generator2.screens.mainscreen4.ui.UIspinner
import com.example.generator2.theme.colorDarkBackground
import libs.modifier.noRippleClickable


@Composable
fun CardAM(str: String = "CH0") {

    val amEN: State<Boolean?> = if (str == "CH0") {
        gen.liveData.ch1_AM_EN.collectAsState()
    } else {
        gen.liveData.ch2_AM_EN.collectAsState()
    }

    Column {

        Box(
                modifier = Modifier
                        .background(Color.DarkGray) //colorGreen else colorOrange)
                        .height(1.dp)
                        .fillMaxWidth()
        )

        Row(
                Modifier.padding(top = 0.dp), verticalAlignment = Alignment.CenterVertically
        ) {

            val amFr: State<Float?> = if (str == "CH0") {
                gen.liveData.ch1_AM_Fr.collectAsState()
            } else {
                gen.liveData.ch2_AM_Fr.collectAsState()
            }


            // Кнопка включения AM
            Box(
                    modifier = Modifier
                            .padding(start = 8.dp)
                            .height(32.dp)
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
                                if (str == "CH0") gen.liveData.ch1_AM_EN.value =
                                        !gen.liveData.ch1_AM_EN.value
                                else gen.liveData.ch2_AM_EN.value = !gen.liveData.ch2_AM_EN.value
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


            val sensing = if (amFr.value!! < 10.0F) LiveConstrain.sensetingSliderAmFm.floatValue else LiveConstrain.sensetingSliderAmFm.floatValue * 10f
            println("sensing $sensing")
            MainscreenTextBoxAndDropdownMenu(
                    str = String.format("%.1f", amFr.value),
                    modifier = Modifier.weight(1f),
                    items = listOf("0.1", "1.0", "5.5", "10.0", "40.0", "100.0"),
                    value = amFr.value!!,
                    onChange = { if (str == "CH0") gen.liveData.ch1_AM_Fr.value = it else gen.liveData.ch2_AM_Fr.value = it },
                    sensing = sensing,
                    range = 0.1f..200f,
            )

            val amDepth: State<Float?> = if (str == "CH0") {
                gen.liveData.ch1AmDepth.collectAsState()
            } else {
                gen.liveData.ch2AmDepth.collectAsState()
            }

            InfinitySlider(
                    value = amDepth.value,
                    sensing = 0.001f,
                    range = 0f..1f,
                    onValueChange = {
                        if (str == "CH0") gen.liveData.ch1AmDepth.value =
                                it else gen.liveData.ch2AmDepth.value = it
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
                    filename = if (str == "CH0") gen.liveData.ch1_AM_Filename.collectAsState()
                    else gen.liveData.ch1_AM_Filename.collectAsState()

            )

        }

        //Spacer(modifier = Modifier.height(8.dp))

    } //}
}
