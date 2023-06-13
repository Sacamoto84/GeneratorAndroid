package com.example.generator2

import android.content.Context
import android.media.AudioManager
import android.util.Log
import timber.log.Timber

interface DataCallback {
    fun onDataReceived(data: FloatArray?)
}

class PlaybackEngine(val context: Context) : DataCallback  {

    private var mEngineHandle: Long = 0

    init {
        Timber.i("PlaybackEngine() init{}")
        System.loadLibrary("app")
    }


    //fun create(context: Context): Boolean {
    fun create(): Boolean {
        Timber.i( "PlaybackEngine -> create()")
        if (mEngineHandle == 0L) {
            setDefaultStreamValues(context)
            mEngineHandle = native_createEngine()
        }
        return mEngineHandle != 0L
    }

    private fun setDefaultStreamValues(context: Context) {
        val myAudioMgr = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val sampleRateStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
        val defaultSampleRate = sampleRateStr.toInt()
        val framesPerBurstStr =
            myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
        var defaultFramesPerBurst = framesPerBurstStr.toInt()

        if (defaultFramesPerBurst < 1152) defaultFramesPerBurst = 1152
        native_setDefaultStreamValues(defaultSampleRate, defaultFramesPerBurst)

        Log.i("!!!", "---PlaybackEngine.setDefaultStreamValues()>>>defaultSampleRate:$defaultSampleRate>>>defaultFramesPerBurst:$defaultFramesPerBurst")

    }

    fun start(): Int {

        Timber.i("PlaybackEngine -> Start()")

        return if (mEngineHandle != 0L) {
            native_startEngine(mEngineHandle)
        } else {
            -1
        }
    }

    fun stop(): Int {
        return if (mEngineHandle != 0L) {
            native_stopEngine(mEngineHandle)
        } else {
            -1
        }
    }

    fun delete() { if (mEngineHandle != 0L) { native_deleteEngine(mEngineHandle) }
        mEngineHandle = 0 }

    fun setAudioApi(audioApi: Int) { if (mEngineHandle != 0L) native_setAudioApi( mEngineHandle, audioApi ) }
    fun setAudioDeviceId(deviceId: Int) { if (mEngineHandle != 0L) native_setAudioDeviceId( mEngineHandle, deviceId ) }

    //Получить текущий номер устройства
    fun getAudioDeviceId() : Int {
        if (mEngineHandle != 0L) return native_getAudioDeviceId( mEngineHandle)
        return -1
    }

    //Прочесть признак того что требуются новые данные
    fun getNeedAllData() : Int {
        if (mEngineHandle != 0L) return native_getAllData( mEngineHandle)
        return 0
    }

    //Прочесть признак того что требуются новые данные
    fun resetNeedAllData()                 { if (mEngineHandle != 0L) return native_resetAllData( mEngineHandle) }
    fun setChannelCount(channelCount: Int) { if (mEngineHandle != 0L) native_setChannelCount( mEngineHandle, channelCount ) }
    fun setBufferSizeInBursts(bufferSizeInBursts: Int) { if (mEngineHandle != 0L) native_setBufferSizeInBursts( mEngineHandle, bufferSizeInBursts ) }

    fun getCurrentOutputLatencyMillis(): Double {
        return if (mEngineHandle == 0L) 0.0 else native_getCurrentOutputLatencyMillis(
            mEngineHandle
        )
    }

    fun isLatencyDetectionSupported(): Boolean { return mEngineHandle != 0L && native_isLatencyDetectionSupported( mEngineHandle ) }

    //Установка частоты
    fun setVolume(CH: Int, value: Float) { if (mEngineHandle != 0L) native_setVolume( mEngineHandle, CH, value ) }

    fun resetAllPhase()        { if (mEngineHandle != 0L) native_resetAllPhase( mEngineHandle) }
    fun setMono(mono: Boolean) { if (mEngineHandle != 0L) native_setMono( mEngineHandle, mono ) }
    fun setInvertPhase(invert: Boolean) { if (mEngineHandle != 0L) native_setInvertPhase  ( mEngineHandle, invert ) }

    fun setEnL(enL: Boolean)         {if (mEngineHandle != 0L) native_setEnL    ( mEngineHandle, enL ) }
    fun setEnR(enR: Boolean)         {if (mEngineHandle != 0L) native_setEnR    ( mEngineHandle, enR ) }
    fun setShuffle(shuffle : Boolean){if (mEngineHandle != 0L) native_setShuffle( mEngineHandle, shuffle ) }

    //Внешняя функция
    //Включалки
    fun setEN(CH: Int, EN: Boolean)    { if (mEngineHandle != 0L) native_setCH_EN  ( mEngineHandle, CH, EN ) }
    fun setAM_EN(CH: Int, EN: Boolean) { if (mEngineHandle != 0L) native_setCH_AMEN( mEngineHandle, CH, EN ) }
    fun setFM_EN(CH: Int, EN: Boolean) { if (mEngineHandle != 0L) native_setCH_FMEN( mEngineHandle, CH, EN ) }
    //Установка частоты
    fun setCarrier_fr(CH: Int, fr: Float) { if (mEngineHandle != 0L) native_setCH_Carrier_fr( mEngineHandle, CH, fr ) }
    fun setAM_fr(CH: Int, fr: Float)      { if (mEngineHandle != 0L) native_setCH_AM_fr     ( mEngineHandle, CH, fr ) }
    fun setFM_fr(CH: Int, fr: Float)      { if (mEngineHandle != 0L) native_setCH_FM_fr     ( mEngineHandle, CH, fr ) }
    //Отправляем буффер 2048 байт
    fun CH_Send_Buffer(CH: Int, mod: Int, buf: ByteArray?) { if (mEngineHandle != 0L) native_setCH_Send_Buffer( mEngineHandle, CH, mod, buf!! ) }
    //fun setFM_Base(CH: Int, fr: Float) { if (mEngineHandle != 0L) native_setCH_FM_Base( mEngineHandle, CH, fr ) }
    fun setFM_Dev(CH: Int, fr: Float)  { if (mEngineHandle != 0L) native_setCH_FM_Dev ( mEngineHandle, CH, fr ) }

