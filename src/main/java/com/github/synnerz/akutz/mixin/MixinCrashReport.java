package com.github.synnerz.akutz.mixin;

import com.github.synnerz.akutz.engine.module.ModuleManager;
import com.github.synnerz.akutz.hooks.CrashReportHook;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
public class MixinCrashReport {
    @Final
    @Shadow
    private CrashReportCategory theReportCategory;

    @Inject(method = "populateEnvironment", at = @At("HEAD"))
    public void onPreCrash(CallbackInfo ci) {
        this.theReportCategory.addCrashSection("Akutz Modules", ModuleManager.INSTANCE.getCrashList$akutz());
    }
}
