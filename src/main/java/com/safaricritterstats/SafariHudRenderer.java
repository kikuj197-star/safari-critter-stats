package com.safaricritterstats;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class SafariHudRenderer {
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
            || minecraft.player == null
            || minecraft.level == null) {
            return;
        }

        Layout layout = liveLayout(minecraft.font);
        float scale = SafariCritterStatsMod.CONFIG.hudScale();
        HudBounds requested = bounds(
            SafariCritterStatsMod.CONFIG.hudX(),
            SafariCritterStatsMod.CONFIG.hudY(),
            scale,
            layout);
        int x = clampPosition(requested.x(), graphics.guiWidth() - requested.width());
        int y = clampPosition(requested.y(), graphics.guiHeight() - requested.height());
        renderLayout(graphics, minecraft.font, layout, x, y, scale);
    }

    public static HudBounds renderPreview(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        float scale
    ) {
        Font font = Minecraft.getInstance().font;
        Layout layout = previewLayout(font);
        renderLayout(graphics, font, layout, x, y, scale);
        return bounds(x, y, scale, layout);
    }

    public static HudBounds previewBounds(int x, int y, float scale) {
        return bounds(x, y, scale, previewLayout(Minecraft.getInstance().font));
    }

    public static int clampPosition(int value, int maximum) {
        return Math.max(0, Math.min(value, Math.max(0, maximum)));
    }

    private static Layout liveLayout(Font font) {
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

        return layout(font, lines);
    }

    private static Layout previewLayout(Font font) {
        List<HudLine> lines = List.of(
            new HudLine("Safari - Haunted", 0xFFFF55FF),
            new HudLine("Unique: 4/10 | Missing: 6", WHITE),
            new HudLine("- Litterbug", WHITE),
            new HudLine("- Solsnatcher", WHITE),
            new HudLine("- Gimmiegold", WHITE),
            new HudLine("- Hideonwall", WHITE),
            new HudLine("- Hideyho", WHITE),
            new HudLine("- Doomspiral", WHITE));
        return layout(font, lines);
    }

    private static Layout layout(Font font, List<HudLine> lines) {
        int lineHeight = font.lineHeight + LINE_GAP;
        int contentWidth = 0;
        for (HudLine line : lines) contentWidth = Math.max(contentWidth, font.width(line.text()));
        int boxWidth = contentWidth + PADDING * 2;
        int boxHeight = lines.size() * lineHeight - LINE_GAP + PADDING * 2;
        return new Layout(List.copyOf(lines), boxWidth, boxHeight);
    }

    private static void renderLayout(
        GuiGraphicsExtractor graphics,
        Font font,
        Layout layout,
        int x,
        int y,
        float scale
    ) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.fill(0, 0, layout.width(), layout.height(), BACKGROUND);

        int textY = PADDING;
        for (HudLine line : layout.lines()) {
            graphics.text(font, Component.literal(line.text()), PADDING, textY, line.color(), true);
            textY += font.lineHeight + LINE_GAP;
        }
        graphics.pose().popMatrix();
    }

    private static HudBounds bounds(int x, int y, float scale, Layout layout) {
        return new HudBounds(
            x,
            y,
            Math.max(1, (int) Math.ceil(layout.width() * scale)),
            Math.max(1, (int) Math.ceil(layout.height() * scale)));
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

    public record HudBounds(int x, int y, int width, int height) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
        }
    }

    private record Layout(List<HudLine> lines, int width, int height) {}

    private record HudLine(String text, int color) {}
}
