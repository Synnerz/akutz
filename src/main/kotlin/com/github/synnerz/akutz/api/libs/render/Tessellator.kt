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

object Tessellator : Base() {
    private var didEsp = false
    private var prevCol = Color.WHITE

    fun getRenderX() = rendManager.renderX

    fun getRenderY() = rendManager.renderY

    fun getRenderZ() = rendManager.renderZ

    fun prepareDraw() = Tessellator.prepareDraw(Color.WHITE)
    fun prepareDraw(color: Color) = prepareDraw(color, true)
    override fun prepareDraw(color: Color, pushMatrix: Boolean) = prepareDraw(color, pushMatrix, false)
    fun prepareDraw(color: Color, pushMatrix: Boolean, esp: Boolean) = apply {
        super.prepareDraw(color, pushMatrix)

        if (pushMatrix) GlStateManager.translate(-getRenderX(), -getRenderY(), -getRenderZ())
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
        prevCol = color
        if (color.a == 255) {
            GlStateManager.disableAlpha()
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
        if (prevCol.a == 255) GlStateManager.enableAlpha()
        else {
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
        val p1 = rescaleHomogenous(x1, y1, z1)
        val p2 = rescaleHomogenous(x2, y2, z2)
        worldRen.begin(1, DefaultVertexFormats.POSITION)
        worldRen.pos(p1[0], p1[1], p1[2]).endVertex()
        worldRen.pos(p2[0], p2[1], p2[2]).endVertex()
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
        xPos: Double,
        yPos: Double,
        zPos: Double,
        width: Double,
        height: Double,
        centered: Boolean = false
    ) = apply {
        val p = rescaleHomogenous(xPos, yPos, zPos)
        val w = width * p[3]
        val h = height * p[3]
        val x = if (centered) p[0] - w / 2 else p[0]
        val y = p[1]
        val z = if (centered) p[2] - w / 2 else p[2]

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
    }

    @JvmOverloads
    fun renderBoxFilled(
        xPos: Double,
        yPos: Double,
        zPos: Double,
        width: Double,
        height: Double,
        centered: Boolean = false
    ) = apply {
        val p = rescaleHomogenous(xPos, yPos, zPos)
        val w = width * p[3]
        val h = height * p[3]
        val x = if (centered) p[0] - w / 2 else p[0]
        val y = p[1]
        val z = if (centered) p[2] - w / 2 else p[2]

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
        worldRen.pos(x, y + h, z).endVertex()
        worldRen.pos(x, y + h, z + w).endVertex()
        tess.draw()
        worldRen.begin(5, DefaultVertexFormats.POSITION)
        worldRen.pos(x + w, y, z).endVertex()
        worldRen.pos(x + w, y + h, z).endVertex()
        worldRen.pos(x + w, y, z + w).endVertex()
        worldRen.pos(x + w, y + h, z + w).endVertex()
        tess.draw()
    }

    private val beaconBeamTexture = ResourceLocation("textures/entity/beacon_beam.png")

    @JvmOverloads
    fun renderBeaconBeam(
        xPos: Double,
        yPos: Double,
        zPos: Double,
        centered: Boolean = false,
        height: Double = 300.0
    ) = apply {
        val p = rescaleHomogenous(xPos, yPos, zPos)
        val x = if (centered) p[0] else p[0] + 0.5 * p[3]
        val y = p[1]
        val z = if (centered) p[2] else p[2] + 0.5 * p[3]

        GlStateManager.enableTexture2D()
        Minecraft.getMinecraft().textureManager.bindTexture(beaconBeamTexture)
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT.toFloat())
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT.toFloat())

        val time = 0.2 * (World.getTime() + partialTicks)
        val t0 = ceil(time) - time
        val t1 = time * -0.1875
        val d0 = cos(t1 + Math.PI * 1 / 4) * 0.2 * p[3]
        val d1 = sin(t1 + Math.PI * 1 / 4) * 0.2 * p[3]
        val d2 = cos(t1 + Math.PI * 3 / 4) * 0.2 * p[3]
        val d3 = sin(t1 + Math.PI * 3 / 4) * 0.2 * p[3]
        val d4 = cos(t1 + Math.PI * 5 / 4) * 0.2 * p[3]
        val d5 = sin(t1 + Math.PI * 5 / 4) * 0.2 * p[3]
        val d6 = cos(t1 + Math.PI * 9 / 4) * 0.2 * p[3]
        val d7 = sin(t1 + Math.PI * 9 / 4) * 0.2 * p[3]
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
        val w = 0.3 * p[3]

        worldRen.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldRen.pos(x - w, y + height, z - w).tex(1.0, d9).color(r, g, b, a / 4).endVertex()
        worldRen.pos(x - w, y, z - w).tex(1.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x + w, y, z - w).tex(0.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x + w, y + height, z - w).tex(0.0, d9).color(r, g, b, a / 4).endVertex()
        worldRen.pos(x + w, y + height, z + w).tex(1.0, d9).color(r, g, b, a / 4).endVertex()
        worldRen.pos(x + w, y, z + w).tex(1.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x - w, y, z + w).tex(0.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x - w, y + height, z + w).tex(0.0, d9).color(r, g, b, a / 4).endVertex()
        worldRen.pos(x + w, y + height, z - w).tex(1.0, d9).color(r, g, b, a / 4).endVertex()
        worldRen.pos(x + w, y, z - w).tex(1.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x + w, y, z + w).tex(0.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x + w, y + height, z + w).tex(0.0, d9).color(r, g, b, a / 4).endVertex()
        worldRen.pos(x - w, y + height, z + w).tex(1.0, d9).color(r, g, b, a / 4).endVertex()
        worldRen.pos(x - w, y, z + w).tex(1.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x - w, y, z - w).tex(0.0, t2).color(r, g, b, 64).endVertex()
        worldRen.pos(x - w, y + height, z - w).tex(0.0, d9).color(r, g, b, a / 4).endVertex()
        tess.draw()

        GlStateManager.disableTexture2D()
        GlStateManager.enableCull()
    }

