package com.github.synnerz.akutz.api.events

import net.minecraftforge.fml.common.eventhandler.Event

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/triggers/EventTrigger.kt)
 */
class CancelableEvent(
    method: (args: Array<out Any?>) -> Unit,
    type: EventType
) : EventTrigger(method, type) {
    private var triggerIfCanceled = true

    fun triggerIfCanceled(bool: Boolean) = apply {
        this.triggerIfCanceled = bool
    }

    override fun trigger(args: Array<out Any?>) {
        val isCanceled = when (val event = args.lastOrNull()) {
            is Cancelable -> event.isCanceled()
            is Event -> event.isCanceled
            else -> throw IllegalArgumentException(
                "Event for trigger ${type.name} was not found, instead it found ${event?.javaClass?.name ?: "null"}"
            )
        }

        if (triggerIfCanceled || !isCanceled)
            callMethod(args)
    }
}