package com.todolist.mod.client.gui;

import com.todolist.mod.forge.ForgeConfigHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HudEditScreen extends Screen {
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    private int hudX, hudY;
    private int hudWidth = 180;

    public HudEditScreen() {
        super(Component.literal("Edit HUD Position"));
    }

    @Override
    protected void init() {
        super.init();
        hudX = ForgeConfigHandler.CLIENT.hudX.get();
        hudY = ForgeConfigHandler.CLIENT.hudY.get();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Semi-transparent background
        graphics.fill(0, 0, this.width, this.height, 0x44000000);

        // Instructions
        graphics.drawCenteredString(this.font, "Drag the HUD to reposition it", this.width / 2, 10, 0xFFFFFFFF);
        graphics.drawCenteredString(this.font, "Press ESC to save and exit", this.width / 2, 24, 0xFFAAAAAA);

        // Draw HUD preview at current position
        float scale = ForgeConfigHandler.CLIENT.hudScale.get().floatValue();
        float opacity = ForgeConfigHandler.CLIENT.hudOpacity.get().floatValue();
        int maxItems = ForgeConfigHandler.CLIENT.hudMaxItems.get();

        int headerHeight = 14;
        int itemHeight = 14;
        int hudHeight = headerHeight + (Math.min(maxItems, 5) * itemHeight) + 6;

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);

        int alpha = (int)(opacity * 200);
        int bgColor = (alpha << 24) | 0x0D0D1A;
        int headerColor = (alpha << 24) | 0x1A1A2E;
        int borderColor = dragging ? 0xFFFFAA00 : ((Math.min(alpha + 30, 255) << 24) | 0x333355);

        // Border (highlighted when dragging)
        graphics.fill(hudX - 2, hudY - 2, hudX + hudWidth + 2, hudY + hudHeight + 2, borderColor);
        graphics.fill(hudX, hudY, hudX + hudWidth, hudY + hudHeight, bgColor);
        graphics.fill(hudX, hudY, hudX + hudWidth, hudY + headerHeight, headerColor);

        graphics.drawString(this.font, "Todo List", hudX + 4, hudY + 3, 0xFFFFAA00, true);
        graphics.drawString(this.font, "0/0", hudX + hudWidth - 20, hudY + 3, 0xFF88FF88, true);

        // Preview items
        for (int i = 0; i < Math.min(maxItems, 5); i++) {
            int y = hudY + headerHeight + 2 + (i * itemHeight);
            if (i % 2 == 0) {
                graphics.fill(hudX + 1, y, hudX + hudWidth - 1, y + itemHeight, 0x11FFFFFF);
            }
            graphics.fill(hudX + 4, y + 2, hudX + 12, y + 10, 0xFF444444);
            graphics.drawString(this.font, "Example task " + (i + 1), hudX + 16, y + 3, 0xFF888888, false);
        }

        graphics.pose().popPose();

        // Position info
        String posText = "Position: " + hudX + ", " + hudY;
        graphics.drawCenteredString(this.font, posText, this.width / 2, this.height - 20, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float scale = ForgeConfigHandler.CLIENT.hudScale.get().floatValue();
        double scaledMouseX = mouseX / scale;
        double scaledMouseY = mouseY / scale;

        int maxItems = ForgeConfigHandler.CLIENT.hudMaxItems.get();
        int hudHeight = 14 + (Math.min(maxItems, 5) * 14) + 6;

        if (scaledMouseX >= hudX && scaledMouseX <= hudX + hudWidth &&
            scaledMouseY >= hudY && scaledMouseY <= hudY + hudHeight) {
            dragging = true;
            dragOffsetX = (int)(scaledMouseX - hudX);
            dragOffsetY = (int)(scaledMouseY - hudY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            float scale = ForgeConfigHandler.CLIENT.hudScale.get().floatValue();
            hudX = Math.max(0, (int)(mouseX / scale) - dragOffsetX);
            hudY = Math.max(0, (int)(mouseY / scale) - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        // Save position
        ForgeConfigHandler.CLIENT.hudX.set(hudX);
        ForgeConfigHandler.CLIENT.hudY.set(hudY);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
