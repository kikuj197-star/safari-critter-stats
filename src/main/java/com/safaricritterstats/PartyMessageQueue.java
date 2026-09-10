package com.safaricritterstats;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public final class PartyMessageQueue {
    private record Pending(String key, Supplier<String> response) {}

    private final long minimumIntervalMillis;
    private final long duplicateCooldownMillis;
    private final int maximumPending;
    private final Deque<Pending> pending = new ArrayDeque<>();
    private final Set<String> pendingKeys = new HashSet<>();
    private final Map<String, Long> recentlySent = new HashMap<>();
    private long nextAllowedAtMillis;

    public PartyMessageQueue(
        long minimumIntervalMillis,
        long duplicateCooldownMillis,
        int maximumPending
    ) {
        if (minimumIntervalMillis < 0 || duplicateCooldownMillis < 0 || maximumPending < 1) {
            throw new IllegalArgumentException("Invalid party message queue limits");
        }
        this.minimumIntervalMillis = minimumIntervalMillis;
        this.duplicateCooldownMillis = duplicateCooldownMillis;
        this.maximumPending = maximumPending;
    }

    public synchronized boolean enqueue(
        String key,
        Supplier<String> response,
        long nowMillis
    ) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(response, "response");
        pruneRecentlySent(nowMillis);

        Long lastSentAt = recentlySent.get(key);
        if (pendingKeys.contains(key)
            || (lastSentAt != null && isWithin(nowMillis, lastSentAt, duplicateCooldownMillis))
            || pending.size() >= maximumPending) {
            return false;
        }

        pending.addLast(new Pending(key, response));
        pendingKeys.add(key);
        return true;
    }

    public synchronized Optional<String> poll(long nowMillis) {
        if (pending.isEmpty() || nowMillis < nextAllowedAtMillis) return Optional.empty();

        Pending next = pending.removeFirst();
        pendingKeys.remove(next.key());
        recentlySent.put(next.key(), nowMillis);
        nextAllowedAtMillis = saturatedAdd(nowMillis, minimumIntervalMillis);
        return Optional.of(next.response().get());
    }

    public synchronized int pendingCount() {
        return pending.size();
    }

    public synchronized void reset() {
        pending.clear();
        pendingKeys.clear();
        recentlySent.clear();
        nextAllowedAtMillis = 0;
    }

    private void pruneRecentlySent(long nowMillis) {
        recentlySent.entrySet().removeIf(entry ->
            !isWithin(nowMillis, entry.getValue(), duplicateCooldownMillis));
    }

    private static boolean isWithin(long nowMillis, long previousMillis, long windowMillis) {
        long elapsed = nowMillis - previousMillis;
        return elapsed >= 0 && elapsed < windowMillis;
    }

    private static long saturatedAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }
}
