package com.safaricritterstats.mixin;

import com.safaricritterstats.SafariLocationTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void safariStats$trackSafariEntry(CallbackInfo ci) {
        SafariLocationTracker.tick();
    }
}
