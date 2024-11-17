package com.github.synnerz.akutz.api.events

import com.github.synnerz.akutz.api.libs.ChatLib
import net.minecraftforge.client.event.ClientChatReceivedEvent

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/triggers/ChatTrigger.kt)
 */
class ChatEvent(
    method: (args: Array<out Any?>) -> Unit
) : BaseEvent(method, EventType.Chat) {
    private lateinit var chatCriteria: Any
    private var formatted: Boolean = false
    private var caseInsensitive: Boolean = false
    private lateinit var criteriaPattern: Regex
    private val parameters = mutableListOf<Parameter?>()
    private var triggerIfCanceled: Boolean = true

    fun triggerIfCanceled(bool: Boolean) = apply {
        this.triggerIfCanceled = bool
    }

    // TODO: unfinished
    fun setCriteria(criteria: Any) = apply {
        this.chatCriteria = criteria
    }

    fun addParameter(param: String) = apply {
        parameters.add(Parameter.getParameterByName(param))
    }

    fun addParameter(vararg params: String) = apply {
        params.forEach(::addParameter)
    }

    fun setParameter(param: String) = apply {
        parameters.clear()
        addParameter(param)
    }

    fun setParameters(vararg param: String) = apply {
        parameters.clear()
        addParameter(*param)
    }

    fun setStart() = apply {
        addParameter("start")
    }

    fun setEnd() = apply {
        addParameter("end")
    }

    fun setContains() = apply {
        addParameter("contains")
    }

    fun setExact() = apply {
        parameters.clear()
    }

    fun setCaseInsensitive() = apply {
        caseInsensitive = true
        if (::chatCriteria.isInitialized)
            setCriteria(chatCriteria)
    }

    override fun trigger(args: Array<out Any?>) {
        require(args[0] is String && args[1] is ClientChatReceivedEvent) {
            "Argument 1 must be a String, Argument 2 must be a ClientChatReceivedEvent"
        }

        val chatEvent = args[1] as ClientChatReceivedEvent
        if (!triggerIfCanceled && chatEvent.isCanceled) return

        val chatMessage = ChatLib.getChatMessage(chatEvent, formatted)
        // TODO: this part should use [criteriaPattern] and not [chatCriteria]
        val variables = if (::chatCriteria.isInitialized) matchesCriteria(chatMessage) else ArrayList()
        if (variables == null) return

        variables.add(chatEvent)
        callMethod(variables.toTypedArray())
    }

    // TODO: whenever setCriteria is finished
    private fun matchesCriteria(msg: String) : MutableList<Any> {
        return mutableListOf()
    }

    /**
     * The parameter to match chat criteria to.
     * Location parameters
     * - contains
     * - start
     * - end
     */
    enum class Parameter constructor(vararg names: String) {
        CONTAINS("<c>", "<contains>", "c", "contains"),
        START("<s>", "<start>", "s", "start"),
        END("<e>", "<end>", "e", "end");

        var names: List<String> = names.asList()

        companion object {
            fun getParameterByName(name: String) =
                entries.find { param ->
                    param.names.any { it.lowercase() == name }
                }
        }
    }
}