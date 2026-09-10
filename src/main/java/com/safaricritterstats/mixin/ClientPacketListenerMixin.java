package com.safaricritterstats.mixin;

import com.safaricritterstats.ChatParser;
import com.safaricritterstats.CritterRegion;
import com.safaricritterstats.SafariLocationTracker;
import com.safaricritterstats.SafariCritterStatsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

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
        ChatParser.parse(packet.content().getString()).ifPresent(event ->
            SafariCritterStatsMod.STATS.record(event, System.currentTimeMillis())
                .ifPresent(completion -> safariStats$sendParty(completion.partyMessage()))
        );
    }

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void safariStats$handlePartyQuery(String command, CallbackInfo ci) {
        String normalized = command.startsWith("/") ? command.substring(1).strip() : command.strip();
        normalized = normalized.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);

        String[] parts = normalized.split(" ");
        if (parts.length < 2 || !parts[0].equals("pc")) return;

        String response = null;
        if (parts[1].equals("!unique")) {
            if (parts.length == 2) {
                response = SafariCritterStatsMod.STATS.uniquePartyMessage();
            } else if (parts.length == 3) {
                CritterRegion region = CritterRegion.fromAlias(parts[2]).orElse(null);
                if (region == null) return;
                response = SafariCritterStatsMod.STATS.uniquePartyMessage(region);
            }
        } else if (parts.length == 2 && parts[1].equals("!total")) {
            response = SafariCritterStatsMod.STATS.totalPartyMessage();
        } else if (parts.length == 2
            && (parts[1].equals("!progress") || parts[1].equals("!prog"))) {
            response = SafariCritterStatsMod.STATS.progressPartyMessage();
        }
        if (response == null) return;

        // Outside the Safari this mod is inert: leave the original /pc message untouched.
        if (!SafariLocationTracker.ensureRunStarted()) return;

        ci.cancel();
        safariStats$sendParty(response);
    }

    private static void safariStats$sendParty(String response) {
        Minecraft.getInstance().execute(() -> {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection != null && SafariLocationTracker.ensureRunStarted()) {
                connection.sendCommand("pc " + response);
            }
        });
    }
}
