package com.safaricritterstats;

import java.util.Locale;
import java.util.Optional;

public record PartyQuery(Kind kind, CritterRegion region) {
    public enum Kind {
        UNIQUE,
        TOTAL,
        PROGRESS
    }

    public static Optional<PartyQuery> parse(String command) {
        if (command == null) return Optional.empty();
        String normalized = command.startsWith("/") ? command.substring(1).strip() : command.strip();
        normalized = normalized.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);

        String[] parts = normalized.split(" ");
        if (parts.length < 2 || !parts[0].equals("pc")) return Optional.empty();

        if (parts[1].equals("!unique") || parts[1].equals("!uniq")) {
            if (parts.length == 2) return Optional.of(new PartyQuery(Kind.UNIQUE, null));
            if (parts.length == 3) {
                return CritterRegion.fromAlias(parts[2])
                    .map(region -> new PartyQuery(Kind.UNIQUE, region));
            }
            return Optional.empty();
        }
        if (parts.length == 2 && parts[1].equals("!total")) {
            return Optional.of(new PartyQuery(Kind.TOTAL, null));
        }
        if (parts.length == 2
            && (parts[1].equals("!progress") || parts[1].equals("!prog"))) {
            return Optional.of(new PartyQuery(Kind.PROGRESS, null));
        }
        return Optional.empty();
    }

    public String response(SafariSessionStats stats) {
        return switch (kind) {
            case UNIQUE -> region == null
                ? stats.uniquePartyMessage()
                : stats.uniquePartyMessage(region);
            case TOTAL -> stats.totalPartyMessage();
            case PROGRESS -> stats.progressPartyMessage();
        };
    }
}
