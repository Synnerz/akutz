package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.Cancelable
import com.github.synnerz.akutz.api.events.EventType
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.Slot
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object GuiContainerHook {
    fun triggerDrawSlot(slot: Slot, container: GuiContainer, ci: CallbackInfo) {
        val event = Cancelable()
        GlStateManager.pushMatrix()
        // TODO: make wrapper for MCSlot
        EventType.RenderSlot.triggerAll(slot, container, event)
        GlStateManager.popMatrix()
        if (event.isCanceled()) ci.cancel()
    }

    fun triggerPreItemRender(slot: Slot?, mouseX: Int, mouseY: Int, container: GuiContainer) {
        if (slot != null) {
            GlStateManager.pushMatrix()
            // TODO: despite ct itself not wrapping this Slot we _should_ wrap it (?)
            EventType.PreItemRender.triggerAll(mouseX, mouseY, slot, container)
            GlStateManager.popMatrix()
        }
    }

    fun triggerDrawSlotHighlight(slot: Slot?, mouseX: Int, mouseY: Int, container: GuiContainer) {
        // TODO: maybe make this cancelable
        if (slot != null) {
            GlStateManager.pushMatrix()
            EventType.RenderSlotHighlight.triggerAll(slot, mouseX, mouseY, container)
            GlStateManager.popMatrix()
        }
    }
}