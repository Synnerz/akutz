package com.github.synnerz.akutz.engine.impl

import com.github.synnerz.akutz.api.events.BaseEvent
import com.github.synnerz.akutz.api.events.EventType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/engine/langs/js/JSLoader.kt)
 */
object Loader {
    private val events = ConcurrentHashMap<EventType, ConcurrentSkipListSet<BaseEvent>>()

    private fun createEventSet() = ConcurrentSkipListSet<BaseEvent>()

    fun execute(type: EventType, args: Array<out Any?>) {
        events[type]?.forEach { it.trigger(args) }
    }

    fun addEvent(event: BaseEvent) {
        events.getOrPut(event.type, ::createEventSet).add(event)
    }

    fun clearEvents() {
        events.clear()
    }

    fun removeEvent(event: BaseEvent) {
        events[event.type]?.remove(event)
    }
}