package com.safaricritterstats;

import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;

/** Uses Hypixel's official Mod API location event as the source of truth. */
public final class SafariLocationTracker {
    private static volatile boolean inSafari;

    private SafariLocationTracker() {}

    public static void initialize() {
        HypixelModAPI api = HypixelModAPI.getInstance();
        api.createHandler(ClientboundLocationPacket.class, SafariLocationTracker::onLocation);
        api.subscribeToEventPacket(ClientboundLocationPacket.class);
    }

    public static boolean isInSafari() {
        return inSafari;
    }

    public static boolean ensureRunStarted() {
        return inSafari;
    }

    public static synchronized void onGameLogin() {
        inSafari = false;
        SafariCritterStatsMod.STATS.reset();
        PartyMessageDispatcher.reset();
    }

    private static synchronized void onLocation(ClientboundLocationPacket packet) {
        boolean nextInSafari = packet.getMode()
            .map(SafariLocationTracker::isSafariMode)
            .orElse(false);
        if (nextInSafari && !inSafari) {
            SafariCritterStatsMod.STATS.startRun(System.currentTimeMillis());
            PartyMessageDispatcher.reset();
            inSafari = true;
            SafariCritterStatsMod.LOGGER.info("[SafariCritterStats] New Critter Safari run detected; counters reset.");
        } else if (!nextInSafari) {
            inSafari = false;
            PartyMessageDispatcher.reset();
        }
    }

    static boolean isSafariMode(String mode) {
        return mode != null && mode.equalsIgnoreCase("safari");
    }
}
