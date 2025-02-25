package com.github.synnerz.akutz.listeners

import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.wrappers.World
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/listeners/MouseListener.kt)
 */
object MouseListener {
    // Beware these listeners get cleaned up whenever the engine gets unloaded
    private val scrollListeners = mutableListOf<(x: Double, y: Double, delta: Int) -> Unit>()
    private val clickListeners = mutableListOf<(x: Double, y: Double, button: Int, pressed: Boolean) -> Unit>()
    private val draggedListeners = mutableListOf<(deltaX: Double, deltaY: Double, x: Double, y: Double, button: Int) -> Unit>()
    // These do NOT get cleared and keep on triggering _should_ only be used for internal use i.e. `clicked` register
    private val onScroll = mutableListOf<(x: Double, y: Double, delta: Int) -> Unit>()
    private val onClick = mutableListOf<(x: Double, y: Double, button: Int, pressed: Boolean) -> Unit>()
    private val onDragged = mutableListOf<(deltaX: Double, deltaY: Double, x: Double, y: Double, button: Int) -> Unit>()

    private val mouseState = mutableMapOf<Int, Boolean>()
    private val draggedState = mutableMapOf<Int, State>()

    class State(val x: Double, val y: Double)

    init {
        registerTriggerListeners()
    }

    fun registerScrollListener(listener: (x: Double, y: Double, delta: Int) -> Unit) {
        scrollListeners.add(listener)
    }

    fun registerClickListener(listener: (x: Double, y: Double, button: Int, pressed: Boolean) -> Unit) {
        clickListeners.add(listener)
    }

    fun registerDraggedListener(listener: (deltaX: Double, deltaY: Double, x: Double, y: Double, button: Int) -> Unit) {
        draggedListeners.add(listener)
    }

    internal fun onScroll(listener: (x: Double, y: Double, delta: Int) -> Unit) {
        onScroll.add(listener)
    }

    internal fun onClick(listener: (x: Double, y: Double, button: Int, pressed: Boolean) -> Unit) {
        onClick.add(listener)
    }

    internal fun onDragged(listener: (deltaX: Double, deltaY: Double, x: Double, y: Double, button: Int) -> Unit) {
        onDragged.add(listener)
    }

    private fun scrolled(x: Double, y: Double, delta: Int) {
        scrollListeners.forEach { it(x, y, delta) }
    }

    private fun clicked(x: Double, y: Double, button: Int, pressed: Boolean) {
        clickListeners.forEach { it(x, y, button, pressed) }
    }

    private fun dragged(deltaX: Double, deltaY: Double, x: Double, y: Double, button: Int) {
        draggedListeners.forEach { it(deltaX, deltaY, x, y, button) }
    }

    fun clearListeners() {
        scrollListeners.clear()
        clickListeners.clear()
        draggedListeners.clear()
    }

    fun registerTriggerListeners() {
        onScroll(EventType.Scrolled::triggerAll)
        onClick(EventType.Clicked::triggerAll)
        onDragged(EventType.Dragged::triggerAll)
    }

    private fun process(button: Int, dWheel: Int) {
        if (dWheel != 0) {
            scrolled(
                Renderer.getMouseX().toDouble(),
                Renderer.getMouseY().toDouble(),
                if (dWheel < 0) -1 else 1,
            )
        }

        if (button == -1)
            return

        // normal clicked
        if (Mouse.isButtonDown(button) == mouseState[button])
            return

        val x = Renderer.getMouseX().toDouble()
        val y = Renderer.getMouseY().toDouble()

        clicked(
            x,
            y,
            button,
            Mouse.isButtonDown(button),
        )

        mouseState[button] = Mouse.isButtonDown(button)

        // add new dragged
        if (Mouse.isButtonDown(button)) {
            draggedState[button] = State(x, y)
        } else if (draggedState.containsKey(button)) {
            draggedState.remove(button)
        }
    }

    @SubscribeEvent
    fun onMouseInput(event: MouseEvent) {
        process(event.button, event.dwheel)
    }

    @SubscribeEvent
    fun onGuiMouseInput(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (!World.isLoaded()) {
            mouseState.clear()
            draggedState.clear()
            return
        }

        val button = Mouse.getEventButton()
        val dWheel = Mouse.getEventDWheel()
        process(button, dWheel)
    }

    internal fun handleDragged() {
        for (button in 0..4) {
            if (button !in draggedState)
                continue

            val x = Renderer.getMouseX().toDouble()
            val y = Renderer.getMouseY().toDouble()

            if (x == draggedState[button]?.x && y == draggedState[button]?.y)
                continue

            dragged(
                x - (draggedState[button]?.x ?: 0.0),
                y - (draggedState[button]?.y ?: 0.0),
                x,
                y,
                button,
            )

            // update dragged
            draggedState[button] = State(x, y)
        }
    }
}