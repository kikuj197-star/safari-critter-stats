package com.safaricritterstats;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SafariSessionStats {
    private final Set<String> caughtTypes = new HashSet<>();
    private final Map<CritterRegion, Long> completionTimes = new EnumMap<>(CritterRegion.class);
    private long totalCritters;
    private long totalShards;
    private long runStartedAtMillis;

    public synchronized void reset() {
        caughtTypes.clear();
        completionTimes.clear();
        totalCritters = 0;
        totalShards = 0;
        runStartedAtMillis = 0;
    }

    public synchronized void startRun(long nowMillis) {
        reset();
        runStartedAtMillis = Math.max(1, nowMillis);
    }

    public synchronized void record(CaptureEvent event) {
        record(event, System.currentTimeMillis());
    }

    public synchronized Optional<RegionCompletion> record(CaptureEvent event, long nowMillis) {
        if (runStartedAtMillis == 0) runStartedAtMillis = Math.max(1, nowMillis);
        boolean firstOfType = caughtTypes.add(event.critter());
        totalCritters = saturatedAdd(totalCritters, 1);
        totalShards = saturatedAdd(totalShards, event.shards());

        if (!firstOfType) return Optional.empty();
        Optional<CritterRegion> region = CritterRegion.forCritter(event.critter());
        if (region.isEmpty() || completionTimes.containsKey(region.get())
            || !missingCritters(region.get()).isEmpty()) {
            return Optional.empty();
        }

        long elapsed = Math.max(0, nowMillis - runStartedAtMillis);
        completionTimes.put(region.get(), elapsed);
        return Optional.of(new RegionCompletion(region.get(), elapsed));
    }

    public synchronized List<String> missingCritters() {
        List<String> missing = new ArrayList<>();
        for (String critter : CritterCatalog.ALL) {
            if (!caughtTypes.contains(critter)) missing.add(critter);
        }
        return List.copyOf(missing);
    }

    public synchronized long totalCritters() {
        return totalCritters;
    }

    public synchronized long totalShards() {
        return totalShards;
    }

    public synchronized String uniquePartyMessage() {
        return missingMessage("", missingCritters());
    }

    public synchronized String uniquePartyMessage(CritterRegion region) {
        List<String> missing = missingCritters(region);
        if (missing.isEmpty()) {
            Long elapsed = completionTimes.get(region);
            return elapsed == null
                ? region.displayName() + " done"
                : new RegionCompletion(region, elapsed).partyMessage();
        }
        return missingMessage("", missing);
    }

    public synchronized String progressPartyMessage() {
        List<String> parts = new ArrayList<>();
        for (CritterRegion region : CritterRegion.values()) {
            Long elapsed = completionTimes.get(region);
            if (elapsed != null) {
                parts.add(region.displayName() + " done (" + formatElapsed(elapsed) + ")");
            } else {
                int caught = region.critters().size() - missingCritters(region).size();
                parts.add(region.displayName() + " " + caught + "/" + region.critters().size());
            }
        }
        return "Progress: " + String.join(" | ", parts);
    }

    public synchronized String totalPartyMessage() {
        return "Total: " + totalCritters + " critters | " + totalShards + " shards";
    }

    public synchronized List<String> missingCritters(CritterRegion region) {
        List<String> missing = new ArrayList<>();
        for (String critter : region.critters()) {
            if (!caughtTypes.contains(critter)) missing.add(critter);
        }
        return List.copyOf(missing);
    }

    private static String missingMessage(String prefix, List<String> missing) {
        if (missing.isEmpty()) return "All 37 critters caught!";
        List<String> shown = missing.subList(0, Math.min(5, missing.size()));
        return prefix + "Missing " + missing.size() + ": " + String.join(", ", shown);
    }

    static String formatElapsed(long elapsedMillis) {
        long seconds = Math.max(0, elapsedMillis) / 1_000;
        long hours = seconds / 3_600;
        long minutes = (seconds % 3_600) / 60;
        long remainingSeconds = seconds % 60;
        if (hours > 0) return hours + "h " + minutes + "m " + remainingSeconds + "s";
        if (minutes > 0) return minutes + "m " + remainingSeconds + "s";
        return remainingSeconds + "s";
    }

    private static long saturatedAdd(long left, long right) {
        if (right > 0 && left > Long.MAX_VALUE - right) return Long.MAX_VALUE;
        return left + right;
    }
}
