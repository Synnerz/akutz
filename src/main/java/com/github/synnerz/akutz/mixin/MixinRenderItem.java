package com.github.synnerz.akutz.mixin;

import com.github.synnerz.akutz.hooks.RenderItemHook;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public class MixinRenderItem {
    @Inject(method = "renderItemAndEffectIntoGUI", at = @At("HEAD"), cancellable = true)
    public void onPreItemRender(ItemStack stack, int xPosition, int yPosition, CallbackInfo ci) {
        RenderItemHook.INSTANCE.triggerRenderItemIntoGui(stack, xPosition, yPosition, ci);
    }

    @Inject(method = "renderItemOverlayIntoGUI", at = @At("HEAD"), cancellable = true)
    public void onPreItemOverlayRender(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
        RenderItemHook.INSTANCE.triggerRenderItemOverlayIntoGui(stack, xPosition, yPosition, ci);
    }
}
