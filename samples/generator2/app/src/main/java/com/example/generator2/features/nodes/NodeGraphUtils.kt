package com.example.generator2.features.nodes

import com.example.generator2.AppPath
import com.example.generator2.features.nodes.model.GraphDto
import com.example.generator2.features.nodes.model.GraphFormatException
import com.example.generator2.features.nodes.model.NodeGraph
import com.example.generator2.features.nodes.model.toDomain
import com.example.generator2.features.nodes.model.toDto
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import timber.log.Timber
import java.io.File

/**
 * Файлы графов в /Gen3/Nodes. Повторяет ScriptUtils: тот же набор операций,
 * другой формат.
 */
class NodeGraphUtils(private val appPath: AppPath, private val gson: Gson = Gson()) {

    /** Имена графов в папке без расширения */
    fun list(): List<String> =
        File(appPath.nodes).list()
            ?.filter { it.endsWith(GRAPH_EXT) }
            ?.map { it.removeSuffix(GRAPH_EXT) }
            ?.sorted()
            ?: emptyList()

    fun save(graph: NodeGraph, name: String) {
        File(appPath.nodes, "$name$GRAPH_EXT").writeText(gson.toJson(graph.toDto()))
    }

    /**
     * @throws GraphFormatException файл не читается: не тот json, неизвестный
     * тип ноды, битый операнд, версия новее нашей
     */
    fun read(name: String): NodeGraph {
        val text = File(appPath.nodes, "$name$GRAPH_EXT").readText()
        val dto = try {
            gson.fromJson(text, GraphDto::class.java)
        } catch (e: JsonSyntaxException) {
            throw GraphFormatException("файл $name$GRAPH_EXT не похож на json: ${e.message}")
        } ?: throw GraphFormatException("файл $name$GRAPH_EXT пуст")

        return dto.toDomain()
    }

    fun delete(name: String) {
        File(appPath.nodes, "$name$GRAPH_EXT").delete()
    }

    /** Новый файл пишется до удаления старого: при сбое записи граф не пропадёт */
    fun rename(from: String, to: String) {
        if (from == to) return
        try {
            save(read(from), to)
            delete(from)
        } catch (e: Exception) {
            Timber.e(e, "rename($from -> $to)")
        }
    }

    private companion object {
        const val GRAPH_EXT = ".ng"
    }
}
