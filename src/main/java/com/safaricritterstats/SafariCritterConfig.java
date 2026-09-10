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

    private final Path path;
    private volatile boolean enabled;
    private volatile boolean respondToPartyCommands;
    private volatile boolean showHud;

    private SafariCritterConfig(
        Path path,
        boolean enabled,
        boolean respondToPartyCommands,
        boolean showHud
    ) {
        this.path = path;
        this.enabled = enabled;
        this.respondToPartyCommands = respondToPartyCommands;
        this.showHud = showHud;
    }

    public static SafariCritterConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (!Files.isRegularFile(path)) return new SafariCritterConfig(path, true, true, true);

        try (Reader reader = Files.newBufferedReader(path)) {
            StoredConfig stored = GSON.fromJson(reader, StoredConfig.class);
            if (stored == null) return new SafariCritterConfig(path, true, true, true);
            boolean enabled = stored.enabled == null || stored.enabled;
            boolean respond = stored.respondToPartyCommands == null || stored.respondToPartyCommands;
            boolean showHud = stored.showHud == null || stored.showHud;
            return new SafariCritterConfig(path, enabled, respond, showHud);
        } catch (Exception exception) {
            SafariCritterStatsMod.LOGGER.warn(
                "[SafariCritterStats] Could not read {}; using defaults.", path, exception);
            return new SafariCritterConfig(path, true, true, true);
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

    private void save() {
        try {
            Files.createDirectories(path.getParent());
            Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                GSON.toJson(new StoredConfig(enabled, respondToPartyCommands, showHud), writer);
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

    private static final class StoredConfig {
        private final Boolean enabled;
        private final Boolean respondToPartyCommands;
        private final Boolean showHud;

        private StoredConfig(boolean enabled, boolean respondToPartyCommands, boolean showHud) {
            this.enabled = enabled;
            this.respondToPartyCommands = respondToPartyCommands;
            this.showHud = showHud;
        }
    }
}
