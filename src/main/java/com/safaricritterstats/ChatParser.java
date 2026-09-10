package com.safaricritterstats;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatParser {
    private static final Pattern CAPTURE = Pattern.compile(
        "^CAPTURE! You caught (?:an? )?(?:SPARKLING )?(.+?) and "
            + "(?:gained|received) (?:a Rainbow Feather and )?"
            + "(?:(?:([\\d,]+)x)|an?) (.+?) Shard!$",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern FOUND_CAPTURE = Pattern.compile(
        "^CAPTURE! You found (?:the |an? )?(?:SPARKLING )?(.+?),? "
            + "and as a reward it gave you "
            + "(?:(?:([\\d,]+)x)|an?) (.+?) Shard!$",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern LOOT_SHARE = Pattern.compile(
        "^LOOT SHARE! You received (?:a Rainbow Feather and )?"
            + "(?:(?:([\\d,]+)x)|an?) (.+?) Shard "
            + "from .+? (?:catching (?:an? )?|finding the )"
            + "(?:SPARKLING )?(.+?)!$",
        Pattern.CASE_INSENSITIVE);

    private ChatParser() {}

    public static Optional<CaptureEvent> parse(String message) {
        if (message == null) return Optional.empty();
        String normalized = ChatText.plain(message);

        Matcher capture = CAPTURE.matcher(normalized);
        if (capture.matches()) {
            return validated(capture.group(1), capture.group(3), capture.group(2));
        }

        Matcher foundCapture = FOUND_CAPTURE.matcher(normalized);
        if (foundCapture.matches()) {
            return validated(foundCapture.group(1), foundCapture.group(3), foundCapture.group(2));
        }

        Matcher lootShare = LOOT_SHARE.matcher(normalized);
        if (lootShare.matches()) {
            return validated(lootShare.group(3), lootShare.group(2), lootShare.group(1));
        }

        return Optional.empty();
    }

    private static Optional<CaptureEvent> validated(
        String caughtName,
        String shardName,
        String amountText
    ) {
        String caught = CritterCatalog.canonicalName(caughtName);
        String shard = CritterCatalog.canonicalName(shardName);
        if (caught == null || shard == null || !caught.equals(shard)) return Optional.empty();

        try {
            long amount = amountText == null ? 1 : Long.parseLong(amountText.replace(",", ""));
            return amount > 0 ? Optional.of(new CaptureEvent(shard, amount)) : Optional.empty();
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }
}
