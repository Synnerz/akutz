package com.github.synnerz.akutz.mixin;

import com.github.synnerz.akutz.hooks.GuiScreenHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {
    @Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
    public void onPreRenderToolTip(ItemStack stack, int x, int y, CallbackInfo ci) {
        GuiScreenHook.INSTANCE.triggerToolTip(stack, x, y, ci);
    }

    @Inject(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;keyTyped(CI)V"), cancellable = true)
    public void onKeyTyped(CallbackInfo ci) {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        GuiScreenHook.INSTANCE.triggerGuiKey(gui, ci);
    }

    @Inject(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;mouseClicked(III)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    public void onMouseClicked(CallbackInfo ci, int i, int j, int k) {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        GuiScreenHook.INSTANCE.triggerGuiMouseClick(i, j, k, gui, ci);
    }
}
