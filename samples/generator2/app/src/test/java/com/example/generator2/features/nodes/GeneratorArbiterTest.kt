package com.example.generator2.features.nodes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratorArbiterTest {

    @Test
    fun `сначала генератором никто не владеет`() {
        assertEquals(RunOwner.NONE, GeneratorArbiter().owner)
    }

    @Test
    fun `захват графом глушит скрипт`() {
        val arbiter = GeneratorArbiter()
        var scriptStopped = false
        arbiter.register(RunOwner.SCRIPT) { scriptStopped = true }

        arbiter.acquire(RunOwner.SCRIPT)
        arbiter.acquire(RunOwner.NODES)

        assertTrue(scriptStopped)
        assertEquals(RunOwner.NODES, arbiter.owner)
    }

    @Test
    fun `повторный захват тем же владельцем себя не глушит`() {
        val arbiter = GeneratorArbiter()
        var stops = 0
        arbiter.register(RunOwner.NODES) { stops++ }

        arbiter.acquire(RunOwner.NODES)
        arbiter.acquire(RunOwner.NODES)

        assertEquals(0, stops)
    }

    @Test
    fun `освобождение чужим владельцем ничего не меняет`() {
        val arbiter = GeneratorArbiter()
        arbiter.acquire(RunOwner.NODES)
        arbiter.release(RunOwner.SCRIPT)
        assertEquals(RunOwner.NODES, arbiter.owner)
    }

    @Test
    fun `после освобождения владельца нет`() {
        val arbiter = GeneratorArbiter()
        arbiter.acquire(RunOwner.NODES)
        arbiter.release(RunOwner.NODES)
        assertEquals(RunOwner.NONE, arbiter.owner)
    }

    @Test
    fun `незарегистрированный владелец не роняет захват`() {
        val arbiter = GeneratorArbiter()
        arbiter.acquire(RunOwner.SCRIPT)
        arbiter.acquire(RunOwner.NODES)
        assertFalse(arbiter.owner == RunOwner.SCRIPT)
    }
}
