package com.example.generator2.di

import com.example.generator2.PlaybackEngine
import com.example.generator2.audio_device.AudioDevice
import com.example.generator2.backup.Backup
import com.example.generator2.screens.scripting.ui.ScriptKeyboard
import com.example.generator2.util.UtilsKT
import com.example.generator2.vm.Script

class Hub(
    var utils: UtilsKT,
    var script: Script,
    var keyboard: ScriptKeyboard,
    var playbackEngine: PlaybackEngine,
    var audioDevice: AudioDevice,
    var backup: Backup
) {


}