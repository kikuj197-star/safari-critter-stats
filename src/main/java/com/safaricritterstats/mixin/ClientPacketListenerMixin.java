package com.safaricritterstats.mixin;

import com.safaricritterstats.ChatParser;
import com.safaricritterstats.PartyMessageDispatcher;
import com.safaricritterstats.PartyQuery;
import com.safaricritterstats.SafariLocationTracker;
import com.safaricritterstats.SafariCritterStatsMod;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    /** Clears stale process state; sidebar tracking handles in-connection island transfers. */
    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void safariStats$resetForNewServer(ClientboundLoginPacket packet, CallbackInfo ci) {
        SafariLocationTracker.onGameLogin();
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    private void safariStats$readCapture(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        if (packet.overlay() || !SafariLocationTracker.ensureRunStarted()) return;
        String message = packet.content().getString();
        ChatParser.parse(message).ifPresent(event ->
            SafariCritterStatsMod.STATS.record(event, System.currentTimeMillis())
                .ifPresent(PartyMessageDispatcher::enqueue)
        );
        if (SafariCritterStatsMod.CONFIG.respondsToPartyCommands()) {
            PartyQuery.parsePartyChat(message).ifPresent(PartyMessageDispatcher::enqueue);
        }
    }

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void safariStats$handlePartyQuery(String command, CallbackInfo ci) {
        PartyQuery query = PartyQuery.parse(command).orElse(null);
        if (query == null) return;

        // Outside the Safari this mod is inert: leave the original /pc message untouched.
        if (!SafariLocationTracker.ensureRunStarted()) return;

        ci.cancel();
        PartyMessageDispatcher.enqueue(query);
    }
}
