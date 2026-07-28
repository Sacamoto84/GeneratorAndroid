package com.example.generator2.features.playlist

import com.example.generator2.features.playlist.model.PlaylistItemJson
import com.example.generator2.features.playlist.model.PlaylistJson
import com.example.generator2.features.storage.KvFile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PlaylistTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val db: File get() = File(folder.root, "playlist.db")

    private fun store() = PlaylistStore(db)

    /** Создаёт Playlist и дожидается первой загрузки */
    private fun loadedPlaylist(store: PlaylistStore = store()) = Playlist(store).also {
        runBlocking { it.initialLoad.join() }
    }

    private fun trackNamed(name: String): File = folder.newFile(name)

    @Test
    fun `до окончания чтения список пуст`() {
        assertTrue(Playlist(store()).list.value.isEmpty())
    }

    @Test
    fun `плейлисты из базы попадают в список`() {
        store().write(
            listOf(
                PlaylistJson("Работа", mutableListOf(PlaylistItemJson("one", "/music/one.mp3"))),
                PlaylistJson("Сон", mutableListOf())
            )
        )

        val names = loadedPlaylist().list.value.map { it.playlistName }.sorted()

        assertEquals(listOf("Работа", "Сон"), names)
    }

    @Test
    fun `существование файла трека проверяется при чтении`() {
        val existing = trackNamed("real.mp3")

        store().write(
            listOf(
                PlaylistJson(
                    "Работа",
                    mutableListOf(
                        PlaylistItemJson("есть", existing.absolutePath),
                        PlaylistItemJson("нет", "/нет/такого/файла.mp3")
                    )
                )
            )
        )

        val tracks = loadedPlaylist().list.value.single().data

        assertTrue(tracks.first { it.name == "есть" }.isExist)
        assertFalse(tracks.first { it.name == "нет" }.isExist)
    }

    @Test
    fun `баланс и громкость трека переносятся как есть`() {
        store().write(
            listOf(
                PlaylistJson(
                    "Работа",
                    mutableListOf(
                        PlaylistItemJson("one", "/music/one.mp3", balance = -3, volume = 0.4f)
                    )
                )
            )
        )

        val track = loadedPlaylist().list.value.single().data.single()

        assertEquals(-3, track.balance)
        assertEquals(0.4f, track.volume, 0f)
        assertEquals("/music/one.mp3", track.path)
    }

    @Test
    fun `битая база не роняет загрузку`() {
        db.writeText("мусор вместо java-сериализации")

        assertTrue(loadedPlaylist().list.value.isEmpty())
    }

    @Test
    fun `reload подхватывает изменения на диске`() {
        val store = store()
        store.write(listOf(PlaylistJson("Первый", mutableListOf())))

        val playlist = loadedPlaylist(store)
        assertEquals(listOf("Первый"), playlist.list.value.map { it.playlistName })

        store.write(listOf(PlaylistJson("Второй", mutableListOf())))
        runBlocking { playlist.reload().join() }

        assertEquals(listOf("Второй"), playlist.list.value.map { it.playlistName })
    }

    @Test
    fun `битый плейлист пропускается остальные читаются`() {
        KvFile.write(
            db,
            mapOf(
                "хороший" to """{"playlistName":"хороший","data":[]}""",
                "битый" to "{это не json"
            )
        )

        assertEquals(listOf("хороший"), loadedPlaylist().list.value.map { it.playlistName })
    }
}
