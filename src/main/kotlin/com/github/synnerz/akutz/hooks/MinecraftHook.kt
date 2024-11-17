package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.EventType
import net.minecraft.client.gui.GuiScreen

object MinecraftHook {
    fun triggerGuiClosed(gui: GuiScreen) {
        EventType.GuiClosed.triggerAll(gui)
    }

    fun triggerGameLoad() {
        EventType.GameLoad.triggerAll()
    }
}