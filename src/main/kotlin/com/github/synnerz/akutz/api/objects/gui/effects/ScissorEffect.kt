package com.github.synnerz.akutz.api.objects.gui.effects

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.objects.gui.components2.UIBase
import org.lwjgl.opengl.GL11.*

class ScissorEffect(comp: UIBase) : Effect(comp) {
    override fun preDraw() {
        val sf = Renderer.sr?.scaleFactor ?: 1
        val x = comp.x.toInt()
        val y = comp.y.toInt() + comp.height.toInt()

        glEnable(GL_SCISSOR_TEST)
        glScissor(
            x * sf,
            ((Renderer.sr?.scaledHeight ?: 0) - y) * sf,
            comp.width.toInt() * sf,
            comp.height.toInt() * sf
        )
    }

    override fun postDraw() {
        glDisable(GL_SCISSOR_TEST)
    }
}