package com.github.synnerz.akutz.mixin;

import com.github.synnerz.akutz.hooks.TileEntityRendererDispatcherHook;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher {
    @Inject(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntity;DDDFI)V", at = @At("HEAD"), cancellable = true)
    public void onPreTileEntityRender(TileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage, CallbackInfo ci) {
        TileEntityRendererDispatcherHook.INSTANCE.triggerRenderTileEntity(tileEntityIn, x, y, z, partialTicks, ci);
    }

    @Inject(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntity;DDDFI)V", at = @At("TAIL"))
    public void onPostTileEntityRender(TileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage, CallbackInfo ci) {
        TileEntityRendererDispatcherHook.INSTANCE.triggerPostRenderTileEntity(tileEntityIn, x, y, z, partialTicks);
    }
}
