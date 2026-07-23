package com.example.generator2.features.nodes

enum class RunOwner { NONE, SCRIPT, NODES }

/**
 * Кто сейчас крутит генератор.
 *
 * Арбитр не знает ни про Script, ни про NodeRunner: каждый владелец сам
 * оставляет здесь обработчик «остановить себя». Иначе получилась бы
 * круговая зависимость — арбитр знает про движки, движки про арбитра.
 */
class GeneratorArbiter {

    private val stoppers = mutableMapOf<RunOwner, () -> Unit>()

    @Volatile
    var owner: RunOwner = RunOwner.NONE
        private set

    fun register(who: RunOwner, stop: () -> Unit) {
        synchronized(this) { stoppers[who] = stop }
    }

    /** Забрать генератор себе, остановив прежнего владельца */
    fun acquire(who: RunOwner) {
        //Обработчик зовём вне synchronized: он останавливает чужой движок,
        //и держать на этом замок арбитра незачем
        val loser = synchronized(this) {
            val previous = owner
            owner = who
            previous.takeIf { it != RunOwner.NONE && it != who }
        }
        loser?.let { stoppers[it]?.invoke() }
    }

    fun release(who: RunOwner) {
        synchronized(this) { if (owner == who) owner = RunOwner.NONE }
    }
}
