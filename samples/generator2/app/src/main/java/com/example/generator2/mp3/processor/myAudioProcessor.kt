package com.example.generator2.mp3.processor

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.audio.AudioProcessor
import com.example.generator2.generator.gen
import com.example.generator2.mp3.chDataStreamOutAudioProcessor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder

@androidx.media3.common.util.UnstableApi
val audioProcessorInputFormat = MutableStateFlow(AudioProcessor.AudioFormat(
    /* sampleRate= */ Format.NO_VALUE,
    /* channelCount= */ Format.NO_VALUE,
    /* encoding= */ Format.NO_VALUE)
)


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
        Timber.e("configure")
        println("Audio Processor: $inputAudioFormat")


        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(
                inputAudioFormat
            )
        }

        this.inputAudioFormat = inputAudioFormat
        isActive = true

        audioProcessorInputFormat.value = inputAudioFormat

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


    var buf = ShortArray(1)

    @OptIn(DelicateCoroutinesApi::class)
    override fun queueInput(inputBuffer: ByteBuffer) {

        //println("qI")

        val enl = gen.liveData.enL.value
        val enr = gen.liveData.enR.value

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

        //if (buf.size < size/2)
        buf = ShortArray(size / 2) { 0 }

        var index = 0

        while (position < limit) {

            if (channelCount == 2) {
                for (channelIndex in 0 until channelCount) { //current = inputBuffer.getShort(position + 2 * channelIndex)

                    val current: Short =
                        if (channelIndex == 0) if (enr) inputBuffer.getShort(position) else 0
                        else if (enl) inputBuffer.getShort(position + 2) else 0

                    //processBuffer.putShort(current)

                    processBuffer.putShort(0)

                    buf[index] = current
                    index++
                }
                position += channelCount * 2
            }

            if (channelCount == 1) {
                val currentR: Short = if (enr) inputBuffer.getShort(position) else 0
                val currentL: Short = if (enl) inputBuffer.getShort(position) else 0

                //processBuffer.putShort(currentR)
                //processBuffer.putShort(currentL)

                processBuffer.putShort(0)
                processBuffer.putShort(0)

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

        if (buf.isNotEmpty()) {
            val s = chDataStreamOutAudioProcessor.trySend(buf).isSuccess
            if (!s) Timber.e("Места в канале из процессора нет")
        }

    }


    override fun queueEndOfStream() {
        Timber.e("queueEndOfStream")
        inputEnded = true
        processBuffer = AudioProcessor.EMPTY_BUFFER
    }

    override fun getOutput(): ByteBuffer {
        val outputBuffer = this.outputBuffer
        this.outputBuffer = AudioProcessor.EMPTY_BUFFER
        return outputBuffer
    }

    override fun isEnded(): Boolean {
        //Timber.e("isEnded")
        return inputEnded && processBuffer === AudioProcessor.EMPTY_BUFFER
    }

    override fun flush() {
        Timber.e("flush")
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        inputEnded = false // A new stream is incoming.
    }

    //Сбрасывает процессор в его ненастроенное состояние, освобождая все ресурсы.
    override fun reset() {
        Timber.e("reset")
        flush()
        processBuffer = AudioProcessor.EMPTY_BUFFER
        inputAudioFormat =
            AudioProcessor.AudioFormat(Format.NO_VALUE, Format.NO_VALUE, Format.NO_VALUE)
    }

}