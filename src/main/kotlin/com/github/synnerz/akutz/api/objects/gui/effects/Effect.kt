package com.github.synnerz.akutz.api.objects.gui.effects

import com.github.synnerz.akutz.api.objects.gui.components2.UIBase

abstract class Effect(val comp: UIBase) {
    open var forceColor: Boolean = false

    open fun preDraw() {}
    open fun postDraw() {}
}