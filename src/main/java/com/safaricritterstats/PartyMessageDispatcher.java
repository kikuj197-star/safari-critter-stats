package com.safaricritterstats;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public final class PartyMessageDispatcher {
    static final long MINIMUM_INTERVAL_MILLIS = 1_000;
    static final long DUPLICATE_COOLDOWN_MILLIS = 1_000;
    static final int MAXIMUM_PENDING = 16;

    private static final PartyMessageQueue QUEUE = new PartyMessageQueue(
        MINIMUM_INTERVAL_MILLIS,
        DUPLICATE_COOLDOWN_MILLIS,
        MAXIMUM_PENDING);

    private PartyMessageDispatcher() {}

    public static void enqueue(PartyQuery query) {
        QUEUE.enqueue(
            query.key(),
            () -> query.response(SafariCritterStatsMod.STATS),
            System.currentTimeMillis());
    }

    public static void enqueue(RegionCompletion completion) {
        QUEUE.enqueue(
            "done:" + completion.region().name(),
            completion::partyMessage,
            System.currentTimeMillis());
    }

    public static void tick() {
        if (!SafariLocationTracker.isInSafari()) return;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;
        QUEUE.poll(System.currentTimeMillis())
            .ifPresent(message -> connection.sendCommand("pc " + message));
    }

    public static void reset() {
        QUEUE.reset();
    }
}
