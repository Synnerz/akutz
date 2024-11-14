package com.github.synnerz.akutz.api.events

import com.github.synnerz.akutz.api.commands.Command

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

    fun reInstance() {
        if (command != null && !command!!.initialized) return

        command?.unregister()
        command = Command(this, commandName, aliases)
        command!!.register()
    }
}