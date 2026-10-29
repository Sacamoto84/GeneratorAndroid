package com.example.generator2.features.threephase

/**
 * Эталоны, посчитанные кодом restim скриптом `threephase/gen_reference.py`.
 * Первая строка — заголовок, разделитель запятая, кодировка UTF-8.
 */
object ReferenceCsv {

    fun read(name: String): List<Map<String, String>> {
        val stream = ReferenceCsv::class.java.getResourceAsStream("/threephase/$name")
            ?: error("Нет эталона /threephase/$name")
        val lines = stream.bufferedReader(Charsets.UTF_8).use { it.readLines() }
            .filter { it.isNotBlank() }
        val header = lines.first().split(',')
        return lines.drop(1).map { line -> header.zip(line.split(',')).toMap() }
    }
}

fun Map<String, String>.d(key: String): Double = getValue(key).toDouble()
fun Map<String, String>.f(key: String): Float = getValue(key).toFloat()
