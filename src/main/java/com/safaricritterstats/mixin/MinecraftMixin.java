package com.safaricritterstats.mixin;

import com.safaricritterstats.PartyMessageDispatcher;
import com.safaricritterstats.SafariZoneTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void safariStats$clientTick(CallbackInfo ci) {
        SafariZoneTracker.tick();
        PartyMessageDispatcher.tick();
    }
}
