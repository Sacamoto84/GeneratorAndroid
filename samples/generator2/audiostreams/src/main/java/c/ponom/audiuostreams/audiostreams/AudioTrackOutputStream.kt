


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
class AudioTrackOutputStream private constructor() : AudioOutputStream(){

    private var currentVolume: Float=1f

    /**
     * @author Sergey Ponomarev, 2022, 461300@mail.ru
     * MIT licence
     * After initialisation, internal AudioRecord object can be accessed for low-level control of
     * microphone recording. <BR>
     * See <code>registerAudioRecordingCallback(), setPreferredDevice(), setRecordPositionUpdateListener()</code>
     *
     */
    var audioOut:AudioTrack?=null
    var closed = false
    private set

    /**
     * Class constructor.
     * @param sampleRateInHz the sample rate expressed in Hertz. 44100Hz is the only
     *   rate which is guaranteed to work on all devices, but other rates such as 22050,
     *   16000, and 11025 should work on most devices.
     * @param channels describes the number of the audio channels. Must be equal 1 or 2.
     *   Mono recording is guaranteed to work on all devices.
     *   Only AudioFormat.ENCODING_PCM_16BIT currently supported.
     * @param minBufferMs the minimal size (in ms) of the buffer where audio data is written
     *   to during the recording. New audio data can be written to this buffer in smaller chunks
     *   than this size.
     *
    * @throws UnsupportedOperationException if the parameters were incompatible,
     * or if they are not supported by the device, or if the device was not available
     * @throws IllegalArgumentException if the channels parameter is not equal to 1 or 2
    **/

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
        //encoding = encoding
        channelsCount=channels
        bytesPerSample = 2
        frameSize =bytesPerSample*channelsCount
        val minBufferInBytes=frameSize*(sampleRate/1000)*(minBufferMs/1000.0).toInt()

