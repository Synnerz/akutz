package com.github.synnerz.akutz.engine.impl

import com.github.synnerz.akutz.api.events.*
import kotlin.reflect.KCallable
import kotlin.reflect.full.memberFunctions

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/engine/IRegister.kt)
 */
object Register {
    val methodMap = mutableMapOf<String, KCallable<*>>()

    fun register(eventType: Any, method: (args: Array<out Any?>) -> Unit) : EventTrigger {
        if (eventType is Class<*>)
            return ForgeEvent(method, eventType)

        require(eventType is String) {
            "register() expects a String or Java Class as its first argument"
        }

        val name = eventType.lowercase()

        val cb = methodMap.getOrPut(name) {
            this::class.memberFunctions.firstOrNull {
                it.name.lowercase() == "register$name"
            } ?: throw NoSuchMethodException("No EventType with type $eventType was found.")
        }

        return cb.call(this, method) as EventTrigger
    }

    fun registerCommand(method: (args: Array<out Any?>) -> Unit): CommandEvent {
        return CommandEvent(method)
    }

    fun registerTick(method: (args: Array<out Any?>) -> Unit): EventTrigger {
        return NormalTrigger(method, EventType.Tick)
    }
}