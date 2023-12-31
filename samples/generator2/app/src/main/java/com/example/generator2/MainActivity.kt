package com.example.generator2

//import com.example.generator2.ui.wiget.GestureOfDrag
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.generator2.Global.onoffconfig
import com.example.generator2.Global.onoffconfig1
import com.example.generator2.ui.slider.SunriseSliderColors
import com.example.generator2.ui.theme.Generator2Theme
import com.example.generator2.ui.wiget.UImodifier.coloredShadow2
import com.jortas.sunriseslider.ProjColors


enum class BikePosition {
    Start, Finish
}

fun valuesList() = listOf(0f, 100f)

fun sunriseSliderColorsDefault() = SunriseSliderColors(
    thumbColor = ProjColors.lightBlue,
    thumbDisabledColor = ProjColors.silver,
    inThumbColor = ProjColors.white,
    trackBrush = Brush.horizontalGradient(
        listOf(
            ProjColors.lightBlue,
            ProjColors.grayish
        ),
        tileMode = TileMode.Clamp
    ),
    inactiveTrackColor = ProjColors.lightBlue.copy(alpha = 0.1f),
    tickActiveColor = ProjColors.white,
    tickInactiveColor = ProjColors.lightBlue.copy(alpha = 0.3f)
)


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Global.ch1_EN.value = true
        Global.componentActivity = this
        Global.contextActivity = applicationContext
        Global.init()
        Global.observe()

        Utils.ContextMainActivity = applicationContext

        /** Запуск Звука
         */
        PlaybackEngine.create(this) //.create(this)
        val result = PlaybackEngine.start()


        onoffconfig1.pathOn = "png/switch/on.png"
        onoffconfig1.pathOff = "png/switch/off.png"
        onoffconfig1.pathGroup = "png/switch/switch.png"
        onoffconfig1.componentW = 64.0f
        onoffconfig1.componentPixelW = 108.0f
        onoffconfig1.componentPixelH = 64.0f
        onoffconfig1.groupW = 30.0f
        onoffconfig1.groupPixelW = 37.0f
        onoffconfig1.groupPixelH = 49.0f
        onoffconfig1.groupDeltaY = 0.dp
        onoffconfig1.groupPositionOn = 34.dp
        onoffconfig1.groupPositionOff = 0.dp




        onoffconfig.componentW = 64.0f
        onoffconfig.componentPixelW = 108.0f
        onoffconfig.componentPixelH = 64.0f
        onoffconfig.groupW = 30.0f
        onoffconfig.groupPixelW = 37.0f
        onoffconfig.groupPixelH = 49.0f
        onoffconfig.groupDeltaY = 2.dp
        onoffconfig.groupPositionOn = 29.dp
        onoffconfig.groupPositionOff = 5.dp


        //PlaybackEngine.CH_EN(0,true)

        //if (result != 0) {
        //showToast("Error opening stream = $result")
        //}

        //Hawk.init(this).setLogInterceptor { message -> Log.i("HAWK:", message) }.build()

        val arrFilesCarrier: Array<String> = Utils.listFileInCarrier()
        //Заполняем список
        for (i in arrFilesCarrier.indices) {
            Global.itemlistCarrier.add(itemList(Global.patchCarrier, arrFilesCarrier[i], 0))
        }
        val arrFilesMod: Array<String> = Utils.listFileInMod() //Получение списка файлов в папке Mod
        for (i in arrFilesMod.indices) {
            Global.itemlistAM.add(itemList(Global.patchMod, arrFilesMod[i], 1))
            Global.itemlistFM.add(itemList(Global.patchMod, arrFilesMod[i], 0))
        }

        val imageLoader = ImageLoader.Builder(applicationContext)
            .components {
                if (SDK_INT >= 28) {
                    //add(GifDecoder.Factory())
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()


        setContent {
            Generator2Theme {


                mainsreen2()


//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    //color = Color(0xff2E3138) //MaterialTheme.colors.background
//                    color = Color(0xFF897F7F) //MaterialTheme.colors.background
//                ) {
//
//
//
//
//
//
//                    Image(
//                        painterResource(id = R.drawable.spire),
//                        contentDescription = "",
//                        contentScale = ContentScale.FillBounds, // or some other scale
//                        modifier = Modifier.fillMaxSize()
//                    )
//
//                    Column(
//                        Modifier
//                            .verticalScroll(rememberScrollState())
//                            .recomposeHighlighter()
//                    ) {
//
//
//                        Card(
//                            shape = RoundedCornerShape(20.dp),
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp),
//                            //.height(200.dp)
//                            //, backgroundColor = Color(0x00000000)
//                            //backgroundColor = Color(0xff40434A),
//                            //backgroundColor = Color(0xFFB9ABAC)
//
//
//                            elevation = 10.dp,
//                            //modifier = Modifier
//                        ) {
//
//
//                            val bC: ButtonColors = buttonColors(
//                                contentColor = Color(0xFFE7E1D5),
//                                backgroundColor = Color(0xFF29A1D3)
//                            )
//
//                            Column(
//                                modifier = Modifier.background(
//                                    brush = GradientMixer.vertical(
//                                        Color(
//                                            0xFFDEDDDF
//                                        ), Color(0xFFC0BFC2)
//                                    )
//                                )
//                            ) {
//
//                                Row(
//                                    //modifier = Modifier.background(Color.Red)
//                                ) {
//
//                                    val r1 by Global.ch1_EN.observeAsState()
//                                    ON_OFF(state = r1!!, config = onoffconfig1, onClick = {
//                                        Global.ch1_EN.value = !Global.ch1_EN.value!!
//                                    }, modifier = Modifier.padding(start = 8.dp, top = 30.dp))
//
//
//                                    Box(
//                                        modifier = Modifier.padding(
//                                            top = 8.dp,
//                                            end = 8.dp,
//                                            start = 8.dp
//                                        )
//                                    )
//                                    {
//                                        Image9Patch(R.drawable.bg1, 310.dp, 70.dp)
//                                        Spinner(
//                                            "CH0",
//                                            "CR",
//                                            transparrent = true,
//                                            modifier = Modifier.padding(start = 4.dp, top = 5.dp)
//                                        )
//                                    }
//
//                                }
//
//                                //Spacer(modifier = Modifier.height(16.dp))
//
//                                val sColor: SliderColors = SliderDefaults.colors(
//                                    thumbColor = Color(0xFFF7F6F2),
//                                    activeTrackColor = Color(0xFF007CBC),//(0xFFF1B018),
//                                    inactiveTrackColor = Color(0xFF24252A),
//                                )
//
//                                var sliderPosition by remember { mutableStateOf(0f) }
//
//                                Box() {
//                                    Text(text = sliderPosition.toString())
//                                    Slider(
//                                        value = sliderPosition,
//                                        onValueChange = { sliderPosition = it },
//                                        valueRange = 0f..360f,
//                                        //colors = sColor,
//                                        modifier = Modifier.padding(start = 60.dp, end = 60.dp)
//                                    )
//                                }
//
//                                ColorfulSlider(
//                                    modifier = Modifier.padding(start = 60.dp, end = 60.dp),
//                                    value = sliderPosition,
//                                    thumbRadius = 10.dp,
//                                    trackHeight = 10.dp,
//                                    onValueChange = { it ->
//                                        sliderPosition = it
//                                    },
//                                    colors = MaterialSliderDefaults.materialColors(
//                                        inactiveTrackColor = SliderBrushColor(color = Color.Transparent),
//                                        //activeTrackColor = SliderBrushColor(
//                                        //    brush = sunriseGradient(),
//                                        //)
//                                    ),
//                                    //borderStroke = BorderStroke(2.dp, sunriseGradient())
//                                )
//
//                                var value by remember { mutableStateOf(-0.5f) }
//                                Text(text = value.toString())
//
//
//
//
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        //.wrapContentHeight()
//                                        .padding(60.dp)
//                                ) {
//
//                                    val sunriseSliderColors = sunriseSliderColorsDefault()
//
//                                    val interactionSource = remember { MutableInteractionSource() }
//                                    val wasPressed = interactionSource.collectIsPressedAsState()
//                                    var enabledValue by remember { mutableStateOf(false) }
//
//                                    var values by remember { mutableStateOf(valuesList()) }
//                                    var steps by remember { mutableStateOf(10) }
//                                    var valueRange: ClosedFloatingPointRange<Float>? by remember {
//                                        mutableStateOf(
//                                            null
//                                        )
//                                    }
//                                    val onValueChangeFinished by remember { mutableStateOf({}) }
//                                    val tutorialEnabled by remember { mutableStateOf(true) }
//
//                                    if (!enabledValue && wasPressed.value) {
//                                        enabledValue = true
//                                    }
//
//
//
//                                    SunriseSlider(
//                                        value = sliderPosition,
//                                        onValueChange = { sliderPosition = it },
//                                        //colors = sColor,
//                                        modifier = Modifier.padding(start = 60.dp, end = 60.dp)
//                                    )
//
//
//                                }
//
//
//                                Row() {
//
//
//                                    EncoderLine(
//                                        modifier = Modifier
//                                            .padding(start = 32.dp)
//                                            .size(80.dp, 80.dp),
//                                        //.background(color = Color.Red)
//                                        //.scale(1f)
//
//                                        imageThump = ImageBitmap.imageResource(id = R.drawable.knob2thumb),
//                                        imageThumpSize = 90.dp,
//                                        imageThumbOffset = Offset(0f, 0f),
//
//                                        imageBG = ImageBitmap.imageResource(id = R.drawable.knob2bg),   //Неподвижная часть
//                                        imageBGSize = 100.dp,
//                                        imageBGOffset = Offset(0f, 0f),
//
//                                        //drawMeasureLine = true,
//                                        //drawMeasureCircle = true,
//                                        //drawMeasureDot = true
//                                        sensitivity = 0.4f,
//                                        onValueChange = { sliderPosition = it },
//                                        value = sliderPosition
//                                    )
//
//
//
//
//
//                                    EncoderLine(
//                                        modifier = Modifier
//                                            //.padding(start = 32.dp, bottom = 64.dp)
//                                            .size(100.dp, 100.dp)
//                                            //.background(color = Color.Red)
//                                            .scale(0.5f),
//                                        imageThump = ImageBitmap.imageResource(id = R.drawable.knob1thumb),
//                                        imageThumpSize = 20.dp,
//                                        imageThumbOffset = Offset(0f, -40f),
//
//                                        imageBG = ImageBitmap.imageResource(id = R.drawable.knob1bg),   //Неподвижная часть
//                                        imageBGSize = 100.dp,
//                                        imageBGOffset = Offset(0f, 1f),
//
//                                        //drawMeasureLine = true,
//                                        //drawMeasureCircle = true,
//                                        //drawMeasureDot = true
//
//                                        rangeAngle = 180f + 90f, //Диапазон
//                                        offsetAngle = -135f, //Смещение начала
//                                        sensitivity = 0.4f,
//                                        onValueChange = { sliderPosition = it },
//                                        value = sliderPosition
//                                    )
//                                }
//
//
//                                var st by remember { mutableStateOf("Privet") }
//                                var count by remember { mutableStateOf(0) }
//
//                                //GreenLCD(value = st)
//                                //GreenLCD(value = "&%:${sliderPosition.format(0)}Hz")
//
//                                Button(onClick = { st = "Ment$%&" }) {}
//
//
//                                //ImageAsset( path = "png/yes1.png")
//
//
//                                Row {
//
//
//                                    Button(onClick = { st = "Savenkov" }) {}
//
//
//
//                                    Column() {
//
//
//
//                                        InfinitiSlider(
//                                            modifier = Modifier
//                                                .padding(start = 0.dp)
//                                                //.size(40.dp, 40.dp),//.background(Color.Red)
//                                                .height(20.dp)
//                                                .width(140.dp),
//                                            sensing = 0.01f,
//                                            image = ImageBitmap.imageResource(id = R.drawable.base),  //Фоновая картинка, неподвижная
//                                            rangeAngle = 1000f,
//                                            value = sliderPosition, //Вывод icrementalAngle от 0 до rangeAngle при rangeAngle != 0, и от +- Float при rangeAngle = 0
//                                            onValueChange = { sliderPosition = it },
//                                        )
//
//
//
//                                        Text(
//                                            text = "${sliderPosition}",
//                                            fontFamily = FontFamily(
//                                                Font(
//                                                    R.font.led_8x6,
//                                                    FontWeight.Normal
//                                                )
//                                            ),
//                                            color = Color.Black
//                                        )
//
//                                        Text(
//                                            text = "${sliderPosition}",
//                                            fontFamily = FontFamily(
//                                                Font(
//                                                    R.font.dotmari,
//                                                    FontWeight.Normal
//                                                )
//                                            ),
//                                            color = Color.Black
//                                        )
//
//                                        Text(
//                                            text = "${sliderPosition}",
//                                            fontFamily = FontFamily(
//                                                Font(
//                                                    R.font.nunito,
//                                                    FontWeight.Normal
//                                                )
//                                            ),
//                                            color = Color.Black
//                                        )
//
//                                        Text(
//                                            text = "${sliderPosition}",
//                                            fontFamily = FontFamily(
//                                                Font(
//                                                    R.font.squaredotmatrix,
//                                                    FontWeight.Normal
//                                                )
//                                            ),
//                                            color = Color.Black
//                                        )
//
//
//
//                                    }
//
//
//
//
//
//
//
//
//
//
//
//                                    Column()
//                                    {
//
//
//                                        EncoderLine(
//                                            modifier = Modifier
//                                                .padding(start = 0.dp)
//                                                .size(80.dp, 80.dp),
//                                            //.background(color = Color.Red)
//                                            //.scale(1f)
//
//                                            imageThump = ImageBitmap.imageResource(id = R.drawable.knob8thumb),
//                                            imageThumpSize = 10.dp,
//                                            imageThumbOffset = Offset(0f, -20f),
//
//                                            imageBG = ImageBitmap.imageResource(id = R.drawable.knob8bg),   //Неподвижная часть
//                                            imageBGSize = 100.dp,
//                                            imageBGOffset = Offset(10f, 7f),
//
//                                            //drawMeasureLine = true,
//                                            //drawMeasureCircle = true,
//                                            //drawMeasureDot = true
//                                            sensitivity = 0.4f,
//                                            onValueChange = { sliderPosition = it },
//                                            value = sliderPosition
//                                        )
//
//                                    }
//
//
//                                }
//
//
//                            }
//                        }
//
//                        Card(
//                            shape = RoundedCornerShape(16.dp),
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp)
//                                .height(200.dp)
//                                .border(0.3.dp, Color(0xFF2D2F36), RoundedCornerShape(16.dp)),
//                            backgroundColor = Color(0xFFB9ABAC),
//                            elevation = 10.dp,
//                        )
//                        {}
//
//
//                        //imageView.load(daimageLoader =  imageLoader )
///*
//                        CoilImage(
//                            imageModel = R.drawable.g1,
//
//                            imageLoader = { imageLoader }, modifier = Modifier
//                                .recomposeHighlighter()
//                                .clickable {
//
//                                    if (GG.ch1_EN.value == 0)
//                                        GG.ch1_EN.value = 1
//                                    else
//                                        GG.ch1_EN.value = 0
//
//                                    //PlaybackEngine.CH_EN(1, GG.ch1_EN.value as Int)
//                                }
//                        )
//
// */
//
//
//                    }
//
//                    //Spinner("CH0", "CR", Global)
//                    //Spinner("CH0", "AM", Global)
//                    //Spinner("CH0", "FM", Global)
//                    //Spinner("CH1", "CR", Global)
//                    //Spinner("CH1", "AM", Global)
//                    //Spinner("CH1", "FM", Global)
//                    //ani()
//
//
//                    /*r text1 by remember { mutableStateOf("eee") }
//                    TextField(
//
//                        value = text1,
//                        onValueChange = { newText ->
//                            text1 = newText
//                        },
//                        modifier = Modifier.recomposeHighlighter()
//                    )
//                    */
//
//                    /*
//                                           mySwitch(state = Global.ch2_EN, onClick =
//                                           {
//                                               Log.d("mySwitch", "onClick")
//                                               Global.ch2_EN = !Global.ch2_EN
//                                               PlaybackEngine.CH_EN(1, Global.ch2_EN)
//                                           })
//                   */
//                    //shadow()
//                    // Create an ImageLoader
//
///*
//                        val drawable =
//                            AppCompatResources.getDrawable(LocalContext.current, R.drawable.g1)
//
//                        Image(
//                            painter = rememberDrawablePainter(drawable = drawable),
//                            contentDescription = "", modifier = Modifier
//                            .scale(scaleX = 0.5f, scaleY = 1.0f)
//                        )
//
//*/
//
//
//                    /*
//
//                    CoilImage(
//                        imageModel = R.drawable.g1,
//                        //contentScale = ContentScale.FillBounds,
//                        imageLoader = { imageLoader },
//                        //modifier = Modifier.size(200.dp,100.dp)
//                    )
//
//                     */
//
//
//                }
            }
        }
    }
}


