package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.Cancelable
import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.wrappers.entity.TileEntity
import org.lwjgl.util.vector.Vector3f
import net.minecraft.tileentity.TileEntity as MCTileEntity
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object TileEntityRendererDispatcherHook {
    fun triggerRenderTileEntity(
        tileEntity: MCTileEntity,
        x: Double,
        y: Double,
        z: Double,
        partialTicks: Float,
        ci: CallbackInfo
    ) {
        val event = Cancelable()
        EventType.RenderTileEntity.triggerAll(
            TileEntity(tileEntity),
            Vector3f(x.toFloat(), y.toFloat(), z.toFloat()),
            partialTicks,
            event
        )
        if (event.isCanceled()) ci.cancel()
    }

    fun triggerPostRenderTileEntity(
        tileEntity: MCTileEntity,
        x: Double,
        y: Double,
        z: Double,
        partialTicks: Float
    ) {
        EventType.PostRenderTileEntity.triggerAll(
            TileEntity(tileEntity),
            Vector3f(x.toFloat(), y.toFloat(), z.toFloat()),
            partialTicks
        )
    }
}