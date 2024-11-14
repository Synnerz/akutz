package com.github.synnerz.akutz.api.events

import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

class ForgeEvent(
    method: (args: Array<out Any?>) -> Unit,
    val clazz: Class<*>
) : EventTrigger(method, EventType.Forge) {

    init {
        require(Event::class.java.isAssignableFrom(clazz)) {
            "ForgeEvent Could not find the given Event class. Please assign a proper Event class and not ${clazz.simpleName}"
        }

        forgeEvents.getOrPut(clazz) { sortedSetOf() }.add(this)
    }

    override fun register(): EventTrigger {
        forgeEvents.getOrPut(clazz) { sortedSetOf() }.add(this)
        return super.register()
    }

    override fun unregister(): EventTrigger {
        forgeEvents[clazz]?.remove(this)
        return super.unregister()
    }

    override fun trigger(args: Array<out Any?>) {
        callMethod(args)
    }

    companion object {
        private val forgeEvents = mutableMapOf<Class<*>, SortedSet<ForgeEvent>>()

        fun unregisterEvents() {
            forgeEvents.values.flatten().forEach {
                it.unregister()
            }
            forgeEvents.clear()
        }

        @SubscribeEvent
        fun onEvent(event: Event) {
            if (Thread.currentThread().name == "Server thread") return

            forgeEvents[event::class.java]?.forEach { it.trigger(arrayOf(event)) }
        }
    }
}