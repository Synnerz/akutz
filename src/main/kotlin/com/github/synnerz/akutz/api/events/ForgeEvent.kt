package com.github.synnerz.akutz.api.events

import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

class ForgeEvent(private val method: (args: Any?) -> Unit, clazz: Class<*>) : Comparable<ForgeEvent> {
    constructor(method: (args: Any?) -> Unit, clazz: String) : this(method, Class.forName(clazz))

    init {
        forgeTriggers.getOrPut(clazz) { sortedSetOf() }.add(this)
    }

    fun trigger(args: Any?) {
        method(args)
    }

    companion object {
        private val forgeTriggers = mutableMapOf<Class<*>, SortedSet<ForgeEvent>>()

        @SubscribeEvent
        fun onEvent(event: Event) {
            if (Thread.currentThread().name == "Server thread") return

            forgeTriggers[event::class.java]?.forEach { it.trigger(arrayOf(event)) }
        }
    }

    override fun compareTo(other: ForgeEvent): Int {
        return hashCode() - other.hashCode()
    }
}