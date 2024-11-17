package com.github.synnerz.akutz.mixin;

import com.github.synnerz.akutz.hooks.MinecraftHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "displayGuiScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;onGuiClosed()V"))
    public void onGuiClosed(GuiScreen guiScreenIn, CallbackInfo ci) {
        MinecraftHook.INSTANCE.triggerGuiClosed(Minecraft.getMinecraft().currentScreen);
    }

    @Inject(method = "startGame", at = @At("TAIL"))
    public void onPostGameStart(CallbackInfo ci) {
        MinecraftHook.INSTANCE.triggerGameLoad();
    }
}
