package com.safaricritterstats;

import java.util.regex.Pattern;

public final class ChatText {
    private static final Pattern LEGACY_FORMATTING = Pattern.compile(
        "\\u00A7[0-9A-FK-ORX]",
        Pattern.CASE_INSENSITIVE);

    private ChatText() {}

    public static String plain(String message) {
        if (message == null) return "";
        return LEGACY_FORMATTING.matcher(message).replaceAll("").strip();
    }
}
