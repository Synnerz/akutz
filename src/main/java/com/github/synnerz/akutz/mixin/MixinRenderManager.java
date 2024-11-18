package com.github.synnerz.akutz.mixin;

import com.github.synnerz.akutz.hooks.RenderManagerHook;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderManager.class)
public class MixinRenderManager {
    @Inject(method = "doRenderEntity", at = @At("HEAD"), cancellable = true)
    public void onPreEntityRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks, boolean p_147939_10_, CallbackInfoReturnable<Boolean> cir) {
        RenderManagerHook.INSTANCE.triggerRenderEntity(entity, x, y, z, partialTicks, cir);
    }

    @Inject(method = "doRenderEntity", at = @At(value = "RETURN", ordinal = 1))
    public void onPostEntityRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks, boolean p_147939_10_, CallbackInfoReturnable<Boolean> cir) {
        RenderManagerHook.INSTANCE.triggerPostEntityRender(entity, x, y, z, partialTicks);
    }
}
