package com.example.generator2.screens.config.molecule

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.example.generator2.screens.config.atom.ConfigLineText
import com.example.generator2.update.Update

@Composable
fun ConfigUpdate() {
    Column {
        ConfigLineText("Текущая версия", Update.currentVersion)
        ConfigLineText("Версия на сервере", Update.externalVersion)
    }
}