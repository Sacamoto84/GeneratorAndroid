package com.example.generator2.features.generator

import com.example.generator2.util.ArrayUtils

enum class GeneratorCH{ CH0, CH1 }

enum class GeneratorMOD{ CR, AM, FM }

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
            GeneratorMOD.AM -> gen.ch1.buffer_am = ArrayUtils.byteToShortArrayLittleEndian(buf)
            GeneratorMOD.FM -> gen.ch1.buffer_fm = ArrayUtils.byteToShortArrayLittleEndian(buf)
            else -> gen.ch1.buffer_carrier = ArrayUtils.byteToShortArrayLittleEndian(buf)
        }
    } else {
        when (Mod) {
            GeneratorMOD.AM -> gen.ch2.buffer_am = ArrayUtils.byteToShortArrayLittleEndian(buf)
            GeneratorMOD.FM -> gen.ch2.buffer_fm = ArrayUtils.byteToShortArrayLittleEndian(buf)
            else -> gen.ch2.buffer_carrier = ArrayUtils.byteToShortArrayLittleEndian(buf)
        }
    }



}