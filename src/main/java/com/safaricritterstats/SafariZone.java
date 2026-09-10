package com.safaricritterstats;

import java.util.Locale;

public enum SafariZone {
    CAVERN("Cavern", CritterRegion.CAVERN),
    FOREST("Forest", CritterRegion.FOREST),
    HAUNTED("Haunted", CritterRegion.HAUNTED),
    ICY("Icy", CritterRegion.ICY),
    CENTER("Center", null),
    UNKNOWN("Unknown", null);

    private final String displayName;
    private final CritterRegion region;

    SafariZone(String displayName, CritterRegion region) {
        this.displayName = displayName;
        this.region = region;
    }

    public String displayName() {
        return displayName;
    }

    public CritterRegion region() {
        return region;
    }

    static SafariZone fromBiomeId(String biomeId) {
        if (biomeId == null) return UNKNOWN;
        return switch (biomeId.toLowerCase(Locale.ROOT)) {
            case "hypixel:cavern" -> CAVERN;
            case "hypixel:forest" -> FOREST;
            case "hypixel:haunted" -> HAUNTED;
            case "hypixel:icy", "hypixel:icy_caves" -> ICY;
            default -> UNKNOWN;
        };
    }
}
