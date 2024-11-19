package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.Cancelable
import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.wrappers.inventory.Item
import net.minecraft.client.gui.GuiScreen
import net.minecraft.item.ItemStack
import org.lwjgl.input.Keyboard
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

    fun triggerGuiKey(
        gui: GuiScreen,
        ci: CallbackInfo
    ) {
        val event = Cancelable()
        EventType.GuiKey.triggerAll(
            Keyboard.getEventCharacter(),
            Keyboard.getEventKey(),
            gui,
            event
        )

        if (event.isCanceled()) ci.cancel()
    }

    fun triggerGuiMouseClick(
        x: Int,
        y: Int,
        mouseBtn: Int,
        gui: GuiScreen,
        ci: CallbackInfo
    ) {
        val event = Cancelable()
        EventType.GuiMouseClick.triggerAll(x, y, mouseBtn, gui, event)

        if (event.isCanceled()) ci.cancel()
    }

    fun triggerGuiMouseRelease(
        x: Int,
        y: Int,
        mouseBtn: Int,
        gui: GuiScreen,
        ci: CallbackInfo
    ) {
        val event = Cancelable()
        EventType.GuiMouseRelease.triggerAll(x, y, mouseBtn, gui, event)

        if (event.isCanceled()) ci.cancel()
    }
}