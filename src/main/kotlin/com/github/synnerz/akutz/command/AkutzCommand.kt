package com.github.synnerz.akutz.command

import com.github.synnerz.akutz.api.commands.BaseCommand
import com.github.synnerz.akutz.api.libs.ChatLib
import com.github.synnerz.akutz.api.objects.data.PersistantData
import com.github.synnerz.akutz.engine.impl.Impl
import com.github.synnerz.akutz.engine.module.ModuleManager
import net.minecraft.client.entity.EntityPlayerSP

object AkutzCommand : BaseCommand("Akutz", listOf("akutz", "az", "akz")) {
    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty()) return ChatLib.chat(getHelp())

        when (args[0]) {
            "load", "reload" -> {
                PersistantData.INSTANCES.forEach{ it.save() }
                PersistantData.INSTANCES.clear()
                Impl.shutdown()
                ModuleManager.setup()
                Impl.setup()
                ModuleManager.start()
                ChatLib.chat("Akutz was loaded")
            }
            "unload" -> {
                if (!Impl.isLoaded()) return ChatLib.chat("Akutz has already been unloaded")

                PersistantData.INSTANCES.forEach{ it.save() }
                PersistantData.INSTANCES.clear()
                Impl.shutdown()
                ChatLib.chat("Akutz was unloaded")
            }
            else -> ChatLib.chat(getHelp())
        }
    }

    private fun getHelp() = """
        Akutz: the BETTER minecraft javascript framework
        Aliases: akutz, az, akz
        /akutz load - Loads the modules in the folder
        /akutz unload - Unloads the modules that were loaded
        /akutz reload - (Alias for /akutz load does the same thing)
    """.trimIndent()
}