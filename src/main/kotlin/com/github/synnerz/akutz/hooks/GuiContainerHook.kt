package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.Cancelable
import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.wrappers.inventory.Slot
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Slot as MCSlot
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object GuiContainerHook {
    fun triggerDrawSlot(slot: MCSlot, container: GuiContainer, ci: CallbackInfo) {
        val event = Cancelable()
        EventType.RenderSlot.triggerAll(Slot(slot), container, event)
        if (event.isCanceled()) ci.cancel()
    }

    fun triggerPreItemRender(slot: MCSlot?, mouseX: Int, mouseY: Int, container: GuiContainer) {
        if (slot != null) {
            EventType.PreItemRender.triggerAll(mouseX, mouseY, Slot(slot), container)
        }
    }

    fun triggerDrawSlotHighlight(slot: MCSlot?, mouseX: Int, mouseY: Int, container: GuiContainer) {
        // TODO: maybe make this cancelable
        if (slot != null) {
            EventType.RenderSlotHighlight.triggerAll(Slot(slot), mouseX, mouseY, container)
        }
    }
}