package com.github.synnerz.akutz.api.events

import com.caoccao.javet.values.reference.V8ValueRegExp
import com.github.synnerz.akutz.api.libs.ChatLib
import net.minecraftforge.client.event.ClientChatReceivedEvent

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/triggers/ChatTrigger.kt)
 */
class ChatEvent(
    method: (args: Array<out Any?>) -> Unit
) : BaseEvent(method, EventType.Chat) {
    private var caseInsensitive: Boolean = false
    private var formatted: Boolean = false
    private var mode: Int = 0
    private var regex: Regex? = null
    private val parameters = mutableSetOf<Parameter>()
    private var triggerIfCanceled: Boolean = true
    private var rawCritS: String? = null
    private var rawCritR: V8ValueRegExp? = null
    private var dirty = true

    private val HAS_GROUP_MATCH = "\\$\\{(?:\\*|\\w+)}".toRegex()
    private val ESCAPE_REGEX = "[/\\-\\\\^$*+?.()|\\[\\]{}]".toRegex()
    private val REPLACE_ANON_MATCH = "\\\$\\{\\*?}".toRegex()
    private val REPLACE_NAMED_MATCH = "\\\$\\{\\w+}".toRegex()
    private val HAS_FORMATTING_CODE = "[&\u00a7]".toRegex()

    private fun mark() = apply { dirty = true }

    private fun updateCriteria() {
        mode = 0
        regex = null
        formatted = true
        if (rawCritS != null) {
            formatted = HAS_FORMATTING_CODE in rawCritS!!
            if (!caseInsensitive && HAS_GROUP_MATCH !in rawCritS!!) mode = 1
            else {
                mode = 2
                val regexStr = rawCritS!!
                    .replace(ESCAPE_REGEX, "\\$0")
                    .replace(REPLACE_ANON_MATCH, "(?:.+)")
                    .replace(REPLACE_NAMED_MATCH, "(.+)")

                val flags = mutableSetOf(RegexOption.UNIX_LINES)
                if (caseInsensitive) flags.add(RegexOption.IGNORE_CASE)

                regex = Regex(regexStr, flags)
            }
        } else if (this.rawCritR != null) {
            this.mode = 2

            val src = ChatLib.replaceFormatting(rawCritR!!.getPropertyString("source"))
            formatted = HAS_FORMATTING_CODE in src

            val flags = mutableSetOf(RegexOption.UNIX_LINES)
            if (caseInsensitive || rawCritR!!.getPropertyBoolean("ignoreCase")) flags.add(RegexOption.IGNORE_CASE)
            if (rawCritR!!.getPropertyBoolean("m")) flags.add(RegexOption.MULTILINE)
            if (rawCritR!!.getPropertyBoolean("s")) flags.add(RegexOption.DOT_MATCHES_ALL)

            regex = Regex(src, flags)
        }
    }

    private fun doesMatchCriteria(str: String): List<Any>? {
        if (mode == 1) {
            if (parameters.size == 0) {
                if (rawCritS != str) return null
            } else {
                val i = str.indexOf(rawCritS!!)
                if (i < 0) return null
                if (parameters.contains(Parameter.START) && i > 0) return null
                if (parameters.contains(Parameter.END) && i < str.length - rawCritS!!.length) return null
                // if (parameters.contains(Parameter.CONTAINS)) {}
            }
        }
        if (mode == 2) {
            val m: MatchResult? =
                if (parameters.size == 0 || parameters.contains(Parameter.START)) regex!!.matchAt(str, 0)
                else regex!!.matchEntire(str)
            if (m == null) return null

            if ((parameters.size == 0 || parameters.contains(Parameter.END)) && m.range.last < str.length - 1) return null
            // if (parameters.contains(Parameter.CONTAINS)) {}
            return m.groupValues.drop(1)
        }
        return listOf()
    }

    fun triggerIfCanceled(bool: Boolean) = apply {
        triggerIfCanceled = bool
    }

    fun setCriteria(criteria: String) = apply {
        rawCritS = ChatLib.replaceFormatting(criteria)
        rawCritR = null
        mark()
    }

    fun setCriteria(criteria: V8ValueRegExp) = apply {
        rawCritS = null
        rawCritR = criteria
        mark()
    }

    fun addParameter(param: String) = apply {
        addParameters(param)
    }

    fun addParameters(vararg params: String) = apply {
        params.forEach { parameters.add(Parameter.getParameterByName(it)) }
        if (mode == 2) mark()
    }

    fun setParameter(param: String) = apply {
        setParameters(param)
    }

    fun setParameters(vararg param: String) = apply {
        parameters.clear()
        addParameters(*param)
    }

    fun setStart() = apply {
        setParameter("start")
    }

    fun setEnd() = apply {
        setParameter("end")
    }

    fun setContains() = apply {
        setParameter("contains")
    }

    fun setExact() = apply {
        parameters.clear()
        if (mode == 2) mark()
    }

    fun setCaseInsensitive() = apply {
        caseInsensitive = true
        mark()
    }

    override fun trigger(args: Array<out Any?>) {
        require(args[0] is String && args[1] is String && args[2] is ClientChatReceivedEvent) {
            "Argument 1, 2 must be a String, Argument 3 must be a ClientChatReceivedEvent"
        }

        if (dirty) updateCriteria()
        dirty = false

        val chatEvent = args[2] as ClientChatReceivedEvent
        if (!triggerIfCanceled && chatEvent.isCanceled) return

        val message = (if (formatted) args[0] else args[1]) as String
        val variables = doesMatchCriteria(message)?.toMutableList() ?: return
        variables.add(chatEvent)

        callMethod(variables.toTypedArray())
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
                } ?: throw IllegalArgumentException("Unknown parameter: $name")
        }
    }
}