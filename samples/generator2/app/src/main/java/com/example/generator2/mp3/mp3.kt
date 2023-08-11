package com.example.generator2.mp3

import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import c.ponom.audiuostreams.audiostreams.ArrayUtils.byteToShortArrayLittleEndian
import c.ponom.audiuostreams.audiostreams.AudioFileSoundStream
import c.ponom.audiuostreams.audiostreams.AudioTrackOutputStream
import c.ponom.audiuostreams.audiostreams.AudioTrackToMemory
import c.ponom.audiuostreams.audiostreams.SoundVolumeUtils.getRMSVolume
import c.ponom.audiuostreams.audiostreams.StreamPump
import com.example.generator2.AppPath
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime
import kotlin.random.Random

const val TAG = "AudioStreamsDemo"

var targetVolume: Float = 1f

// using different filenames for different instances of application,
// or else files from old versions cannot be overwriten
private val testFileNum = Random.Default.nextInt(100000).toString(16)
var recordLevel: MutableLiveData<Float> = MutableLiveData(0.0f)
var bytesPassed: MutableLiveData<Int> = MutableLiveData(0)
//var recorderState: MutableLiveData<MicRecordState> = MutableLiveData(NO_FILE_RECORDED)

//private val outDirName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString()
//private val outDir = File("$outDirName/AudioStreams/").apply { mkdir() }
private lateinit var audioPump: StreamPump

//private val testFileMp3 = File(outDir, "/TestMicStream_$testFileNum.mp3")
private val testFileMp3 = File("asset:///1.mp3")
private var recordingIsOn = false

val mp3TimeStamp = MutableStateFlow<Long>(0)
val mp3Duration = MutableStateFlow<Long>(0)


@OptIn(DelicateCoroutinesApi::class)
fun play() {

//    val audioIn = AudioFileSoundStream(AppPath().download + "/1.mp3")
//    //val audioOut = AudioTrackOutputStream(audioIn.sampleRate, audioIn.channelsCount, 0)
//    val audioOut = AudioTrackToMemory(audioIn.sampleRate, audioIn.channelsCount, 0)
//
//
//    GlobalScope.launch(Dispatchers.IO) {
//        while (!audioIn.closed) {
//            delay(100)
//            mp3TimeStamp.value = audioIn.timestampMP3
//            mp3Duration.value = audioIn.duration
//
//            //println(audioIn.timestampMP3)
//        }
//    }
//
//    val datetime = System.currentTimeMillis()
//
//    audioPump = StreamPump(audioIn, audioOut, 2048,
//        onEachPump = {
//            recordLevel.postValue(getRMSVolume(byteToShortArrayLittleEndian(it)).toFloat())
//        },
//        onWrite = {
//            //bytesPassed.postValue(it.toInt())
//        },
//        onFinish = {
//            val now = System.currentTimeMillis() - datetime
//
//            //recorderState.postValue(STOPPED_READY)
//            Timber.e("-----------------------------")
//            Timber.e("| Окончание воспроизведения")
//            Timber.e("| Время воспроизведения $now ms")
//            Timber.e("-----------------------------")
//        },
//        onFatalError = {
//            Timber.tag(TAG).e("Error=%s", it.localizedMessage)
//        })
//
//    //audioOut.play()
//
//    audioPump.start(true)
//    //recorderState.postValue(PLAYING)

}