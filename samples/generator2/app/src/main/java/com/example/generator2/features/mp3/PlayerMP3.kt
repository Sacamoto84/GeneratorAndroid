package com.example.generator2.features.mp3

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.EditedMediaItem
import com.example.generator2.AppPath
import com.example.generator2.features.scope.Scope
import com.example.generator2.features.scope.dataCompressor
import com.example.generator2.features.scope.lissaguToBitmap
import com.example.generator2.features.scope.renderDataToPoints
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

@androidx.media3.common.util.UnstableApi
class PlayerMP3(val context: Context, scope: Scope) {

    var player: ExoPlayer

    var sampleRate = 0
    var bitrate = 0
    var averageBitrate = 0
    var channelCount = 0

    var durationMs = MutableStateFlow(0L)          //Общая продолжительность в мс
    val currentTime = MutableStateFlow(0L)         //Текущее время воспроизведения
    val bufferedPercentage = MutableStateFlow(0)   //Процент воспроизведения 0..100
    val isPlaying = MutableStateFlow(false)
    var isPlayingD = false

    val playbackState = MutableStateFlow(0)


    var uriCurrent: Uri = Uri.parse("asset:///1.mp3")

    //playlist


    lateinit var listener: Player.Listener


    init {

        dataCompressor()
        renderDataToPoints(scope)
        lissaguToBitmap(scope)

        player = ExoPlayer.Builder(context, renderersFactory(context, isPlayingD)).build()
        listener()
        player.addListener(listener)
        loop()


        //val uri = Uri.parse("asset:///1.mp3")
        //val uri = Uri.parse("asset:///CH Blow Me_beats_in_phase Rc.mp3")
        //val uri = Uri.parse("asset:///Get Hard.mp3")

        //val uri = Uri.parse("asset:///CH Teen Edition StL A.mp3")

        ////val uri = Uri.parse(AppPath().music + "/CH Teen Edition StL A.mp3")
        ////val a = EditedMediaItem.Builder(MediaItem.fromUri(uri)).build()
        ////player.setMediaItem(a.mediaItem)
        ////player.prepare()

        //player.playWhenReady = true

        //player.play()
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


            override fun onEvents(player1: Player, events: Player.Events) {
                Timber.w("onEvents")
                super.onEvents(player1, events)
                durationMs.value = player1.duration.coerceAtLeast(0L)
                currentTime.value = player1.currentPosition.coerceAtLeast(0L)
                bufferedPercentage.value = player1.bufferedPercentage
                isPlaying.value = player1.isPlaying
                isPlayingD = player1.isPlaying
                playbackState.value = player1.playbackState

                val format = player.audioFormat

                if (format != null) {

                    sampleRate = format.sampleRate
                    bitrate = format.bitrate
                    averageBitrate = format.averageBitrate
                    channelCount = format.channelCount
                    //durationMs = player.duration
                }

            }


            //isPlaying — играет ли игрок.
            override fun onIsPlayingChanged(isPlaying1: Boolean) {
                Timber.w("onIsPlayingChanged")
                isPlaying.value = isPlaying1
                isPlayingD = isPlaying1
            }

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


            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {

//                val currentMediaItem = player.currentMediaItem
//                val currentMediaUri = currentMediaItem!!.playbackProperties!!.uri
//
//                val p = currentMediaUri.toString()
////
//                val mp3file = Mp3File(p)
//
//                println("Length of this mp3 is: " + mp3file.lengthInSeconds + " seconds")
//                println("Bitrate: " + mp3file.bitrate + " kbps " + if (mp3file.isVbr) "(VBR)" else "(CBR)")
//                println("Sample rate: " + mp3file.sampleRate + " Hz")
//
//                //println("mp3file.channelMode: " + mp3file.channelMode)
//
//                println("Has ID3v1 tag?: " + if (mp3file.hasId3v1Tag()) "YES" else "NO")
//                println("Has ID3v2 tag?: " + if (mp3file.hasId3v2Tag()) "YES" else "NO")
//                println("Has custom tag?: " + if (mp3file.hasCustomTag()) "YES" else "NO")
//
//
//                if (mp3file.hasId3v1Tag()) {
//                    val id3v1Tag = mp3file.id3v1Tag
//                    println("Track: " + id3v1Tag.track)
//                    println("Artist: " + id3v1Tag.artist)
//                    println("Title: " + id3v1Tag.title)
//                    println("Album: " + id3v1Tag.album)
//                    println("Year: " + id3v1Tag.year)
//                    println("Genre: " + id3v1Tag.genre + " (" + id3v1Tag.genreDescription + ")")
//                    println("Comment: " + id3v1Tag.comment)
//                }
//
//                if (mp3file.hasId3v2Tag()) {
//                    val id3v2Tag = mp3file.id3v2Tag
//                    println("Track: " + id3v2Tag.track)
//                    println("Artist: " + id3v2Tag.artist)
//                    println("Title: " + id3v2Tag.title)
//                    println("Album: " + id3v2Tag.album)
//                    println("Year: " + id3v2Tag.year)
//                    println("Genre: " + id3v2Tag.genre + " (" + id3v2Tag.genreDescription + ")")
//                    println("Comment: " + id3v2Tag.comment)
//                    println("Lyrics: " + id3v2Tag.lyrics)
//                    println("Composer: " + id3v2Tag.composer)
//                    println("Publisher: " + id3v2Tag.publisher)
//                    println("Original artist: " + id3v2Tag.originalArtist)
//                    println("Album artist: " + id3v2Tag.albumArtist)
//                    println("Copyright: " + id3v2Tag.copyright)
//                    println("URL: " + id3v2Tag.url)
//                    println("Encoder: " + id3v2Tag.encoder)
//                    val albumImageData = id3v2Tag.albumImage
//                    if (albumImageData != null) {
//                        println("Have album image data, length: " + albumImageData.size + " bytes")
//                        println("Album image mime type: " + id3v2Tag.albumImageMimeType)
//                    }
//                }


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