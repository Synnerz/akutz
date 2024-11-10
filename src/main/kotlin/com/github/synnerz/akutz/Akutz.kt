package com.github.synnerz.akutz

import com.github.synnerz.akutz.engine.JSImpl
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
        val configLocation = File("./config/Akutz/")
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        JSImpl.setup()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        if (!configLocation.exists()) configLocation.mkdirs()
        // is this even good ?
        // TODO: run this on a different thread (?)
        for (res1 in configLocation.listFiles()!!) {
            if (!res1.isDirectory) return

            for (res2 in res1.listFiles()!!) {
                if (res2.extension != "js") continue
                println("EXECUTING JS FILE: ${res2.name}")
                JSImpl.execute(res2)
            }
        }
    }
}
