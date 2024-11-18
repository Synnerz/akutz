package com.github.synnerz.akutz.mixin;

import com.github.synnerz.akutz.hooks.PlayerControllerMPHook;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    @Inject(method = "attackEntity", at = @At("HEAD"))
    public void onPreAttackEntity(EntityPlayer playerIn, Entity targetEntity, CallbackInfo ci) {
        PlayerControllerMPHook.INSTANCE.triggerAttackEntity(targetEntity);
    }

    @Inject(method = "onPlayerDestroyBlock", at = @At("HEAD"))
    public void onPlayerDestroy(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        PlayerControllerMPHook.INSTANCE.triggerBlockBreak(pos);
    }
}
