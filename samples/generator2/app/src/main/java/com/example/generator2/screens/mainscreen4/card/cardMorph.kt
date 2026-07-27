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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.generator2.common.haptic.Haptic
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.generator.MORPH_MODE_SMOOTH
import com.example.generator2.features.generator.MORPH_MODE_STEP
import com.example.generator2.screens.common.modifier.noRippleClickable
import com.example.generator2.screens.mainscreen4.ms4SwitchWidth
import com.example.generator2.screens.mainscreen4.textStyleButtonOnOff
import com.example.generator2.screens.mainscreen4.ui.MainscreenTextBoxAndDropdownMenu
import com.example.generator2.screens.mainscreen4.ui.UIspinner
import com.example.generator2.theme.colorDarkBackground
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun CardMorph(str: String = "CH0", gen: Generator) {

    val isCh0 = str == "CH0"

    val en by (if (isCh0) gen.liveData.chL_Morph_EN else gen.liveData.chR_Morph_EN).collectAsState()
    val mode by (if (isCh0) gen.liveData.chL_Morph_Mode else gen.liveData.chR_Morph_Mode).collectAsState()
    val time by (if (isCh0) gen.liveData.chL_Morph_Time else gen.liveData.chR_Morph_Time).collectAsState()

    Column {

        Box(
            modifier = Modifier
                .background(Color.DarkGray)
                .height(1.dp)
                .fillMaxWidth()
        )

        // Строка 1: включение, режим смены, длительность шага
        Row(Modifier.padding(top = 0.dp), verticalAlignment = Alignment.CenterVertically) {

            MorphButton("MOR", en, Modifier.width(ms4SwitchWidth)) {
                if (isCh0) gen.liveData.chL_Morph_EN.value = !gen.liveData.chL_Morph_EN.value
                else gen.liveData.chR_Morph_EN.value = !gen.liveData.chR_Morph_EN.value
            }

            fun setMode(m: Int) {
                if (isCh0) gen.liveData.chL_Morph_Mode.value = m
                else gen.liveData.chR_Morph_Mode.value = m
            }

            MorphButton("Ступень", mode == MORPH_MODE_STEP, Modifier.weight(1f)) {
                setMode(MORPH_MODE_STEP)
            }
            MorphButton("Плавно", mode == MORPH_MODE_SMOOTH, Modifier.weight(1f)) {
                setMode(MORPH_MODE_SMOOTH)
            }

            MainscreenTextBoxAndDropdownMenu(
                str = String.format("%.1f c", time),
                modifier = Modifier.weight(1f),
                items = listOf("0.1", "0.5", "1.0", "2.0", "5.0", "10.0", "60.0"),
                value = time,
                onChange = {
                    if (isCh0) gen.liveData.chL_Morph_Time.value = it
                    else gen.liveData.chR_Morph_Time.value = it
                },
                sensing = 0.05f,
                range = 0.1f..100f,
            )
        }

        // Строка 2: три слота форм — галочка участия + выбор формы
        Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            MorphSlot(str, 0, gen, Modifier.weight(1f))
            MorphSlot(str, 1, gen, Modifier.weight(1f))
            MorphSlot(str, 2, gen, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MorphSlot(str: String, slot: Int, gen: Generator, modifier: Modifier = Modifier) {

    val isCh0 = str == "CH0"

    val enFlow: MutableStateFlow<Boolean> = if (isCh0) when (slot) {
        0 -> gen.liveData.chL_Morph_Slot0_EN
        1 -> gen.liveData.chL_Morph_Slot1_EN
        else -> gen.liveData.chL_Morph_Slot2_EN
    } else when (slot) {
        0 -> gen.liveData.chR_Morph_Slot0_EN
        1 -> gen.liveData.chR_Morph_Slot1_EN
        else -> gen.liveData.chR_Morph_Slot2_EN
    }

    val nameFlow: MutableStateFlow<String> = if (isCh0) when (slot) {
        0 -> gen.liveData.chL_Morph_Slot0_Filename
        1 -> gen.liveData.chL_Morph_Slot1_Filename
        else -> gen.liveData.chL_Morph_Slot2_Filename
    } else when (slot) {
        0 -> gen.liveData.chR_Morph_Slot0_Filename
        1 -> gen.liveData.chR_Morph_Slot1_Filename
        else -> gen.liveData.chR_Morph_Slot2_Filename
    }

    val slotEn by enFlow.collectAsState()

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {

        MorphButton(if (slotEn) "V" else "-", slotEn, Modifier.width(40.dp)) {
            enFlow.value = !enFlow.value
        }

        UIspinner.Spinner(
            CH = str,
            Mod = "MORPH$slot",
            modifier = Modifier
                .padding(start = 4.dp, end = 4.dp)
                .wrapContentWidth()
                .clip(shape = RoundedCornerShape(4.dp)),
            filename = nameFlow.collectAsState(),
            gen = gen,
            enable = slotEn
        )
    }
}

@Composable
private fun MorphButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(start = 8.dp)
            .height(32.dp)
            .border(2.dp, if (selected) Color(0xFF1B5E20) else Color.DarkGray, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF01AE0F) else colorDarkBackground)
            .noRippleClickable(onClick = {
                onClick()
                Haptic.confirm()
            }),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = if (selected) colorDarkBackground else Color.LightGray,
            style = textStyleButtonOnOff
        )
    }
}