        val audioFormat= Builder()
            .setEncoding(encoding)
            .setSampleRate(sampleRate)
            .setChannelMask(channelConfig)
            .build()
        val minBuffer = getMinBufferSize(sampleRate, channelConfig, encoding)
        Log.d(TAG, "AUDIO TRACK: MIN.BUFFER.SIZE=$minBuffer bytes")
        audioOut=AudioTrack.Builder()
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(minBuffer.coerceAtLeast(minBufferInBytes))
            .setTransferMode(MODE_STREAM)
            .build()

    }


    /**
     * Starts playing an AudioTrack.
     * You can optionally prime the data path prior to calling play(), by writing data to buffer
     * If you don't call write() first, or if you call write() with an insufficient amount of
     * data, then the track will be in underrun state at play().  In this case,
     * playback will not actually start playing until the data path is filled to a
     * device-specific minimum level.  This requirement for the path to be filled
     * to a minimum level is also true when resuming audio playback after calling stop().
     * Similarly, the buffer will need to be filled up again after
     * the track underruns due to failure to call write() in a timely manner with sufficient data.
     * This allows play() to start immediately, and reduces the chance of underrun.
     *
     * @throws IllegalStateException if the track isn't properly initialized or was already closed
     */
    @Throws(IOException::class)
    fun play(){
        if (closed||audioOut == null||audioOut?.state != STATE_INITIALIZED)
            throw IOException("Stream closed or in error state")
        audioOut?.play()
    }

    /**
     * Stops playing the audio data, discarding audio data that hasn't been played
     * back yet.
     */
    fun stopAndClear(){
        if (closed||audioOut == null||audioOut?.playState!= PLAYSTATE_STOPPED) return
        try {
        audioOut?.setVolume(0.02f)
            CoroutineScope(IO).launch {
                delay(90) // shorter delays can give audible click
                audioOut?.pause()
                audioOut?.flush()
                audioOut?.stop()
                audioOut?.setVolume(currentVolume)
            }
        } catch (e:IllegalStateException){
            e.printStackTrace()
        }
    }

    /**
     * Stops playing the audio data, without discarding audio data that hasn't been played
     * back yet.
     */
    fun stop(){
        if (closed||audioOut == null||audioOut?.playState!= PLAYSTATE_STOPPED) return
        try {
            audioOut?.stop()
        } catch (e:IllegalStateException){
            e.printStackTrace()
        }
    }

    /**
    * Pauses the playback of the audio data. Data that have not been played
    * back will not be discarded. Subsequent calls to play() will play
    * this data back. See flush() to discard this data.
    *
    */
    fun pause (){
        if (closed||audioOut == null||audioOut?.playState!= PLAYSTATE_PLAYING) return
        try {
            audioOut?.setVolume(0.02f)
            CoroutineScope(IO).launch {
                delay(90) // shorter delays can give audible click
                audioOut?.pause()
                audioOut?.setVolume(currentVolume)
            }
        } catch (e:IllegalStateException){
            e.printStackTrace()
        }
    }


    /**
     * Resume playing an AudioTrack if in was paused before
     *
     * @throws IllegalStateException if the track isn't properly initialized or was already closed
     */
    fun resume() {
        if (closed||audioOut == null||audioOut?.playState!= PLAYSTATE_PAUSED) return
        try {
             audioOut?.play()
        } catch (e:IllegalStateException){
            e.printStackTrace()
        }
    }

    /**
     * Sets the specified output gain value on all channels of this track.
     * A value of 0.0 results in zero gains (silence), and
     * a value of 1.0 means unity gains (signal unchanged).
     * Gain values are clamped to the closed interval [0.0, max] where max is the value of
     * audioOut.getMaxVolume() if it was set previously
     *
     * The default value is 1.0 meaning unity gain.
     * @param vol output gain for all channels.
     */
    override fun setVolume(vol: Float) {
       currentVolume =vol
       audioOut?.setVolume(vol.coerceAtLeast(0f).coerceAtMost(1f))
    }

    /**
     *Writes the audio data to the audio sink for playback (streaming mode), or copies audio
     * data for later playback.
     * The write() will normally block until all the data have been enqueued for playback.
     * @param b the byte array that holds the data to play.
     * @param off the offset expressed in bytes in b where the data to write starts.
     *    Must not be negative, or cause the data access to go out of bounds of the array.
     * @param len the number of bytes to write in audioData after the offset.
     *    Must not be negative, or cause the data access to go out of bounds of the array.
     * @throws IOException if the track isn't properly initialized, or he AudioTrack is not valid
     * anymore and needs to be recreated
     * @throws IllegalArgumentException if the parameters don't resolve to valid data and indexes
     * @throws NullPointerException if a null array passed
     */

    @Throws(IOException::class,NullPointerException::class,IllegalArgumentException::class)
    override fun write(b: ByteArray?, off: Int, len: Int){
        // should add evenness checks for all params in all bytes writes converted to short
        if (audioOut == null||closed) throw IOException("Stream closed or in error state")
        if (b == null) throw NullPointerException ("Null array passed")
        if (off < 0 || len < 0 || len > b.size - off)
            throw IllegalArgumentException("Wrong write(....) parameters")
        val samplesShorts = ArrayUtils.byteToShortArrayLittleEndian(b)
        val result:Int = audioOut!!.write(samplesShorts, off/2, len/2)
        bytesSent += result.coerceAtLeast(0)
        if (result<0){
            audioOut?.release()
            audioOut=null
            closed=true
            throw IOException ("Error code $result - see codes for AudioTrack write(...)")
        }
    }


    /**
     *Writes the audio data to the audio sink for playback, or copies audio
     * data for later playback calling writeShorts(b,0,b.size)
     *
     * In streaming mode the write(...) will normally block until all the data have been enqueued
     * for playback.
     * @param b the short array that holds the data to play.
     * @throws IOException if the track isn't properly initialized, or the AudioTrack is not valid
     * anymore and needs to be recreated
    */
    @Throws(IOException::class,NullPointerException::class,IllegalArgumentException::class)
    override fun writeShorts(b: ShortArray) {
        writeShorts(b,0,b.size)
    }

    /**
     * True if writeShorts(b: ShortArray) and writeShorts(b: ShortArray, off: Int, len: Int)
     * methods supported by class.
     * @return true for AudioTrackOutputStream
     */
    override fun canWriteShorts(): Boolean = true

    /**
     *Writes the audio data to the audio sink for playback (streaming mode), or copies audio
     * data for later playback.
     * In streaming mode the write(...) will normally block until all the data have been enqueued
     * for playback.
     * @param b the array that holds the data to play.
     * @param off the offset in b where the data to write starts.
     * @param len the number of samples to write in b after the offset.
     * @throws IOException if the track isn't properly initialized, or the AudioTrack is not valid
     * anymore and needs to be recreated
     * @throws IllegalArgumentException if the parameters don't resolve to valid data and indexes
     */
    @Throws(IllegalArgumentException::class,IOException::class)
    override fun writeShorts(b: ShortArray, off: Int, len: Int) {
        if (audioOut == null||closed) throw IOException("Stream closed or in error state")
        val size=b.size
        if (off > len ||len>size||off>size||off<0||len<0)
            throw IllegalArgumentException("Wrong write(....) parameters")
        val result = audioOut!!.write(b, off, len, WRITE_BLOCKING)
        bytesSent += result.coerceAtLeast(0)*2
        if (result<0){
            audioOut?.release()
            audioOut=null
            closed=true
            throw IOException ("Error code $result - see codes for AudioTrack write(...)")
        }
   }


    /**
     * Stops playback and closes this input stream and releases any system resources associated.
     * write(...) calls are no longer valid after this call and will throw exception
     *  Do nothing if the stream already closed
     */
    override fun close() {
        stopAndClear()
        audioOut?.release()
        audioOut=null
        closed=true
    }

    /**
     * Not implemented for class
     */
    override fun write(b: Int) {
        throw NotImplementedError ("Not implemented, use write(...), writeShorts(...)")
    }

    /**
     * Returns the effective size of the AudioTrack buffer that the application writes to.
     * @return current mic buffer size in bytes.
     * Always check value before setting own buffers size. Zero buffer size means that the
     * device didn't initialise properly or stream is already closed
     */
    fun currentBufferSize(): Int {
        if (audioOut==null) return 0
        return try {
            audioOut?.bufferSizeInFrames!!.times(frameSize)
        } catch (e:IllegalStateException){
            0
        }
    }

    /* Static and async API for class */
    companion object {

        /**
         * Async creation of AudioTrack audio stream.
         *
         * @param sampleRateInHz the sample rate expressed in Hertz. 44100Hz is currently the only
         *   rate which is guaranteed to work on all devices, but other rates such as 22050,
         *   16000, and 11025 should work on most devices.
         * @param channels describes the number of the audio channels. Must be equal 1 or 2.
         *   Mono recording is guaranteed to work on all devices.
         *   Only AudioFormat.ENCODING_PCM_16BIT currently supported.
         * @param minBufferMs the minimal size (in ms) of the buffer where audio data is written
         *   to during the recording. New audio data can be written to this buffer in smaller chunks
         *   than this size.
         * @return  the Result&lt;AudioTrackOutputStream&gt; object containing created stream or
         * Throwable
         *
         */
        @JvmStatic
        fun createChannelAsync(
            sampleRateInHz: Int,
            channels: Int,
            minBufferMs: Int = 0
        ): Deferred<Result<AudioTrackOutputStream>> = CoroutineScope(IO).async{
            runCatching{AudioTrackOutputStream (sampleRateInHz, channels,minBufferMs)  }}

    }

}