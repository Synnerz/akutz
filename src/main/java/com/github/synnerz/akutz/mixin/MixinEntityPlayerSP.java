package com.github.synnerz.akutz.mixin;

import com.github.synnerz.akutz.hooks.EntityPlayerSPHook;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void onPreSendMessage(String message, CallbackInfo ci) {
        EntityPlayerSPHook.INSTANCE.trigger(message, ci);
    }
}
