package com.safaricritterstats;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public final class SafariZoneTracker {
    private static volatile SafariZone currentZone = SafariZone.UNKNOWN;

    private SafariZoneTracker() {}

    public static SafariZone currentZone() {
        return currentZone;
    }

    public static void tick() {
        if (!SafariCritterStatsMod.CONFIG.isEnabled() || !SafariLocationTracker.isInSafari()) {
            currentZone = SafariZone.UNKNOWN;
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            currentZone = SafariZone.UNKNOWN;
            return;
        }

        BlockPos position = minecraft.player.blockPosition();
        if (isSpawnCoordinates(position.getX(), position.getY(), position.getZ())) {
            currentZone = SafariZone.CENTER;
            return;
        }

        Holder<Biome> biome = minecraft.level.getBiome(position);
        currentZone = biome.unwrapKey()
            .map(key -> SafariZone.fromBiomeId(key.identifier().toString()))
            .orElse(SafariZone.UNKNOWN);
    }

    public static void reset() {
        currentZone = SafariZone.UNKNOWN;
    }

    static boolean isSpawnCoordinates(int x, int y, int z) {
        return x >= -58 && x <= -41
            && y >= 67 && y <= 75
            && z >= 19 && z <= 27;
    }
}
