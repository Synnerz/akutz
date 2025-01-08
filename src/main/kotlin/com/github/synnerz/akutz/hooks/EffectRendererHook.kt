package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.Cancelable
import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.wrappers.entity.Particle
import net.minecraft.client.particle.EntityFX
import net.minecraft.util.EnumParticleTypes
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

object EffectRendererHook {
    fun trigger(entityFX: EntityFX, particleId: Int, ci: Any) {
        val event = Cancelable()
        val particleEnum = EnumParticleTypes.getParticleFromId(particleId)

        EventType.SpawnParticle.triggerAll(Particle(entityFX), particleEnum.name, particleEnum, event)
        if (event.isCanceled()) {
            when (ci) {
                is CallbackInfoReturnable<*> -> {
                    ci.returnValue = null
                }
                is CallbackInfo -> {
                    ci.cancel()
                }
            }
        }
    }
}