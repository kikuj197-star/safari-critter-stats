package com.safaricritterstats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class SafariCritterConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "safari-critter-stats.json";
    public static final float MIN_HUD_SCALE = 0.5F;
    public static final float MAX_HUD_SCALE = 2.5F;

    private final Path path;
    private volatile boolean enabled;
    private volatile boolean respondToPartyCommands;
    private volatile boolean showHud;
    private volatile int hudX;
    private volatile int hudY;
    private volatile float hudScale;

    private SafariCritterConfig(
        Path path,
        boolean enabled,
        boolean respondToPartyCommands,
        boolean showHud,
        int hudX,
        int hudY,
        float hudScale
    ) {
        this.path = path;
        this.enabled = enabled;
        this.respondToPartyCommands = respondToPartyCommands;
        this.showHud = showHud;
        this.hudX = Math.max(0, hudX);
        this.hudY = Math.max(0, hudY);
        this.hudScale = clampScale(hudScale);
    }

    public static SafariCritterConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (!Files.isRegularFile(path)) {
            return new SafariCritterConfig(path, true, true, true, 8, 8, 1.0F);
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            StoredConfig stored = GSON.fromJson(reader, StoredConfig.class);
            if (stored == null) {
                return new SafariCritterConfig(path, true, true, true, 8, 8, 1.0F);
            }
            boolean enabled = stored.enabled == null || stored.enabled;
            boolean respond = stored.respondToPartyCommands == null || stored.respondToPartyCommands;
            boolean showHud = stored.showHud == null || stored.showHud;
            int hudX = stored.hudX == null ? 8 : stored.hudX;
            int hudY = stored.hudY == null ? 8 : stored.hudY;
            float hudScale = stored.hudScale == null ? 1.0F : stored.hudScale;
            return new SafariCritterConfig(path, enabled, respond, showHud, hudX, hudY, hudScale);
        } catch (Exception exception) {
            SafariCritterStatsMod.LOGGER.warn(
                "[SafariCritterStats] Could not read {}; using defaults.", path, exception);
            return new SafariCritterConfig(path, true, true, true, 8, 8, 1.0F);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean respondsToPartyCommands() {
        return respondToPartyCommands;
    }

    public boolean showsHud() {
        return showHud;
    }

    public int hudX() {
        return hudX;
    }

    public int hudY() {
        return hudY;
    }

    public float hudScale() {
        return hudScale;
    }

    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public synchronized void setRespondToPartyCommands(boolean respondToPartyCommands) {
        this.respondToPartyCommands = respondToPartyCommands;
        save();
    }

    public synchronized void setShowHud(boolean showHud) {
        this.showHud = showHud;
        save();
    }

    public synchronized void setHudLayout(int hudX, int hudY, float hudScale) {
        this.hudX = Math.max(0, hudX);
        this.hudY = Math.max(0, hudY);
        this.hudScale = clampScale(hudScale);
        save();
    }

    private void save() {
        try {
            Files.createDirectories(path.getParent());
            Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                GSON.toJson(new StoredConfig(
                    enabled,
                    respondToPartyCommands,
                    showHud,
                    hudX,
                    hudY,
                    hudScale), writer);
            }
            try {
                Files.move(
                    temporary,
                    path,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            SafariCritterStatsMod.LOGGER.error(
                "[SafariCritterStats] Could not save {}.", path, exception);
        }
    }

    public static float clampScale(float scale) {
        if (!Float.isFinite(scale)) return 1.0F;
        return Math.max(MIN_HUD_SCALE, Math.min(MAX_HUD_SCALE, scale));
    }

    private static final class StoredConfig {
        private final Boolean enabled;
        private final Boolean respondToPartyCommands;
        private final Boolean showHud;
        private final Integer hudX;
        private final Integer hudY;
        private final Float hudScale;

        private StoredConfig(
            boolean enabled,
            boolean respondToPartyCommands,
            boolean showHud,
            int hudX,
            int hudY,
            float hudScale
        ) {
            this.enabled = enabled;
            this.respondToPartyCommands = respondToPartyCommands;
            this.showHud = showHud;
            this.hudX = hudX;
            this.hudY = hudY;
            this.hudScale = hudScale;
        }
    }
}
