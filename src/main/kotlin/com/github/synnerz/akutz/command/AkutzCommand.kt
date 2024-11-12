package com.github.synnerz.akutz.command

import com.github.synnerz.akutz.api.commands.BaseCommand
import com.github.synnerz.akutz.engine.impl.Impl
import com.github.synnerz.akutz.engine.module.ModuleManager
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.ChatComponentText

object AkutzCommand : BaseCommand("Akutz", listOf("akutz", "az", "akz")) {
    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args[0] == "load") {
            Impl.setup()
            ModuleManager.setup()
            player.addChatMessage(ChatComponentText("Successfully loaded akutz"))
            return
        } else if (args[0] == "unload") {
            Impl.clear()
            player.addChatMessage(ChatComponentText("Successfully unloaded akutz"))
            return
        }
    }
}