package com.github.synnerz.akutz.listeners

import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.libs.ChatLib
import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.libs.render.Tessellator
import com.github.synnerz.akutz.api.objects.render.Color
import com.github.synnerz.akutz.api.wrappers.Client
import com.github.synnerz.akutz.api.wrappers.World
import com.github.synnerz.akutz.api.wrappers.entity.Entity
import com.github.synnerz.akutz.hooks.ChannelDuplexHook
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import org.lwjgl.util.vector.Vector3f
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/listeners/ClientListener.kt)
 */
object ClientListener {
    private val tasks = CopyOnWriteArrayList<Task>()
    private var currentTitle: Title? = null

    class Title(val title: String, val subTitle: String?, var ticks: Int, val shadow: Boolean)

    class Task(var delay: Int, val callback: () -> Unit)

    fun addTask(delay: Int, callback: () -> Unit) = tasks.add(Task(delay, callback))

    fun setTitle(title: String, subTitle: String?, ticks: Int, shadow: Boolean) {
        currentTitle = Title(title, subTitle, ticks, shadow)
    }

    private fun renderTitle() {
        if (!World.isLoaded() || currentTitle == null || Renderer.sr == null) return

        if (currentTitle!!.ticks <= 0) {
            currentTitle = null
            return
        }

        val x = (Renderer.sr!!.scaledWidth / 2).toFloat()
        val y = (Renderer.sr!!.scaledHeight / 2).toFloat()

        Renderer.beginDraw(Color(255, 255, 255, 255))
        Renderer.translate(x, y)
        Renderer.scale(4f, 4f)
        Renderer.drawString(
            currentTitle!!.title,
            (-(Renderer.getStringWidth(currentTitle!!.title) / 2)).toFloat(),
            -10f,
            currentTitle!!.shadow
        )

        if (currentTitle!!.subTitle !== null) {
            GlStateManager.popMatrix()
            GlStateManager.pushMatrix()
            Renderer.translate(x, y)
            Renderer.scale(2f, 2f)
            Renderer.drawString(
                currentTitle!!.subTitle!!,
                (-(Renderer.getStringWidth(currentTitle!!.subTitle!!) / 2)).toFloat(),
                5f,
                currentTitle!!.shadow
            )
        }

        Renderer.finishDraw()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END) return

        tasks.removeAll {
            if (it.delay-- <= 0) {
                Client.getMinecraft().addScheduledTask { it.callback() }
                true
            } else false
        }

        if (!World.isLoaded()) return

        if (currentTitle !== null) currentTitle!!.ticks--

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
            RenderGameOverlayEvent.ElementType.TEXT -> {
                EventType.RenderOverlay.triggerAll(event)
                renderTitle()
            }
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

        val pos = event.target.blockPos ?: BlockPos(0, 0, 0)
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

        event.manager.channel().pipeline().addAfter(
            "fml:packet_handler",
            "akutz_packet_handler",
            ChannelDuplexHook()
        )
    }

    @SubscribeEvent
    fun onChatEvent(event: ClientChatReceivedEvent) {
        when (event.type.toInt()) {
            in 0..1 -> {
                EventType.Chat.triggerAll(
                    ChatLib.replaceFormatting(event.message.formattedText),
                    event.message.unformattedText,
                    event
                )
            }
            2 -> {
                EventType.ActionBar.triggerAll(
                    ChatLib.replaceFormatting(event.message.formattedText),
                    event.message.unformattedText,
                    event
                )
            }
        }
    }

    @SubscribeEvent
    fun onEntityJoinWorld(event: EntityJoinWorldEvent) {
        EventType.SpawnEntity.triggerAll(Entity(event.entity), event)
    }

    @SubscribeEvent
    fun onRenderTick(event: RenderTickEvent) {
        if (event.phase !== TickEvent.Phase.END) return
        if (Tessellator.pushedMatrix != 0) {
            ChatLib.chat("[Akutz] Looks like you forgot to #finishDraw while drawing with Tessellator, please make sure to finish your draws")
        } else if (Renderer.pushedMatrix != 0) {
            ChatLib.chat("[Akutz] Looks like you forgot to #finishDraw while drawing with Renderer, please make sure to finish your draws")
        }
    }
}