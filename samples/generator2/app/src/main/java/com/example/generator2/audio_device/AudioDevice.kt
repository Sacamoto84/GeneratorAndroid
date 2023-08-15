package com.example.generator2.audio_device

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import com.example.generator2.R
import com.example.generator2.util.UtilsKT
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(DelicateCoroutinesApi::class)
class AudioDevice(private var context: Context, var utils: UtilsKT) {

    private var mDirectionType = 0

    //private var mDeviceAdapter: AudioDeviceAdapter? = null
    private var mAudioManager: AudioManager? = null

    //private var mDeviceAdapter: MutableList<AudioDeviceListEntry> = mutableListOf()

    var mDeviceAdapter = mutableStateListOf<AudioDeviceListEntry>()

    var mScoStarted = false

    var mDeviceId by mutableIntStateOf(0)

    init {

        GlobalScope.launch(Dispatchers.IO) {

            Timber.i("AudioDevice init{}")

            //playbackEngine.create()
            //playbackEngine.start()

            println("┌----------------------┐")
            println("│  AudioDevice init{}  │")
            println("└----------------------┘")

            mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager //mDeviceAdapter = AudioDeviceAdapter(context)
            mDeviceAdapter.add(
                AudioDeviceListEntry(0, context.getString(R.string.auto_select))
            )

            mDirectionType = AudioManager.GET_DEVICES_OUTPUTS

            GlobalScope.launch (Dispatchers.IO){
                setupAudioDeviceCallback()
            }
            println("│  AudioDevice init{}  end │")
        }

    }

    fun getDeviceId()
    {
        //mDeviceId = playbackEngine.getAudioDeviceId()
    }



    private fun setupAudioDeviceCallback() {

        // Note that we will immediately receive a call to onDevicesAdded with the list of
        // devices which are currently connected.
        mAudioManager!!.registerAudioDeviceCallback(object : AudioDeviceCallback() {

            override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {

                //script.command( StateCommandScript.STOP )

                val deviceList = AudioDeviceListEntry.createListFrom(addedDevices, mDirectionType)

                if (deviceList.size > 0) {
                    mDeviceAdapter.addAll(deviceList)
                }

                println("┌----------------------------------------------------------------------------┐")
                println("│  onAudioDevicesAdded                                                       │")
                println("├----------------------------------------------------------------------------┘")
                println(deviceList.joinToString("\n"))
                println("└----------------------------------------------------------------------------┘")

                //mDeviceId = playbackEngine.getAudioDeviceId()


            }

            override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {

                //script.command(StateCommandScript.STOP)

                val deviceList = AudioDeviceListEntry.createListFrom(removedDevices, mDirectionType)
                for (entry in deviceList) {
                    mDeviceAdapter.remove(entry)
                }

                println("┌----------------------------------------------------------------------------┐")
                println("│  onAudioDevicesRemoved                                                     │")
                println("├----------------------------------------------------------------------------┘")
                println(deviceList.joinToString(", "))
                println("└----------------------------------------------------------------------------┘")

                //mDeviceId = playbackEngine.getAudioDeviceId()

            }

        }, null)
    }


    fun onItemSelectedListener(i: Int) {

         println("Изменить устройство вывода на:${i}")
        // Start Bluetooth SCO if needed.

        if (isScoDevice(getPlaybackDeviceId(i)) && !mScoStarted) {
            startBluetoothSco()
            mScoStarted = true
            println("Start Bluetooth SCO")
        } else

            if (!isScoDevice(getPlaybackDeviceId(i)) && mScoStarted)
            {
              stopBluetoothSco()
              mScoStarted = false
              println("Stop Bluetooth SCO")
            }

        println("id : ${getPlaybackDeviceId(i)}")

        //playbackEngine.setAudioDeviceId(getPlaybackDeviceId(i))




    }

    private fun getPlaybackDeviceId(i: Int): Int {
        return mDeviceAdapter[i].id
    }

    /**
     * @param deviceId
     * @return true if the device is TYPE_BLUETOOTH_SCO
     */
    private fun isScoDevice(deviceId: Int): Boolean {
        if (deviceId == 0) return false // Unspecified
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        val devices: Array<AudioDeviceInfo> =
            audioManager!!.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (device in devices) {
            if (device.id == deviceId) {
                return device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            }
        }
        return false
    }

    private fun startBluetoothSco() {
        val myAudioMgr = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        myAudioMgr!!.startBluetoothSco()
    }

    private fun stopBluetoothSco() {
        val myAudioMgr = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        myAudioMgr!!.stopBluetoothSco()
    }

}


