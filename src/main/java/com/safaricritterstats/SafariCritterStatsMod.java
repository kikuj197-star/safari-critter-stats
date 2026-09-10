package com.safaricritterstats;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SafariCritterStatsMod implements ClientModInitializer {
    public static final String MOD_ID = "safaricritterstats";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final SafariSessionStats STATS = new SafariSessionStats();
    public static final SafariCritterConfig CONFIG = SafariCritterConfig.load();

    @Override
    public void onInitializeClient() {
        SafariLocationTracker.initialize();
        LOGGER.info("[SafariCritterStats] Ready. Use /pc !unique (!uniq), !total, or !progress.");
    }
}
