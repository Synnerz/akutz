package com.github.synnerz.akutz.api.events

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/triggers/SoundPlayTrigger.kt)
 */
class SoundPlayEvent(
    method: (args: Array<out Any?>) -> Unit
) : BaseEvent(method, EventType.SoundPlay) {
    private var soundCriteria: String? = null

    fun setCriteria(criteria: String) = apply {
        this.soundCriteria = criteria
    }

    override fun trigger(args: Array<out Any?>) {
        if (
            args[1] is String &&
            soundCriteria != null &&
            !(args[1] as String).equals(soundCriteria, ignoreCase = true)
            ) return

        callMethod(args)
    }
}