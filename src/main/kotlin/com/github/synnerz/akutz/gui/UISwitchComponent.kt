package com.github.synnerz.akutz.gui

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.engine.module.ModuleGui
import com.github.synnerz.talium.components.UIBase
import com.github.synnerz.talium.components.UISwitch
import com.github.synnerz.talium.events.UIClickEvent
import java.awt.Color

class UISwitchComponent @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var radius: Double = 0.0,
    var state: Boolean = false,
    var title: String,
    var description: String,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    val switch = UISwitch(70.0, 70.0, 25.0, 25.0, radius, state, this)

    init {
        switch.setColor(Color(35, 35, 35, 255))
        switch.knob.enabledColor = Color(245, 245, 245, 150) // white-smoke
        switch.knob.disabledColor = Color(25, 25, 25, 255)
    }

    override fun render() {
        // Draw main background
        when (radius) {
            0.0 -> Renderer.drawRect(x, y, width, height)
            else -> Renderer.drawRoundRect(x, y, width, height, radius)
        }

        // Draw title
        val cx = (width - Renderer.getStringWidth(title)) / 2
        Renderer.drawString(title, x.toFloat() + cx.toFloat(), y.toFloat() + 5f)
        // Draw description
        val mut = ModuleGui.wrapStrByWidth(description, width - 8.0)
        var dy = y + 16f

        mut.forEach {
            Renderer.drawString(
                it,
                x.toFloat() + 4f,
                dy.toFloat(),
                true
            )
            dy += 9f
        }
    }

    override fun onMouseClick(event: UIClickEvent) = apply {
        state = !state
        // TODO: fix talium, i somehow managed to mess this up
        if (switch.inBounds(event.x, event.y))
            switch.propagateMouseClick(event.x, event.y, event.button)
    }
}
