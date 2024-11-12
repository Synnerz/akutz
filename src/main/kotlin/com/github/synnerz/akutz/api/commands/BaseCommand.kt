package com.github.synnerz.akutz.api.commands

import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

abstract class BaseCommand(private val name: String, private val aliases: List<String> = emptyList()) : CommandBase() {
    final override fun getCommandName(): String = name
    final override fun getCommandAliases(): List<String> = aliases
    final override fun getRequiredPermissionLevel() = 0

    open fun getCommandUsage(player: EntityPlayerSP): String = "/$commandName"
    abstract fun processCommand(player: EntityPlayerSP, args: Array<String>)

    final override fun processCommand(sender: ICommandSender, args: Array<String>) = processCommand(sender as EntityPlayerSP, args)
    final override fun getCommandUsage(sender: ICommandSender) = getCommandUsage(sender as EntityPlayerSP)
}