package com.safaricritterstats;

import java.util.Optional;

public final class ChatParserTest {
    public static void main(String[] args) {
        assertEvent(
            "LOOT SHARE! You received 2x Polaris Shard from CherryWoodPlank catching a Polaris!",
            "Polaris", 2);
        assertEvent(
            "LOOT SHARE! You received a Troodon Shard from Cloudy catching a Troodon!",
            "Troodon", 1);
        assertEvent(
            "CAPTURE! You caught a Bluebird and gained 2x Bluebird Shard!",
            "Bluebird", 2);
        assertEvent(
            "CAPTURE! You caught a Bluebird and gained a Bluebird Shard!",
            "Bluebird", 1);
        assertEvent(
            "§a§lCAPTURE! §7You caught a §fFoxtrot§7 and gained a §fFoxtrot Shard§7!",
            "Foxtrot", 1);
        assertEvent(
            "§a§lCAPTURE! §7You caught a §aHoneybug§7 and gained 2x §aHoneybug Shard§7!",
            "Honeybug", 2);
        assertEvent(
            "CAPTURE! You caught a SPARKLING Mantis Shrimp and received a Rainbow Feather and 10x Mantis Shrimp Shard!",
            "Mantis Shrimp", 10);
        assertEvent(
            "LOOT SHARE! You received a Rainbow Feather and 20x Macaw Shard from Player catching a SPARKLING Macaw!",
            "Macaw", 20);
        assertEmpty("You found 1x Polaris Shard on the floor!");
        assertEmpty("Party > Player: CAPTURE! You caught a Bluebird and gained 2x Bluebird Shard!");
        assertEmpty("CAPTURE! You caught a Bluebird and gained 2x Polaris Shard!");

        assertRegionAlias("I", CritterRegion.ICY);
        assertRegionAlias("i", CritterRegion.ICY);
        assertRegionAlias("ice", CritterRegion.ICY);
        assertRegionAlias("icy", CritterRegion.ICY);
        assertRegionAlias("cave", CritterRegion.CAVERN);
        assertRegionAlias("forest", CritterRegion.FOREST);
        assertRegionAlias("haunted", CritterRegion.HAUNTED);
        assertPartyQuery("pc !unique", PartyQuery.Kind.UNIQUE, null);
        assertPartyQuery("/pc !uniq", PartyQuery.Kind.UNIQUE, null);
        assertPartyQuery("pc !uniq f", PartyQuery.Kind.UNIQUE, CritterRegion.FOREST);
        assertPartyQuery("/pc   !UNIQ   Ice", PartyQuery.Kind.UNIQUE, CritterRegion.ICY);
        assertPartyQuery("pc !prog", PartyQuery.Kind.PROGRESS, null);
        assertNoPartyQuery("pc !uniq nowhere");

        SafariSessionStats regionStats = new SafariSessionStats();
        regionStats.startRun(1_000);
        if (!regionStats.uniquePartyMessage(CritterRegion.ICY).equals(
            "Missing 9: Strongarm, Tepid, Polaris, Shuddersquid, Billygoat")) {
            throw new AssertionError(regionStats.uniquePartyMessage(CritterRegion.ICY));
        }
        for (int i = 0; i < CritterRegion.CAVERN.critters().size() - 1; i++) {
            Optional<RegionCompletion> earlyCompletion = regionStats.record(
                new CaptureEvent(CritterRegion.CAVERN.critters().get(i), 1), 10_000 + i * 1_000L);
            if (earlyCompletion.isPresent()) {
                throw new AssertionError("region completed early: " + earlyCompletion.get());
            }
        }
        String lastCavernCritter = CritterRegion.CAVERN.critters().getLast();
        RegionCompletion cavernCompletion = regionStats.record(
            new CaptureEvent(lastCavernCritter, 1), 126_000)
            .orElseThrow(() -> new AssertionError("missing Cavern completion"));
        if (!cavernCompletion.partyMessage().equals("Cavern done (2m 5s)")) {
            throw new AssertionError(cavernCompletion.partyMessage());
        }
        if (regionStats.record(new CaptureEvent(lastCavernCritter, 4), 130_000).isPresent()) {
            throw new AssertionError("duplicate capture announced Cavern twice");
        }
        if (!regionStats.uniquePartyMessage(CritterRegion.CAVERN).equals("Cavern done (2m 5s)")) {
            throw new AssertionError(regionStats.uniquePartyMessage(CritterRegion.CAVERN));
        }
        if (!regionStats.progressPartyMessage().equals(
            "Progress: Cavern done (2m 5s) | Forest 0/9 | Haunted 0/10 | Icy 0/9")) {
            throw new AssertionError(regionStats.progressPartyMessage());
        }

        SafariSessionStats stats = new SafariSessionStats();
        if (!stats.uniquePartyMessage().equals(
            "Missing 37: Cavernfish, Flitter, Shyworm, Driftling, Chuckwalla")) {
            throw new AssertionError(stats.uniquePartyMessage());
        }
        stats.record(new CaptureEvent("Cavernfish", 2));
        stats.record(new CaptureEvent("Cavernfish", 3));
        if (!stats.totalPartyMessage().equals("Total: 2 critters | 5 shards")) {
            throw new AssertionError(stats.totalPartyMessage());
        }
        for (int i = 1; i < 32; i++) stats.record(new CaptureEvent(CritterCatalog.ALL.get(i), 1));
        if (!stats.uniquePartyMessage().equals(
            "Missing 5: Billygoat, Mantis Shrimp, Nozzlenose, Troodon, Wumpa")) {
            throw new AssertionError(stats.uniquePartyMessage());
        }
        for (int i = 32; i < CritterCatalog.ALL.size(); i++) {
            stats.record(new CaptureEvent(CritterCatalog.ALL.get(i), 1));
        }
        if (!stats.uniquePartyMessage().equals("All 37 critters caught!")) {
            throw new AssertionError(stats.uniquePartyMessage());
        }
        stats.reset();
        if (stats.totalCritters() != 0 || stats.totalShards() != 0
            || stats.missingCritters().size() != 37) {
            throw new AssertionError("reset failed");
        }
    }

    private static void assertEvent(String message, String critter, long shards) {
        CaptureEvent event = ChatParser.parse(message)
            .orElseThrow(() -> new AssertionError("Not parsed: " + message));
        if (!event.critter().equals(critter) || event.shards() != shards) {
            throw new AssertionError("Unexpected event: " + event);
        }
    }

    private static void assertEmpty(String message) {
        Optional<CaptureEvent> event = ChatParser.parse(message);
        if (event.isPresent()) throw new AssertionError("Should not parse: " + event.get());
    }

    private static void assertRegionAlias(String alias, CritterRegion expected) {
        CritterRegion actual = CritterRegion.fromAlias(alias)
            .orElseThrow(() -> new AssertionError("Missing alias: " + alias));
        if (actual != expected) throw new AssertionError(alias + " mapped to " + actual);
    }

    private static void assertPartyQuery(
        String command,
        PartyQuery.Kind expectedKind,
        CritterRegion expectedRegion
    ) {
        PartyQuery query = PartyQuery.parse(command)
            .orElseThrow(() -> new AssertionError("Not parsed: " + command));
        if (query.kind() != expectedKind || query.region() != expectedRegion) {
            throw new AssertionError("Unexpected query: " + query);
        }
    }

    private static void assertNoPartyQuery(String command) {
        if (PartyQuery.parse(command).isPresent()) {
            throw new AssertionError("Should not parse: " + command);
        }
    }

}
