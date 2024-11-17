package com.github.synnerz.akutz.api.wrappers

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiNewChat
import net.minecraft.client.gui.GuiPlayerTabOverlay
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.inventory.Slot
import org.lwjgl.opengl.Display

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/Client.kt)
 */
object Client {
    @JvmStatic
    fun getMinecraft(): Minecraft = Minecraft.getMinecraft()

    @JvmStatic
    fun getRenderManager(): RenderManager = getMinecraft().renderManager

    @JvmStatic
    fun getConnection(): NetHandlerPlayClient? = getMinecraft().netHandler

    @JvmStatic
    fun getChatGui(): GuiNewChat? = getMinecraft().ingameGUI?.chatGUI

    @JvmStatic
    fun isInChat(): Boolean = getMinecraft().currentScreen is GuiChat

    @JvmStatic
    fun getTabGui(): GuiPlayerTabOverlay? = getMinecraft().ingameGUI?.tabList

    @JvmStatic
    fun isInTab(): Boolean = getMinecraft().gameSettings.keyBindPlayerList.isKeyDown

    @JvmStatic
    fun isTabbedIn(): Boolean = Display.isActive()

    @JvmStatic
    fun isControlDown(): Boolean = GuiScreen.isCtrlKeyDown()

    @JvmStatic
    fun isShiftDown(): Boolean = GuiScreen.isShiftKeyDown()

    @JvmStatic
    fun isAltDown(): Boolean = GuiScreen.isAltKeyDown()

    @JvmStatic
    fun getFPS(): Int = Minecraft.getDebugFPS()

    @JvmStatic
    fun getSystemTime(): Long = System.currentTimeMillis()

    @JvmStatic
    fun isInGui(): Boolean = getMinecraft().currentScreen != null

    @JvmField
    val currentGui = object {
        fun get(): GuiScreen? = getMinecraft().currentScreen

        fun getClassName(): String = get()?.javaClass?.simpleName ?: "null"

        // TODO: wrap the slot (?)
        fun getSlotUnderMouse(): Slot? {
            val gui = get()
            return if ((gui is GuiContainer) && (gui.slotUnderMouse != null))
                gui.slotUnderMouse
            else null
        }

        fun close() {
            Player.getPlayer()?.closeScreen()
        }
    }

    @JvmField
    val camera = object {
        fun getX(): Double = getRenderManager().viewerPosX

        fun getY(): Double = getRenderManager().viewerPosY

        fun getZ(): Double = getRenderManager().viewerPosZ
    }
}