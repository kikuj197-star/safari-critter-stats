package com.safaricritterstats;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public enum CritterRegion {
    CAVERN("Cavern", List.of(
        "Cavernfish", "Flitter", "Shyworm", "Driftling", "Chuckwalla",
        "Rockmite", "Scrappy", "Snoozle", "Gemzie"
    ), List.of("c", "cave", "cavern")),

    FOREST("Forest", List.of(
        "Foxtrot", "Bluebird", "Honeybug", "Treefrog", "Woodchucker",
        "Fluffling", "Hideonfloor", "Parakeet", "Macaw"
    ), List.of("f", "forest")),

    HAUNTED("Haunted", List.of(
        "Areita", "Bloodbat", "Duplico", "Gazer", "Litterbug",
        "Solsnatcher", "Gimmiegold", "Hideonwall", "Hideyho", "Doomspiral"
    ), List.of("h", "haunt", "haunted")),

    ICY("Icy", List.of(
        "Strongarm", "Tepid", "Polaris", "Shuddersquid", "Billygoat",
        "Mantis Shrimp", "Nozzlenose", "Troodon", "Wumpa"
    ), List.of("i", "ice", "icy"));

    private static final Map<String, CritterRegion> BY_ALIAS = buildAliases();
    private static final Map<String, CritterRegion> BY_CRITTER = buildCritters();

    private final String displayName;
    private final List<String> critters;
    private final List<String> aliases;

    CritterRegion(String displayName, List<String> critters, List<String> aliases) {
        this.displayName = displayName;
        this.critters = critters;
        this.aliases = aliases;
    }

    public String displayName() {
        return displayName;
    }

    public List<String> critters() {
        return critters;
    }

    public static Optional<CritterRegion> fromAlias(String alias) {
        if (alias == null) return Optional.empty();
        return Optional.ofNullable(BY_ALIAS.get(alias.strip().toLowerCase(Locale.ROOT)));
    }

    public static Optional<CritterRegion> forCritter(String critter) {
        if (critter == null) return Optional.empty();
        return Optional.ofNullable(BY_CRITTER.get(critter));
    }

    private static Map<String, CritterRegion> buildAliases() {
        Map<String, CritterRegion> result = new LinkedHashMap<>();
        for (CritterRegion region : values()) {
            for (String alias : region.aliases) result.put(alias, region);
        }
        return Map.copyOf(result);
    }

    private static Map<String, CritterRegion> buildCritters() {
        Map<String, CritterRegion> result = new LinkedHashMap<>();
        for (CritterRegion region : values()) {
            for (String critter : region.critters) result.put(critter, region);
        }
        return Map.copyOf(result);
    }
}
