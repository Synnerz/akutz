package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.Cancelable
import com.github.synnerz.akutz.api.events.EventType
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object EntityPlayerSPHook {
    fun trigger(msg: String, ci: CallbackInfo) {
        val event = Cancelable()
        EventType.MessageSent.triggerAll(msg, event)
        if (event.isCanceled()) ci.cancel()
    }
}