package com.github.synnerz.akutz.api.events

import com.github.synnerz.akutz.api.commands.Command
import com.github.synnerz.akutz.engine.impl.Loader

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/triggers/CommandTrigger.kt)
 */
class CommandEvent(
    method: (args: Array<out Any?>) -> Unit
) : EventTrigger(method, EventType.Command) {
    private lateinit var commandName: String
    private var overrideExisting: Boolean = false
    private val aliases = mutableListOf<String>()
    private var command: Command? = null

    override fun trigger(args: Array<out Any?>) {
        callMethod(args)
    }

    fun setAliases(vararg args: String) = apply {
        check(::commandName.isInitialized) { "Command name must be set first before adding Aliases." }

        aliases.addAll(args)
        reInstance()
    }

    @JvmOverloads
    fun setCommandName(commandName: String, overrideExisting: Boolean = false) = apply {
        this.commandName = commandName
        this.overrideExisting = overrideExisting
        reInstance()
    }

    @JvmOverloads
    fun setName(commandName: String, overrideExisting: Boolean = false) = setCommandName(commandName, overrideExisting)

    override fun register(): EventTrigger {
        if (registered) return this

        command?.register()
        registered = true
        Loader.addEvent(this)
        return this
    }

    override fun unregister(): EventTrigger {
        if (!registered || command == null) return this

        command!!.unregister()
        registered = false
        Loader.removeEvent(this)
        return this
    }

    fun reInstance() {
        if (!registered) return

        unregister()
        command = Command(this, commandName, aliases)
        register()
    }
}