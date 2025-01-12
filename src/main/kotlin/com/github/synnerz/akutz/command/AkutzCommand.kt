package com.github.synnerz.akutz.command

import com.github.synnerz.akutz.Akutz
import com.github.synnerz.akutz.api.commands.BaseCommand
import com.github.synnerz.akutz.api.libs.ChatLib
import com.github.synnerz.akutz.api.objects.data.PersistentData
import com.github.synnerz.akutz.console.Console
import com.github.synnerz.akutz.engine.impl.Impl
import com.github.synnerz.akutz.engine.module.ModuleGui
import com.github.synnerz.akutz.engine.module.ModuleManager
import com.github.synnerz.akutz.gui.Config
import com.github.synnerz.akutz.gui.ConfigGui
import net.minecraft.client.entity.EntityPlayerSP
import java.awt.Desktop

object AkutzCommand : BaseCommand("Akutz", listOf("akutz", "az", "akz")) {
    private var reloads = 0

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty()) return ChatLib.chat(getHelp())

        when (args[0]) {
            "load", "reload" -> {
                PersistentData.INSTANCES.forEach{ it.save() }
                PersistentData.INSTANCES.clear()
                Impl.shutdown()
                Impl.setup()
                if (Config.get("autoUpdate")) reloads++
                if (reloads >= 5) {
                    ChatLib.chat("§b§lAkutz§r: §bDetected \"5\" \"/az (re)load\" done while having Auto Update Modules enabled, it is recommended to disable this while you are debugging your module")
                    reloads = 0
                }
                ModuleManager.setup()
                ModuleManager.start()

                ChatLib.chat("§b§lAkutz§r: §bLoaded modules.")
            }
            "unload" -> {
                if (!Impl.isLoaded()) return ChatLib.chat("§b§lAkutz§r: §cModules have already been unloaded.")

                PersistentData.INSTANCES.forEach{ it.save() }
                PersistentData.INSTANCES.clear()
                Impl.shutdown()
                ChatLib.chat("§b§lAkutz§r: §cUnloaded modules.")
            }
            "file", "files" -> {
                try {
                    Desktop.getDesktop().open(Akutz.configLocation)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            "import" -> {
                if (args.getOrNull(1).isNullOrEmpty()) return ChatLib.chat("§b§lAkutz§r: §cPlease specify a module name")
                if (ModuleManager.import(args[1])) ChatLib.chat("§b§lAkutz§r: §bSuccessfully installed module \"${args[1]}\"")
                else ChatLib.chat("§b§lAkutz§r: §cThere was a problem installing module \"${args[1]}\"")
            }
            "delete" -> {
                if (args.getOrNull(1).isNullOrEmpty()) return ChatLib.chat("§b§lAkutz§r: §cPlease specify a module name to delete")
                if (ModuleManager.deleteModule(args[1])) ChatLib.chat("§b§lAkutz§r: §bSuccessfully deleted module with name §6${args[1]}")
                else ChatLib.chat("§b§lAkutz§r: §cThere was a problem deleting module with name §6${args[1]}")
            }
            "module", "modules" -> {
                ModuleGui.open()
            }
            "console" -> {
                Console.showConsole()
            }
            "config" -> {
                ConfigGui.open()
            }
            else -> ChatLib.chat(getHelp())
        }
    }

    private fun getHelp() = """
        &b&m${ChatLib.getChatBreak()}
        §b§lAliases§r: §eakutz, az, akz
        §a/akutz load §e- §bLoads the modules in the folder.
        §a/akutz unload §e- §bUnloads the modules that were loaded.
        §a/akutz reload §e- §b(Alias for §a/akutz load§b does the same thing).
        §a/akutz file§8(s) §e- §bOpens the modules folder.
        §a/akutz import §e- §bImports a module.
        §a/akutz delete §e- §bDeletes an installed module.
        §a/akutz module§8(s) §e- §bOpens a gui that displays all the modules you currently have installed.
        §a/akutz console §e- §bOpens a console for error debugging.
        §a/akutz config §e- §bOpens akutz configurations.
        §a/akutz help §e- §bDisplays this message in chat.
        &b&m${ChatLib.getChatBreak()}
    """.trimIndent()
}