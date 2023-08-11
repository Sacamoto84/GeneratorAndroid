package com.example.generator2.mp3

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.exoplayer.ExoPlayer
import java.nio.ByteBuffer

@androidx.media3.common.util.UnstableApi
class PlayerMP3(context: Context) {

    var player: ExoPlayer


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
        player = ExoPlayer.Builder(context, renderersFactory(context)).build()
        player.addListener(listener)
    }

    var bigBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER







































}