package c.ponom.audiuostreams.audiostreams

import android.media.AudioFormat.*
import android.media.AudioFormat.Builder
import android.media.AudioTrack
import android.media.AudioTrack.*
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.io.IOException


@Suppress("MemberVisibilityCanBePrivate", "unused")
class AudioTrackToMemory private constructor() : AudioOutputStream(){

    private var currentVolume: Float=1f

    @JvmOverloads
    @Throws(IllegalArgumentException::class, UnsupportedOperationException::class)
    constructor(
        sampleRateInHz: Int,
        channels: Int,
        minBufferMs: Int = 0,
        encoding: Int = ENCODING_PCM_16BIT

    ) : this() {

        sampleRate = sampleRateInHz
        channelConfig=channelConfig(channels)

        if (!(channelConfig== CHANNEL_OUT_MONO ||channelConfig== CHANNEL_OUT_STEREO))
            throw IllegalArgumentException("Only 1 or 2 channels(CHANNEL_OUT_MONO " +
                    "and CHANNEL_OUT_STEREO) supported")

    }

    @Throws(IOException::class,NullPointerException::class,IllegalArgumentException::class)
    override fun write(b: ByteArray?, off: Int, len: Int){

    }

    @Throws(IOException::class,NullPointerException::class,IllegalArgumentException::class)
    override fun writeShorts(b: ShortArray) {
        writeShorts(b,0,b.size)
    }

    override fun canWriteShorts(): Boolean = true


    @Throws(IllegalArgumentException::class,IOException::class)
    override fun writeShorts(b: ShortArray, off: Int, len: Int) {

//        if (audioOut == null||closed) throw IOException("Stream closed or in error state")
//
//        val size=b.size
//        if (off > len ||len>size||off>size||off<0||len<0)
//            throw IllegalArgumentException("Wrong write(....) parameters")
//        val result = audioOut!!.write(b, off, len, WRITE_BLOCKING)
//
//        bytesSent += result.coerceAtLeast(0)*2
//        if (result<0){
//            audioOut?.release()
//            audioOut=null
//            closed=true
//            throw IOException ("Error code $result - see codes for AudioTrack write(...)")
//        }


    }


    /**
     * Stops playback and closes this input stream and releases any system resources associated.
     * write(...) calls are no longer valid after this call and will throw exception
     *  Do nothing if the stream already closed
     */
    override fun close() {

    }

    /**
     * Not implemented for class
     */
    override fun write(b: Int) {
        throw NotImplementedError ("Not implemented, use write(...), writeShorts(...)")
    }





}