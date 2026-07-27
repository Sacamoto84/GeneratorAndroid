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
import com.example.generator2.features.generator.MASTER_MODE_BUTTON
import com.example.generator2.features.generator.MASTER_MODE_ONOFF
import com.example.generator2.features.generator.MASTER_MODE_SLOW
import com.example.generator2.screens.common.modifier.noRippleClickable
import com.example.generator2.screens.mainscreen4.ms4SwitchWidth
import com.example.generator2.screens.mainscreen4.textStyleButtonOnOff
import com.example.generator2.screens.mainscreen4.ui.MainscreenTextBoxAndDropdownMenu
import com.example.generator2.screens.mainscreen4.ui.UIspinner
import com.example.generator2.theme.colorDarkBackground

@Composable
fun CardMaster(str: String = "CH0", gen: Generator) {

    val isCh0 = str == "CH0"

    val en by (if (isCh0) gen.liveData.chL_Master_EN else gen.liveData.chR_Master_EN).collectAsState()
    val mode by (if (isCh0) gen.liveData.chL_Master_Mode else gen.liveData.chR_Master_Mode).collectAsState()

    Column {

        Box(
            modifier = Modifier
                .background(Color.DarkGray)
                .height(1.dp)
                .fillMaxWidth()
        )

        // Строка 1: включение + выбор режима
        Row(Modifier.padding(top = 0.dp), verticalAlignment = Alignment.CenterVertically) {

            // Кнопка включения MAS
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(32.dp)
                    .width(ms4SwitchWidth)
                    .border(
                        2.dp,
                        color = if (en) Color(0xFF1B5E20) else Color.DarkGray,
                        RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = if (en) Color(0xFF01AE0F) else colorDarkBackground)
                    .noRippleClickable(onClick = {
                        if (isCh0) gen.liveData.chL_Master_EN.value = !gen.liveData.chL_Master_EN.value
                        else gen.liveData.chR_Master_EN.value = !gen.liveData.chR_Master_EN.value
                        Haptic.confirm()
                    }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MAS",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (en) colorDarkBackground else Color.LightGray,
                    style = textStyleButtonOnOff
                )
            }

            fun setMode(m: Int) {
                if (isCh0) gen.liveData.chL_Master_Mode.value = m
                else gen.liveData.chR_Master_Mode.value = m
                Haptic.confirm()
            }

            ModeButton("Плавно", mode == MASTER_MODE_SLOW, Modifier.weight(1f)) { setMode(MASTER_MODE_SLOW) }
            ModeButton("Вкл/Выкл", mode == MASTER_MODE_ONOFF, Modifier.weight(1f)) { setMode(MASTER_MODE_ONOFF) }
            ModeButton("Кнопка", mode == MASTER_MODE_BUTTON, Modifier.weight(1f)) { setMode(MASTER_MODE_BUTTON) }
        }

        // Строка 2: параметры режима
        Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            when (mode) {
                MASTER_MODE_SLOW -> {
                    val period by (if (isCh0) gen.liveData.chL_Master_Period else gen.liveData.chR_Master_Period).collectAsState()
                    MainscreenTextBoxAndDropdownMenu(
                        str = String.format("%.1f", period),
                        modifier = Modifier.weight(1f),
                        items = listOf("0.1", "1.0", "2.0", "10.0", "60.0", "100.0"),
                        value = period,
                        onChange = {
                            if (isCh0) gen.liveData.chL_Master_Period.value = it
                            else gen.liveData.chR_Master_Period.value = it
                        },
                        sensing = 0.05f,
                        range = 0.1f..100f,
                    )
                    UIspinner.Spinner(
                        str,
                        "MASTER",
                        modifier = Modifier
                            .padding(top = 0.dp, start = 8.dp, end = 8.dp)
                            .wrapContentWidth()
                            .clip(shape = RoundedCornerShape(4.dp)),
                        filename = if (isCh0) gen.liveData.chL_Master_Filename.collectAsState()
                        else gen.liveData.chR_Master_Filename.collectAsState(),
                        gen = gen
                    )
                }

                MASTER_MODE_ONOFF -> {
                    val tOn by (if (isCh0) gen.liveData.chL_Master_TOn else gen.liveData.chR_Master_TOn).collectAsState()
                    val tOff by (if (isCh0) gen.liveData.chL_Master_TOff else gen.liveData.chR_Master_TOff).collectAsState()
                    MainscreenTextBoxAndDropdownMenu(
                        str = "ON " + String.format("%.1f", tOn),
                        modifier = Modifier.weight(1f),
                        items = listOf("0.1", "0.5", "1.0", "2.0", "5.0", "10.0"),
                        value = tOn,
                        onChange = {
                            if (isCh0) gen.liveData.chL_Master_TOn.value = it
                            else gen.liveData.chR_Master_TOn.value = it
                        },
                        sensing = 0.05f,
                        range = 0.1f..100f,
                    )
                    MainscreenTextBoxAndDropdownMenu(
                        str = "OFF " + String.format("%.1f", tOff),
                        modifier = Modifier.weight(1f),
                        items = listOf("0.1", "0.5", "1.0", "2.0", "5.0", "10.0"),
                        value = tOff,
                        onChange = {
                            if (isCh0) gen.liveData.chL_Master_TOff.value = it
                            else gen.liveData.chR_Master_TOff.value = it
                        },
                        sensing = 0.05f,
                        range = 0.1f..100f,
                    )
                }

                else -> {
                    Text(
                        text = "Держи общую кнопку MASTER внизу экрана",
                        color = Color.LightGray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .padding(start = 8.dp)
            .height(32.dp)
            .border(2.dp, if (selected) Color(0xFF1B5E20) else Color.DarkGray, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF01AE0F) else colorDarkBackground)
            .noRippleClickable(onClick = onClick),
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
