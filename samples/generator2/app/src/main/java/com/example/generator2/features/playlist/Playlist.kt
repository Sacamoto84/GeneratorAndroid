package com.example.generator2.features.playlist

import com.example.generator2.features.playlist.model.PlaylistItem
import com.example.generator2.features.playlist.model.PlaylistList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Плейлисты для UI.
 *
 * Хранилище приходит готовым — про пути и Android класс не знает, поэтому
 * проверяется обычным JVM-тестом. Кто откуда читает файл, решает DI-модуль.
 */
@Singleton
class Playlist @Inject constructor(
    private val store: PlaylistStore
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _list = MutableStateFlow<List<PlaylistList>>(emptyList())

    /**
     * Список из списков для UI. До окончания чтения файла пуст — экран рисуется
     * сразу, содержимое приезжает следом.
     */
    val list: StateFlow<List<PlaylistList>> = _list.asStateFlow()

    /**
     * Первая загрузка, запускается при создании. Публична, чтобы вызывающий мог
     * её дождаться — без этого «загружено или ещё нет» проверить нечем.
     */
    val initialLoad: Job = reload()

    /**
     * Перечитать плейлисты с диска. Возвращает управление сразу: чтение базы и
     * проверка существования каждого файла идут на IO, результат приходит в [list].
     */
    fun reload(): Job = scope.launch {
        try {
            _list.value = readAll()
        } catch (e: Exception) {
            Timber.e(e, "Не удалось прочитать плейлисты")
        }
    }

    /**
     * Прочесть все записи из базы и проверить, на месте ли файлы треков
     */
    private fun readAll(): List<PlaylistList> =
        store.readAll().map { json ->
            val data = json.data.map { item ->
                PlaylistItem(
                    name = item.name,
                    path = item.path,
                    isExist = File(item.path).exists(),
                    balance = item.balance,
                    volume = item.volume
                )
            }

            PlaylistList(json.playlistName, data.toMutableList())
        }






}