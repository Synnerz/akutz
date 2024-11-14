package com.github.synnerz.akutz.api.events

import com.github.synnerz.akutz.engine.impl.Impl
import com.github.synnerz.akutz.engine.impl.Loader

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/triggers/Trigger.kt)
 */
abstract class EventTrigger protected constructor(
    var method: (args: Array<out Any?>) -> Unit,
    var type: EventType
) : Comparable<EventTrigger> {
    private var registered: Boolean = false
    private var priority: Priority = Priority.NORMAL

    init {
        @Suppress("LeakingThis")
        register()
    }

    fun setPriority(prio: Priority) = apply {
        this.priority = prio

        unregister()
        register()
    }

    open fun register() = apply {
        if (registered) return this

        registered = true
        Loader.addEvent(this)
    }

    open fun unregister() = apply {
        if (!registered) return this

        registered = false
        Loader.removeEvent(this)
    }

    open fun isRegistered() : Boolean = registered

    abstract fun trigger(args: Array<out Any?>)

    fun callMethod(args: Array<out Any?>) {
        if (!Impl.isLoaded()) return

        try {
            method(args)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun compareTo(other: EventTrigger): Int {
        val ordCmp = priority.ordinal - other.priority.ordinal
        return if (ordCmp == 0)
            hashCode() - other.hashCode()
        else ordCmp
    }

    enum class Priority {
        //LOWEST IS RAN LAST
        HIGHEST,
        HIGH,
        NORMAL,
        LOW,
        LOWEST
    }
}