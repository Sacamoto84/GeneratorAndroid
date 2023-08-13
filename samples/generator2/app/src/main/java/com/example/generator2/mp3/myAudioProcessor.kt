package com.example.generator2.mp3

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.audio.AudioProcessor
import com.example.generator2.model.LiveData
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import libs.structure.FIFO
import java.nio.ByteBuffer
import java.nio.ByteOrder

val bufferQueueAudioProcessor: FIFO<ShortArray> = FIFO(4)

@androidx.media3.common.util.UnstableApi
class myAudioProcessor : AudioProcessor {


    companion object {
        const val SAMPLE_SIZE = 4096

        // From DefaultAudioSink.java:160 'MIN_BUFFER_DURATION_US'
        private const val EXO_MIN_BUFFER_DURATION_US: Long = 250000

        // From DefaultAudioSink.java:164 'MAX_BUFFER_DURATION_US'
        private const val EXO_MAX_BUFFER_DURATION_US: Long = 750000

        // From DefaultAudioSink.java:173 'BUFFER_MULTIPLICATION_FACTOR'
        private const val EXO_BUFFER_MULTIPLICATION_FACTOR = 4

        // Extra size next in addition to the AudioTrack buffer size
        private const val BUFFER_EXTRA_SIZE = SAMPLE_SIZE * 8
    }

    private lateinit var inputAudioFormat: AudioProcessor.AudioFormat
    private var isActive: Boolean = false
    private var inputEnded: Boolean = false

    private var outputBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var processBuffer = AudioProcessor.EMPTY_BUFFER


    //inputAudioFormat.sampleRate, inputAudioFormat.channelCount, C.ENCODING_PCM_FLOAT)

    // sampleRate= */ Format.NO_VALUE,
    // channelCount= */ Format.NO_VALUE,
    // encoding= */ Format.NO_VALUE);

    //Настраиваем выходной формат на Float
    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {

        println("Audio Processor: $inputAudioFormat")


        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(
                inputAudioFormat
            )
        }


        this.inputAudioFormat = inputAudioFormat
        isActive = true

        //        return if (inputAudioFormat.encoding != C.ENCODING_PCM_FLOAT) AudioProcessor.AudioFormat(
        //            inputAudioFormat.sampleRate, inputAudioFormat.channelCount, C.ENCODING_PCM_FLOAT
        //        )
        //        else inputAudioFormat

        return AudioProcessor.AudioFormat(
            inputAudioFormat.sampleRate, 2, C.ENCODING_PCM_16BIT
        )
    }

    //Возвращает, настроен ли процессор и будет ли он обрабатывать входные буферы.
    override fun isActive(): Boolean {
        return isActive
    }


    @OptIn(DelicateCoroutinesApi::class)
    override fun queueInput(inputBuffer: ByteBuffer) {

        val enl = LiveData.enL.value
        val enr = LiveData.enR.value

        var position = inputBuffer.position()
        val limit = inputBuffer.limit()
        var size = limit - position

        val channelCount = inputAudioFormat.channelCount

        //val frameCount = (size) / (2 * inputAudioFormat.channelCount)

        if (channelCount == 1) size *= 2

        if (processBuffer.capacity() < size) {
            processBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
        } else {
            processBuffer.clear()
        }

        val buf = ShortArray(size / 2)
        var index = 0

        while (position < limit) {

            if (channelCount == 2) {
                for (channelIndex in 0 until channelCount) { //current = inputBuffer.getShort(position + 2 * channelIndex)

                    val current: Short =
                        if (channelIndex == 0) if (enr) inputBuffer.getShort(position) else 0
                        else if (enl) inputBuffer.getShort(position + 2) else 0

                    processBuffer.putShort(current)

                    buf[index] = current
                    index++
                }
                position += channelCount * 2
            }

            if (channelCount == 1) {
                val currentR: Short = if (enr) inputBuffer.getShort(position) else 0
                val currentL: Short = if (enl) inputBuffer.getShort(position) else 0
                processBuffer.putShort(currentR)
                processBuffer.putShort(currentL)
                position += 2
                buf[index] = currentR
                index++
                buf[index] = currentL
                index++
            }


        }

        inputBuffer.position(limit)

        processBuffer.flip()
        outputBuffer = this.processBuffer

//        val buf = ShortArray()
//
        if (buf.isNotEmpty())
            GlobalScope.launch(Dispatchers.IO) {
                channelDataStreamOutAudioProcessor.send(buf)
            }
            //bufferQueueAudioProcessor.enqueue(buf)

    }


    override fun queueEndOfStream() {
        inputEnded = true
        processBuffer = AudioProcessor.EMPTY_BUFFER
    }

    override fun getOutput(): ByteBuffer {
        val outputBuffer = this.outputBuffer
        this.outputBuffer = AudioProcessor.EMPTY_BUFFER
        return outputBuffer
    }

    override fun isEnded(): Boolean {
        return inputEnded && processBuffer === AudioProcessor.EMPTY_BUFFER
    }

    override fun flush() {
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        inputEnded = false // A new stream is incoming.
    }

    //Сбрасывает процессор в его ненастроенное состояние, освобождая все ресурсы.
    override fun reset() {
        flush()
        processBuffer = AudioProcessor.EMPTY_BUFFER
        inputAudioFormat =
            AudioProcessor.AudioFormat(Format.NO_VALUE, Format.NO_VALUE, Format.NO_VALUE)
    }


    private val FLOAT_NAN_AS_INT = java.lang.Float.floatToIntBits(Float.NaN)
    private val PCM_32_BIT_INT_TO_PCM_32_BIT_FLOAT_FACTOR = 1.0 / 0x7FFFFFFF

    /**
     * Converts the provided 32-bit integer to a 32-bit float value and writes it to `buffer`.
     *
     * @param pcm32BitInt The 32-bit integer value to convert to 32-bit float in [-1.0, 1.0].
     * @param buffer The output buffer.
     */
    private fun writePcm32BitFloat(pcm32BitInt: Int, buffer: ByteBuffer) {
        val pcm32BitFloat = (PCM_32_BIT_INT_TO_PCM_32_BIT_FLOAT_FACTOR * pcm32BitInt).toFloat()
        var floatBits = java.lang.Float.floatToIntBits(pcm32BitFloat)
        if (floatBits == FLOAT_NAN_AS_INT) {
            floatBits = java.lang.Float.floatToIntBits(0.0.toFloat())
        }
        buffer.putInt(floatBits)
    }

}