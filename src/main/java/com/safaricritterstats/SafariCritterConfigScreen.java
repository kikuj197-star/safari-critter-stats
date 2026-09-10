package com.safaricritterstats;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SafariCritterConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 240;
    private static final int BUTTON_HEIGHT = 20;
    private final Screen parent;
    private CycleButton<Boolean> partyCommandsButton;

    public SafariCritterConfigScreen(Screen parent) {
        super(Component.literal("Safari Critter Stats"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(BUTTON_WIDTH, width - 40);
        int left = (width - contentWidth) / 2;
        int firstButtonY = Math.max(82, height / 2 - 28);

        StringWidget titleWidget = new StringWidget(title, font);
        titleWidget.setX((width - titleWidget.getWidth()) / 2);
        titleWidget.setY(25);
        addRenderableWidget(titleWidget);
        MultiLineTextWidget description = new MultiLineTextWidget(
            20,
            48,
            Component.literal("Changes are saved immediately."),
            font).setMaxWidth(width - 40).setCentered(true);
        addRenderableWidget(description);

        SafariCritterConfig config = SafariCritterStatsMod.CONFIG;
        addRenderableWidget(CycleButton.onOffBuilder(config.isEnabled()).create(
            left,
            firstButtonY,
            contentWidth,
            BUTTON_HEIGHT,
            Component.literal("Mod enabled"),
            (button, enabled) -> {
                config.setEnabled(enabled);
                SafariLocationTracker.onMasterEnabledChanged(enabled);
                partyCommandsButton.active = enabled;
            }));

        partyCommandsButton = addRenderableWidget(
            CycleButton.onOffBuilder(config.respondsToPartyCommands()).create(
                left,
                firstButtonY + 28,
                contentWidth,
                BUTTON_HEIGHT,
                Component.literal("Respond to party commands"),
                (button, respond) -> {
                    config.setRespondToPartyCommands(respond);
                    if (!respond) PartyMessageDispatcher.reset();
                }));
        partyCommandsButton.active = config.isEnabled();

        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
            .bounds(left, height - 32, contentWidth, BUTTON_HEIGHT)
            .build());
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
