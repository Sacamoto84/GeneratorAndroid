package com.example.generator2.features.scope

import java.nio.ByteBuffer

object NativePhosphor {

    /** Число бинов по вертикали, должно совпадать с PhosphorGrid::kBins. */
    const val BINS = 512

    init {
        System.loadLibrary("plasma")
    }

    /**
     * Задаёт геометрию сетки и режим отрисовки.
     * @param columns ширина области вывода в пикселях.
     * @param layout 0 — каналы совмещены, 1 — каналы в своих половинах.
     * @param rollMode true при развёртке 32 и выше.
     */
    external fun configure(columns: Int, layout: Int, rollMode: Boolean)

    /** Возвращает [начальный столбец, количество] изменившихся столбцов. */
    external fun update(): IntArray

    /**
     * Direct-буфер всей сетки: columns * BINS * 2 float.
     *
     * Буфер оборачивает нативную память аккумулятора. Любой вызов configure()
     * перевыделяет её, поэтому держать ссылку между вызовами нельзя —
     * получишь обращение к освобождённой памяти. Запрашивать заново после
     * каждого configure().
     */
    external fun gridBuffer(): ByteBuffer?

    /** Смещение чтения текстуры в диапазоне [0, 1). */
    external fun ringOffset(): Float
}
