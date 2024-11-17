package com.github.synnerz.akutz.api.libs.render

import com.github.synnerz.akutz.api.libs.ChatLib
import com.github.synnerz.akutz.api.objects.render.Color
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
object Renderer {
    private var lineWidth: Float = 1f
    private var pushedMatrix = false
    private val tess = MCTessellator.getInstance()
    private val worldRen = tess.worldRenderer

    @JvmStatic
    fun getTessellator() = tess

    @JvmStatic
    fun getWorldRenderer() = worldRen

    @JvmStatic
    fun getFontRenderer() = Minecraft.getMinecraft().fontRendererObj

    @JvmStatic
    fun getRenderManager() = Minecraft.getMinecraft().renderManager

    @JvmStatic
    fun getStringWidth(text: String) = getFontRenderer().getStringWidth(text)

    @JvmOverloads
    @JvmStatic
    fun prepareDraw(color: Color, pushMatrix: Boolean = true) = apply {
        if (pushMatrix) GlStateManager.pushMatrix()
        pushedMatrix = pushMatrix

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(
            color.getRf().toFloat(),
            color.getGf().toFloat(),
            color.getBf().toFloat(),
            color.getAf().toFloat()
        )
    }

    @JvmOverloads
    @JvmStatic
    fun finishDraw() = apply {
        if (pushedMatrix) GlStateManager.popMatrix()
        pushedMatrix = false
        
        lineWidth(1f)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    @JvmStatic
    fun lineWidth(width: Float) = apply {
        if (width != lineWidth) glLineWidth(width)
        lineWidth = width
    }

    @JvmStatic
    fun translate(x: Float, y: Float, z: Float = 0f) = apply {
        GlStateManager.translate(x, y, z)
    }

    @JvmStatic
    fun scale(scaleX: Float, scaleY: Float = scaleX) = apply {
        GlStateManager.scale(scaleX, scaleY, 1f)
    }

    @JvmStatic
    fun rotate(angle: Float) = apply {
        GlStateManager.rotate(angle, 0f, 0f, 1f)
    }

    @JvmStatic
    fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) = apply {
        worldRen.begin(1, DefaultVertexFormats.POSITION)
        worldRen.pos(x1, y1, 0.0).endVertex()
        worldRen.pos(x2, y2, 0.0).endVertex()
        tess.draw()
    }

    @JvmOverloads
    @JvmStatic
    fun drawRectangle(x: Double, y: Double, w: Double, h: Double, solid: Boolean = true) = apply {
        worldRen.begin(if (solid) 6 else 2, DefaultVertexFormats.POSITION);
        worldRen.pos(x, y + h, 0.0).endVertex()
        worldRen.pos(x + w, y + h, 0.0).endVertex()
        worldRen.pos(x + w, y, 0.0).endVertex()
        worldRen.pos(x, y, 0.0).endVertex()
        tess.draw()
    }

    @JvmOverloads
    @JvmStatic
    fun drawArc(
        x: Double,
        y: Double,
        xr: Double,
        yr: Double,
        start: Double = 0.0,
        end: Double = PI * 2,
        solid: Boolean = true,
        segments: Int = 10
    ) = apply {
        val a1 = if (start < end) start else end
        val a2 = if (start < end) end else start
        val l = segments.coerceAtLeast(1)
        val da = (a2 - a1) / l
        worldRen.begin(if (solid) 6 else 3, DefaultVertexFormats.POSITION)
        worldRen.pos(x + cos(a1) * xr, y - sin(a1) * yr, 0.0).endVertex()
        for (i in 1..l) {
            val a = a1 + da * i
            worldRen.pos(x + cos(a) * xr, y - sin(a) * yr, 0.0).endVertex()
        }
        tess.draw()
    }

    @JvmOverloads
    @JvmStatic
    fun drawRoundRectangle(
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        r: Double,
        solid: Boolean = true,
        segments: Int = 5
    ) = apply {
        drawArc(x + r, y + r, r, r, PI / 2, PI, solid, segments)
        drawArc(x + w - r, y + r, r, r, 0.0, PI / 2, solid, segments)
        drawArc(x + r, y + h - r, r, r, PI, PI * 3 / 2, solid, segments)
        drawArc(x + w - r, y + h - r, r, r, PI * 3 / 2, PI * 2, solid, segments)

        if (solid) {
            drawRectangle(x + r, y, w - 2 * r, h, true)
            // drawRectangleVertex(x, y + r, w, h - 2 * r, true)
            drawRectangle(x, y + r, r, h - 2 * r, true)
            drawRectangle(x + w - r, y + r, r, h - 2 * r, true)
        } else {
            drawLine(x + r, y, x + w - r, y)
            drawLine(x + r, y + h, x + w - r, y + h)
            drawLine(x, y + r, x, y + h - r)
            drawLine(x + w, y + r, x + w, y + h - r)
        }
    }

