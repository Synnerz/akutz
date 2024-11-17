package com.github.synnerz.akutz.api.commands

import com.github.synnerz.akutz.api.events.BaseEvent
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraftforge.client.ClientCommandHandler

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/commands/Command.kt)
 */
class Command @JvmOverloads constructor(
    private val event: BaseEvent,
    private val name: String,
    private var aliases: MutableList<String>,
    private val overrideExisting: Boolean = false,
    private val tabCompletions: MutableList<String>,
    private val cb: ((Array<out String>) -> ArrayList<String>)? = null
) : BaseCommand(name, aliases) {
    @JvmField
    var initialized: Boolean = false

    override fun getCommandAliases(): List<String> = aliases

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) = event.trigger(args)

    override fun addTabCompletionOptions(
        sender: ICommandSender?,
        args: Array<out String>?,
        pos: BlockPos?
    ): MutableList<String> {
        return cb?.invoke(args ?: arrayOf())?.toMutableList() ?: tabCompletions
    }

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
        if (aliases.isNotEmpty()) aliases.forEach(ClientCommandHandler.instance.commands::remove)

        activeCommands.remove(name)
    }

    companion object {
        @JvmField
        val activeCommands = mutableMapOf<String, Command>()
    }
}