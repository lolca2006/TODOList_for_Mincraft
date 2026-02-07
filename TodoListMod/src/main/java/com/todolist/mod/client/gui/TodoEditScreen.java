package com.todolist.mod.client.gui;

import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.common.model.Category;
import com.todolist.mod.common.model.TodoItem;
import com.todolist.mod.common.model.TodoVisibility;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class TodoEditScreen extends Screen {
    private final Screen parentScreen;
    private final TodoItem item;
    private EditBox textInput;
    private String selectedCategory;
    private TodoVisibility selectedVisibility;
    private final List<String> requiredItems;
    private int itemScrollOffset = 0;

    private int panelLeft, panelTop, panelRight, panelBottom;
    private static final int PANEL_W = 300, PANEL_H = 240;

    public TodoEditScreen(Screen parent, TodoItem item) {
        super(Component.literal("Edit Todo"));
        this.parentScreen = parent;
        this.item = item;
        this.selectedCategory = item.getCategory();
        this.selectedVisibility = item.getVisibility();
        this.requiredItems = new ArrayList<>(item.getRequiredItems());
    }

    @Override
    protected void init() {
        super.init();
        panelLeft = (this.width - PANEL_W) / 2;
        panelTop = (this.height - PANEL_H) / 2;
        panelRight = panelLeft + PANEL_W;
        panelBottom = panelTop + PANEL_H;

        // Text input
        textInput = new EditBox(this.font, panelLeft + 10, panelTop + 28, PANEL_W - 20, 18, Component.literal("Text"));
        textInput.setMaxLength(200);
        textInput.setValue(item.getText());
        this.addRenderableWidget(textInput);

        // Category buttons
        List<Category> cats = ClientTodoManager.getCategories();
        int cx = panelLeft + 10;
        int cy = panelTop + 58;
        for (int i = 0; i < Math.min(cats.size(), 6); i++) {
            Category cat = cats.get(i);
            boolean sel = cat.getName().equals(selectedCategory);
            Button btn = Button.builder(Component.literal(cat.getName()), b -> {
                selectedCategory = cat.getName();
                this.rebuildWidgets();
            }).bounds(cx, cy, 46, 14).build();
            if (sel) btn.active = false;
            this.addRenderableWidget(btn);
            cx += 48;
            if (cx + 46 > panelRight - 10) { cx = panelLeft + 10; cy += 16; }
        }

        // Visibility toggle
        int visY = panelTop + 96;
        this.addRenderableWidget(Button.builder(
                Component.literal(selectedVisibility == TodoVisibility.PRIVATE ? "Private" : "Shared"),
                btn -> {
                    selectedVisibility = selectedVisibility == TodoVisibility.PRIVATE ? TodoVisibility.SHARED : TodoVisibility.PRIVATE;
                    btn.setMessage(Component.literal(selectedVisibility == TodoVisibility.PRIVATE ? "Private" : "Shared"));
                }).bounds(panelLeft + 10, visY, 60, 16).build());

        // Add item button
        this.addRenderableWidget(Button.builder(Component.literal("+ Add Item"), btn -> {
            this.minecraft.setScreen(new ItemPickerScreen(this, itemId -> {
                if (itemId != null && !requiredItems.contains(itemId)) {
                    requiredItems.add(itemId);
                }
            }));
        }).bounds(panelLeft + 80, visY, 70, 16).build());

        // Save button
        this.addRenderableWidget(Button.builder(Component.literal("Save"), btn -> {
            String newText = textInput.getValue().trim();
            if (!newText.isEmpty()) {
                ClientTodoManager.editTodo(item.getId(), newText, selectedCategory, selectedVisibility, requiredItems);
            }
            this.minecraft.setScreen(parentScreen);
        }).bounds(panelLeft + 10, panelBottom - 28, 60, 18).build());

        // Delete button
        this.addRenderableWidget(Button.builder(Component.literal("Delete"), btn -> {
            ClientTodoManager.deleteTodo(item.getId());
            this.minecraft.setScreen(parentScreen);
        }).bounds(panelLeft + 80, panelBottom - 28, 60, 18).build());

        // Cancel button
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> {
            this.minecraft.setScreen(parentScreen);
        }).bounds(panelRight - 70, panelBottom - 28, 60, 18).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        // Panel
        graphics.fill(panelLeft - 2, panelTop - 2, panelRight + 2, panelBottom + 2, 0xFF333333);
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xDD111111);
        graphics.fill(panelLeft, panelTop, panelRight, panelTop + 16, 0xFF1A1A2E);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop + 4, 0xFFFFAA00);

        // Labels
        graphics.drawString(this.font, "Category:", panelLeft + 10, panelTop + 50, 0xFFAAAAAA, false);
        graphics.drawString(this.font, "Visibility:", panelLeft + 10, panelTop + 88, 0xFFAAAAAA, false);

        // Required items section
        int itemsY = panelTop + 118;
        graphics.drawString(this.font, "Required Items:", panelLeft + 10, itemsY, 0xFFAAAAAA, false);
        itemsY += 12;

        int maxVisible = 3;
        int itemSize = 20;
        for (int i = itemScrollOffset; i < Math.min(requiredItems.size(), itemScrollOffset + maxVisible * 9); i++) {
            int col = (i - itemScrollOffset) % 9;
            int row = (i - itemScrollOffset) / 9;
            int ix = panelLeft + 10 + col * itemSize;
            int iy = itemsY + row * itemSize;

            try {
                var regItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(requiredItems.get(i)));
                if (regItem != null) {
                    boolean hovered = mouseX >= ix && mouseX < ix + itemSize && mouseY >= iy && mouseY < iy + itemSize;
                    graphics.fill(ix, iy, ix + itemSize - 1, iy + itemSize - 1, hovered ? 0x55FF4444 : 0x22FFFFFF);
                    graphics.renderItem(new ItemStack(regItem), ix + 2, iy + 2);
                    if (hovered) {
                        graphics.renderTooltip(this.font, Component.literal("Click to remove: " + requiredItems.get(i)), mouseX, mouseY);
                    }
                }
            } catch (Exception ignored) {}
        }

        if (requiredItems.isEmpty()) {
            graphics.drawString(this.font, "No items added yet", panelLeft + 10, itemsY + 6, 0xFF666666, false);
        }

        // Created by info
        if (item.getCreatedByName() != null) {
            graphics.drawString(this.font, "By: " + item.getCreatedByName(), panelRight - this.font.width("By: " + item.getCreatedByName()) - 10, panelTop + 88, 0xFF888888, false);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check item removal clicks
        int itemsY = panelTop + 130;
        int itemSize = 20;
        for (int i = itemScrollOffset; i < Math.min(requiredItems.size(), itemScrollOffset + 27); i++) {
            int col = (i - itemScrollOffset) % 9;
            int row = (i - itemScrollOffset) / 9;
            int ix = panelLeft + 10 + col * itemSize;
            int iy = itemsY + row * itemSize;
            if (mouseX >= ix && mouseX < ix + itemSize && mouseY >= iy && mouseY < iy + itemSize) {
                requiredItems.remove(i);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0 && itemScrollOffset > 0) itemScrollOffset -= 9;
        else if (delta < 0) itemScrollOffset += 9;
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
