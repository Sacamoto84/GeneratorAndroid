package com.example.generator2.features.mp3.processor

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.audio.AudioProcessor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder

@androidx.media3.common.util.UnstableApi
val audioProcessorInputFormat = MutableStateFlow(
    AudioProcessor.AudioFormat(
        /* sampleRate= */ Format.NO_VALUE,
        /* channelCount= */ Format.NO_VALUE,
        /* encoding= */ Format.NO_VALUE
    )
)

@androidx.media3.common.util.UnstableApi
class MyAudioProcessor(private var isPlayingD: MutableStateFlow<Boolean>, private val streamOut: Channel<FloatArray>) : AudioProcessor {

    private lateinit var inputAudioFormat: AudioProcessor.AudioFormat
    private var isActive: Boolean = false
    private var inputEnded: Boolean = false

    private var outputBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var processBuffer = AudioProcessor.EMPTY_BUFFER

    //Настраиваем выходной формат на Float
    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        Timber.e("configure")
        println("Audio Processor: $inputAudioFormat")
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }
        this.inputAudioFormat = inputAudioFormat
        isActive = true
        audioProcessorInputFormat.value = inputAudioFormat
        return AudioProcessor.AudioFormat(inputAudioFormat.sampleRate, 2, C.ENCODING_PCM_16BIT)
    }

    //Возвращает, настроен ли процессор и будет ли он обрабатывать входные буферы.
    override fun isActive(): Boolean {
        return isActive
    }


    override fun queueInput(inputBuffer: ByteBuffer) {

        var position = inputBuffer.position()
        val limit = inputBuffer.limit()
        var size = limit - position

        val channelCount = inputAudioFormat.channelCount
        if (channelCount == 1) size *= 2

        if (processBuffer.capacity() < size)
            processBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
        else
            processBuffer.clear()

        val buf = FloatArray(size / 2)

        var index = 0

        while (position < limit) {

            if (channelCount == 2) {
                for (channelIndex in 0 until channelCount) { //current = inputBuffer.getShort(position + 2 * channelIndex)
                    val current: Float = if (channelIndex == 0) inputBuffer.getShort(position)
                        .toFloat() else inputBuffer.getShort(position + 2).toFloat()
                    //processBuffer.putShort(current)
                    processBuffer.putShort(0)
                    buf[index] = current / Short.MAX_VALUE
                    index++
                }
                position += channelCount * 2
            }

            if (channelCount == 1) {
                val currentL: Float = inputBuffer.getShort(position).toFloat()
                val currentR: Float = inputBuffer.getShort(position).toFloat()
                //processBuffer.putShort(currentR)
                //processBuffer.putShort(currentL)
                processBuffer.putShort(0)
                processBuffer.putShort(0)
                position += 2
                buf[index] = currentR / Short.MAX_VALUE
                index++
                buf[index] = currentL / Short.MAX_VALUE
                index++
            }

        }

        inputBuffer.position(limit)

        processBuffer.flip()
        outputBuffer = this.processBuffer

        if (buf.isNotEmpty()) {
            val s = streamOut.trySend(buf).isSuccess
            if (!s) Timber.e("Места в канале из процессора нет")
        }

    }


    override fun queueEndOfStream() {
        isPlayingD.value = false
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