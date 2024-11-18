package com.github.synnerz.akutz.api.libs.render

import com.github.synnerz.akutz.api.libs.ChatLib
import com.github.synnerz.akutz.api.objects.render.Color
import com.github.synnerz.akutz.api.wrappers.World
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import kotlin.math.*

class Tessellator : Base() {
    private var didEsp = false
    private var prevCol = Color.WHITE

    fun getRenderX() = renderManager.renderX

    fun getRenderY() = renderManager.renderY

    fun getRenderZ() = renderManager.renderZ

    fun prepareDraw(color: Color) = prepareDraw(color, true)
    override fun prepareDraw(color: Color, pushMatrix: Boolean) = prepareDraw(color, pushMatrix, false)
    fun prepareDraw(color: Color, pushMatrix: Boolean, esp: Boolean) = apply {
        super.prepareDraw(color, pushMatrix)

        if (pushMatrix) GlStateManager.translate(-getRenderX(), -getRenderY(), -getRenderZ())
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.disableAlpha()
        prevCol = color
        if (color.a == 255) {
            GlStateManager.depthMask(true)
            GlStateManager.disableBlend()
        } else {
            GlStateManager.depthMask(false)
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0)
        }
        if (esp) GlStateManager.disableDepth()
        didEsp = esp
    }

    override fun finishDraw() = apply {
        super.finishDraw()

        GlStateManager.enableTexture2D()
        GlStateManager.enableAlpha()
        if (prevCol.a != 255) {
            GlStateManager.depthMask(true)
            GlStateManager.disableBlend()
        }
        if (didEsp) GlStateManager.enableDepth()
        didEsp = false
    }

    fun rescaleHomogenous(x: Double, y: Double, z: Double): List<Double> {
        val rx = getRenderX()
        val ry = getRenderY()
        val rz = getRenderZ()
        val d = (rx - x).pow(2) + (ry - y).pow(2) + (rz - z).pow(2)
        val maxD = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16.0

        if (d >= maxD * maxD) {
            val f = maxD / sqrt(d)
            return listOf(
                rx + (x - rx) * f,
                ry + (y - ry) * f,
                rz + (z - rz) * f,
                f
            )
        }
        return listOf(x, y, z, 1.0)
    }

    fun scale(scale: Double) = apply { GlStateManager.scale(scale, scale, scale) }

    fun renderLine(
        x1: Double,
        y1: Double,
        z1: Double,
        x2: Double,
        y2: Double,
        z2: Double
    ) = apply {
        worldRen.begin(1, DefaultVertexFormats.POSITION)
        worldRen.pos(x1, y1, z1).endVertex()
        worldRen.pos(x2, y2, z2).endVertex()
        tess.draw()
    }

    fun renderTracer(x: Double, y: Double, z: Double) = apply {
        val p = Minecraft.getMinecraft().thePlayer ?: return@apply
        val look = p.getLook(partialTicks)
        renderLine(
            x,
            y,
            z,
            getRenderX() + look.xCoord,
            getRenderY() + look.yCoord + p.eyeHeight,
            getRenderZ() + look.zCoord
        )
    }

    @JvmOverloads
    fun renderBoxOutline(
        x: Double,
        y: Double,
        z: Double,
        w: Double,
        h: Double,
        centered: Boolean = false
    ): Tessellator {
        if (centered) return renderBoxOutline(x - w / 2, y, z - w / 2, w, h, false)

        worldRen.begin(2, DefaultVertexFormats.POSITION)
        worldRen.pos(x, y, z).endVertex()
        worldRen.pos(x, y, z + w).endVertex()
        worldRen.pos(x + w, y, z + w).endVertex()
        worldRen.pos(x + w, y, z).endVertex()
        tess.draw()
        worldRen.begin(2, DefaultVertexFormats.POSITION)
        worldRen.pos(x, y + h, z).endVertex()
        worldRen.pos(x, y + h, z + w).endVertex()
        worldRen.pos(x + w, y + h, z + w).endVertex()
        worldRen.pos(x + w, y + h, z).endVertex()
        tess.draw()
        worldRen.begin(1, DefaultVertexFormats.POSITION)
        worldRen.pos(x, y, z).endVertex()
        worldRen.pos(x, y + h, z).endVertex()
        worldRen.pos(x, y, z + w).endVertex()
        worldRen.pos(x, y + h, z + w).endVertex()
        worldRen.pos(x + w, y, z + w).endVertex()
        worldRen.pos(x + w, y + h, z + w).endVertex()
        worldRen.pos(x + w, y, z).endVertex()
        worldRen.pos(x + w, y + h, z).endVertex()
        tess.draw()

        return this
    }

    @JvmOverloads
    fun renderBoxFilled(x: Double, y: Double, z: Double, w: Double, h: Double, centered: Boolean = false): Tessellator {
        worldRen.begin(5, DefaultVertexFormats.POSITION)
        worldRen.pos(x, y, z).endVertex()
        worldRen.pos(x + w, y, z).endVertex()
        worldRen.pos(x, y, z + w).endVertex()
        worldRen.pos(x + w, y, z + w).endVertex()
        worldRen.pos(x, y + h, z + w).endVertex()
        worldRen.pos(x + w, y + h, z + w).endVertex()
        worldRen.pos(x, y + h, z).endVertex()
        worldRen.pos(x + w, y + h, z).endVertex()
        worldRen.pos(x, y, z).endVertex()
        worldRen.pos(x + w, y, z).endVertex()
        tess.draw()
        worldRen.begin(5, DefaultVertexFormats.POSITION)
        worldRen.pos(x, y, z).endVertex()
        worldRen.pos(x, y, z + w).endVertex()
        worldRen.pos(x, y + h, z + w).endVertex()
        worldRen.pos(x, y + h, z).endVertex()
        tess.draw()
        worldRen.begin(5, DefaultVertexFormats.POSITION)
        worldRen.pos(x + w, y, z).endVertex()
        worldRen.pos(x + w, y + h, z).endVertex()
        worldRen.pos(x + w, y + h, z + w).endVertex()
        worldRen.pos(x + w, y, z + w).endVertex()
        tess.draw()

        return this
    }

    private val beaconBeamTexture = ResourceLocation("textures/entity/beacon_beam.png")

    @JvmOverloads
    fun renderBeaconBeam(
        x: Double,
        y: Double,
        z: Double,
        centered: Boolean = false,
        height: Double = 300.0
    ): Tessellator {
        if (!centered) return renderBeaconBeam(x + 0.5, y, z + 0.5, false, height)
        GlStateManager.enableTexture2D()
        Minecraft.getMinecraft().textureManager.bindTexture(beaconBeamTexture)
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT.toFloat())
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT.toFloat())

        val time = 0.2 * (World.getTime() + partialTicks)
        val t0 = ceil(time) - time
        val t1 = time * -0.1875
        val d0 = 0.5 + cos(t1 + Math.PI * 1 / 4) * 0.2
        val d1 = 0.5 + sin(t1 + Math.PI * 1 / 4) * 0.2
        val d2 = 0.5 + cos(t1 + Math.PI * 3 / 4) * 0.2
        val d3 = 0.5 + sin(t1 + Math.PI * 3 / 4) * 0.2
        val d4 = 0.5 + cos(t1 + Math.PI * 5 / 4) * 0.2
        val d5 = 0.5 + sin(t1 + Math.PI * 5 / 4) * 0.2
        val d6 = 0.5 + cos(t1 + Math.PI * 9 / 4) * 0.2
        val d7 = 0.5 + sin(t1 + Math.PI * 9 / 4) * 0.2
        val t2 = -1 + t0
        val d8 = height * 2.5 + t2

        val r = prevCol.r
        val g = prevCol.g
        val b = prevCol.b
        val a = prevCol.a

        worldRen.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldRen.pos(x + d2, y + height, z + d3).tex(1.0, d8).color(r, g, b, a).endVertex()
        worldRen.pos(x + d2, y, z + d3).tex(0.0, t2).color(r, g, b, a).endVertex()
        worldRen.pos(x + d0, y, z + d1).tex(0.0, t2).color(r, g, b, 255).endVertex()
        worldRen.pos(x + d0, y + height, z + d1).tex(0.0, d8).color(r, g, b, a).endVertex()
        worldRen.pos(x + d6, y + height, z + d7).tex(1.0, d8).color(r, g, b, a).endVertex()
        worldRen.pos(x + d6, y, z + d7).tex(1.0, t2).color(r, g, b, 255).endVertex()
        worldRen.pos(x + d4, y, z + d5).tex(0.0, t2).color(r, g, b, 255).endVertex()
        worldRen.pos(x + d4, y + height, z + d5).tex(0.0, d8).color(r, g, b, a).endVertex()
        worldRen.pos(x + d0, y + height, z + d1).tex(1.0, d8).color(r, g, b, a).endVertex()
        worldRen.pos(x + d0, y, z + d1).tex(1.0, t2).color(r, g, b, 255).endVertex()
        worldRen.pos(x + d6, y, z + d7).tex(0.0, t2).color(r, g, b, 255).endVertex()
        worldRen.pos(x + d6, y + height, z + d7).tex(0.0, d8).color(r, g, b, a).endVertex()
        worldRen.pos(x + d4, y + height, z + d5).tex(1.0, d8).color(r, g, b, a).endVertex()
        worldRen.pos(x + d4, y, z + d5).tex(1.0, t2).color(r, g, b, 255).endVertex()
        worldRen.pos(x + d2, y, z + d3).tex(0.0, t2).color(r, g, b, 255).endVertex()
        worldRen.pos(x + d2, y + height, z + d3).tex(0.0, d8).color(r, g, b, a).endVertex()
        tess.draw()

        GlStateManager.disableCull()

        val d9 = height + t2

        worldRen.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldRen.pos(x + 0.2, y + height, z + 0.2).tex(1.0, d9).color(r, g, b, a / 4).endVertex()
        worldRen.pos(x + 0.2, y, z + 0.2).tex(1.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x + 0.8, y, z + 0.2).tex(0.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x + 0.8, y + height, z + 0.2).tex(0.0, d9).color(r, g, b, a / 4).endVertex()
        worldRen.pos(x + 0.8, y + height, z + 0.8).tex(1.0, d9).color(r, g, b, a / 4).endVertex()
        worldRen.pos(x + 0.8, y, z + 0.8).tex(1.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x + 0.2, y, z + 0.8).tex(0.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x + 0.2, y + height, z + 0.8).tex(0.0, d9).color(r, g, b, a / 4).endVertex()
        worldRen.pos(x + 0.8, y + height, z + 0.2).tex(1.0, d9).color(r, g, b, a / 4).endVertex()
        worldRen.pos(x + 0.8, y, z + 0.2).tex(1.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x + 0.8, y, z + 0.8).tex(0.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x + 0.8, y + height, z + 0.8).tex(0.0, d9).color(r, g, b, a / 4).endVertex()
        worldRen.pos(x + 0.2, y + height, z + 0.8).tex(1.0, d9).color(r, g, b, a / 4).endVertex()
        worldRen.pos(x + 0.2, y, z + 0.8).tex(1.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x + 0.2, y, z + 0.2).tex(0.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x + 0.2, y + height, z + 0.2).tex(0.0, d9).color(r, g, b, a / 4).endVertex()
        tess.draw()

        GlStateManager.disableTexture2D()
        GlStateManager.enableCull()

        return this
    }

    @JvmOverloads
    fun renderString() = apply {

        fun drawString(
            text: String,
            x: Double,
            y: Double,
            z: Double,
            renderBlackBox: Boolean = true,
            shadow: Boolean = true
        ) {
            val lines = ChatLib.addColor(text).split('\n')
            val xMultiplier = if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) 1 else -1

            GlStateManager.pushMatrix()
            GlStateManager.rotate(renderManager.viewY, 0.0f, 1.0f, 0.0f)
            GlStateManager.rotate(renderManager.viewX * xMultiplier, 1.0f, 0.0f, 0.0f)

            val widths = lines.map { fontRenderer.getStringWidth(it) / 2.0 }
            val w = widths.max()

            if (renderBlackBox) {
                worldRen.begin(7, DefaultVertexFormats.POSITION_COLOR)
                worldRen.pos(-w - 1, -1.0, 0.0).color(0, 0, 0, 64).endVertex()
                worldRen.pos(-w - 1, 8.0 * lines.size + 1, 0.0).color(0, 0, 0, 64).endVertex()
                worldRen.pos(w + 1, 8.0 * lines.size + 1, 0.0).color(0, 0, 0, 64).endVertex()
                worldRen.pos(w + 1, -1.0, 0.0).color(0, 0, 0, 64).endVertex()
                tess.draw()
            }

            lines.forEachIndexed { i, s ->
                fontRenderer.drawString(
                    s,
                    (-widths[i]).toFloat(),
                    i * 8f,
                    0xFFFFFFFF.toInt(),
                    shadow
                )
            }
            GlStateManager.popMatrix()
        }
    }
}