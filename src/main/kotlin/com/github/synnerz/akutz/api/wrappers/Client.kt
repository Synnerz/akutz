package com.github.synnerz.akutz.api.wrappers

import com.github.synnerz.akutz.api.libs.render.Tessellator
import com.github.synnerz.akutz.listeners.ClientListener
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.*
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.inventory.Slot
import net.minecraft.network.INetHandler
import net.minecraft.network.Packet
import net.minecraft.realms.RealmsBridge
import net.minecraftforge.fml.client.FMLClientHandler
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

    @JvmStatic
    fun <T : INetHandler> sendPacket(packet: Packet<T>) {
        getConnection()?.networkManager?.sendPacket(packet)
    }

    @JvmOverloads
    @JvmStatic
    fun scheduleTask(delay: Int = 0, callback: () -> Unit) {
        ClientListener.addTask(delay, callback)
    }

    @JvmStatic
    fun disconnect() {
        scheduleTask {
            World.getWorld()?.sendQuittingDisconnectingPacket()
            getMinecraft().loadWorld(null as WorldClient?)

            when {
                getMinecraft().isIntegratedServerRunning -> getMinecraft().displayGuiScreen(GuiMainMenu())
                getMinecraft().isConnectedToRealms -> RealmsBridge().switchToRealms(GuiMainMenu())
                else -> getMinecraft().displayGuiScreen(GuiMultiplayer(GuiMainMenu()))
            }
        }
    }

    @JvmStatic
    fun connect(ip: String) {
        scheduleTask {
            FMLClientHandler.instance().connectToServer(
                GuiMultiplayer(GuiMainMenu()),
                ServerData("Server", ip, false)
            )
        }
    }

    @JvmOverloads
    @JvmStatic
    fun reconnect(ip: String? = null) {
        if (getMinecraft().isSingleplayer) return

        val serv = ip ?: getMinecraft().currentServerData.serverIP
        if (serv == null) return

        disconnect()
        scheduleTask { connect(serv) }
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