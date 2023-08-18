package com.example.generator2.mp3

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.EditedMediaItem
import com.example.generator2.mp3.stream.dataCompressor
import com.example.generator2.mp3.stream.renderDataToPoints

lateinit var exoplayer: PlayerMP3

@androidx.media3.common.util.UnstableApi
class PlayerMP3(val context: Context) {

    var player: ExoPlayer

    var sampleRate = 0
    var duration = 0
    var bitrate = 0
    var averageBitrate = 0
    var channelCount = 0
    var durationMs: Long = 0
    val currentPosition: Long = 0 //Текущая позиция



    var uriCurrent: Uri = Uri.parse("asset:///1.mp3")

    //playlist


    lateinit var listener: Player.Listener


    init {

        dataCompressor()
        renderDataToPoints()

        player = ExoPlayer.Builder(context, renderersFactory(context)).build()
        listener()
        player.addListener(listener)

        //val uri = Uri.parse("asset:///1.mp3")
        //val uri = Uri.parse("asset:///CH Blow Me_beats_in_phase Rc.mp3")
        //val uri = Uri.parse("asset:///Get Hard.mp3")
        val uri = Uri.parse("asset:///CH Teen Edition StL A.mp3")

        val a = EditedMediaItem.Builder(MediaItem.fromUri(uri)).build()
        player.setMediaItem(a.mediaItem)
        player.prepare()

        player.play()
    }


    private fun listener() {
        listener = object : Player.Listener {

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
                val format = player.audioFormat

                if (format != null) {

                    sampleRate = format.sampleRate
                    bitrate = format.bitrate
                    averageBitrate = format.averageBitrate
                    channelCount = format.channelCount

                    durationMs = player.duration

                }

            }

        }
    }




}