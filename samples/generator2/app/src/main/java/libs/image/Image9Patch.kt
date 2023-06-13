package libs.image

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

// Картинка 9Patch
@Composable
fun Image9Patch(id: Int, w: Dp, h: Dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val (_w, _h) = with(LocalDensity.current) {
        w.roundToPx() to h.roundToPx()
    }

    val image = remember {
        ContextCompat.getDrawable(context, id)?.toBitmap(_w, _h)?.asImageBitmap()!!
    }

    Image(image, contentDescription = null, modifier = modifier)
}