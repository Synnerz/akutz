package com.github.synnerz.akutz.mixin;

import com.github.synnerz.akutz.hooks.EffectRendererHook;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.IParticleFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EffectRenderer.class)
public class MixinEffectRenderer {
    @Inject(method = "spawnEffectParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/EffectRenderer;addEffect(Lnet/minecraft/client/particle/EntityFX;)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    public void onSpawnEffect(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed, double p_178927_10_, double p_178927_12_, int[] p_178927_14_, CallbackInfoReturnable<EntityFX> cir, IParticleFactory iparticlefactory, EntityFX entityfx) {
        EffectRendererHook.INSTANCE.trigger(entityfx, particleId, cir);
    }
}
