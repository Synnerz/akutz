package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.Cancelable
import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.wrappers.inventory.Item
import net.minecraft.item.ItemStack
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object RenderItemHook {
    fun triggerRenderItemIntoGui(
        itemStack: ItemStack?,
        x: Int,
        y: Int,
        ci: CallbackInfo
    ) {
        if (itemStack == null) return

        val event = Cancelable()
        EventType.RenderItemIntoGui.triggerAll(Item(itemStack), x, y, event)
        if (event.isCanceled()) ci.cancel()
    }

    fun triggerRenderItemOverlayIntoGui(
        itemStack: ItemStack?,
        x: Int,
        y: Int,
        ci: CallbackInfo
    ) {
        if (itemStack == null) return

        val event = Cancelable()
        EventType.RenderItemOverlayIntoGui.triggerAll(Item(itemStack), x, y, event)
        if (event.isCanceled()) ci.cancel()
    }
}