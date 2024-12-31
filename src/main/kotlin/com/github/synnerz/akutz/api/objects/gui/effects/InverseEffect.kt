package com.github.synnerz.akutz.api.objects.gui.effects

import com.github.synnerz.akutz.api.objects.gui.components2.UIBase
import net.minecraft.client.renderer.GlStateManager.*

class InverseEffect(comp: UIBase) : Effect(comp) {
    override var forceColor: Boolean = true
    override fun preDraw() {
        enableBlend()
        tryBlendFuncSeparate(775, 0, 1, 0)
        color(1f, 1f, 1f, 1f)
    }

    override fun postDraw() {
        disableBlend()
        color(0f, 0f, 0f, 0f)
    }
}