@Composable
fun Greeting() {

    Text(text = "Hello ${Global.ch1_Carrier_Filename}!")
    Text(text = "${Global.ch1_EN}!")
    Button(onClick = {
        Global.ch1_EN.value = true
        Global.ch1_Carrier_Filename.value = "on"
        Log.d("---", "on")
        //PlaybackEngine.CH_EN(0, Global.ch1_EN)
    })
    {
        Text(text = "On")
    }
    Button(
        onClick = {
            Global.ch1_EN.value = false
            Global.ch1_Carrier_Filename.value = "off"
            Log.d("---", "off")
            //PlaybackEngine.CH_EN(0, Global.ch1_EN)
        },
        modifier = Modifier.recomposeHighlighter()
    )
    {
        Text(text = "Off")
    }
}

@Composable
fun Greeting1() {
    Text(text = "${Global.ch1_AM_EN}!")
}

@Composable
fun GreetingAM() {
    Button(onClick = {
        //Global.ch1_AM_EN = true
        //PlaybackEngine.CH_EN(0, Global.ch1_AM_EN)
    })
    {
        Text(text = "On")
    }
    Button(onClick = {
        //Global.ch1_AM_EN = false
        //PlaybackEngine.CH_EN(0, Global.ch1_AM_EN)
    })
    {
        Text(text = "Off")
    }
}

