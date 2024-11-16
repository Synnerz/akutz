package com.github.synnerz.akutz.api.events

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/listeners/CancellableEvent.kt)
 */
class Cancelable {
    private var cancelled = false

    @JvmOverloads
    fun setCanceled(newVal: Boolean = true) {
        cancelled = newVal
    }

    @JvmOverloads
    fun setCancelled(newVal: Boolean = true) {
        cancelled = newVal
    }

    fun isCancelable() = true
    fun isCancellable() = true

    fun isCancelled() = cancelled
    fun isCanceled() = cancelled
}