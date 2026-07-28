package com.example.generator2.features.storage

import timber.log.Timber
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * ### Файл «ключ-значение» в формате Satchel
 *
 * Формат тот же, что у `RawSatchelSerializer`: вся мапа записана java-сериализацией
 * одним куском. Менять его нельзя — файлы пресетов и плейлистов лежат у
 * пользователей на sdcard и попадают в их ручные бэкапы.
 *
 * Отличия от самой библиотеки Satchel:
 * - **запись атомарная**: сначала временный файл с fsync, потом переименование,
 *   поэтому обрыв процесса не оставляет обрезанный файл;
 * - **запись синхронная**: вызывающий знает, что данные легли на диск (Satchel
 *   пишет асинхронно, и `close()` флаш не гарантирует);
 * - **прошлая версия остаётся в `.bak`** и подхватывается, если основной файл
 *   повреждён или исчез;
 * - **нет вечной корутины** на каждый открытый файл.
 *
 * Ввод-вывод блокирующий: вызывать с фонового потока.
 */
object KvFile {

    private const val EXT_TMP = ".tmp"
    private const val EXT_BAK = ".bak"

    /**
     * Чтение. Битый или отсутствующий файл не выбрасывает исключение: сначала
     * пробуется `.bak`, потом возвращается пустая мапа и вызывающий получает
     * значения по умолчанию.
     */
    fun read(file: File): Map<String, Any> {
        readOrNull(file)?.let { return it }

        val bak = bakOf(file)
        readOrNull(bak)?.let {
            Timber.w("Взят ${bak.name}: основной файл ${file.name} отсутствует или повреждён")
            return it
        }

        return emptyMap()
    }

    /**
     * Атомарная запись. Кидает исключение, если записать не удалось — тихо
     * терять сохранение пользователя нельзя.
     */
    fun write(file: File, values: Map<String, Any>) {
        file.parentFile?.mkdirs()

        val tmp = File(file.parentFile, file.name + EXT_TMP)

        FileOutputStream(tmp).use { stream ->
            //Закрывать ObjectOutputStream здесь нельзя: он закроет и сам
            //FileOutputStream, а синхронизировать нужно живой дескриптор
            val objects = ObjectOutputStream(BufferedOutputStream(stream))
            objects.writeObject(LinkedHashMap(values))
            objects.flush()
            stream.fd.sync() //данные на диске, а не в кеше страниц
        }

        //Прошлая версия уезжает в .bak. Если процесс умрёт между двумя
        //переименованиями, read() поднимет её вместо пустоты
        val bak = bakOf(file)
        if (file.exists()) {
            bak.delete()
            file.renameTo(bak)
        }

        if (!tmp.renameTo(file)) {
            //Подмена не удалась — возвращаем прошлую версию на место
            bak.renameTo(file)
            tmp.delete()
            error("Не удалось заменить файл ${file.name}")
        }
    }

    private fun bakOf(file: File) = File(file.parentFile, file.name + EXT_BAK)

    @Suppress("UNCHECKED_CAST")
    private fun readOrNull(file: File): Map<String, Any>? {
        if (!file.exists() || file.length() == 0L) return null

        return try {
            ObjectInputStream(BufferedInputStream(file.inputStream())).use {
                it.readObject() as Map<String, Any>
            }
        } catch (e: Exception) {
            Timber.e(e, "Не удалось прочитать ${file.name}")
            null
        }
    }
}

/**
 * Значение по ключу с приведением типа. Тип не совпал или ключа нет — [default].
 */
inline fun <reified T : Any> Map<String, Any>.valueOr(key: String, default: T): T =
    this[key] as? T ?: default