@ExperimentalAnimationApi
@Composable
fun ani() {

    var editable = remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = editable.value,
        Modifier.clickable { editable.value = !editable.value }) {
        Text(text = "Edit", fontSize = 48.sp)
    }

    var visible = remember { mutableStateOf(true) }
    val density = LocalDensity.current
    AnimatedVisibility(
        visible = visible.value,
        enter = slideInVertically {
            // Slide in from 40 dp from the top.
            with(density) { -40.dp.roundToPx() }
        } + expandVertically(
            // Expand from the top.
            expandFrom = Alignment.Top
        ) + fadeIn(
            // Fade in with the initial alpha of 0.3f.
            initialAlpha = 0.3f
        ),
        exit = slideOutVertically() + shrinkVertically() + fadeOut()
    ) {
        Text(
            "Hello",
            Modifier
                .fillMaxWidth()
                .height(200.dp), fontSize = 48.sp
        )
    }

    var count = remember { mutableStateOf(0) }
    Row {

        Button(onClick = { count.value++ }) {
            Text("Add")
        }
        AnimatedContent(targetState = count.value) { targetCount ->
            // Make sure to use `targetCount`, not `count`.
            Text(text = "Count: $targetCount")
        }
    }


    AnimatedContent(
        targetState = count.value,
        transitionSpec = {
            // Compare the incoming number with the previous number.
            if (targetState > initialState) {
                // If the target number is larger, it slides up and fades in
                // while the initial (smaller) number slides up and fades out.
                slideInVertically { height -> height } + fadeIn() with
                        slideOutVertically { height -> -height } + fadeOut()
            } else {
                // If the target number is smaller, it slides down and fades in
                // while the initial number slides down and fades out.
                slideInVertically { height -> -height } + fadeIn() with
                        slideOutVertically { height -> height } + fadeOut()
            }.using(
                // Disable clipping since the faded slide-in/out should
                // be displayed out of bounds.
                SizeTransform(clip = false)
            )
        }
    ) { targetCount ->
        Text(text = "$targetCount")
    }


    ////////////////////////////
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.7f at 500
            },
            repeatMode = RepeatMode.Reverse
        )
    )

    Text(text = "dddddddd", modifier = Modifier.alpha(alpha))

    var st by remember { mutableStateOf(true) }


    //mySwitchRender("on", "off", true)


    Box {
        //NightSky(height, particleCount = 2500)
        //angleRotation// hack to get the canvas called again!


        Canvas(modifier = Modifier
            .fillMaxSize(),

            onDraw = {

                val canvasWidth = size.width
                val canvasHeight = size.height
                val canvasSize = size

                drawLine(
                    start = Offset(x = canvasWidth, y = 0f),
                    end = Offset(x = 0f, y = canvasHeight),
                    color = Color.Blue
                )

                drawCircle(
                    color = Color.Blue,
                    center = Offset(x = canvasWidth / 2, y = canvasHeight / 2),
                    radius = size.minDimension / 4
                )

                rotate(degrees = 45F) {
                    drawRect(
                        color = Color.Gray,
                        topLeft = Offset(x = canvasWidth / 3F, y = canvasHeight / 3F),
                        size = canvasSize / 3F

                    )
                }


                // axis
                //drawCircle(
                //   color = Color.Gray,
                //center = centerOffset,
                //radius = it.orbitRadius,
                //   style = Stroke(width = 2f)
                // )

                //planet
                //drawCircle(
                //color = it.planetColor,
                //center = Offset(it.x, it.y),
                //radius = it.radius,
                //)

                //moon
                //drawCircle(
                // color = Color.Gray,
                //center = Offset(it.moon.x, it.moon.y),
                //radius = 2f
                //)


            })


    }
}

