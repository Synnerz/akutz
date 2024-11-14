package com.github.synnerz.akutz.engine.impl

import com.github.synnerz.akutz.api.events.EventTrigger
import com.github.synnerz.akutz.api.events.ForgeEvent

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/engine/IRegister.kt)
 */
object Register {
    fun register(eventType: Any, method: (args: Array<out Any?>) -> Unit) : EventTrigger {
        if (eventType is Class<*>)
            return ForgeEvent(method, eventType)

        return "TODO impl this" as EventTrigger
    }
}