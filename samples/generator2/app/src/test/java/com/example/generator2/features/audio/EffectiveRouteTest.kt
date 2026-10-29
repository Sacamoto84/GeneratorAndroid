package com.example.generator2.features.audio

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * В 3-фазе блок MP3 с выбором маршрута скрыт: выход всегда с генератора,
 * иначе оставшийся маршрут MP3 или OFF глушит режим без видимой причины.
 */
class EffectiveRouteTest {

    @Test
    fun `в 3-фазе выход всегда с генератора`() {
        for (route in ROUTESTREAM.entries) {
            assertEquals(ROUTESTREAM.GEN, effectiveRoute(route, threePhase = true))
        }
    }

    @Test
    fun `без 3-фазы маршрут как выбран`() {
        for (route in ROUTESTREAM.entries) {
            assertEquals(route, effectiveRoute(route, threePhase = false))
        }
    }
}
