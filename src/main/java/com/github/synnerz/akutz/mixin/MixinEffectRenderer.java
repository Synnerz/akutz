package com.github.synnerz.akutz.mixin;

import com.github.synnerz.akutz.hooks.EffectRendererHook;
import com.github.synnerz.akutz.hooks.FakeDiggingFX;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EffectRenderer.class)
public class MixinEffectRenderer {
    @Shadow
    protected World worldObj;

    @Inject(
            method = "spawnEffectParticle",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/EffectRenderer;addEffect(Lnet/minecraft/client/particle/EntityFX;)V"
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    public void onSpawnEffect(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed, double p_178927_10_, double p_178927_12_, int[] p_178927_14_, CallbackInfoReturnable<EntityFX> cir, IParticleFactory iparticlefactory, EntityFX entityfx) {
        EffectRendererHook.INSTANCE.trigger(entityfx, particleId, cir);
    }

    @Inject(
            method = "addBlockDestroyEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/EffectRenderer;addEffect(Lnet/minecraft/client/particle/EntityFX;)V"
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    public void onBreakParticle(BlockPos pos, IBlockState state, CallbackInfo ci, int i, int j, int k, int l, double d0, double d1, double d2) {
        EffectRendererHook.INSTANCE.trigger(new FakeDiggingFX(
                worldObj,
                d0,
                d1,
                d2,
                d0 - pos.getX() - 0.5,
                d1 - pos.getY() - 0.5,
                d2 - pos.getZ() - 0.5
        ), EnumParticleTypes.BLOCK_CRACK.getParticleID(), ci);
    }

    @Inject(
            method = "addBlockHitEffects(Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/EffectRenderer;addEffect(Lnet/minecraft/client/particle/EntityFX;)V"
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    public void onBlockHitParticle(BlockPos pos, EnumFacing side, CallbackInfo ci, IBlockState iblockstate, Block block, int i, int j, int k, float f, double d0, double d1, double d2) {
        EffectRendererHook.INSTANCE.trigger(new FakeDiggingFX(
                worldObj,
                d0,
                d1,
                d2,
                0.0,
                0.0,
                0.0
        ).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F),
        EnumParticleTypes.BLOCK_CRACK.getParticleID(), ci);
    }
}
