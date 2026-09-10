package com.safaricritterstats;

import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CritterCatalog {
    /**
     * Wiki order: Cavern, Forest, Haunted, then Icy. Keeping this order makes
     * the short missing list stable and useful while moving through the map.
     */
    public static final List<String> ALL = Arrays.stream(CritterRegion.values())
        .flatMap(region -> region.critters().stream())
        .toList();

    private static final Map<String, String> BY_LOWERCASE_NAME = buildLookup();

    private CritterCatalog() {}

    public static String canonicalName(String name) {
        if (name == null) return null;
        return BY_LOWERCASE_NAME.get(name.strip().toLowerCase(Locale.ROOT));
    }

    private static Map<String, String> buildLookup() {
        Map<String, String> lookup = new LinkedHashMap<>();
        for (String name : ALL) lookup.put(name.toLowerCase(Locale.ROOT), name);
        return Map.copyOf(lookup);
    }
}
