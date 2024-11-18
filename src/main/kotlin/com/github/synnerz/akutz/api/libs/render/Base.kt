package com.github.synnerz.akutz.api.libs.render

import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.libs.ChatLib
import com.github.synnerz.akutz.api.objects.render.Color
import com.github.synnerz.akutz.mixin.AccessorRenderManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import net.minecraft.client.renderer.Tessellator as MCTessellator

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/libs/renderer/Renderer.kt)
 */
open class Base {
    var partialTicks = 0f
    private var lineWidth: Float = 1f
    private var pushedMatrix = false

    protected val tess = MCTessellator.getInstance()
    protected val worldRen = tess.worldRenderer
    protected val renderManager = Minecraft.getMinecraft().renderManager as AccessorRenderManager
    protected val fontRenderer = Minecraft.getMinecraft().fontRendererObj

    fun getTessellator() = tess

    fun getWorldRenderer() = worldRen

    fun getFontRenderer() = fontRenderer

    fun getRenderManager() = renderManager

    fun color(color: Color) = apply {
        GlStateManager.color(
            color.getRf().toFloat(),
            color.getGf().toFloat(),
            color.getBf().toFloat(),
            color.getAf().toFloat()
        )
    }

    open fun prepareDraw(color: Color, pushMatrix: Boolean) = apply {
        if (pushMatrix) GlStateManager.pushMatrix()
        pushedMatrix = pushMatrix
        this.color(color)
    }

    open fun finishDraw() = apply {
        if (pushedMatrix) GlStateManager.popMatrix()
        pushedMatrix = false
        lineWidth(1f)
    }

    fun lineWidth(width: Float) = apply {
        if (width != lineWidth) glLineWidth(width)
        lineWidth = width
    }

    @JvmField
    val screen = object {
        protected var sr: ScaledResolution? = null

        @SubscribeEvent
        fun onRenderOverlayPre(event: TickEvent.RenderTickEvent) {
            if (event.phase != TickEvent.Phase.START) return
            partialTicks = event.renderTickTime
            if (sr != null && !Display.wasResized()) return

            sr = ScaledResolution(Minecraft.getMinecraft())
            EventType.ScreenResize.triggerAll(sr)
        }

        fun getWidth() = sr?.scaledWidth ?: 0

        fun getHeight() = sr?.scaledHeight ?: 0

        fun getScale() = sr?.scaleFactor ?: 1
    }
}