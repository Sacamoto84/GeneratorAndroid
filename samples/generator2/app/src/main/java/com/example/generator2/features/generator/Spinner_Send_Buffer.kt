package com.example.generator2.features.generator

import com.example.generator2.util.ArrayUtils
import com.example.generator2.util.maping
import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class GeneratorCH { CH0, CH1 }

enum class GeneratorMOD { CR, AM, FM }

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


    val index = if (Mod == GeneratorMOD.CR)
        gen.itemlistCarrier.indexOfFirst { it.name == name }
    else
        gen.itemlistAM.indexOfFirst { it.name == name }

    if (index == -1) {
        return
    }

    val buf = if (Mod == GeneratorMOD.CR)
        gen.itemlistCarrier[index].buf
    else
        gen.itemlistAM[index].buf

    if (CH == GeneratorCH.CH0) {
        when (Mod) {
            GeneratorMOD.AM -> {
                gen.ch1.buffer_am =  byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, 0f, 1f)
                RenderChannel().sendBuffer(0, 1, gen.ch1.buffer_am)
            }
            GeneratorMOD.FM -> {
                gen.ch1.buffer_fm =  byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, -1f, 1f)//byteToFloatArrayLittleEndian4096(buf)
                gen.createFm(0)
                RenderChannel().sendBuffer(0, 2, gen.ch1.calculate_buffer_fm)
            }
            else -> {
                gen.ch1.buffer_carrier = byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, -1f, 1f)//byteToFloatArrayLittleEndian4096(buf)
                RenderChannel().sendBuffer(0, 0, gen.ch1.buffer_carrier)
            }

        }
    } else {
        when (Mod) {
            GeneratorMOD.AM -> {
                gen.ch2.buffer_am = byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, 0f, 1f) //byteToFloatArrayLittleEndianAM(buf)
                RenderChannel().sendBuffer(1, 1, gen.ch2.buffer_am)
            }
            //ArrayUtils.byteToShortArrayLittleEndian(buf)
            GeneratorMOD.FM -> {
                gen.ch2.buffer_fm = byteToFloatArrayLittleEndian4096(buf)
                RenderChannel().sendBuffer(1, 2, gen.ch2.calculate_buffer_fm)
            }
            else -> {gen.ch2.buffer_carrier = byteToFloatArrayLittleEndianMap(buf, 0f, 4095f, -1f, 1f)//byteToFloatArrayLittleEndian4096(buf)
                RenderChannel().sendBuffer(1, 0, gen.ch2.buffer_carrier)
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

private fun byteToFloatArrayLittleEndian4096(bytes: ByteArray): FloatArray {

    val shorts = ShortArray(bytes.size / 2)
    ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()[shorts]

    val floats = FloatArray(shorts.size)

    floats.forEachIndexed { index, _ ->
        floats[index] = (shorts[index] - 2048.0F) / 2048.0F
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