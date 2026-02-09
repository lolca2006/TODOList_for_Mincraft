package com.todolist.mod.client.gui;

import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.common.model.TodoItem;
import com.todolist.mod.common.model.TodoVisibility;
import com.todolist.mod.integration.jei.JEIHelper;
import com.todolist.mod.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TodoHudOverlay {

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        if (!Services.CONFIG.hudVisible()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Only hide for our own todo screens, NOT for chat, inventory, etc.
        if (mc.screen instanceof TodoScreen || mc.screen instanceof TodoEditScreen
                || mc.screen instanceof ResourcePickerScreen || mc.screen instanceof ImportScreen
                || mc.screen instanceof HudEditScreen) {
            return;
        }

        // Reduce opacity when a screen is open (chat, inventory, etc.)
        boolean screenOpen = mc.screen != null;

        Font font = mc.font;
        int posX = Services.CONFIG.hudX();
        int posY = Services.CONFIG.hudY();
        float baseOpacity = (float) Services.CONFIG.hudOpacity();
        float opacity = screenOpen ? baseOpacity * 0.6f : baseOpacity;
        float scale = (float) Services.CONFIG.hudScale();
        int maxItems = Services.CONFIG.hudMaxItems();
        boolean showCompleted = Services.CONFIG.showCompletedInHud();
        String activeCategory = Services.CONFIG.hudActiveCategory();

        // Get filtered items
        List<TodoItem> items = ClientTodoManager.getTodoList().getItems().stream()
                .filter(item -> showCompleted || !item.isCompleted())
                .filter(item -> activeCategory.isEmpty() || item.getCategory().equals(activeCategory))
                .collect(Collectors.toList());

        if (items.isEmpty()) return;

        int displayCount = Math.min(maxItems, items.size());

        // Calculate dimensions
        int hudWidth = 200;
        int headerHeight = 16;
        int itemHeight = 18;
        int hudHeight = headerHeight + (displayCount * itemHeight) + 8;

        // Dynamic positioning: shift left if JEI ingredient list is visible on right
        int adjustedPosX = posX;
        if (JEIHelper.isJEIOverlayVisible()) {
            // JEI takes about 170px on the right - shift our HUD left if it's in the way
            int jeiLeftEdge = (int)(screenWidth / scale) - 175;
            if (adjustedPosX + hudWidth > jeiLeftEdge) {
                adjustedPosX = Math.max(0, jeiLeftEdge - hudWidth - 4);
            }
        }

        // If inventory is open and overlay is on the right, shift left to avoid overlap
        if (mc.screen instanceof AbstractContainerScreen<?>) {
            int screenCenter = (int)(screenWidth / scale) / 2;
            int invRightEdge = screenCenter + 90; // approximate inventory right edge
            if (adjustedPosX > screenCenter && adjustedPosX < invRightEdge + hudWidth) {
                adjustedPosX = Math.max(0, Math.min(adjustedPosX, screenCenter - hudWidth - 10));
            }
        }

        // Clamp position to screen
        adjustedPosX = Math.max(0, Math.min(adjustedPosX, (int)(screenWidth / scale) - hudWidth));
        posY = Math.max(0, Math.min(posY, (int)(screenHeight / scale) - hudHeight));

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);

        int alpha = (int)(opacity * 200);
        int bgColor = (alpha << 24) | 0x0D0D1A;
        int headerColor = (alpha << 24) | 0x1A1A2E;
        int borderColor = (Math.min(alpha + 30, 255) << 24) | 0x333355;

        // Background with rounded look
        graphics.fill(adjustedPosX - 1, posY - 1, adjustedPosX + hudWidth + 1, posY + hudHeight + 1, borderColor);
        graphics.fill(adjustedPosX, posY, adjustedPosX + hudWidth, posY + hudHeight, bgColor);

        // Header
        graphics.fill(adjustedPosX, posY, adjustedPosX + hudWidth, posY + headerHeight, headerColor);
        // Header bottom accent line
        int accentColor = (Math.min(alpha + 50, 255) << 24) | 0xFFAA00;
        graphics.fill(adjustedPosX, posY + headerHeight - 1, adjustedPosX + hudWidth, posY + headerHeight, accentColor);

        long completed = ClientTodoManager.getTodoList().getCompletedCount();
        int total = ClientTodoManager.getTodoList().size();
        String title = "Todo List";
        String stats = completed + "/" + total;

        // Show active category in header if filtered
        if (!activeCategory.isEmpty()) {
            int catColor = ClientTodoManager.getCategoryColor(activeCategory);
            title = activeCategory;
            graphics.drawString(font, title, adjustedPosX + 4, posY + 4, catColor, true);
        } else {
            graphics.drawString(font, title, adjustedPosX + 4, posY + 4, 0xFFFFAA00, true);
        }

        // Progress bar in header
        int barX = adjustedPosX + hudWidth - 50;
        int barWidth = 40;
        int barY = posY + 5;
        int barHeight = 6;
        float progress = total > 0 ? (float) completed / total : 0;
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x44000000);
        if (progress > 0) {
            int progressColor = progress >= 1.0f ? 0xFF44DD44 : 0xFF44AAFF;
            graphics.fill(barX, barY, barX + (int)(barWidth * progress), barY + barHeight, progressColor);
        }
        // Stats text right of bar
        graphics.drawString(font, stats, barX - font.width(stats) - 3, posY + 4, 0xFF88FF88, true);

        // Items
        int y = posY + headerHeight + 3;
        for (int i = 0; i < displayCount; i++) {
            TodoItem item = items.get(i);

            // Alternating row bg
            if (i % 2 == 0) {
                graphics.fill(adjustedPosX + 1, y - 1, adjustedPosX + hudWidth - 1, y + itemHeight - 1, 0x11FFFFFF);
            }

            int curX = adjustedPosX + 4;

            // --- Player head for shared/assigned todos ---
            if (item.getVisibility() == TodoVisibility.SHARED) {
                UUID playerUuid = item.getAssignedTo() != null ? item.getAssignedTo() : item.getCreatedBy();
                if (playerUuid != null) {
                    try {
                        PlayerInfo playerInfo = mc.getConnection() != null ?
                                mc.getConnection().getPlayerInfo(playerUuid) : null;
                        if (playerInfo != null) {
                            ResourceLocation skinLocation = playerInfo.getSkinLocation();
                            PlayerFaceRenderer.draw(graphics, skinLocation, curX, y, 8);
                            curX += 11;
                        }
                    } catch (Exception ignored) {}
                }
            }

            // --- Checkbox ---
            if (item.isCompleted()) {
                graphics.fill(curX, y + 1, curX + 8, y + 9, 0xFF2D8B2D);
                graphics.fill(curX + 1, y + 2, curX + 7, y + 8, 0xFF3CBB3C);
                graphics.fill(curX + 1, y + 5, curX + 3, y + 7, 0xFFFFFFFF);
                graphics.fill(curX + 3, y + 6, curX + 5, y + 7, 0xFFFFFFFF);
                graphics.fill(curX + 5, y + 3, curX + 7, y + 6, 0xFFFFFFFF);
            } else {
                graphics.fill(curX, y + 1, curX + 8, y + 9, 0xFF444444);
                graphics.fill(curX + 1, y + 2, curX + 7, y + 8, 0xFF1A1A1A);
            }
            curX += 11;

            // --- Resource icons with counts (first 2, mini scale) ---
            List<ResourceRequirement> resources = item.getResources();
            if (!resources.isEmpty()) {
                int maxIcons = Math.min(resources.size(), 2);
                for (int j = 0; j < maxIcons; j++) {
                    ResourceRequirement req = resources.get(j);
                    try {
                        var regItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(req.getItemId()));
                        if (regItem != null) {
                            graphics.pose().pushPose();
                            graphics.pose().scale(0.5f, 0.5f, 1.0f);
                            graphics.renderItem(new ItemStack(regItem),
                                    (int)(curX / 0.5f), (int)((y) / 0.5f));
                            graphics.pose().popPose();
                            if (req.getCount() > 1) {
                                String cnt = req.getCount() > 99 ? "99+" : String.valueOf(req.getCount());
                                graphics.pose().pushPose();
                                graphics.pose().scale(0.5f, 0.5f, 1.0f);
                                graphics.drawString(font, cnt, (int)((curX + 5) / 0.5f), (int)((y + 6) / 0.5f), 0xFFFFFF00, true);
                                graphics.pose().popPose();
                            }
                            curX += 12;
                        }
                    } catch (Exception ignored) {}
                }
                if (resources.size() > 2) {
                    curX += 1;
                }
            }

            // --- Category label ---
            String catName = item.getCategory();
            int catColor = ClientTodoManager.getCategoryColor(catName);
            String catLabel = catName.length() > 5 ? catName.substring(0, 4) + "." : catName;
            int catLabelWidth = font.width(catLabel) + 4;
            int catBgColor = (catColor & 0x00FFFFFF) | 0x55000000;
            graphics.fill(curX, y + 1, curX + catLabelWidth, y + 10, catBgColor);
            graphics.fill(curX, y + 1, curX + 1, y + 10, catColor);
            graphics.drawString(font, catLabel, curX + 2, y + 2, catColor, false);
            curX += catLabelWidth + 3;

            // --- Text ---
            int maxWidth = hudWidth - (curX - adjustedPosX) - 4;
            String text = item.getText();
            if (font.width(text) > maxWidth) {
                text = font.plainSubstrByWidth(text, maxWidth - 6) + "..";
            }

            int textColor = item.isCompleted() ? 0xFF666666 : 0xFFDDDDDD;
            if (item.isCompleted()) {
                graphics.drawString(font, "\u00A7m" + text, curX, y + 2, textColor, false);
            } else {
                graphics.drawString(font, text, curX, y + 2, textColor, false);
            }

            // --- Assignee name ---
            if (item.getVisibility() == TodoVisibility.SHARED) {
                String info = null;
                if (item.getAssignedToName() != null) {
                    info = item.getAssignedToName();
                } else if (item.getCreatedByName() != null) {
                    info = item.getCreatedByName();
                }
                if (info != null) {
                    String shortName = info.length() > 8 ? info.substring(0, 7) + "." : info;
                    int nameWidth = font.width(shortName);
                    graphics.drawString(font, shortName,
                            adjustedPosX + hudWidth - nameWidth - 4, y + 10, 0xFF666666, false);
                }
            }

            y += itemHeight;
        }

        // "More items" indicator
        if (items.size() > displayCount) {
            int moreCount = items.size() - displayCount;
            String moreText = "+" + moreCount + " more...";
            graphics.drawString(font, moreText, adjustedPosX + hudWidth - font.width(moreText) - 4,
                    y, 0xFF888888, false);
        }

        graphics.pose().popPose();
    }

    public static void toggleVisibility() {
        boolean current = Services.CONFIG.hudVisible();
        Services.CONFIG.setHudVisible(!current);
    }
}
