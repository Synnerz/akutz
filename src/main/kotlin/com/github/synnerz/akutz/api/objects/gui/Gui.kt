package com.github.synnerz.akutz.api.objects.gui

import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.events.NormalEvent
import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.wrappers.Client
import com.github.synnerz.akutz.api.wrappers.Player
import com.github.synnerz.akutz.listeners.MouseListener
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import java.util.ArrayList

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/objects/gui/Gui.kt)
 */
class Gui : GuiScreen() {
    private val listeners = object {
        var onDraw: NormalEvent? = null
        var onKeyTyped: NormalEvent? = null
        var onOpened: NormalEvent? = null
        var onClosed: NormalEvent? = null
        var onResize: NormalEvent? = null
        // Mouse events
        var onClick: NormalEvent? = null
        var onScroll: NormalEvent? = null
        var onReleased: NormalEvent? = null
        var onDragged: NormalEvent? = null
    }

    private var mouseX = 0
    private var mouseY = 0

    init {
        MouseListener.registerScrollListener { x, y, delta ->
            if (isOpen()) listeners.onScroll?.trigger(arrayOf(x, y, delta))
        }
    }

    fun isOpen(): Boolean = Client.getMinecraft().currentScreen === this

    fun open() = apply {
        GuiHandler.openGui(this)
        listeners.onOpened?.trigger(arrayOf(this))
    }

    fun close() = apply {
        if (isOpen()) Player.getPlayer()?.closeScreen()
    }

    fun drawString(str: String, x: Int, y: Int, color: Int) {
        drawString(Renderer.getFontRenderer(), str, x, y, color)
    }

    fun drawCreativeTabHoveringString(str: String, mouseX: Int, mouseY: Int) {
        drawCreativeTabHoveringText(str, mouseX, mouseY)
    }

    fun drawHoveringString(args: ArrayList<String>, x: Int, y: Int) {
        drawHoveringText(args.toMutableList(), x, y, Renderer.getFontRenderer())
    }

    fun isControlDown(): Boolean = isCtrlKeyDown()

    fun isShiftDown(): Boolean = isShiftKeyDown()

    fun isAltDown(): Boolean = isAltKeyDown()

    fun onDraw(method: (args: Array<out Any?>) -> Unit) = run {
        listeners.onDraw = NormalEvent(method, EventType.Other)
        listeners.onDraw
    }

    fun onKeyTyped(method: (args: Array<out Any?>) -> Unit) = run {
        listeners.onKeyTyped = NormalEvent(method, EventType.Other)
        listeners.onKeyTyped
    }

    fun onOpened(method: (args: Array<out Any?>) -> Unit) = run {
        listeners.onOpened = NormalEvent(method, EventType.Other)
        listeners.onOpened
    }

    fun onClosed(method: (args: Array<out Any?>) -> Unit) = run {
        listeners.onClosed = NormalEvent(method, EventType.Other)
        listeners.onClosed
    }

    fun onResize(method: (args: Any?) -> Unit) = run {
        listeners.onResize = NormalEvent(method, EventType.Other)
        listeners.onResize
    }

    fun onClick(method: (args: Array<out Any?>) -> Unit) = run {
        listeners.onClick = NormalEvent(method, EventType.Other)
        listeners.onClick
    }

    fun onScroll(method: (args: Array<out Any?>) -> Unit) = run {
        listeners.onScroll = NormalEvent(method, EventType.Other)
        listeners.onScroll
    }

    fun onReleased(method: (args: Array<out Any?>) -> Unit) = run {
        listeners.onReleased = NormalEvent(method, EventType.Other)
        listeners.onReleased
    }

    fun onDragged(method: (args: Array<out Any?>) -> Unit) = run {
        listeners.onDragged = NormalEvent(method, EventType.Other)
        listeners.onDragged
    }

    // Override methods to be able to trigger the events
    // this is meant for internal use only.
    override fun onGuiClosed() {
        super.onGuiClosed()
        listeners.onClosed?.trigger(arrayOf(this))
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        listeners.onClick?.trigger(arrayOf(mouseX, mouseY, mouseButton))
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseReleased(mouseX, mouseY, state)
        listeners.onReleased?.trigger(arrayOf(mouseX, mouseY, state))
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
        listeners.onDraw?.trigger(arrayOf(mouseX, mouseY, clickedMouseButton, timeSinceLastClick))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)

        GlStateManager.pushMatrix()

        this.mouseX = mouseX
        this.mouseY = mouseY

        listeners.onDraw?.trigger(arrayOf(mouseX, mouseY, partialTicks))

        GlStateManager.popMatrix()
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
        listeners.onKeyTyped?.trigger(arrayOf(typedChar, keyCode))
    }

    override fun onResize(mcIn: Minecraft?, w: Int, h: Int) {
        super.onResize(mcIn, w, h)
        listeners.onResize?.trigger(arrayOf(w, h))
    }

    // You don't get to choose, I do.
    override fun doesGuiPauseGame(): Boolean = false
}