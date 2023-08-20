package com.example.generator2.mp3

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.PositionInfo
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.EditedMediaItem
import com.example.generator2.R
import com.example.generator2.mp3.stream.dataCompressor
import com.example.generator2.mp3.stream.renderDataToPoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

lateinit var exoplayer: PlayerMP3

@androidx.media3.common.util.UnstableApi
class PlayerMP3(val context: Context) {

    var player: ExoPlayer

    var sampleRate = 0
    var bitrate = 0
    var averageBitrate = 0
    var channelCount = 0

    val currentPosition: Long = 0 //Текущая позиция

    var durationMs = MutableStateFlow(0L)          //Общая продолжительность в мс
    val currentTime = MutableStateFlow(0L)         //Текущее время воспроизведения
    val bufferedPercentage = MutableStateFlow(0)   //Процент воспроизведения 0..100
    val isPlaying = MutableStateFlow(false)
    val playbackState = MutableStateFlow(0)

    var uriCurrent: Uri = Uri.parse("asset:///1.mp3")

    //playlist


    lateinit var listener: Player.Listener


    init {

        dataCompressor()
        renderDataToPoints()

        player = ExoPlayer.Builder(context, renderersFactory(context)).build()
        listener()
        player.addListener(listener)
        loop()


        //val uri = Uri.parse("asset:///1.mp3")
        //val uri = Uri.parse("asset:///CH Blow Me_beats_in_phase Rc.mp3")
        //val uri = Uri.parse("asset:///Get Hard.mp3")
        val uri = Uri.parse("asset:///CH Teen Edition StL A.mp3")

        val a = EditedMediaItem.Builder(MediaItem.fromUri(uri)).build()
        player.setMediaItem(a.mediaItem)
        player.prepare()

        player.play()
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun loop() {
        GlobalScope.launch(Dispatchers.Main) {
            while (true) {

                try {
                    currentTime.value = player.currentPosition.coerceAtLeast(0L)
                } catch (e: Exception) {
                    Timber.e(e.localizedMessage)
                }

                delay(250)
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun listener() {
        listener = object : Player.Listener {


            override fun onEvents(player: Player, events: Player.Events) {
                Timber.w("onEvents")
                super.onEvents(player, events)
                durationMs.value = player.duration.coerceAtLeast(0L)
                currentTime.value = player.currentPosition.coerceAtLeast(0L)
                bufferedPercentage.value = player.bufferedPercentage
                isPlaying.value = player.isPlaying

                playbackState.value = player.playbackState

            }


//            override fun onPositionDiscontinuity(
//                oldPosition: PositionInfo,
//                newPosition: PositionInfo,
//                reason: Int
//            ) {
//                Timber.w("onPositionDiscontinuity")
//                currentTime.value = newPosition.positionMs
//            }


//            //            //isPlaying — играет ли игрок.
//            override fun onIsPlayingChanged(isPlaying: Boolean) {
//                Timber.w("onIsPlayingChanged")
//
//                //player.volume = 0f
//
//                if (isPlaying) {
//                    // Active playback.
//                    GlobalScope.launch(Dispatchers.Main) {
//                        //player.volume = 0f
//                        //delay(500)
//                        //player.volume = 1f
//                    }
//
//                } else {
//                    // Not playing because playback is paused, ended, suppressed, or the player
//                    // is buffering, stopped or failed. Check player.playWhenReady,
//                    // player.playbackState, player.playbackSuppressionReason and
//                    // player.playerError for details.
//                    //GlobalScope.launch {
//                       //player.volume = 0f
//                    //}
//                }
//            }

            override fun onTracksChanged(tracks: Tracks) {
                Timber.w("onTracksChanged")
                // Update UI using current tracks.
                val format = player.audioFormat

                if (format != null) {

                    sampleRate = format.sampleRate
                    bitrate = format.bitrate
                    averageBitrate = format.averageBitrate
                    channelCount = format.channelCount
                    //durationMs = player.duration
                }

            }

        }
    }


}

fun Long.formatMinSec(): String {
    return if (this == 0L) {
        "..."
    } else {
        String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(this),
            TimeUnit.MILLISECONDS.toSeconds(this) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(this)
                    )
        )
    }
}