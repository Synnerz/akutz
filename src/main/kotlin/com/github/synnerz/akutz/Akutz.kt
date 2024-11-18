package com.github.synnerz.akutz

import com.github.synnerz.akutz.api.events.ForgeEvent
import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.command.AkutzCommand
import com.github.synnerz.akutz.engine.impl.Impl
import com.github.synnerz.akutz.engine.module.ModuleManager
import com.github.synnerz.akutz.listeners.ClientListener
import com.github.synnerz.akutz.listeners.WorldListener
import com.google.gson.Gson
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import java.io.File

@Mod(
    modid = Akutz.MOD_ID,
    name = Akutz.MOD_NAME,
    version = Akutz.VERSION
)
class Akutz {
    companion object {
        const val MOD_ID = "akutz"
        const val MOD_NAME = "Akutz"
        const val VERSION = "1.0.0"
        @JvmStatic
        val configLocation = File("./config/Akutz/")
        val gson = Gson()
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        ModuleManager.setup()
        Impl.setup()
        ClientCommandHandler.instance.registerCommand(AkutzCommand)
        listOf(
            ForgeEvent,
            ClientListener,
            WorldListener,
            Renderer
        ).forEach(MinecraftForge.EVENT_BUS::register)
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        // TODO: run this on a different thread (?)
        ModuleManager.start()

        Runtime.getRuntime().addShutdownHook(Thread(Impl::shutdown))
    }
}
