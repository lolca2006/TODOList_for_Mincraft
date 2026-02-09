package com.todolist.mod.client.gui;

import com.todolist.mod.platform.Services;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SettingsScreen extends Screen {
    private final Screen parentScreen;
    private int panelLeft, panelTop, panelRight, panelBottom;

    public SettingsScreen(Screen parent) {
        super(Component.literal("Todo List Settings"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        panelLeft = centerX - 140;
        panelTop = centerY - 100;
        panelRight = centerX + 140;
        panelBottom = centerY + 100;

        int buttonWidth = 120;
        int buttonX = centerX - buttonWidth / 2;
        int y = panelTop + 25;

        // HUD Visible toggle
        this.addRenderableWidget(Button.builder(
                Component.literal("HUD: " + (Services.CONFIG.hudVisible() ? "ON" : "OFF")),
                btn -> {
                    boolean val = !Services.CONFIG.hudVisible();
                    Services.CONFIG.setHudVisible(val);
                    btn.setMessage(Component.literal("HUD: " + (val ? "ON" : "OFF")));
                }).bounds(buttonX, y, buttonWidth, 20).build());

        y += 26;

        // HUD Opacity
        this.addRenderableWidget(Button.builder(
                Component.literal("Opacity: " + String.format("%.0f%%", Services.CONFIG.hudOpacity() * 100)),
                btn -> {
                    double val = Services.CONFIG.hudOpacity() + 0.1;
                    if (val > 1.0) val = 0.2;
                    Services.CONFIG.setHudOpacity((float) val);
                    btn.setMessage(Component.literal("Opacity: " + String.format("%.0f%%", val * 100)));
                }).bounds(buttonX, y, buttonWidth, 20).build());

        y += 26;

        // HUD Scale
        this.addRenderableWidget(Button.builder(
                Component.literal("Scale: " + String.format("%.1fx", Services.CONFIG.hudScale())),
                btn -> {
                    double val = Services.CONFIG.hudScale() + 0.25;
                    if (val > 2.0) val = 0.5;
                    Services.CONFIG.setHudScale((float) val);
                    btn.setMessage(Component.literal("Scale: " + String.format("%.1fx", val)));
                }).bounds(buttonX, y, buttonWidth, 20).build());

        y += 26;

        // Max HUD items
        this.addRenderableWidget(Button.builder(
                Component.literal("Max Items: " + Services.CONFIG.hudMaxItems()),
                btn -> {
                    int val = Services.CONFIG.hudMaxItems() + 2;
                    if (val > 20) val = 2;
                    Services.CONFIG.setHudMaxItems(val);
                    btn.setMessage(Component.literal("Max Items: " + val));
                }).bounds(buttonX, y, buttonWidth, 20).build());

        y += 26;

        // Show completed in HUD
        this.addRenderableWidget(Button.builder(
                Component.literal("Show Done: " + (Services.CONFIG.showCompletedInHud() ? "YES" : "NO")),
                btn -> {
                    boolean val = !Services.CONFIG.showCompletedInHud();
                    Services.CONFIG.setShowCompletedInHud(val);
                    btn.setMessage(Component.literal("Show Done: " + (val ? "YES" : "NO")));
                }).bounds(buttonX, y, buttonWidth, 20).build());

        y += 26;

        // Completion sound
        this.addRenderableWidget(Button.builder(
                Component.literal("Sound: " + (Services.CONFIG.completionSoundEnabled() ? "ON" : "OFF")),
                btn -> {
                    boolean val = !Services.CONFIG.completionSoundEnabled();
                    Services.CONFIG.setCompletionSoundEnabled(val);
                    btn.setMessage(Component.literal("Sound: " + (val ? "ON" : "OFF")));
                }).bounds(buttonX, y, buttonWidth, 20).build());

        // Back button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), btn -> {
            this.minecraft.setScreen(parentScreen);
        }).bounds(centerX - 40, panelBottom - 25, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(panelLeft - 2, panelTop - 2, panelRight + 2, panelBottom + 2, 0xFF333333);
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xDD111111);
        graphics.fill(panelLeft, panelTop, panelRight, panelTop + 16, 0xFF1A1A2E);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop + 4, 0xFFFFAA00);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
