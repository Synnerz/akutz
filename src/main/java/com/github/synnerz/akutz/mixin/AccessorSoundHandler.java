package com.github.synnerz.akutz.mixin;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SoundHandler.class)
public interface AccessorSoundHandler {
    @Accessor("sndManager")
    SoundManager getSoundManager();
}
