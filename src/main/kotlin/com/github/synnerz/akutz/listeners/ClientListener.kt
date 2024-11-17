package com.github.synnerz.akutz.listeners

import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.wrappers.World
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/listeners/ClientListener.kt)
 */
object ClientListener {
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase !== TickEvent.Phase.END || !World.isLoaded()) return

        EventType.Tick.triggerAll()
    }

    @SubscribeEvent
    fun onDrawScreenEvent(event: DrawScreenEvent) {
        EventType.PostGuiRender.triggerAll(event.mouseX, event.mouseY, event.gui)
    }

    @SubscribeEvent
    fun onGuiOpened(event: GuiOpenEvent) {
        EventType.GuiOpened.triggerAll(event)
    }

    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGameOverlayEvent.Pre) {
        when (event.type) {
            RenderGameOverlayEvent.ElementType.TEXT -> EventType.RenderOverlay.triggerAll(event)
            RenderGameOverlayEvent.ElementType.CHAT -> EventType.RenderChat.triggerAll(event)
            else -> null
        }
    }

    @SubscribeEvent
    fun onGuiRender(event: GuiScreenEvent.BackgroundDrawnEvent) {
        EventType.GuiRender.triggerAll(
            event.mouseX,
            event.mouseY,
            event.gui
        )
    }
}