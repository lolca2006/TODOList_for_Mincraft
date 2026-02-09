package com.todolist.mod.client.gui;

import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.client.gui.widgets.ColorPickerWidget;
import com.todolist.mod.common.model.Category;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagerScreen extends Screen {
    private final Screen parentScreen;
    private List<Category> categories;
    private EditBox nameInput;
    private ColorPickerWidget colorPicker;
    private int selectedIndex = -1;
    private int scrollOffset = 0;

    private int panelLeft, panelTop, panelRight, panelBottom;

    public CategoryManagerScreen(Screen parent) {
        super(Component.literal("Category Manager"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.categories = new ArrayList<>(ClientTodoManager.getCategories());

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        panelLeft = centerX - 160;
        panelTop = centerY - 120;
        panelRight = centerX + 160;
        panelBottom = centerY + 120;

        // Name input
        nameInput = new EditBox(this.font, panelLeft + 10, panelBottom - 70, 140, 16,
                Component.literal("Category name"));
        nameInput.setMaxLength(30);
        nameInput.setHint(Component.literal("Category name..."));
        this.addRenderableWidget(nameInput);

        // Color picker
        colorPicker = new ColorPickerWidget(panelLeft + 160, panelBottom - 75, 150, 36,
                0xFFFFFFFF, color -> {});
        this.addRenderableWidget(colorPicker);

        // Add button
        this.addRenderableWidget(Button.builder(Component.literal("Add"), btn -> {
            String name = nameInput.getValue().trim();
            if (!name.isEmpty()) {
                categories.add(new Category(name, colorPicker.getSelectedColor(), categories.size()));
                ClientTodoManager.setCategories(categories);
                nameInput.setValue("");
            }
        }).bounds(panelLeft + 10, panelBottom - 30, 50, 18).build());

        // Delete selected button
        this.addRenderableWidget(Button.builder(Component.literal("Delete"), btn -> {
            if (selectedIndex >= 0 && selectedIndex < categories.size()) {
                categories.remove(selectedIndex);
                selectedIndex = -1;
                ClientTodoManager.setCategories(categories);
            }
        }).bounds(panelLeft + 65, panelBottom - 30, 55, 18).build());

        // Back button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), btn -> {
            this.minecraft.setScreen(parentScreen);
        }).bounds(panelRight - 55, panelBottom - 30, 45, 18).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        // Panel
        graphics.fill(panelLeft - 2, panelTop - 2, panelRight + 2, panelBottom + 2, 0xFF333333);
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xDD111111);

        // Title
        graphics.fill(panelLeft, panelTop, panelRight, panelTop + 16, 0xFF1A1A2E);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop + 4, 0xFFFFAA00);

        // Category list
        int listTop = panelTop + 22;
        int maxVisible = 8;
        for (int i = 0; i < Math.min(maxVisible, categories.size() - scrollOffset); i++) {
            int index = i + scrollOffset;
            Category cat = categories.get(index);
            int itemY = listTop + i * 18;

            int bgColor = index == selectedIndex ? 0x44FFFFFF : (index % 2 == 0 ? 0x22FFFFFF : 0x11FFFFFF);
            graphics.fill(panelLeft + 5, itemY, panelRight - 5, itemY + 16, bgColor);

            // Color swatch
            graphics.fill(panelLeft + 8, itemY + 2, panelLeft + 20, itemY + 14, cat.getColor());

            // Name
            graphics.drawString(this.font, cat.getName(), panelLeft + 25, itemY + 4, 0xFFFFFFFF, false);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check category list clicks
        int listTop = panelTop + 22;
        int maxVisible = 8;
        for (int i = 0; i < Math.min(maxVisible, categories.size() - scrollOffset); i++) {
            int index = i + scrollOffset;
            int itemY = listTop + i * 18;
            if (mouseX >= panelLeft + 5 && mouseX <= panelRight - 5 &&
                mouseY >= itemY && mouseY <= itemY + 16) {
                selectedIndex = index;
                Category cat = categories.get(index);
                nameInput.setValue(cat.getName());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
        if (scrollDeltaY > 0 && scrollOffset > 0) scrollOffset--;
        else if (scrollDeltaY < 0 && scrollOffset + 8 < categories.size()) scrollOffset++;
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
