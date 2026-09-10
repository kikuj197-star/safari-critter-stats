package com.safaricritterstats;

import de.hysky.skyblocker.utils.Utils;

/** Uses Skyblocker's Hypixel Mod API-backed location state as the source of truth. */
public final class SafariLocationTracker {
    private static boolean wasInSafari;

    private SafariLocationTracker() {}

    public static boolean isInSafari() {
        return Utils.isInSafari();
    }

    public static boolean ensureRunStarted() {
        boolean inSafari = isInSafari();
        update(inSafari);
        return inSafari;
    }

    public static void onGameLogin() {
        wasInSafari = false;
        SafariCritterStatsMod.STATS.reset();
    }

    public static void tick() {
        update(isInSafari());
    }

    private static void update(boolean inSafari) {
        if (inSafari && !wasInSafari) {
            SafariCritterStatsMod.STATS.startRun(System.currentTimeMillis());
            SafariCritterStatsMod.LOGGER.info("[SafariCritterStats] New Critter Safari run detected; counters reset.");
        }
        wasInSafari = inSafari;
    }
}
