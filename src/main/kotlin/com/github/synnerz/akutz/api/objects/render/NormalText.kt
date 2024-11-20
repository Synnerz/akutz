package com.github.synnerz.akutz.api.objects.render

import com.github.synnerz.akutz.api.libs.render.Renderer

class NormalText(private var text: String = "") : Text {
    private var w: Float = 0f
    private var vw: Float = 0f
    private fun calcWidth() {
        w = Renderer.getFontRenderer().getStringWidth(text).toFloat()
        vw = Renderer.getFontRenderer().getStringWidth(text.trim()).toFloat()
    }

    init {
        calcWidth()
    }

    override fun getText(): String = text
    override fun setText(v: String): Text = apply {
        text = v
        calcWidth()
    }

    override fun getWidth(): Float = w
    override fun getHeight(): Float = 10f
    override fun getVisibleWidth(): Float = vw
    override fun getVisibleHeight(): Float = 10f
}