package com.github.synnerz.akutz.api.events

import com.github.synnerz.akutz.api.objects.state.StateVar
import com.github.synnerz.akutz.engine.impl.Impl
import com.github.synnerz.akutz.engine.impl.Loader

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/triggers/Trigger.kt)
 */
abstract class BaseEvent protected constructor(
    var method: (args: Array<out Any?>) -> Unit,
    var type: EventType
) : Comparable<BaseEvent> {
    var registered: Boolean = false
    var actuallyRegistered: Boolean = false
    private var priority: Priority = Priority.NORMAL
    var enabled: StateVar<Boolean> = StateVar(true)

    init {
        @Suppress("LeakingThis")
        register()
    }

    fun setPriority(prio: Priority) = apply {
        this.priority = prio

        if (actuallyRegistered) {
            unregister()
            register()
        }
    }

    fun setEnabled(state: StateVar<Boolean>) = apply {
        enabled = state
        enabled.listen { b -> update(b) }
        update(enabled.get())
    }

    private fun update(bool: Boolean) {
        if (!registered) return
        if (bool) {
            if (!actuallyRegistered) onRegister()
            actuallyRegistered = true
        } else {
            if (actuallyRegistered) onUnregister()
            actuallyRegistered = false
        }
    }

    protected open fun onRegister() = apply {
        Loader.addEvent(this)
    }

    protected open fun onUnregister() = apply {
        Loader.removeEvent(this)
    }

    fun register() = apply {
        if (registered) return this
        if (!actuallyRegistered && enabled.get()) {
            actuallyRegistered = true
            onRegister()
        }
        registered = true
    }

    fun unregister() = apply {
        if (!registered) return this
        if (actuallyRegistered) onUnregister()
        registered = false;
        actuallyRegistered = false;
    }

    open fun isRegistered() : Boolean = registered

    open fun isActuallyRegistered(): Boolean = actuallyRegistered

    // Leaving this as "open" so EventTriggers with custom trigger method can override
    open fun trigger(args: Array<out Any?>) {
        callMethod(args)
    }

    fun callMethod(args: Array<out Any?>) {
        if (!Impl.isLoaded()) return
        // just in case
        if (!registered) return

        try {
            method(args)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun compareTo(other: BaseEvent): Int {
        val ordCmp = priority.ordinal - other.priority.ordinal
        return if (ordCmp == 0)
            hashCode() - other.hashCode()
        else ordCmp
    }

    override fun toString(): String = "Event{type=\"${this.type}\", registered=\"${isRegistered()}\", actuallyRegistered=\"${isActuallyRegistered()}\", priority=\"${priority.name}\"}"

    enum class Priority {
        //LOWEST IS RAN LAST
        HIGHEST,
        HIGH,
        NORMAL,
        LOW,
        LOWEST
    }
}