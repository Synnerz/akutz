package com.github.synnerz.akutz.engine.module

import com.github.synnerz.akutz.api.libs.ChatLib
import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.libs.render.Renderer.sr
import com.github.synnerz.akutz.api.objects.gui.GuiHandler
import com.github.synnerz.akutz.api.objects.render.Color
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager

object ModuleGui : GuiScreen() {
    private var dirty = true
    private var cachedModules: List<ModuleElement>? = null

    fun open() = apply {
        GuiHandler.openGui(this)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)

        GlStateManager.pushMatrix()
        if (dirty) {
            cachedModules = ModuleManager.getInstalledModules()?.map { ModuleElement(it) }
            dirty = false
        }
        val width = sr?.scaledWidth?.toDouble() ?: 0.0
        val height = sr?.scaledHeight?.toDouble() ?: 0.0

        Renderer.beginDraw(Color(0, 0, 0, 150))
        Renderer.drawRect(0.0, 0.0, width, height)

        var oldy = 15.0

        cachedModules?.forEachIndexed { index, moduleElement ->
            // Cram values so every 3rd element it changes column
            if (index != 0 && index % 3 == 0) oldy += 27
            // And this resets its position back to 10 if so
            // otherwise it keeps adding rows
            val x = 10.0 + (index % 3) * 27
            moduleElement.draw(x, oldy, 25.0, 20.0)
        }

        Renderer.finishDraw()
        GlStateManager.popMatrix()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        cachedModules?.forEach { it.onMouseClick(mouseX, mouseY, mouseButton) }
    }

    internal fun markDirty() {
        dirty = true
    }

    internal fun getPixels(percent: Double, width: Double): Float = ((percent / 100) * width).toFloat()
    internal fun getPixelsWidth(percent: Double): Float {
        val width = sr?.scaledWidth?.toFloat() ?: 0.0f
        return ((percent / 100) * width).toFloat()
    }
    internal fun getPixelsHeight(percent: Double): Float {
        val height = sr?.scaledHeight?.toFloat() ?: 0.0f
        return ((percent / 100) * height).toFloat()
    }

    internal fun wrapStrByWidth(string: String, width: Double): List<String> {
        val list = mutableListOf<String>()
        var twidth = 0.0
        var tstr = ""

        string.forEachIndexed { index, c ->
            tstr += c
            twidth += Renderer.getStringWidth("$c")
            if (twidth >= width) {
                // Add the current string since it does not go over the limit
                list.add(tstr.trim())
                // Reset the values so we can re-make a new string
                tstr = ""
                twidth = 0.0
            }
            else if (index == string.length - 1) list.add(tstr.trim())
        }

        // If the list is empty this means that the
        // string never got over the threshold, so we can add it
        if (list.size == 0) list.add(tstr)

        return list
    }
}

class ModuleElement(module: ModuleMetadata) {
    private val moduleName: String = module.moduleName ?: "None"
    private val moduleDescription: String = module.description ?: ""
    private val moduleVersion: String = module.version ?: "0.0.0"
    private var bx: Double = 0.0
    private var by: Double = 0.0
    private var bwidth: Double = 0.0
    private var bheight: Double = 0.0

    fun draw(x: Double, y: Double, width: Double, height: Double) {
        val rx = ModuleGui.getPixelsWidth(x)
        val ry = ModuleGui.getPixelsHeight(y)
        val rwidth = ModuleGui.getPixelsWidth(width)
        val rheight = ModuleGui.getPixelsHeight(height)
        val rbottom = ry + rheight

        // Draw outline
        Renderer.setColor(Color(50, 50, 50, 255))
        Renderer.drawRect(
            rx.toDouble() - 0.5,
            ry.toDouble() - 0.5,
            rwidth.toDouble() + 1.0,
            rheight.toDouble() + 1.0,
            false
        )
        // Draw module rect
        Renderer.setColor(Color(25, 25, 25, 255))
        Renderer.drawRect(
            rx.toDouble(),
            ry.toDouble(),
            rwidth.toDouble(),
            rheight.toDouble()
        )
        // Draw title
        val centeredWidth = (rwidth - Renderer.getStringWidth(ChatLib.removeFormatting(moduleName))) / 2
        Renderer.drawString(
            moduleName,
            rx + centeredWidth,
            ry + ModuleGui.getPixels(8.0, rheight.toDouble()),
            true
        )
        // Draw description
        val mwidth = ModuleGui.getPixelsWidth(width - 1.5)
        val descriptions = ModuleGui.wrapStrByWidth(moduleDescription, mwidth.toDouble())
        var dy = ry + ModuleGui.getPixels(30.0, rheight.toDouble())

        descriptions.forEach {
            Renderer.drawString(
                it,
                rx + ModuleGui.getPixelsWidth(0.5),
                dy,
                true
            )
            dy += 9f
        }
        // Draw version
        Renderer.drawString(
            ChatLib.addColor("&7v$moduleVersion"),
            rx + ModuleGui.getPixelsWidth(0.5),
            rbottom - 12f
        )
        // Draw delete button
        bwidth = ModuleGui.getPixels(18.0, rwidth.toDouble()).toDouble()
        bheight = ModuleGui.getPixels(15.0, rheight.toDouble()).toDouble()
        bx = rx + rwidth - bwidth - 4f
        by = ry + rheight - bheight - 4f
        // Draw outline
        Renderer.color(Color(50, 50, 50, 255))
        Renderer.drawRect(
            bx - 0.5,
            by - 0.5,
            bwidth + 1.0,
            bheight + 1.0,
            false
        )
        // Draw delete button rect
        Renderer.color(Color(35, 35, 35, 255))
        Renderer.drawRect(
            bx,
            by,
            bwidth,
            bheight
        )
        // Draw text inside delete button
        val bcenter = (bwidth - Renderer.getStringWidth("Delete")) / 2
        Renderer.drawString(
            ChatLib.addColor("&cDelete"),
            (bx + bcenter).toFloat(),
            (by + (bheight - 9) / 2).toFloat(),
            true
        )
    }

    fun onMouseClick(x: Int, y: Int, button: Int) {
        if (button != 0 || bx == 0.0 || by == 0.0) return
        if (x < bx || x > bx + bwidth || y < by || y > by + bheight) return

        if (ModuleManager.deleteModule(moduleName))
            ChatLib.chat("&b&lAkutz&r: &bSuccessfully deleted module with name &6${moduleName}")
        else
            ChatLib.chat("&b&lAkutz&r: &cThere was a problem deleting module with name &6${moduleName}")
    }
}
