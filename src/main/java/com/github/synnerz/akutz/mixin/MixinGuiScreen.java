package com.github.synnerz.akutz.mixin;

import com.github.synnerz.akutz.hooks.GuiScreenHook;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {
    @Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
    public void onPreRenderToolTip(ItemStack stack, int x, int y, CallbackInfo ci) {
        GuiScreenHook.INSTANCE.triggerToolTip(stack, x, y, ci);
    }
}
