package com.example.generator2.features.threephase

import com.example.generator2.features.threephase.patterns.ThreePhasePatternSet
import com.example.generator2.features.threephase.patterns.ThreePhasePatterns
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Паттерны сверяются с restim: `threephase/patterns.csv` — 600 вызовов
 * `update(1/60)` каждого паттерна с конструктором по умолчанию.
 */
class ThreePhasePatternsTest {

    private val reference = ReferenceCsv.read("patterns.csv").groupBy { it.getValue("name") }

    @Test
    fun `каждый паттерн совпадает с restim 600 шагов`() {
        val set = ThreePhasePatternSet()
        assertEquals(16, reference.size)
        for ((name, rows) in reference) {
            val pattern = set.all[set.indexOf(name)]
            assertEquals(name, pattern.name)
            for (row in rows) {
                pattern.update(1.0 / 60)
                assertEquals("$name alpha шаг ${row["step"]}", row.f("alpha"), pattern.alpha, 1e-4f)
                assertEquals("$name beta шаг ${row["step"]}", row.f("beta"), pattern.beta, 1e-4f)
            }
        }
    }

    @Test
    fun `reset возвращает паттерн к началу`() {
        val set = ThreePhasePatternSet()
        for ((name, rows) in reference) {
            val pattern = set.all[set.indexOf(name)]
            repeat(100) { pattern.update(1.0 / 60) }
            pattern.reset()
            pattern.update(1.0 / 60)
            assertEquals("$name alpha", rows[0].f("alpha"), pattern.alpha, 1e-4f)
            assertEquals("$name beta", rows[0].f("beta"), pattern.beta, 1e-4f)
        }
    }

    @Test
    fun `порядок набора совпадает с NAMES`() {
        val set = ThreePhasePatternSet()
        assertEquals(ThreePhasePatterns.NAMES, set.all.map { it.name })
        assertEquals(17, ThreePhasePatterns.NAMES.size)
        assertEquals("Палец", ThreePhasePatterns.NAMES[0])
    }

    @Test
    fun `неизвестное имя даёт Circle`() {
        val set = ThreePhasePatternSet()
        assertEquals(ThreePhasePatterns.NAMES.indexOf("Circle"), set.indexOf("Mouse"))
    }
}
