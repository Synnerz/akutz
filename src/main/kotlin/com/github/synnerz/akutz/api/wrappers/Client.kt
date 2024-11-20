package com.github.synnerz.akutz.api.wrappers

import com.github.synnerz.akutz.api.libs.render.Tessellator
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiNewChat
import net.minecraft.client.gui.GuiPlayerTabOverlay
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.inventory.Slot
import net.minecraft.network.INetHandler
import net.minecraft.network.Packet
import org.lwjgl.opengl.Display
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/Client.kt)
 */
object Client {
    @JvmStatic
    fun getMinecraft(): Minecraft = Minecraft.getMinecraft()

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

    fun <T : INetHandler> sendPacket(packet: Packet<T>) {
        getConnection()?.networkManager?.sendPacket(packet)
    }

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
        fun getX(): Double = Tessellator.getRenderX()

        fun getY(): Double = Tessellator.getRenderY()

        fun getZ(): Double = Tessellator.getRenderZ()
    }

    @JvmField
    val clipboard = object {
        private val board = Toolkit.getDefaultToolkit().systemClipboard
        fun get(): String {
            try {
                val t = board.getContents(null)
                val s = StringBuilder()
                DataFlavor.selectBestTextFlavor(t.transferDataFlavors).getReaderForText(t).use {
                    val b = CharArray(65536)
                    var l = 0
                    do {
                        l = it.read(b, 0, 65536)
                        if (l <= 0) break
                        s.appendRange(b, 0, l)
                    } while (true)
                }
                return s.toString()
            } catch (_: Exception) {
                return ""
            }
        }

        fun set(value: String) {
            board.setContents(StringSelection(value), null)
        }
    }
}