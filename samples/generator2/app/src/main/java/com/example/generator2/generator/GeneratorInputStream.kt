package com.example.generator2.generator

import android.media.AudioFormat
import c.ponom.audiuostreams.audiostreams.AudioInputStream


@Suppress("ConvertSecondaryConstructorToPrimary")
class GeneratorInputStream (sampleRate : Int = 48000): AudioInputStream() {

    private var closed: Boolean = false

    init {

        this.sampleRate = sampleRate
        this.encoding = AudioFormat.ENCODING_PCM_16BIT
        this.channelsCount = 2

        this.frameSize = bytesPerSample * channelsCount
        this.channelConfig = channelConfig

        bytesPerSample = 4
        frameSize = bytesPerSample * channelsCount
    }

    @Throws(NullPointerException::class)
    override fun readShorts(b: ShortArray, off: Int, len: Int): Int {
        if (closed) return -1
        if (off < 0 || len < 0 || len > b.size - off) throw IndexOutOfBoundsException("Wrong read(...) params")
        if (len == 0) return 0
        if (off != 0) throw IllegalArgumentException("Non zero offset currently not implemented")
        val length = b.size.coerceAtMost(len)

        val dataArray = ShortArray(length)


        val s = renderAudio(len)

        s.forEachIndexed { i, v ->
            dataArray[i] = (v * 32000F).toInt().toShort()
        }

        dataArray.copyInto(b)

        //channelDataStreamOutGenerator.trySend(dataArray)

        bytesRead += length * 2
        return len
    }

    override fun readShorts(b: ShortArray): Int {
        if (closed) return -1
        return if (b.isEmpty()) 0 else readShorts(b, 0, b.size)
    }

    override fun canReadShorts(): Boolean = true

    override fun close() {
        closed = true
    }

    override fun read(): Int {
        TODO("Not yet implemented")
    }

    override fun read(b: ByteArray?, off: Int, len: Int): Int {
        TODO("Not yet implemented")
    }


}