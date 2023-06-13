package com.example.generator2.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class ConfigOnOff {

    var pathGroup        : String  = ""
    var pathOn           : String  = ""
    var pathOff          : String  = ""

    var componentW       : Float = 10.0f
    var componentPixelW  : Float = 10.0f
    var componentPixelH  : Float = 10.0f

    var groupW           : Float = 10.0f

    var groupPixelW      : Float = 10.0f
    var groupPixelH      : Float = 10.0f
    var groupDeltaY      : Dp    = 0.dp
    var groupPositionOn  : Dp    = 10.dp
    var groupPositionOff : Dp    = 0.dp

}