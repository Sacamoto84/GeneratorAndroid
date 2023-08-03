package com.example.generator2.mp3

import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import c.ponom.audiuostreams.audiostreams.ArrayUtils.byteToShortArrayLittleEndian
import c.ponom.audiuostreams.audiostreams.AudioFileSoundStream
import c.ponom.audiuostreams.audiostreams.AudioTrackOutputStream
import c.ponom.audiuostreams.audiostreams.SoundVolumeUtils.getRMSVolume
import c.ponom.audiuostreams.audiostreams.StreamPump
import com.example.generator2.AppPath
import timber.log.Timber
import java.io.File
import kotlin.random.Random

const val TAG = "AudioStreamsDemo"

var targetVolume: Float=1f

// using different filenames for different instances of application,
// or else files from old versions cannot be overwriten
private val testFileNum = Random.Default.nextInt(100000).toString(16)
var recordLevel: MutableLiveData<Float> = MutableLiveData(0.0f)
var bytesPassed: MutableLiveData<Int> = MutableLiveData(0)
//var recorderState: MutableLiveData<MicRecordState> = MutableLiveData(NO_FILE_RECORDED)

//private val outDirName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString()
//private val outDir = File("$outDirName/AudioStreams/").apply { mkdir() }
private lateinit var audioPump:StreamPump
//private val testFileMp3 = File(outDir, "/TestMicStream_$testFileNum.mp3")
private val testFileMp3 = File("asset:///1.mp3")
private var recordingIsOn=false

fun play() {

    val audioIn = AudioFileSoundStream(AppPath().download+"/1.mp3")
    val audioOut= AudioTrackOutputStream(audioIn.sampleRate,audioIn.channelsCount,0)

    audioPump= StreamPump(audioIn, audioOut, 2048,
        onEachPump = {
            recordLevel.postValue(getRMSVolume(byteToShortArrayLittleEndian(it)).toFloat())},
        onWrite =  {
            bytesPassed.postValue(it.toInt())
                   },
        onFinish = {
            //recorderState.postValue(STOPPED_READY)
                   },
        onFatalError={ Timber.tag(TAG).e("Error=" + it.localizedMessage)})
    audioOut.play()
    audioPump.start(true)
    //recorderState.postValue(PLAYING)

}