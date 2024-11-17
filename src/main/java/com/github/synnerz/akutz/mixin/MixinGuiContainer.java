package com.github.synnerz.akutz.mixin;

import com.github.synnerz.akutz.hooks.GuiContainerHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {
    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    protected void onPreDrawSlot(Slot slotIn, CallbackInfo ci) {
        // TODO: make this workaround less yuck
        GuiContainer gui = (GuiContainer) Minecraft.getMinecraft().currentScreen;
        GuiContainerHook.INSTANCE.triggerDrawSlot(slotIn, gui, ci);
    }

    @Shadow
    private Slot theSlot;

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGuiContainerForegroundLayer(II)V"))
    public void onDrawForeground(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiContainer gui = (GuiContainer) Minecraft.getMinecraft().currentScreen;
        GuiContainerHook.INSTANCE.triggerPreItemRender(this.theSlot, mouseX, mouseY, gui);
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGradientRect(IIIIII)V"))
    public void onDrawSlotHighlight(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiContainer gui = (GuiContainer) Minecraft.getMinecraft().currentScreen;
        GuiContainerHook.INSTANCE.triggerDrawSlotHighlight(this.theSlot, mouseX, mouseY, gui);
    }
}
