package com.example.generator2.backup

import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.ktx.getOrDefault
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import com.example.generator2.AppPath
import com.example.generator2.generator.Generator
import com.example.generator2.model.LiveConstrain
import java.io.File

class MMKv {

    fun readVolume(gen: Generator) {

        val satchel = Satchel.with(
            storer = FileSatchelStorer(File(AppPath().config, "volume.txt")),
            encrypter = BypassSatchelEncrypter, serializer = RawSatchelSerializer
        )

        gen.liveData.maxVolume0.value = satchel.getOrDefault("maxVolume0", 1.0F)
        gen.liveData.maxVolume1.value = satchel.getOrDefault("maxVolume1", 1.0F)

        gen.liveData.volume0.value =
            gen.liveData.currentVolume0.value * satchel.getOrDefault("maxVolume0", 1.0F)
        gen.liveData.volume1.value =
            gen.liveData.currentVolume1.value * satchel.getOrDefault("maxVolume1", 1.0F)

        println("ok")
    }

    fun saveVolume(gen: Generator) {
        val satchel = Satchel.with(
            storer = FileSatchelStorer(File(AppPath().config, "volume.txt")),
            encrypter = BypassSatchelEncrypter, serializer = RawSatchelSerializer
        )

        satchel["maxVolume0"] = gen.liveData.maxVolume0.value
        satchel["maxVolume1"] = gen.liveData.maxVolume0.value
    }

    fun saveConstrain() {
        val satchel = Satchel.with(
            storer = FileSatchelStorer(File(AppPath().config, "constrain.txt")),
            encrypter = BypassSatchelEncrypter, serializer = RawSatchelSerializer
        )

        satchel["sensetingSliderCr"] = LiveConstrain.sensetingSliderCr.floatValue
        satchel["sensetingSliderFmDev"] = LiveConstrain.sensetingSliderFmDev.floatValue
        satchel["sensetingSliderAmFm"] = LiveConstrain.sensetingSliderAmFm.floatValue
    }

    fun readConstrain() {
        val satchel = Satchel.with(
            storer = FileSatchelStorer(File(AppPath().config, "constrain.txt")),
            encrypter = BypassSatchelEncrypter, serializer = RawSatchelSerializer
        )

        LiveConstrain.sensetingSliderCr.floatValue = satchel.getOrDefault("sensetingSliderCr", 0.2F)
        LiveConstrain.sensetingSliderFmDev.floatValue =
            satchel.getOrDefault("sensetingSliderFmDev", 0.2f)
        LiveConstrain.sensetingSliderAmFm.floatValue =
            satchel.getOrDefault("sensetingSliderAmFm", 0.01f)
    }

}