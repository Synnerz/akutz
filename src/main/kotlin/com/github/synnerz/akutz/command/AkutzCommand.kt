package com.github.synnerz.akutz.command

import com.github.synnerz.akutz.api.commands.BaseCommand
import com.github.synnerz.akutz.engine.impl.Impl
import com.github.synnerz.akutz.engine.module.ModuleManager
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.ChatComponentText

object AkutzCommand : BaseCommand("Akutz", listOf("akutz", "az", "akz")) {
    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty()) return showHelp(player)
        when (args[0]) {
            "load", "reload" -> {
                Impl.clear()
                Impl.setup()
                ModuleManager.setup()
                player.addChatMessage(ChatComponentText("Successfully loaded akutz"))
            }
            "unload" -> {
                Impl.clear()
                player.addChatMessage(ChatComponentText("Successfully unloaded akutz"))
            }
            else -> player.addChatMessage(ChatComponentText("Unknown argument " + args[0]))
        }
    }

    private fun showHelp(player: EntityPlayerSP) {
        player.addChatMessage(ChatComponentText("Akutz: the BETTER minecraft javascript bindings"))
        player.addChatMessage(ChatComponentText("/akutz load"))
        player.addChatMessage(ChatComponentText("/akutz unload"))
    }
}