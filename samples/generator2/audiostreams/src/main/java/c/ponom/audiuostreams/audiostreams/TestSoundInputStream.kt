

@file:Suppress("unused")
package c.ponom.audiuostreams.audiostreams

import android.media.AudioFormat.*
import android.util.Log
import androidx.annotation.IntRange
import c.ponom.audiuostreams.audiostreams.ArrayUtils.shortToByteArrayLittleEndian
import c.ponom.audiuostreams.audiostreams.TestSoundInputStream.TestSignalType.MONO
import c.ponom.audiuostreams.audiostreams.TestSoundInputStream.TestSignalType.STEREO
import c.ponom.audiuostreams.audiostreams.TestSoundInputStream.TestStreamMode.FILE
import c.ponom.audiuostreams.audiostreams.TestSoundInputStream.TestStreamMode.GENERATOR
import java.io.IOException
import kotlin.math.PI
import kotlin.math.sin


/**
 * @author Sergey Ponomarev,2022, 461300@mail.ru
 * MIT licence
 */
@Suppress("ConvertSecondaryConstructorToPrimary")
class TestSoundInputStream private constructor() : AudioInputStream()  {
    private var testChannelsMode: TestSignalType =MONO
    private var closed: Boolean=false
    private lateinit var monoParams: MonoSoundParameters
    private lateinit var stereoParams: StereoSoundParameters
    private  var testMode = GENERATOR
    private var fileLen=0L


    // todo realise Builder





    /**
     * This constructor is usable only for CHANNEL_IN_MONO and encoding ENCODING_PCM_16BIT.
     * Only 16 bit encoding currently supported <BR>
     *Test frequency below 32 or above 16000 Hz can be inaudible. Non-standard sampling rates below
     * 16000 or over 48000 can be problematic for testing of media encoders or players
     */
    @JvmOverloads
    @Throws(IllegalArgumentException::class,IOException::class)
    constructor (
        testFrequencyMono: Double, volume: Short,
        @IntRange(from = 8000, to= 48000 )sampleRate: Int,
        @IntRange(from = 1, to= 16)channelConfig: Int,
        @IntRange(from = 1, to= 2) encoding: Int = ENCODING_PCM_16BIT,
        mode: TestStreamMode = GENERATOR //todo
    ) : this() {
        if (channelConfig!= CHANNEL_IN_MONO) // todo заменить как в AudioOut, 1 и 2 канала
            throw IllegalArgumentException("This constructor usable only for CHANNEL_IN_MONO")
        if (encoding!=ENCODING_PCM_16BIT)
            throw IllegalArgumentException("Only PCM 16 bit encoding currently supported")
        // кинуть предупреждение если частоты в неслышимом диапазоне
        if (testFrequencyMono<32||testFrequencyMono>16000||testFrequencyMono>sampleRate/2)
            Log.v(TAG, "Test frequency = $testFrequencyMono Hz, probably inaudible")
        if (sampleRate<16000||sampleRate>48000)
            Log.v(TAG, "Non standard sampling rate of $sampleRate can by problematic for testing" +
                    "of media encoders or players")
        testMode=mode
        this.sampleRate = sampleRate
        this.encoding=encoding
        this.channelsCount = 1
        this.frameSize=bytesPerSample*channelsCount
        this.channelConfig=channelConfig
        monoParams=MonoSoundParameters(volume,testFrequencyMono)
        this.testChannelsMode= MONO
        bytesPerSample = if (encoding== ENCODING_PCM_16BIT) 2 else  1
        frameSize=bytesPerSample*channelsCount
    }


    /**
     *This constructor is usable only for CHANNEL_IN_STEREO and encoding ENCODING_PCM_16BIT.
     * Only 16 bit encoding currently supported <BR>
     *Test frequency below 32 or above 16000 Hz can be inaudible. Non-standard sampling rates below 16000 or over 48000 can be problematic for testing of media encoders or players
     */
    @JvmOverloads
    @Throws(IllegalArgumentException::class, IOException::class)
    constructor (
        testFrequencyLeft: Double, testFrequencyRight: Double,
        volumeLeft: Short, volumeRight: Short,
        @IntRange(from = 8000, to= 48000 )sampleRate: Int,
        @IntRange(from = 12, to= 12)channelConfig: Int,
        @IntRange(from = 1, to= 2) encoding: Int= ENCODING_PCM_16BIT,
        mode: TestStreamMode = GENERATOR
    ) : this() {
        if (channelConfig!= CHANNEL_IN_STEREO)
            throw IllegalArgumentException("This constructor usable only for CHANNEL_IN_STEREO")
        if (encoding!=ENCODING_PCM_16BIT)
            throw IllegalArgumentException("Only PCM 16 bit encoding currently supported")
        if (testFrequencyLeft<32||testFrequencyLeft>16000||testFrequencyLeft>sampleRate/2)
            Log.v(TAG, "Test frequency L = $testFrequencyLeft Hz, probably inaudible")
        if (testFrequencyRight<32||testFrequencyRight>16000||testFrequencyRight>sampleRate/2)
            Log.v(TAG, "Test frequency R = $testFrequencyRight Hz, probably inaudible")
        if (sampleRate<8000||sampleRate>48000)
            Log.v(TAG, "Non standard sampling rate of $sampleRate can by problematic for testing" +
                    "on most media encoders and devices")
        this.sampleRate = sampleRate
        stereoParams=StereoSoundParameters(testFrequencyLeft,testFrequencyRight,volumeLeft,volumeRight)
        testMode=mode
        testChannelsMode=STEREO
        this.sampleRate = sampleRate
        this.encoding=encoding
        this.channelsCount = 2
        this.channelConfig=channelConfig
        this.frameSize=bytesPerSample*channelsCount
        bytesPerSample = if (encoding== ENCODING_PCM_16BIT) 2 else  1

    }




