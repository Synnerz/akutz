package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.Cancelable
import com.github.synnerz.akutz.api.events.EventType
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object GuiIngameForgeHook {
    fun trigger(displayTitle: String, displaySubTitle: String, ci: CallbackInfo) {
        val event = Cancelable()
        EventType.RenderTitle.triggerAll(displayTitle, displaySubTitle, event)
        if (event.isCanceled()) ci.cancel()
    }
}