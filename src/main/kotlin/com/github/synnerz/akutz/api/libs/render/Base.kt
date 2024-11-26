package com.github.synnerz.akutz.api.libs.render

import com.github.synnerz.akutz.api.objects.render.Color
import com.github.synnerz.akutz.mixin.AccessorRenderManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.WorldRenderer
import org.lwjgl.opengl.GL11.*
import net.minecraft.client.renderer.Tessellator as MCTessellator

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/libs/renderer/Renderer.kt)
 */
open class Base {
    var partialTicks = 0f
    protected var lineWidth: Float = 1f
    var pushedMatrix = 0
    protected var prevCol: Color = Color.WHITE

    protected val tess: MCTessellator by lazy { MCTessellator.getInstance() }
    protected val worldRen: WorldRenderer by lazy { tess.worldRenderer }
    protected val rendManager: AccessorRenderManager by lazy { Minecraft.getMinecraft().renderManager as AccessorRenderManager }
    protected val fontRend: FontRenderer by lazy { Minecraft.getMinecraft().fontRendererObj }

    fun getTessellator() = tess

    fun getWorldRenderer() = worldRen

    fun getFontRenderer() = fontRend

    fun getRenderManager() = rendManager

    protected fun _color(color: Color) = apply {
        GlStateManager.color(
            color.getRf().toFloat(),
            color.getGf().toFloat(),
            color.getBf().toFloat(),
            color.getAf().toFloat()
        )
    }

    fun color(color: Color) = apply {
        prevCol = color
        _color(color)
    }

    @JvmOverloads
    fun color(r: Int, g: Int, b: Int, a: Int = 255) = color(Color(r, g, b, a))

    fun setColor(color: Color) = apply {
        this.color(color)
    }

    open fun beginDraw(color: Color, pushMatrix: Boolean) = apply {
        if (pushMatrix) {
            GlStateManager.pushMatrix()
            pushedMatrix = (pushedMatrix shl 1) or 1
        } else pushedMatrix = pushedMatrix shl 1
        this.color(color)
    }

    open fun finishDraw() = apply {
        if (pushedMatrix and 1 == 1) GlStateManager.popMatrix()
        pushedMatrix = pushedMatrix shr 1
        lineWidth(1f)
    }

    fun lineWidth(width: Float) = apply {
        if (width != lineWidth) glLineWidth(width)
        lineWidth = width
    }

    @JvmField
    var screen: Any? = null
}