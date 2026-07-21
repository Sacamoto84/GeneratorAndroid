package com.example.generator2.features.script

import com.example.generator2.AppPath
import timber.log.Timber
import java.io.File

class ScriptUtils(val appPath: AppPath) {

    /**
     * Получить список файлов по пути
     *
     *  @param dir Путь к сканируемой папке Mod
     *  @return Текстовый список имен файлов
     */
    fun filesInDirToList(dir: String): List<String> { ///storage/emulated/0/Android/data/com.example.generator2/files
        val pathDocuments = File(dir)//context.getExternalFilesDir(dir)
        val r: MutableList<String> = mutableListOf()
        pathDocuments.list()?.let { r.addAll(it) }
        return r
    }

    /**
     * Имена скриптов в папке /Script без расширения .sk
     */
    fun filesInScriptToList(): List<String> =
        File(appPath.script).list()
            ?.filter { it.endsWith(SCRIPT_EXT) }
            ?.map { it.removeSuffix(SCRIPT_EXT) }
            ?.sorted()
            ?: emptyList()

    /**
     * Сохранить list String в файл /Script/name.sk
     */
    fun saveListToScriptFile(list: List<String>, name: String) {
        val text = list.mapIndexed { index, line -> "$index:$line" }.joinToString("\n", postfix = "\n")
        File(appPath.script + "/${name}$SCRIPT_EXT").writeText(text)
    }

    /**
     *  Прочитать файл скрипта и записать его в список.
     *
     *  Формат строки — "индекс:команда"; индекс необязателен и при чтении отбрасывается,
     *  пустые строки пропускаются.
     */
    fun readScriptFileToList(name: String): List<String> =
        File(appPath.script + "/${name}$SCRIPT_EXT")
            .readText()
            .lineSequence()
            .filter { it.isNotBlank() }
            .map { it.replaceFirst(linePrefix, "").trim() }
            .toList()

    fun deleteScriptFile(name: String) {
        File(appPath.script + "/${name}$SCRIPT_EXT").delete()
    }

    /**
     * Переименовать скрипт. Новый файл пишется до удаления старого,
     * чтобы при ошибке записи скрипт не пропал.
     */
    fun renameScriptFile(nameSource: String, nameDestination: String) {
        if (nameSource == nameDestination) return
        try {
            val l = readScriptFileToList(nameSource)
            saveListToScriptFile(l, nameDestination)
            deleteScriptFile(nameSource)
        } catch (e: Exception) {
            Timber.e(e, "renameScriptFile($nameSource -> $nameDestination)")
        }
    }

    private companion object {
        const val SCRIPT_EXT = ".sk"

        /** Ведущий "12:" в строке файла */
        val linePrefix = Regex("^\\s*\\d+\\s*:")
    }

}
