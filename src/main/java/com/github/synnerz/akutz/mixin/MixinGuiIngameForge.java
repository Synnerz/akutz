package com.github.synnerz.akutz.mixin;

import com.github.synnerz.akutz.hooks.GuiIngameForgeHook;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameForge.class)
public class MixinGuiIngameForge {
    @Shadow
    protected String displayedTitle;

    @Shadow
    protected String displayedSubTitle;

    @Inject(method = "renderTitle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V"), cancellable = true)
    protected void onPreRenderTitle(int width, int height, float partialTicks, CallbackInfo ci) {
        GuiIngameForgeHook.INSTANCE.trigger(displayedTitle, displayedSubTitle, ci);
    }
}
