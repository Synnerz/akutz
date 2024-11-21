package com.github.synnerz.akutz.api.libs.render

import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.libs.ChatLib
import com.github.synnerz.akutz.api.objects.render.Color
import com.github.synnerz.akutz.api.wrappers.Client.getMinecraft
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import kotlin.math.*


/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/libs/renderer/Renderer.kt)
 */
object Renderer : Base() {
    fun getStringWidth(text: String) = getFontRenderer().getStringWidth(text)

    fun beginDraw(color: Color) = beginDraw(color, true)

    override fun beginDraw(color: Color, pushMatrix: Boolean) = apply {
        super.beginDraw(color, pushMatrix)
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.disableCull()
    }

    override fun finishDraw() = apply {
        super.finishDraw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.enableCull()
    }

    @JvmOverloads
    fun translate(x: Float, y: Float, z: Float = 0f) = apply {
        GlStateManager.translate(x, y, z)
    }

    @JvmOverloads
    fun scale(scaleX: Float, scaleY: Float = scaleX) = apply {
        GlStateManager.scale(scaleX, scaleY, 1f)
    }

    fun rotate(angle: Float) = apply {
        GlStateManager.rotate(angle, 0f, 0f, 1f)
    }

    fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) = apply {
        worldRen.begin(1, DefaultVertexFormats.POSITION)
        worldRen.pos(x1, y1, 0.0).endVertex()
        worldRen.pos(x2, y2, 0.0).endVertex()
        tess.draw()
    }

    @JvmOverloads
    fun drawRectangle(x: Double, y: Double, w: Double, h: Double, solid: Boolean = true) = apply {
        worldRen.begin(if (solid) 6 else 2, DefaultVertexFormats.POSITION)
        worldRen.pos(x, y + h, 0.0).endVertex()
        worldRen.pos(x + w, y + h, 0.0).endVertex()
        worldRen.pos(x + w, y, 0.0).endVertex()
        worldRen.pos(x, y, 0.0).endVertex()
        tess.draw()
    }

    @JvmOverloads
    fun drawRect(x: Double, y: Double, w: Double, h: Double, solid: Boolean = true) = drawRectangle(x, y, w, h, solid)

    @JvmOverloads
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
        if (solid) worldRen.pos(x, y, 0.0).endVertex()

        worldRen.pos(x + cos(a1) * xr, y - sin(a1) * yr, 0.0).endVertex()
        for (i in 1..l) {
            val a = a1 + da * i
            worldRen.pos(x + cos(a) * xr, y - sin(a) * yr, 0.0).endVertex()
        }
        tess.draw()
    }

    @JvmOverloads
    fun drawRoundRectangle(
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        radius: Double,
        solid: Boolean = true,
        segments: Int = 5
    ) = apply {
        val r = min(w / 2, min(h / 2, max(radius, 0.0)))
        drawArc(x + r, y + r, r, r, PI / 2, PI, solid, segments)
        drawArc(x + w - r, y + r, r, r, 0.0, PI / 2, solid, segments)
        drawArc(x + r, y + h - r, r, r, PI, PI * 3 / 2, solid, segments)
        drawArc(x + w - r, y + h - r, r, r, PI * 3 / 2, PI * 2, solid, segments)

        if (solid) {
            drawRectangle(x + r, y, w - 2 * r, h, true)
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
    fun drawRoundRect(
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        radius: Double,
        solid: Boolean = true,
        segments: Int = 5
    ) = drawRoundRectangle(x, y, w, h, radius, solid, segments)

    @JvmOverloads
    fun drawString(text: String, x: Float, y: Float, shadow: Boolean = false) = apply {
        val fr = getFontRenderer()
        var _y = y
        GlStateManager.enableTexture2D()
        ChatLib.addColor(text).split('\n').forEach {
            fr.drawString(it, x, _y, 0xFFFFFFFF.toInt(), shadow)
            _y += fr.FONT_HEIGHT
        }
        GlStateManager.disableTexture2D()
    }

    private val outlinedStringRegex = "^".toRegex(RegexOption.MULTILINE)

    fun drawOutlinedString(text: String, x: Float, y: Float) = apply {
        val str = ChatLib.removeFormatting(text).replace(outlinedStringRegex, "&0")
        drawString(str, x + 1, y + 0)
        drawString(str, x - 1, y + 0)
        drawString(str, x + 0, y + 1)
        drawString(str, x + 0, y - 1)
        drawString(text, x, y)
    }

    private fun getPoint(vertices: List<List<Double>>, index: Int) =
        if (index >= vertices.size) vertices[index - vertices.size] else vertices[index]

    // @JvmOverloads doesn't work with varargs because javet is shit or something fuck this
    fun drawPolygon(vertices: ArrayList<ArrayList<Double>>) = drawPolygon(vertices, true)
    fun drawPolygon(vertices: ArrayList<ArrayList<Double>>, solid: Boolean) = apply {
        worldRen.begin(if (solid) 6 else 2, DefaultVertexFormats.POSITION)
        for (p in vertices) worldRen.pos(p[0], p[1], 0.0).endVertex()
        tess.draw()
    }

    @JvmOverloads
    fun drawTexturedRect(
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        u: Double = 0.0,
        v: Double = 0.0,
        tw: Double = w,
        th: Double = h,
        uw: Double = w,
        vh: Double = h
    ) = apply {
        val f = 1 / tw
        val g = 1 / th
        val m = u * f
        val n = v * g
        val mw = uw * f
        val nh = vh * g

        GlStateManager.enableTexture2D()
        worldRen.begin(6, DefaultVertexFormats.POSITION_TEX)
        worldRen.pos(x, y + h, 0.0).tex(m, n + nh).endVertex()
        worldRen.pos(x + w, y + h, 0.0).tex(m + mw, n + nh).endVertex()
        worldRen.pos(x + w, y, 0.0).tex(m + mw, n).endVertex()
        worldRen.pos(x, y, 0.0).tex(m, n).endVertex()
        tess.draw()
        GlStateManager.disableTexture2D()
    }

    var sr: ScaledResolution? = null

    @SubscribeEvent
    fun onRenderOverlayPre(event: TickEvent.RenderTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        partialTicks = event.renderTickTime
        Tessellator.partialTicks = event.renderTickTime
        val newSr = ScaledResolution(Minecraft.getMinecraft())
        if (sr != null && sr!!.scaledWidth == newSr.scaledWidth && sr!!.scaledHeight == newSr.scaledHeight && sr!!.scaleFactor == newSr.scaleFactor) return
        sr = newSr
        EventType.ScreenResize.triggerAll(sr)
    }

    @JvmStatic
    fun getMouseX(): Float {
        val mx = Mouse.getX().toFloat()
        val rw = sr!!.scaledWidth.toFloat()
        val dw = getMinecraft().displayWidth.toFloat()
        return mx * rw / dw
    }

    @JvmStatic
    fun getMouseY(): Float {
        val my = Mouse.getY().toFloat()
        val rh = sr!!.scaledHeight.toFloat()
        val dh = getMinecraft().displayHeight.toFloat()
        return rh - my * rh / dh - 1f
    }

    init {
        screen = object {
            fun getWidth() = sr?.scaledWidth ?: 0

            fun getHeight() = sr?.scaledHeight ?: 0

            fun getScale() = sr?.scaleFactor ?: 1
        }
    }
}