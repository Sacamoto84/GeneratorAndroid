package com.example.generator2.features.generator

import com.example.generator2.util.ArrayUtils
import com.example.generator2.util.maping
import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class GeneratorCH { CHL, CHR }

enum class GeneratorMOD { CR, AM, FM, MASTER, MORPH0, MORPH1, MORPH2 }

//Для спиннера, отсылка массива
fun Spinner_Send_Buffer(
    CH: GeneratorCH,
    Mod: GeneratorMOD,
    name: String,
    gen: Generator
) { //String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
    //var path = ""
    //path += if (Mod == "CR") Utils.patchCarrier + name + ".dat" else Utils.patchMod + name + ".dat"


    //val buf = readFileMod2048byte(path) //Здесь должны прочитать файл и записать в массив;


    //Несущая и все слоты метаморфозы берутся из библиотеки несущих
    val list = when (Mod) {
        GeneratorMOD.CR, GeneratorMOD.MORPH0, GeneratorMOD.MORPH1, GeneratorMOD.MORPH2 ->
            gen.itemlistCarrier

        else -> gen.itemlistAM
    }

    val index = list.indexOfFirst { it.name == name }

    if (index == -1) {
        return
    }

    val buf = list[index].buf

    if (CH == GeneratorCH.CHL) {
        when (Mod) {
            GeneratorMOD.AM -> {
                gen.chL.buffer_am =  byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, 0f, 1f)
                RenderChannel.sendBuffer(0, 1, gen.chL.buffer_am)
            }
            GeneratorMOD.MASTER -> {
                gen.chL.buffer_master = byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, 0f, 1f)
                RenderChannel.sendBuffer(0, 3, gen.chL.buffer_master)
            }
            GeneratorMOD.FM -> {
                gen.chL.buffer_fm =  byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, -1f, 1f)//byteToFloatArrayLittleEndian4096(buf)
                gen.updateFm(0)
            }
            GeneratorMOD.MORPH0, GeneratorMOD.MORPH1, GeneratorMOD.MORPH2 -> {
                val slot = Mod.ordinal - GeneratorMOD.MORPH0.ordinal
                gen.chL.buffer_morph[slot] =
                    byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, -1f, 1f)
                RenderChannel.sendBuffer(0, 4 + slot, gen.chL.buffer_morph[slot])
            }
            else -> {
                gen.chL.buffer_carrier = byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, -1f, 1f)//byteToFloatArrayLittleEndian4096(buf)
                RenderChannel.sendBuffer(0, 0, gen.chL.buffer_carrier)
            }

        }
    } else {
        when (Mod) {
            GeneratorMOD.AM -> {
                gen.chR.buffer_am = byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, 0f, 1f) //byteToFloatArrayLittleEndianAM(buf)
                RenderChannel.sendBuffer(1, 1, gen.chR.buffer_am)
            }
            GeneratorMOD.MASTER -> {
                gen.chR.buffer_master = byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, 0f, 1f)
                RenderChannel.sendBuffer(1, 3, gen.chR.buffer_master)
            }
            //ArrayUtils.byteToShortArrayLittleEndian(buf)
            GeneratorMOD.FM -> {
                gen.chR.buffer_fm = byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, -1f, 1f)
                gen.updateFm(1)
            }
            GeneratorMOD.MORPH0, GeneratorMOD.MORPH1, GeneratorMOD.MORPH2 -> {
                val slot = Mod.ordinal - GeneratorMOD.MORPH0.ordinal
                gen.chR.buffer_morph[slot] =
                    byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, -1f, 1f)
                RenderChannel.sendBuffer(1, 4 + slot, gen.chR.buffer_morph[slot])
            }
            else -> {gen.chR.buffer_carrier = byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, -1f, 1f)//byteToFloatArrayLittleEndian4096(buf)
                RenderChannel.sendBuffer(1, 0, gen.chR.buffer_carrier)
            }
        }
    }


}


private fun byteToFloatArrayLittleEndianMap(bytes: ByteArray, inMin : Float, inMax : Float, outMin : Float, outMax : Float): FloatArray {

    val shorts = ShortArray(bytes.size / 2)
    ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()[shorts]

    val floats = FloatArray(shorts.size)

    floats.forEachIndexed { index, _ ->
        floats[index] = maping( shorts[index].toFloat() , inMin, inMax, outMin, outMax)
    }

    return floats
}

fun byteToFloatArrayLittleEndianAM(bytes: ByteArray): FloatArray {

    val shorts = ShortArray(bytes.size / 2)
    ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()[shorts]

    val floats = FloatArray(shorts.size)

    floats.forEachIndexed { index, _ ->
        floats[index] = shorts[index]  / 4096.0F
    }

    return floats
}