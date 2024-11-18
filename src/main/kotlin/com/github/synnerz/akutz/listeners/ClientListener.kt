package com.github.synnerz.akutz.listeners

import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.wrappers.World
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import org.lwjgl.util.vector.Vector3f

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

    @SubscribeEvent
    fun onBlockHighlight(event: DrawBlockHighlightEvent) {
        if (event.target == null && event.target.blockPos == null) return

        val pos = event.target.blockPos
        val vec = Vector3f(
            pos.x.toFloat(),
            pos.y.toFloat(),
            pos.z.toFloat()
        )

        EventType.BlockHighlight.triggerAll(vec, event)
    }

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        val action: String = when(event.action) {
            PlayerInteractEvent.Action.LEFT_CLICK_BLOCK -> return
            PlayerInteractEvent.Action.RIGHT_CLICK_AIR -> "RIGHT_CLICK_EMPTY"
            PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK -> "RIGHT_CLICK_BLOCK"
            null -> "UNKNOWN"
        }

        val pos = event.pos ?: BlockPos(0, 0, 0)

        EventType.PlayerInteract.triggerAll(
            action,
            Vector3f(
                pos.x.toFloat(),
                pos.y.toFloat(),
                pos.z.toFloat()
            ),
            event
        )
    }

    @SubscribeEvent
    fun onClientDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        EventType.ServerDisconnect.triggerAll(event)
    }

    @SubscribeEvent
    fun onNetworkEvent(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        EventType.ServerConnect.triggerAll(event)
    }
}