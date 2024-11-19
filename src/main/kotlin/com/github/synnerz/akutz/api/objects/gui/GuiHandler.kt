package com.github.synnerz.akutz.api.objects.gui

import com.github.synnerz.akutz.api.wrappers.Client
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/objects/gui/GuiHandler.kt)
 */
object GuiHandler {
    private var pendingGui: GuiScreen? = null

    @JvmStatic
    fun openGui(gui: GuiScreen) {
        pendingGui = gui
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase !== TickEvent.Phase.END || pendingGui == null) return

        Client.getMinecraft().displayGuiScreen(pendingGui)
        pendingGui = null
    }
}