   /**
    * Return -1 when there is no estimated stream length (for example, for endless streams)
    * or estimated rest of bytes in stream
    */
   override fun totalBytesEstimate(): Long {
       //todo -1 для генератора и устройства, фиксированное и заданное для файла(отдельный
       // конструктор)
       return if (testMode==FILE) fileLen
       else -1L

   }

    /**
     * Return -1 when there is no estimated stream length (for example, for endless streams)
     * or estimated rest of bytes in stream
     */
   override fun bytesRemainingEstimate(): Long {
        return if (testMode==FILE) fileLen-bytesRead
        else -1L
   }


   override fun available(): Int {
       return 1024*16
   }


    override fun read(): Int {
        val b=ByteArray(1)
        if (closed) return -1
        return read(b)+128
    }


    @Throws(NullPointerException::class)
    override fun read(b: ByteArray?): Int {
        if (b==null) throw NullPointerException ("Null array passed")
        if (closed) return -1
        return read(b,0,b.size)
    }


    /**
     *
     */

    @Throws(NullPointerException::class,IllegalArgumentException::class)
    override fun read(b: ByteArray?, off: Int, len: Int): Int {
        if (b == null) throw NullPointerException("Null array passed")
        if (off < 0 || len < 0 || len > b.size - off)
            throw IndexOutOfBoundsException("Wrong read(...) params")
        if (len == 0) return 0
        if (off != 0) throw IllegalArgumentException("Non zero offset currently not implemented")
        if (closed) return -1
        val shortArray=ShortArray((len / 2).coerceAtMost(b.size / 2))
        val bytes= readShorts(shortArray,0,len/2)*2
        shortToByteArrayLittleEndian(shortArray).copyInto(b)
        bytesRead+=bytes
        onReadCallback?.invoke(bytesRead)
        return bytes
   }




    @Throws(NullPointerException::class)
    override fun readShorts(b: ShortArray, off: Int, len: Int): Int {
        if (closed) return -1
        if (off < 0 || len < 0 || len > b.size - off) throw IndexOutOfBoundsException("Wrong read(...) params")
        if (len == 0) return 0
        if (off != 0) throw IllegalArgumentException("Non zero offset currently not implemented")
        val length = b.size.coerceAtMost(len)
        val dataArray=ShortArray(length)
        if (testChannelsMode== MONO) dataArray.forEachIndexed { index, _ ->
            dataArray[index] = calculateSampleValueMono(index.toLong())
        }
        if (testChannelsMode== STEREO) {
            for (index in 0..dataArray.size-2 step 2 ){
            val samplesPair= calculateSampleValueStereo(index.toLong())
            dataArray[index] =samplesPair.first
            dataArray[index+1] =samplesPair.second
            }
        }
        dataArray.copyInto(b)
        bytesRead+=length*2
        return len
    }

    override fun skip(n: Long): Long {
        return read(ByteArray(n.toInt())).toLong()
    }



    override fun readShorts(b: ShortArray): Int {
        if (closed) return -1
        return if (b.isEmpty()) 0 else readShorts(b, 0, b.size)
    }

    override fun canReadShorts():Boolean =true

    override fun close() {
       closed=true
    }


    private fun calculateSampleValueMono(sampleNum:Long):Short{
        val x =(sampleNum/monoParams.samplesInPeriodMono*2*PI)
        return (sin(x)*monoParams.volumeMono).toInt().toShort()
    }

    private fun calculateSampleValueStereo(sampleNum:Long):Pair<Short,Short>{
        val xLeft =(sampleNum/stereoParams.samplesInPeriodLeft*2*PI)
        val xRight =((sampleNum+1)/stereoParams.samplesInPeriodRight*2*PI)
        val left =(sin(xLeft)*stereoParams.volumeLeft).toInt().toShort()
        val right =(sin(xRight)*stereoParams.volumeRight).toInt().toShort()
        return Pair(left,right)
    }

    enum class TestStreamMode{
        GENERATOR,
        FILE, //todo
        DEVICE //todo
    }


    enum class TestSignalType{
        MONO,
        STEREO;
    }

    inner class MonoSoundParameters {
        var samplesInPeriodMono: Double
        private var periodDurationMono: Double
        val volumeMono: Short
        private val testFrequencyMono: Double
        constructor(volumeMono: Short, testFrequencyMono: Double) {
            this.volumeMono = volumeMono
            this.testFrequencyMono = testFrequencyMono
            periodDurationMono=1.0/testFrequencyMono
            samplesInPeriodMono=sampleRate.toDouble()/testFrequencyMono
        }
    }

    inner  class  StereoSoundParameters {
        private var periodDurationRight: Double
        var samplesInPeriodRight: Double
        var samplesInPeriodLeft: Double
        private var periodDurationLeft: Double
        private var testFrequencyLeft: Double
        private var testFrequencyRight: Double
        var volumeLeft: Short
        var volumeRight: Short

        constructor(
            testFrequencyLeft: Double,
            testFrequencyRight: Double,
            volumeLeft: Short,
            volumeRight: Short
        ) {
            this.testFrequencyLeft = testFrequencyLeft
            this.testFrequencyRight = testFrequencyRight
            this.volumeLeft = volumeLeft
            this.volumeRight = volumeRight
            periodDurationLeft=1.0/testFrequencyLeft
            samplesInPeriodLeft=sampleRate.toDouble()/testFrequencyLeft*2
            periodDurationRight = 1.0 / testFrequencyRight
            samplesInPeriodRight=sampleRate.toDouble()/testFrequencyRight*2

        }
    }

}


