package com.github.synnerz.akutz.api.commands

import com.github.synnerz.akutz.api.events.EventTrigger
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraftforge.client.ClientCommandHandler

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/commands/Command.kt)
 */
class Command @JvmOverloads constructor(
    private val event: EventTrigger,
    private val name: String,
    private var aliases: MutableList<String>,
    private val overrideExisting: Boolean = false
) : BaseCommand(name, aliases) {
    @JvmField
    var initialized: Boolean = false

    override fun getCommandAliases(): List<String> = aliases

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) = event.trigger(args)

    fun register() {
        if (initialized) return

        initialized = true

        if (name in ClientCommandHandler.instance.commands.keys && !overrideExisting)
            return println("Command with name $name already exists, since [overrideExisting] parameter is set to false it will not override the command.")

        ClientCommandHandler.instance.registerCommand(this)
        activeCommands[name] = this
    }

    fun unregister() {
        if (!initialized) return

        initialized = false
        ClientCommandHandler.instance.commandSet.remove(this)
        ClientCommandHandler.instance.commands.remove(name)
        activeCommands.remove(name)
    }

    companion object {
        @JvmField
        val activeCommands = mutableMapOf<String, Command>()
    }
}