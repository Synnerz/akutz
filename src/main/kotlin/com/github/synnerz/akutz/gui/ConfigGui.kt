package com.github.synnerz.akutz.gui

import com.github.synnerz.akutz.api.objects.gui.GuiHandler
import com.github.synnerz.talium.components.UIRect
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

// TODO: make a better looking gui as well as config api
object ConfigGui : GuiScreen() {
    val parent = UIRect(0.0, 0.0, 100.0, 100.0).setColor(Color(0, 0, 0, 150))
    val UIThreadLoading = UISwitchComponent(
        10.0,
        10.0,
        20.0,
        15.0,
        title = "Thread Loading",
        description = "Uses threads whenever loading modules",
        state = Config.get("threadLoading"),
        parent = parent)
    val UIAutoUpdater = UISwitchComponent(
        35.0,
        10.0,
        20.0,
        15.0,
        title = "Auto Update Modules",
        description = "Checks for module updates and installs them",
        state = Config.get("autoUpdate"),
        parent = parent)

    init {
        UIThreadLoading.setColor(Color(25, 25, 25, 255))
        UIAutoUpdater.setColor(Color(25, 25, 25, 255))
        UIThreadLoading.onMouseClick { Config.set("threadLoading", UIThreadLoading.state) }
        UIAutoUpdater.onMouseClick { Config.set("autoUpdate", UIAutoUpdater.state) }
    }

    fun open() = apply {
        GuiHandler.openGui(this)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)

        GlStateManager.pushMatrix()
        parent.draw()
        GlStateManager.popMatrix()
    }
}
