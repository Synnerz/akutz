package com.github.synnerz.akutz.api.hooks

import com.github.synnerz.akutz.api.events.Cancelable
import com.github.synnerz.akutz.api.events.EventType
import net.minecraft.util.EnumParticleTypes
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object EffectRendererHook {
    fun trigger(particleId: Int, ci: CallbackInfo) {
        val event = Cancelable()
        // TODO: make Particle wrapper and wrap this
        EventType.SpawnParticle.triggerAll(EnumParticleTypes.getParticleFromId(particleId), event)
        if (event.isCanceled()) {
            ci.cancel()
        }
    }
}