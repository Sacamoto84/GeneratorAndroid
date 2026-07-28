import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.common.haptic.Haptic
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.generator.GeneratorCH
import com.example.generator2.features.generator.GeneratorMOD
import com.example.generator2.model.LiveConstrain
import com.example.generator2.screens.mainscreen4.modifierInfinitySlider
import com.example.generator2.screens.mainscreen4.ms4SwitchWidth
import com.example.generator2.screens.mainscreen4.textStyleButtonOnOff
import com.example.generator2.screens.mainscreen4.ui.InfinitySlider
import com.example.generator2.screens.mainscreen4.ui.MainscreenTextBoxAndDropdownMenu
import com.example.generator2.screens.mainscreen4.ui.UIspinner
import com.example.generator2.theme.colorChL
import com.example.generator2.theme.colorChR
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.screens.common.modifier.noRippleClickable
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun CardCarrier(ch: GeneratorCH, gen: Generator) {

    val chEN: State<Boolean> =
        if (ch == GeneratorCH.CHL) gen.liveData.chL_EN.collectAsStateWithLifecycle() else gen.liveData.chR_EN.collectAsStateWithLifecycle()

    val carrierFr: State<Float> =
        if (ch == GeneratorCH.CHL) gen.liveData.chL_Carrier_Fr.collectAsStateWithLifecycle() else gen.liveData.chR_Carrier_Fr.collectAsStateWithLifecycle()

    val fmSelectMode: State<Int?> = if (ch == GeneratorCH.CHL)
        gen.liveData.parameterInt0.collectAsStateWithLifecycle() //CHL режим выбора частот FM модуляции 0-обычный 1-минимум макс
    else
        gen.liveData.parameterInt1.collectAsStateWithLifecycle() //CHR режим выбора частот FM модуляции 0-обычный 1-минимум макс

    val fmEN: State<Boolean> =
        if (ch == GeneratorCH.CHL) gen.liveData.chL_FM_EN.collectAsStateWithLifecycle() else gen.liveData.chR_FM_EN.collectAsStateWithLifecycle()

    //Несущая заблокирована только когда FM включена в режиме минимум/максимум
    val carrierEnable = fmSelectMode.value == 0 || !fmEN.value

    //Форму несущей задаёт метаморфоза, пока она включена
    val morphEN: State<Boolean> =
        if (ch == GeneratorCH.CHL) gen.liveData.chL_Morph_EN.collectAsStateWithLifecycle()
        else gen.liveData.chR_Morph_EN.collectAsStateWithLifecycle()

    Column {

        Box(
            modifier = Modifier
                .background(if (ch == GeneratorCH.CHL) colorChL else colorChR)
                .heightIn(min = 16.dp)
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            val mono = gen.liveData.mono.collectAsStateWithLifecycle().value
            Text(
                text = if (mono) "MONO" else if (ch == GeneratorCH.CHL) "CHL" else "CHR",
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }


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
                        if (ch == GeneratorCH.CHL) gen.liveData.chL_EN.value = !gen.liveData.chL_EN.value
                        else gen.liveData.chR_EN.value = !gen.liveData.chR_EN.value

                        Haptic.confirm()
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
                enable = carrierEnable,
                items = listOf(
                    "20",
                    "100",
                    "600",
                    "800",
                    "1000",
                    "1500",
                    "2000",
                    "2500",
                    "3000",
                    "3500",
                    "4000",
                    "4800",
                    "6000",
                    "9600",
                    "12000",
                    "20000",
                    "24000",
                    "26000"
                ),
                value = carrierFr.value,
                onChange = {

                    if (carrierEnable)
                        if (ch == GeneratorCH.CHL) gen.liveData.chL_Carrier_Fr.value =
                            it else gen.liveData.chR_Carrier_Fr.value = it
                },
                range = 50f..10000f
            )



            InfinitySlider(
                value = carrierFr.value,
                sensing = LiveConstrain.sensetingSliderCr.floatValue / 4,
                range = 50f..100000f,
                onValueChange = {
                    if (carrierEnable) if (ch == GeneratorCH.CHL) gen.liveData.chL_Carrier_Fr.value =
                        it else gen.liveData.chR_Carrier_Fr.value = it
                },
                modifier = modifierInfinitySlider,
                vertical = true,
                invert = true,
                visibleText = false
            )

            UIspinner.Spinner(
                ch = ch,
                mod = GeneratorMOD.CR,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .wrapContentWidth()
                    .clip(shape = RoundedCornerShape(4.dp)),
                filename = if (ch == GeneratorCH.CHL) gen.liveData.chL_Carrier_Filename.collectAsStateWithLifecycle()
                else gen.liveData.chR_Carrier_Filename.collectAsStateWithLifecycle(), gen = gen,
                enable = !morphEN.value
            )

        }

    }


}
