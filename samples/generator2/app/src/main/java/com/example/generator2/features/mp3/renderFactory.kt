package com.example.generator2.features.mp3

import android.content.Context
import android.os.Handler
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioCapabilities
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import com.example.generator2.features.mp3.processor.MyAudioProcessor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow

@androidx.media3.common.util.UnstableApi
fun renderersFactory(context: Context,
                     isPlayingD: MutableStateFlow<Boolean>,
                     streamOut: Channel<FloatArray>

): DefaultRenderersFactory {
    return object : DefaultRenderersFactory(context) {

        override fun buildAudioRenderers(
            context: Context,
            extensionRendererMode: Int,
            mediaCodecSelector: MediaCodecSelector,
            enableDecoderFallback: Boolean,
            audioSink: AudioSink,
            eventHandler: Handler,
            eventListener: AudioRendererEventListener,
            out: ArrayList<Renderer>
        ) {

            val sonicAudioProcessor = MyAudioProcessor(isPlayingD, streamOut)//SonicAudioProcessor()

            val myAudioSink: AudioSink = DefaultAudioSink.Builder(context)
                .setAudioCapabilities(AudioCapabilities.getCapabilities(context))
                .setAudioProcessors(arrayOf(sonicAudioProcessor))
                .build()

            out.add(
                MediaCodecAudioRenderer(
                    context,
                    mediaCodecSelector,
                    enableDecoderFallback,
                    eventHandler,
                    eventListener,
                    myAudioSink
                )
            )

            super.buildAudioRenderers(
                context,
                extensionRendererMode,
                mediaCodecSelector,
                enableDecoderFallback,
                audioSink,
                eventHandler,
                eventListener,
                out
            )
        }
    }
}