    /**
     * Установить глубину AM модуляции
     */
    fun setAmDepth(ch: Int, depth: Float)  { if (mEngineHandle != 0L) native_setAmDepth ( mEngineHandle, ch, depth ) }


    /**
     * # Импульс
     */
    fun setImpulse(CH: Int, EN: Boolean)    { if (mEngineHandle != 0L) native_setCH_Impulse( mEngineHandle, CH, EN ) }

    /**
     * Установка ширины импульса
     */
    fun setImpulseWidthTime(ch: Int, width: Int)    { if (mEngineHandle != 0L) native_setImpulseWidthTime( mEngineHandle, ch, width ) }

    /**
     * Установка времени паузы импульса
     */
    fun setImpulsePauseTime(ch: Int, width: Int)    { if (mEngineHandle != 0L) native_setImpulsePauseTime( mEngineHandle, ch, width ) }








    // Native methods
    private external fun native_createEngine(): Long
    private external fun native_startEngine     (engineHandle: Long): Int
    private external fun native_stopEngine      (engineHandle: Long): Int
    private external fun native_deleteEngine    (engineHandle: Long)
    private external fun native_setAudioApi     (engineHandle: Long, audioApi: Int)
    private external fun native_setAudioDeviceId(engineHandle: Long, deviceId: Int)
    private external fun native_getAudioDeviceId(engineHandle: Long) : Int
    //Прочесть признак того что требуются новые данные
    private external fun native_getAllData                   ( engineHandle: Long) : Int
    private external fun native_resetAllData                 ( engineHandle: Long)
    private external fun native_setChannelCount              ( mEngineHandle: Long, channelCount: Int)
    private external fun native_setBufferSizeInBursts        ( engineHandle: Long, bufferSizeInBursts: Int)
    private external fun native_getCurrentOutputLatencyMillis( engineHandle: Long): Double
    private external fun native_isLatencyDetectionSupported  ( engineHandle: Long): Boolean
    private external fun native_setDefaultStreamValues       ( sampleRate: Int, framesPerBurst: Int)
    //Мои функции
    //Переключалка


    private external fun native_setEnL           ( engineHandle: Long, value: Boolean)
    private external fun native_setEnR           ( engineHandle: Long, value: Boolean)
    private external fun native_setShuffle       ( engineHandle: Long, value: Boolean)
    private external fun native_resetAllPhase    ( engineHandle: Long)
    private external fun native_setInvertPhase   ( engineHandle: Long, invert: Boolean)
    private external fun native_setMono          ( engineHandle: Long, mono: Boolean)
    private external fun native_setCH_EN         ( engineHandle: Long, CH: Int, EN: Boolean)
    private external fun native_setCH_AMEN       ( engineHandle: Long, CH: Int, EN: Boolean)
    private external fun native_setCH_FMEN       ( engineHandle: Long, CH: Int, EN: Boolean)
    private external fun native_setVolume        ( engineHandle: Long, CH: Int, value: Float ) //Изменить частоту несущей
    private external fun native_setCH_Carrier_fr ( engineHandle: Long, CH: Int, fr: Float ) //Изменить частоту несущей
    private external fun native_setCH_AM_fr      ( engineHandle: Long, CH: Int, fr: Float ) //Изменить частоту Амплитудной модуляции
    private external fun native_setCH_FM_fr      ( engineHandle: Long, CH: Int, fr: Float ) //Изменить частоту Частотной модуляции
    private external fun native_setCH_FM_Dev     ( engineHandle: Long, CH: Int, fr: Float ) //Изменить девиацию частоту Частотной модуляции
    private external fun native_setCH_Send_Buffer( engineHandle: Long, CH: Int, mod: Int, buf: ByteArray )


    /**
     * Установить глубину AM модуляции
     */
    private external fun native_setAmDepth( engineHandle: Long, ch: Int, depth: Float )


    /**
     * # Импульс
     * ## Включение режима импульс
     */
    private external fun native_setCH_Impulse    ( engineHandle: Long, CH: Int, EN: Boolean)

    /**
     * Установка ширины импульса
     */
    private external fun native_setImpulseWidthTime ( engineHandle: Long, ch: Int, width: Int)

    /**
     * Установка времени паузы
     */
    private external fun native_setImpulsePauseTime ( engineHandle: Long, ch: Int, width: Int)







    fun StartListening()
    {
        startListening(this)
    }

    private external fun startListening(callback: DataCallback)

    override fun onDataReceived(data: FloatArray?) {
        //TODO("Not yet implemented")
        data
    }



}

