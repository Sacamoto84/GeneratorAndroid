package com.example.generator2.features.scope

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.generator2.theme.Generator2Theme

@Preview
@Composable
fun OscilloscopePreview() {
    Generator2Theme {
        val scope = remember { Scope() }
        Oscilloscope(scope)
    }
}

@Preview
@Composable
fun LissaguPreview() {
    Generator2Theme {
        val scope = remember { Scope() }
        Lissagu(scope)
    }
}

@Preview
@Composable
fun PanelButtonPreview() {
    Generator2Theme {
        val scope = remember { Scope() }
        PanelButton(scope)
    }
}

@Preview
@Composable
fun OscilloscopeControlPreview() {
    Generator2Theme {
        val scope = remember { Scope() }
        OscilloscopeControl(scope)
    }
}

@Preview
@Composable
fun OscilloscopeComposePreview() {
    Generator2Theme {
        val scope = remember { Scope() }
        OscilloscopeCompose(scope)
    }
}
