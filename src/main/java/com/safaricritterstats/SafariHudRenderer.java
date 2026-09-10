package com.safaricritterstats;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class SafariHudRenderer {
    private static final int LEFT = 8;
    private static final int TOP = 8;
    private static final int PADDING = 5;
    private static final int LINE_GAP = 2;
    private static final int BACKGROUND = 0xB0000000;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int GREEN = 0xFF55FF55;

    private SafariHudRenderer() {}

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!SafariCritterStatsMod.CONFIG.isEnabled()
            || !SafariCritterStatsMod.CONFIG.showsHud()
            || !SafariLocationTracker.isInSafari()
            || minecraft.options.hideGui
            || minecraft.player == null
            || minecraft.level == null) {
            return;
        }

        SafariZone zone = SafariZoneTracker.currentZone();
        List<HudLine> lines = new ArrayList<>();
        lines.add(new HudLine("Safari - " + zone.displayName(), zoneColor(zone)));

        CritterRegion region = zone.region();
        if (region == null) {
            lines.add(new HudLine(
                zone == SafariZone.CENTER ? "Move into a biome to view missing critters" : "Detecting current biome...",
                GRAY));
        } else {
            List<String> missing = SafariCritterStatsMod.STATS.missingCritters(region);
            int caught = region.critters().size() - missing.size();
            lines.add(new HudLine(
                "Unique: " + caught + "/" + region.critters().size() + " | Missing: " + missing.size(),
                missing.isEmpty() ? GREEN : WHITE));
            if (missing.isEmpty()) {
                lines.add(new HudLine("All critters caught!", GREEN));
            } else {
                for (String critter : missing) {
                    lines.add(new HudLine("- " + critter, WHITE));
                }
            }
        }

        Font font = minecraft.font;
        int lineHeight = font.lineHeight + LINE_GAP;
        int contentWidth = 0;
        for (HudLine line : lines) contentWidth = Math.max(contentWidth, font.width(line.text()));
        int boxWidth = contentWidth + PADDING * 2;
        int boxHeight = lines.size() * lineHeight - LINE_GAP + PADDING * 2;
        graphics.fill(LEFT, TOP, LEFT + boxWidth, TOP + boxHeight, BACKGROUND);

        int textY = TOP + PADDING;
        for (HudLine line : lines) {
            graphics.text(font, Component.literal(line.text()), LEFT + PADDING, textY, line.color(), true);
            textY += lineHeight;
        }
    }

    private static int zoneColor(SafariZone zone) {
        return switch (zone) {
            case CAVERN -> 0xFFFFAA00;
            case FOREST -> 0xFF55FF55;
            case HAUNTED -> 0xFFFF55FF;
            case ICY -> 0xFF55FFFF;
            case CENTER, UNKNOWN -> WHITE;
        };
    }

    private record HudLine(String text, int color) {}
}
