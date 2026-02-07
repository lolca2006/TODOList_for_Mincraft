package com.todolist.mod.client.gui;

import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.common.model.TodoItem;
import com.todolist.mod.common.model.TodoVisibility;
import com.todolist.mod.forge.ForgeConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TodoHudOverlay {

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        if (!ForgeConfigHandler.CLIENT.hudVisible.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        Font font = mc.font;
        int posX = ForgeConfigHandler.CLIENT.hudX.get();
        int posY = ForgeConfigHandler.CLIENT.hudY.get();
        float opacity = ForgeConfigHandler.CLIENT.hudOpacity.get().floatValue();
        float scale = ForgeConfigHandler.CLIENT.hudScale.get().floatValue();
        int maxItems = ForgeConfigHandler.CLIENT.hudMaxItems.get();
        boolean showCompleted = ForgeConfigHandler.CLIENT.showCompletedInHud.get();
        String activeCategory = ForgeConfigHandler.CLIENT.hudActiveCategory.get();

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

        // Clamp position to screen
        posX = Math.max(0, Math.min(posX, (int)(screenWidth / scale) - hudWidth));
        posY = Math.max(0, Math.min(posY, (int)(screenHeight / scale) - hudHeight));

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);

        int alpha = (int)(opacity * 200);
        int bgColor = (alpha << 24) | 0x0D0D1A;
        int headerColor = (alpha << 24) | 0x1A1A2E;
        int borderColor = (Math.min(alpha + 30, 255) << 24) | 0x333355;

        // Background with rounded look
        graphics.fill(posX - 1, posY - 1, posX + hudWidth + 1, posY + hudHeight + 1, borderColor);
        graphics.fill(posX, posY, posX + hudWidth, posY + hudHeight, bgColor);

        // Header
        graphics.fill(posX, posY, posX + hudWidth, posY + headerHeight, headerColor);
        // Header bottom accent line
        int accentColor = (Math.min(alpha + 50, 255) << 24) | 0xFFAA00;
        graphics.fill(posX, posY + headerHeight - 1, posX + hudWidth, posY + headerHeight, accentColor);

        long completed = ClientTodoManager.getTodoList().getCompletedCount();
        int total = ClientTodoManager.getTodoList().size();
        String title = "Todo List";
        String stats = completed + "/" + total;

        // Show active category in header if filtered
        if (!activeCategory.isEmpty()) {
            int catColor = ClientTodoManager.getCategoryColor(activeCategory);
            title = activeCategory;
            graphics.drawString(font, title, posX + 4, posY + 4, catColor, true);
        } else {
            graphics.drawString(font, title, posX + 4, posY + 4, 0xFFFFAA00, true);
        }

        // Progress bar in header
        int barX = posX + hudWidth - 50;
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
                graphics.fill(posX + 1, y - 1, posX + hudWidth - 1, y + itemHeight - 1, 0x11FFFFFF);
            }

            int curX = posX + 4;

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
                // Green filled checkbox
                graphics.fill(curX, y + 1, curX + 8, y + 9, 0xFF2D8B2D);
                graphics.fill(curX + 1, y + 2, curX + 7, y + 8, 0xFF3CBB3C);
                // Checkmark pixels
                graphics.fill(curX + 1, y + 5, curX + 3, y + 7, 0xFFFFFFFF);
                graphics.fill(curX + 3, y + 6, curX + 5, y + 7, 0xFFFFFFFF);
                graphics.fill(curX + 5, y + 3, curX + 7, y + 6, 0xFFFFFFFF);
            } else {
                graphics.fill(curX, y + 1, curX + 8, y + 9, 0xFF444444);
                graphics.fill(curX + 1, y + 2, curX + 7, y + 8, 0xFF1A1A1A);
            }
            curX += 11;

            // --- Required item icons (first 2, mini scale) ---
            List<String> reqItems = item.getRequiredItems();
            if (!reqItems.isEmpty()) {
                int maxIcons = Math.min(reqItems.size(), 2);
                for (int j = 0; j < maxIcons; j++) {
                    try {
                        var regItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(reqItems.get(j)));
                        if (regItem != null) {
                            graphics.pose().pushPose();
                            graphics.pose().scale(0.5f, 0.5f, 1.0f);
                            graphics.renderItem(new ItemStack(regItem),
                                    (int)(curX / 0.5f), (int)((y) / 0.5f));
                            graphics.pose().popPose();
                            curX += 9;
                        }
                    } catch (Exception ignored) {}
                }
                if (reqItems.size() > 2) {
                    curX += 1;
                }
            }

            // --- Category label (colored background tag) ---
            String catName = item.getCategory();
            int catColor = ClientTodoManager.getCategoryColor(catName);
            // Truncate long category names
            String catLabel = catName.length() > 5 ? catName.substring(0, 4) + "." : catName;
            int catLabelWidth = font.width(catLabel) + 4;
            int catBgColor = (catColor & 0x00FFFFFF) | 0x55000000;
            graphics.fill(curX, y + 1, curX + catLabelWidth, y + 10, catBgColor);
            // Left colored stripe
            graphics.fill(curX, y + 1, curX + 1, y + 10, catColor);
            graphics.drawString(font, catLabel, curX + 2, y + 2, catColor, false);
            curX += catLabelWidth + 3;

            // --- Text ---
            int maxWidth = hudWidth - (curX - posX) - 4;
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

            // --- Assignee name (small, bottom-right of row) ---
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
                            posX + hudWidth - nameWidth - 4, y + 10, 0xFF666666, false);
                }
            }

            y += itemHeight;
        }

        // "More items" indicator
        if (items.size() > displayCount) {
            int moreCount = items.size() - displayCount;
            String moreText = "+" + moreCount + " more...";
            graphics.drawString(font, moreText, posX + hudWidth - font.width(moreText) - 4,
                    y, 0xFF888888, false);
        }

        graphics.pose().popPose();
    }

    public static void toggleVisibility() {
        boolean current = ForgeConfigHandler.CLIENT.hudVisible.get();
        ForgeConfigHandler.CLIENT.hudVisible.set(!current);
    }
}
