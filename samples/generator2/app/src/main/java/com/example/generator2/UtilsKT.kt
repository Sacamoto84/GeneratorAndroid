package com.example.generator2

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import java.io.IOException
import java.io.InputStream



//                                    Image(
//                                        painterResource(id = R.drawable.boldon),
//                                        contentDescription = "",
//                                        contentScale = ContentScale.FillBounds, // or some other scale
//
//                                    )
///////////////////////////////////////////////////////////////////////////////////////
// Image         //////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////
@Composable
fun Image9Patch(id: Int, W: Dp, H: Dp, modifier: Modifier = Modifier)
{
    val context = LocalContext.current

    val (w, h) = kotlin.with(LocalDensity.current) {
        W.roundToPx() to H.roundToPx()
    }

    val image = remember {
        ContextCompat.getDrawable(context, id)?.toBitmap(w, h)?.asImageBitmap()!!
    }

    Image(image, contentDescription = null, modifier = modifier)
}

@Composable
fun ImageAsset(path : String, modifier: Modifier = Modifier)
{
    val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
    val bitmap: Bitmap? = getBitmapFromAsset(LocalContext.current, path)

    bitmap?.apply { imageBitmap.value = this.asImageBitmap() }

    imageBitmap.value?.apply {
        Image(
            bitmap = this,
            contentDescription = null,
            //contentScale = ContentScale.Crop,
            modifier = Modifier.then(modifier)
            //.fillMaxWidth()
            //.fillMaxHeight(0.95F)
            //.clip(RoundedCornerShape(16.dp))
            //.border(
            //    3.dp,
            //    Color(0xFF000000),
            //    RoundedCornerShape(16.dp)
            //)

        )
    }
}

fun getBitmapFromAsset(context: Context, filePath: String?): Bitmap? {
    val assetManager: AssetManager = context.assets
    val istr: InputStream
    var bitmap: Bitmap? = null
    try {
        istr = assetManager.open(filePath!!)
        bitmap = BitmapFactory.decodeStream(istr)
    } catch (e: IOException) {
        // handle exception
    }
    return bitmap
}
///////////////////////////////////////////////////////////////////////////////////////


fun Float.format(digits: Int) = "%.${digits}f".format(this)


fun log(text : String)
{

    Global.log.add(LogMessage(text))
}

