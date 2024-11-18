package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.Cancelable
import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.wrappers.entity.Entity
import org.lwjgl.util.vector.Vector3f
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import net.minecraft.entity.Entity as MCEntity

object RenderManagerHook {
    fun triggerRenderEntity(
        entity: MCEntity,
        x: Double,
        y: Double,
        z: Double,
        partialTicks: Float,
        ci: CallbackInfoReturnable<Boolean>
    ) {
        val event = Cancelable()
        EventType.RenderEntity.triggerAll(
            Entity(entity),
            Vector3f(x.toFloat(), y.toFloat(), z.toFloat()),
            partialTicks,
            event
        )
        if (event.isCanceled()) ci.returnValue = false
    }

    fun triggerPostEntityRender(
        entity: MCEntity,
        x: Double,
        y: Double,
        z: Double,
        partialTicks: Float,
    ) {
        EventType.PostRenderEntity.triggerAll(
            Entity(entity),
            Vector3f(x.toFloat(), y.toFloat(), z.toFloat()),
            partialTicks
        )
    }
}