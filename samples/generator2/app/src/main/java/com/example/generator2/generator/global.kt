package com.example.generator2.generator

import android.media.AudioFormat
import c.ponom.audiuostreams.audiostreams.AudioTrackOutputStream




val ch1 : StructureCh = StructureCh(ch = 0)
val ch2 : StructureCh = StructureCh(ch = 1)

data class StructureCh  (

    var ch: Int = 0, //Номер канала 0 1

    //Буфферы
    var buffer_carrier: ShortArray = ShortArray(1024),
    var buffer_am: ShortArray = ShortArray(1024),
    var buffer_fm: ShortArray = ShortArray(1024),

    var source_buffer_fm: ShortArray = ShortArray(1024), //Используется для перерасчета модуляции

    //Аккумуляторы
    var phase_accumulator_carrier: UInt = 0u,
    var phase_accumulator_am: UInt = 0u,
    var phase_accumulator_fm: UInt = 0u,

    //var mBuffer: FloatArray = FloatArray(4096),

)