@Composable
fun MySwitch(
    onClick: () -> Unit,
    state: Boolean
) {
    MySwitchRender(_text_on = "On", _text_off = "off", state = state, onClick = onClick)
}

@Composable
fun MySwitchRender(_text_on: String, _text_off: String, state: Boolean, onClick: () -> Unit) {

    //var state = //by remember { mutableStateOf(_state) }

    val switchHeight: Dp = 32.dp  //Высота свитча
    val switchWight: Dp = 96.dp  //Ширина свитча
    val round: Dp = switchHeight / 2 //Сглаживание
    val diameter: Dp = switchHeight - 20.dp //Радиус круга

    //var bikeState by remember { mutableStateOf(BikePosition.Start) }
    //var expanded by remember { mutableStateOf() }

    val offsetAnimation: Dp by animateDpAsState(
        targetValue = if (state) switchWight - round - diameter / 2 else round - diameter / 2,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
            //     //,stiffness = Spring.StiffnessLow
            // )
            //tween (durationMillis = 500,
            //easing = CubicBezierEasing(0.4f, 0.0f, 0.8f, 0.8f)
            //    easing = FastOutSlowInEasing
        )

        //keyframes {
        //    durationMillis = 1000
        //    50.dp.at(20).with(LinearEasing)
        //    200.dp.at(600).with(LinearOutSlowInEasing)
        //    250.dp.at(700).with(FastOutSlowInEasing)
        //}

        //repeatable(iterations = 5, animation = tween(), repeatMode = RepeatMode.Reverse)

    )

    Box(
        modifier = Modifier
            .size(switchWight, switchHeight)
            .recomposeHighlighter()
            .clickable(
                onClick = {
                    onClick()
                }
            )
    )
    {

        Box(
            modifier = Modifier
                .size(switchWight, switchHeight)
                .clip(RoundedCornerShape(round))
                .background(Color.Green)
                .border(
                    border = BorderStroke(2.dp, Color.Blue),
                    shape = RoundedCornerShape(round)
                )


        )

        //Круг
        Box(
            modifier = Modifier
                .size(diameter)
                .absoluteOffset(x = offsetAnimation, y = round - diameter / 2)
                .clip(CircleShape)
                .background(
                    when (state) {
                        true -> Color.Red
                        false -> Color.Black
                    }
                )
        )


    }


}

@Composable
fun MySwitchPreview() {
    MySwitchRender("on", "off", true, onClick = {})
}

@Composable
fun shadow() {
    val translation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
    )

    Box(
        modifier = Modifier
            .height(300.dp)
            .background(Color(0xff426ca0)),

        )
    {
        Column() {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .coloredShadow2(
                        color = Color(0xffEBF4EC),
                        alpha = translation,
                        shadowRadius = 10.dp
                    )
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xff28A138)), Alignment.Center
            )
            {
                Text("ON")
            }

            //Icon(bitmap = "drawable/normal.9.png", contentDescription = "ddd")

            val context = LocalContext.current
            val (w, h) = with(LocalDensity.current) {
                400.dp.roundToPx() to 100.dp.roundToPx()
            }
            val image = remember {
                ContextCompat.getDrawable(context, R.drawable.normal)?.toBitmap(800, 100)
                    ?.asImageBitmap()!!
            }
            Image(image, contentDescription = null)


        }


    }
}



