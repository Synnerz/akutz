package com.github.synnerz.akutz.api.objects.keybind

import com.github.synnerz.akutz.api.wrappers.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/objects/keybind/KeyBindHandler.kt)
 */
object KeybindHandler {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @JvmStatic
    private val keyBinds = CopyOnWriteArrayList<Keybind>()

    @JvmStatic
    fun registerKeybind(keybind: Keybind) {
        keyBinds.add(keybind)
    }

    @JvmStatic
    fun unregisterKeybind(keybind: Keybind) {
        keyBinds.remove(keybind)
    }

    @JvmStatic
    fun clearKeybinds() = keyBinds.clear()

    @JvmStatic
    fun getKeybinds() = keyBinds

    @JvmStatic
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!World.isLoaded() || event.phase == TickEvent.Phase.END) return
        keyBinds.forEach { it.onTick() }
    }
}