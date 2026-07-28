package com.example.generator2.features.playlist

import com.example.generator2.features.playlist.model.PlaylistItemJson
import com.example.generator2.features.playlist.model.PlaylistJson
import com.example.generator2.features.storage.KvFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PlaylistStoreTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val file: File get() = File(folder.root, "playlist.db")

    private fun store() = PlaylistStore(file)

    private fun playlist(name: String, vararg tracks: String) = PlaylistJson(
        playlistName = name,
        data = tracks.map { PlaylistItemJson(name = it, path = "/music/$it.mp3") }.toMutableList()
    )

    @Test
    fun `записанные плейлисты читаются обратно`() {
        val store = store()

        store.write(listOf(playlist("Работа", "one", "two"), playlist("Сон", "three")))

        val restored = store.readAll().sortedBy { it.playlistName }

        assertEquals(listOf("Работа", "Сон"), restored.map { it.playlistName })
        assertEquals(2, restored.first { it.playlistName == "Работа" }.data.size)
        assertEquals("/music/three.mp3", restored.first { it.playlistName == "Сон" }.data[0].path)
    }

    @Test
    fun `пустая база даёт пустой список`() {
        assertTrue(store().readAll().isEmpty())
    }

    @Test
    fun `битый json одного плейлиста не теряет остальные`() {
        KvFile.write(
            file,
            mapOf(
                "хороший" to """{"playlistName":"хороший","data":[]}""",
                "битый" to "{это не json"
            )
        )

        val restored = store().readAll()

        assertEquals(listOf("хороший"), restored.map { it.playlistName })
    }

    @Test
    fun `перезапись заменяет содержимое целиком`() {
        val store = store()

        store.write(listOf(playlist("Первый", "a")))
        store.write(listOf(playlist("Второй", "b")))

        assertEquals(listOf("Второй"), store.readAll().map { it.playlistName })
    }

    @Test
    fun `clear очищает базу`() {
        val store = store()
        store.write(listOf(playlist("Первый", "a")))

        store.clear()

        assertTrue(store.readAll().isEmpty())
    }
}
