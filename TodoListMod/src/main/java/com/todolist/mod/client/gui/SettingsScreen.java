package com.todolist.mod.client.gui;

import com.todolist.mod.forge.ForgeConfigHandler;
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
                Component.literal("HUD: " + (ForgeConfigHandler.CLIENT.hudVisible.get() ? "ON" : "OFF")),
                btn -> {
                    boolean val = !ForgeConfigHandler.CLIENT.hudVisible.get();
                    ForgeConfigHandler.CLIENT.hudVisible.set(val);
                    btn.setMessage(Component.literal("HUD: " + (val ? "ON" : "OFF")));
                }).bounds(buttonX, y, buttonWidth, 20).build());

        y += 26;

        // HUD Opacity
        this.addRenderableWidget(Button.builder(
                Component.literal("Opacity: " + String.format("%.0f%%", ForgeConfigHandler.CLIENT.hudOpacity.get() * 100)),
                btn -> {
                    double val = ForgeConfigHandler.CLIENT.hudOpacity.get() + 0.1;
                    if (val > 1.0) val = 0.2;
                    ForgeConfigHandler.CLIENT.hudOpacity.set(val);
                    btn.setMessage(Component.literal("Opacity: " + String.format("%.0f%%", val * 100)));
                }).bounds(buttonX, y, buttonWidth, 20).build());

        y += 26;

        // HUD Scale
        this.addRenderableWidget(Button.builder(
                Component.literal("Scale: " + String.format("%.1fx", ForgeConfigHandler.CLIENT.hudScale.get())),
                btn -> {
                    double val = ForgeConfigHandler.CLIENT.hudScale.get() + 0.25;
                    if (val > 2.0) val = 0.5;
                    ForgeConfigHandler.CLIENT.hudScale.set(val);
                    btn.setMessage(Component.literal("Scale: " + String.format("%.1fx", val)));
                }).bounds(buttonX, y, buttonWidth, 20).build());

        y += 26;

        // Max HUD items
        this.addRenderableWidget(Button.builder(
                Component.literal("Max Items: " + ForgeConfigHandler.CLIENT.hudMaxItems.get()),
                btn -> {
                    int val = ForgeConfigHandler.CLIENT.hudMaxItems.get() + 2;
                    if (val > 20) val = 2;
                    ForgeConfigHandler.CLIENT.hudMaxItems.set(val);
                    btn.setMessage(Component.literal("Max Items: " + val));
                }).bounds(buttonX, y, buttonWidth, 20).build());

        y += 26;

        // Show completed in HUD
        this.addRenderableWidget(Button.builder(
                Component.literal("Show Done: " + (ForgeConfigHandler.CLIENT.showCompletedInHud.get() ? "YES" : "NO")),
                btn -> {
                    boolean val = !ForgeConfigHandler.CLIENT.showCompletedInHud.get();
                    ForgeConfigHandler.CLIENT.showCompletedInHud.set(val);
                    btn.setMessage(Component.literal("Show Done: " + (val ? "YES" : "NO")));
                }).bounds(buttonX, y, buttonWidth, 20).build());

        y += 26;

        // Completion sound
        this.addRenderableWidget(Button.builder(
                Component.literal("Sound: " + (ForgeConfigHandler.CLIENT.playCompletionSound.get() ? "ON" : "OFF")),
                btn -> {
                    boolean val = !ForgeConfigHandler.CLIENT.playCompletionSound.get();
                    ForgeConfigHandler.CLIENT.playCompletionSound.set(val);
                    btn.setMessage(Component.literal("Sound: " + (val ? "ON" : "OFF")));
                }).bounds(buttonX, y, buttonWidth, 20).build());

        // Back button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), btn -> {
            this.minecraft.setScreen(parentScreen);
        }).bounds(centerX - 40, panelBottom - 25, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        graphics.fill(panelLeft - 2, panelTop - 2, panelRight + 2, panelBottom + 2, 0xFF333333);
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xDD111111);
        graphics.fill(panelLeft, panelTop, panelRight, panelTop + 16, 0xFF1A1A2E);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop + 4, 0xFFFFAA00);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
