package com.github.synnerz.akutz.api.libs.render

import com.github.synnerz.akutz.api.objects.render.Color
import com.github.synnerz.akutz.mixin.AccessorRenderManager
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*
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
    protected val rendManager = Minecraft.getMinecraft().renderManager as AccessorRenderManager
    protected val fontRend = Minecraft.getMinecraft().fontRendererObj

    fun getTessellator() = tess

    fun getWorldRenderer() = worldRen

    fun getFontRenderer() = fontRend

    fun getRenderManager() = rendManager

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
}