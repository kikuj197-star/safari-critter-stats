package com.safaricritterstats;

public record RegionCompletion(CritterRegion region, long elapsedMillis) {
    public RegionCompletion {
        if (region == null) throw new IllegalArgumentException("region");
        elapsedMillis = Math.max(0, elapsedMillis);
    }

    public String partyMessage() {
        return region.displayName() + " done (" + SafariSessionStats.formatElapsed(elapsedMillis) + ")";
    }
}