    @JvmOverloads
    @JvmStatic
    fun drawString(text: String, x: Float, y: Float, shadow: Boolean = false) = apply {
        val fr = getFontRenderer()
        var _y = y
        text.split('\n').forEach {
            fr.drawString(it, x, _y, Color.WHITE.asARGB().toInt(), shadow)
            _y += fr.FONT_HEIGHT;
        }
    }

    private val outlinedStringRegex = "^".toRegex(RegexOption.MULTILINE)

    @JvmStatic
    fun drawOutlinedString(text: String, x: Float, y: Float) = apply {
        val str = ChatLib.removeFormatting(text).replace(outlinedStringRegex, "&0")
        drawString(str, x + 1, y + 0)
        drawString(str, x - 1, y + 0)
        drawString(str, x + 0, y + 1)
        drawString(str, x + 0, y - 1)
        drawString(text, x, y)
    }

    @JvmStatic
    private fun getPoint(vertices: Array<out List<Double>>, index: Int) =
        if (index >= vertices.size) vertices[index - vertices.size] else vertices[index]

    @JvmOverloads
    @JvmStatic
    fun drawPolygon(vararg vertices: List<Double>, solid: Boolean = true, isConvex: Boolean?) = apply {
        var area = 0.0
        for (i in vertices.indices) {
            area += getPoint(vertices, i + 0)[0] * getPoint(vertices, i + 1)[1]
            area -= getPoint(vertices, i + 0)[1] * getPoint(vertices, i + 1)[0]
        }
        if (area < 0) vertices.reverse()

        var doStencil: Boolean
        if (!solid) doStencil = false
        else if (isConvex == null) {
            var sign = 0.0
            doStencil = false
            for (i in vertices.indices) {
                val a = getPoint(vertices, i + 0)
                val b = getPoint(vertices, i + 1)
                val c = getPoint(vertices, i + 2)
                val s = ((b[0] - a[0]) * (c[1] - b[1]) - (b[1] - a[1]) * (c[0] - b[0])).sign
                if (i == 0) sign = s
                else if (sign != s) {
                    doStencil = true
                    break
                }
            }
        } else doStencil = !isConvex

        if (doStencil) {
            GlStateManager.pushAttrib()
            GlStateManager.disableDepth()
            glClearStencil(0)

            glClear(GL_STENCIL_BUFFER_BIT)
            glEnable(GL_STENCIL_TEST)
            GlStateManager.colorMask(false, false, false, false)
            glStencilOp(GL_KEEP, GL_KEEP, GL_INVERT)
            glStencilFunc(GL_ALWAYS, 0x1, 0x1)
        }

        worldRen.begin(if (solid) 6 else 2, DefaultVertexFormats.POSITION)
        for (p in vertices) worldRen.pos(p[0], p[1], 0.0).endVertex()
        tess.draw()

        if (doStencil) {
            GlStateManager.colorMask(true, true, true, true)
            glStencilFunc(GL_EQUAL, 0x1, 0x1)

            worldRen.begin(if (solid) 6 else 2, DefaultVertexFormats.POSITION)
            for (p in vertices) worldRen.pos(p[0], p[1], 0.0).endVertex()
            tess.draw()

            glDisable(GL_STENCIL_TEST)
            GlStateManager.popAttrib()
        }
    }

    @JvmField
    val screen = object {
        private var sr: ScaledResolution? = null

        @SubscribeEvent
        fun onRenderOverlayPre(event: TickEvent.RenderTickEvent) {
            if (event.phase != TickEvent.Phase.START) return
            if (sr != null && !Display.wasResized()) return
            sr = ScaledResolution(Minecraft.getMinecraft())
            // TODO: fire off an event
        }

        fun getWidth() = sr?.scaledWidth ?: 0

        fun getHeight() = sr?.scaledHeight ?: 0

        fun getScale() = sr?.scaleFactor ?: 1
    }
}