package com.todolist.mod.client.gui;

import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.common.model.TodoItem;
import com.todolist.mod.forge.ForgeConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * Renders resource requirements overlay on the right side of inventory screens.
 * Aggregates all resources needed from incomplete todos.
 */
public class InventoryResourceOverlay {

    private static Map<String, Integer> cachedAggregation = null;
    private static long cacheTime = 0;

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        if (!ForgeConfigHandler.CLIENT.inventoryOverlayEnabled.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Map<String, Integer> needed = aggregateNeededResources();
        if (needed.isEmpty()) return;

        Font font = mc.font;
        int maxItems = ForgeConfigHandler.CLIENT.inventoryOverlayMaxItems.get();

        // Position on right side of screen
        int panelWidth = 160;
        int rowHeight = 16;
        int displayCount = Math.min(needed.size(), maxItems);
        int panelHeight = 16 + displayCount * rowHeight + 6;
        int panelX = screenWidth - panelWidth - 4;
        int panelY = 4;

        // Get player inventory counts
        Map<String, Integer> playerInv = getPlayerInventory(mc);

        // Background
        graphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, 0x88333355);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC0D0D1A);

        // Header
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 14, 0xCC1A1A2E);
        graphics.drawString(font, "Resources Needed", panelX + 4, panelY + 3, 0xFFFFAA00, true);

        int y = panelY + 16;
        int idx = 0;
        String hoveredTooltip = null;
        int tooltipX = 0, tooltipY = 0;

        for (Map.Entry<String, Integer> entry : needed.entrySet()) {
            if (idx >= displayCount) break;

            String itemId = entry.getKey();
            int neededCount = entry.getValue();
            int haveCount = playerInv.getOrDefault(itemId, 0);

            try {
                var regItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                if (regItem == null) { idx++; continue; }

                ItemStack stack = new ItemStack(regItem);
                boolean rowHovered = mouseX >= panelX && mouseX <= panelX + panelWidth && mouseY >= y && mouseY < y + rowHeight;

                // Alternating row
                if (idx % 2 == 0) {
                    graphics.fill(panelX + 1, y, panelX + panelWidth - 1, y + rowHeight, 0x11FFFFFF);
                }
                if (rowHovered) {
                    graphics.fill(panelX + 1, y, panelX + panelWidth - 1, y + rowHeight, 0x22FFFFFF);
                }

                // Icon (half scale)
                graphics.pose().pushPose();
                graphics.pose().scale(0.75f, 0.75f, 1.0f);
                graphics.renderItem(stack, (int)((panelX + 2) / 0.75f), (int)((y) / 0.75f));
                graphics.pose().popPose();

                // Name (truncated)
                String name = stack.getHoverName().getString();
                if (name.length() > 14) name = name.substring(0, 12) + "..";
                graphics.drawString(font, name, panelX + 16, y + 4, 0xFFCCCCCC, false);

                // Count
                boolean hasEnough = haveCount >= neededCount;
                String countText = haveCount + "/" + neededCount;
                int countColor = hasEnough ? 0xFF44DD44 : (haveCount > 0 ? 0xFFFFAA00 : 0xFFFF4444);
                int countWidth = font.width(countText);
                graphics.drawString(font, countText, panelX + panelWidth - countWidth - 4, y + 4, countColor, false);

                // Checkmark if complete
                if (hasEnough) {
                    graphics.drawString(font, "\u2713", panelX + panelWidth - countWidth - 14, y + 4, 0xFF44DD44, false);
                }

                // Tooltip
                if (rowHovered) {
                    hoveredTooltip = stack.getHoverName().getString() + "\nNeed: " + neededCount + " | Have: " + haveCount;
                    tooltipX = mouseX;
                    tooltipY = mouseY;
                }
            } catch (Exception ignored) {}

            y += rowHeight;
            idx++;
        }

        if (needed.size() > displayCount) {
            graphics.drawString(font, "+" + (needed.size() - displayCount) + " more...",
                    panelX + 16, y + 2, 0xFF888888, false);
        }

        // Render tooltip last (on top)
        if (hoveredTooltip != null) {
            String[] lines = hoveredTooltip.split("\n");
            List<net.minecraft.network.chat.Component> tooltipLines = new ArrayList<>();
            for (String line : lines) {
                tooltipLines.add(net.minecraft.network.chat.Component.literal(line));
            }
            graphics.renderTooltip(font, tooltipLines, Optional.empty(), tooltipX, tooltipY);
        }
    }

    private static Map<String, Integer> aggregateNeededResources() {
        long now = System.currentTimeMillis();
        if (cachedAggregation != null && (now - cacheTime) < 1000) {
            return cachedAggregation;
        }

        Map<String, Integer> result = new LinkedHashMap<>();
        for (TodoItem item : ClientTodoManager.getTodoList().getItems()) {
            if (!item.isCompleted()) {
                for (ResourceRequirement req : item.getResources()) {
                    result.merge(req.getItemId(), req.getCount(), Integer::sum);
                }
            }
        }

        cachedAggregation = result;
        cacheTime = now;
        return result;
    }

    private static Map<String, Integer> getPlayerInventory(Minecraft mc) {
        Map<String, Integer> result = new HashMap<>();
        if (mc.player != null) {
            for (ItemStack stack : mc.player.getInventory().items) {
                if (!stack.isEmpty()) {
                    ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (key != null) {
                        result.merge(key.toString(), stack.getCount(), Integer::sum);
                    }
                }
            }
        }
        return result;
    }
}
