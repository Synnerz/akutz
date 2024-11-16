package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.Cancelable
import com.github.synnerz.akutz.api.events.EventType
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object GuiIngameHook {
    fun trigger(ci: CallbackInfo) {
        val event = Cancelable()
        EventType.RenderScoreboard.triggerAll(event)
        if (event.isCanceled()) ci.cancel()
    }
}