    @JvmOverloads
    fun renderString(
        text: String,
        xPos: Double,
        yPos: Double,
        zPos: Double,
        renderBlackBox: Boolean = true,
        shadow: Boolean = true
    ) = apply {
        val p = rescaleHomogenous(xPos, yPos, zPos)
        val x = p[0]
        val y = p[1]
        val z = p[2]

        val lines = ChatLib.addColor(text).split('\n')
        val xMultiplier = if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) -1 else 1

        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, z)
        GlStateManager.rotate(-rendManager.viewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(rendManager.viewX * xMultiplier, 1.0f, 0.0f, 0.0f)
        GlStateManager.scale(-p[3], -p[3], -p[3])
        GlStateManager.enableAlpha()
        GlStateManager.depthMask(false)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0)

        val widths = lines.map { fontRend.getStringWidth(it) / 2.0 }
        val w = widths.max()

        if (renderBlackBox) {
            color(Color(0, 0, 0, 64))
            worldRen.begin(5, DefaultVertexFormats.POSITION)
            worldRen.pos(-w - 1, -1.0, 0.0).endVertex()
            worldRen.pos(-w - 1, 8.0 * lines.size + 1, 0.0).endVertex()
            worldRen.pos(w + 1, -1.0, 0.0).endVertex()
            worldRen.pos(w + 1, 8.0 * lines.size + 1, 0.0).endVertex()
            tess.draw()
            color(prevCol)
        }

        GlStateManager.enableTexture2D()
        lines.forEachIndexed { i, s ->
            fontRend.drawString(
                s,
                -widths[i].toFloat(),
                i * 8f,
                0xFFFFFFFF.toInt(),
                shadow
            )
        }
        GlStateManager.popMatrix()
        GlStateManager.disableTexture2D()
        if (prevCol.a == 255) {
            GlStateManager.disableAlpha()
            GlStateManager.disableBlend()
            GlStateManager.depthMask(true)
        }
    }
}