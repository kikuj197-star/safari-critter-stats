package com.safaricritterstats;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class SafariHudEditorScreen extends Screen {
    private static final int BUTTON_WIDTH = 110;
    private static final int BUTTON_HEIGHT = 20;
    private final Screen parent;
    private int hudX;
    private int hudY;
    private float hudScale;
    private boolean draggingHud;
    private double dragOffsetX;
    private double dragOffsetY;

    public SafariHudEditorScreen(Screen parent) {
        super(Component.literal("Safari HUD Editor"));
        this.parent = parent;
        SafariCritterConfig config = SafariCritterStatsMod.CONFIG;
        hudX = config.hudX();
        hudY = config.hudY();
        hudScale = config.hudScale();
    }

    @Override
    protected void init() {
        clampToScreen();

        MultiLineTextWidget instructions = new MultiLineTextWidget(
            20,
            16,
            Component.literal("Drag the HUD with the left mouse button. Scroll over it to resize."),
            font).setMaxWidth(width - 40).setCentered(true);
        addRenderableWidget(instructions);

        int gap = 8;
        int left = (width - BUTTON_WIDTH * 2 - gap) / 2;
        addRenderableWidget(Button.builder(Component.literal("Reset"), button -> {
            hudX = 8;
            hudY = 8;
            hudScale = 1.0F;
            clampToScreen();
            saveLayout();
        }).bounds(left, height - 32, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
            .bounds(left + BUTTON_WIDTH + gap, height - 32, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build());
    }

    @Override
    public void extractRenderState(
        GuiGraphicsExtractor graphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {
        SafariHudRenderer.HudBounds bounds = SafariHudRenderer.renderPreview(
            graphics, hudX, hudY, hudScale);
        int border = draggingHud || bounds.contains(mouseX, mouseY)
            ? 0xFFFFFFFF
            : 0xFF777777;
        graphics.outline(bounds.x() - 1, bounds.y() - 1, bounds.width() + 2, bounds.height() + 2, border);
        graphics.centeredText(font, "Scale: " + Math.round(hudScale * 100) + "%", width / 2, height - 55, 0xFFFFFFFF);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        SafariHudRenderer.HudBounds bounds = SafariHudRenderer.previewBounds(hudX, hudY, hudScale);
        if (event.button() == 0 && bounds.contains(event.x(), event.y())) {
            draggingHud = true;
            dragOffsetX = event.x() - hudX;
            dragOffsetY = event.y() - hudY;
            setDragging(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingHud && event.button() == 0) {
            hudX = (int) Math.round(event.x() - dragOffsetX);
            hudY = (int) Math.round(event.y() - dragOffsetY);
            clampToScreen();
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingHud && event.button() == 0) {
            draggingHud = false;
            setDragging(false);
            saveLayout();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        SafariHudRenderer.HudBounds bounds = SafariHudRenderer.previewBounds(hudX, hudY, hudScale);
        if (scrollY == 0 || !bounds.contains(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        float previousScale = hudScale;
        float step = scrollY > 0 ? 0.1F : -0.1F;
        hudScale = SafariCritterConfig.clampScale(Math.round((hudScale + step) * 10.0F) / 10.0F);
        float ratio = hudScale / previousScale;
        hudX = (int) Math.round(mouseX - (mouseX - hudX) * ratio);
        hudY = (int) Math.round(mouseY - (mouseY - hudY) * ratio);
        clampToScreen();
        saveLayout();
        return true;
    }

    @Override
    public void onClose() {
        saveLayout();
        minecraft.setScreen(parent);
    }

    private void clampToScreen() {
        SafariHudRenderer.HudBounds bounds = SafariHudRenderer.previewBounds(hudX, hudY, hudScale);
        hudX = SafariHudRenderer.clampPosition(hudX, width - bounds.width());
        hudY = SafariHudRenderer.clampPosition(hudY, height - bounds.height());
    }

    private void saveLayout() {
        SafariCritterStatsMod.CONFIG.setHudLayout(hudX, hudY, hudScale);
    }
}
