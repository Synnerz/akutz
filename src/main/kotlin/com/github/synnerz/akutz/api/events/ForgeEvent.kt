package com.github.synnerz.akutz.api.events

import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/triggers/ForgeTrigger.kt)
 */
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

    override fun onRegister() = apply {
        forgeEvents.getOrPut(clazz) { sortedSetOf() }.add(this)
    }

    override fun onUnregister() = apply {
        forgeEvents[clazz]?.remove(this)
    }

    companion object {
        private val forgeEvents = mutableMapOf<Class<*>, SortedSet<ForgeEvent>>()

        fun unregisterEvents() {
            forgeEvents.values.flatten().forEach {
                it.onUnregister()
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