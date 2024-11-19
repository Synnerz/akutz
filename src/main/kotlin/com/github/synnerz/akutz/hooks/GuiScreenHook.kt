package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.Cancelable
import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.wrappers.inventory.Item
import net.minecraft.item.ItemStack
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object GuiScreenHook {
    fun triggerToolTip(
        itemStack: ItemStack,
        x: Int,
        y: Int,
        ci: CallbackInfo
    ) {
        val item = Item(itemStack)
        val toolTip = item.getLore()
        val event = Cancelable()

        EventType.Tooltip.triggerAll(toolTip, item, x, y, event)
        if (event.isCanceled()) ci.cancel()
    }
}