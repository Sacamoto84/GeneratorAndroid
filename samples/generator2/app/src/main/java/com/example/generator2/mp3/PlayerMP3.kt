package com.example.generator2.mp3

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.EditedMediaItem
import com.example.generator2.mp3.stream.compressDataToPoints
import com.example.generator2.mp3.stream.dataCompressor

@androidx.media3.common.util.UnstableApi
class PlayerMP3(val context: Context) {

    var player: ExoPlayer


    var uriCurrent: Uri = Uri.parse("asset:///1.mp3")

    //playlist


    val listener = object : Player.Listener {

        //isPlaying — играет ли игрок.
        override fun onIsPlayingChanged(isPlaying: Boolean) {

            if (isPlaying) {
                // Active playback.
            } else {
                // Not playing because playback is paused, ended, suppressed, or the player
                // is buffering, stopped or failed. Check player.playWhenReady,
                // player.playbackState, player.playbackSuppressionReason and
                // player.playerError for details.
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
            // Update UI using current tracks.
        }

    }


    init {

        dataCompressor()
        compressDataToPoints()

        player = ExoPlayer.Builder(context, renderersFactory(context)).build()
        player.addListener(listener)

        //val uri = Uri.parse("asset:///1.mp3")
        val uri = Uri.parse("asset:///CH Blow Me_beats_in_phase Rc.mp3")
        //val uri = Uri.parse("asset:///Get Hard.mp3")

        val a = EditedMediaItem.Builder(MediaItem.fromUri(uri)).build()
        player.setMediaItem(a.mediaItem)
        player.prepare()
        player.play()
    }








//    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    var bigBuffer: ShortBuffer = ShortBuffer.allocate(0)
//    var bufR: ShortBuffer = ShortBuffer.allocate(0)
//    var bufL: ShortBuffer = ShortBuffer.allocate(0)
//    fun playUri(uri: Uri = uriCurrent) {
//        val pump = PumpMp3ToMemory(uri,
//
//            onDone = {
//                bigBuffer = it
//                //Toast.makeText(context, "sd", Toast.LENGTH_SHORT).show()
//
//
//                bufL = ShortBuffer.allocate(0)
//                bufR = ShortBuffer.allocate(0)
//
//                bigBuffer.rewind()
//                val c = bigBuffer.capacity() / 2
//                bufL = ShortBuffer.allocate(c)
//                bufR = ShortBuffer.allocate(c)
//
//                for (i in 0 until c step 2) {
//                    val t = bigBuffer.get(i)
//                    bufR.put(t)
//                    val tt = bigBuffer.get(i+1)
//                    bufL.put(tt)
//                }
//
//                bigBuffer = ShortBuffer.allocate(0)
//
//                println("onDone..........................................bigBuffer")
//
//            }
//        ).run()
//    }

}