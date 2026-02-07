package com.todolist.mod.client.gui.widgets;

import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.common.model.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class CategoryTabBar extends AbstractWidget {
    private String selectedCategory = null;
    private final Consumer<String> onCategoryChanged;
    private int scrollOffset = 0;

    public CategoryTabBar(int x, int y, int width, int height, Consumer<String> onCategoryChanged) {
        super(x, y, width, height, Component.literal("Categories"));
        this.onCategoryChanged = onCategoryChanged;
    }

    public String getSelectedCategory() { return selectedCategory; }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        List<Category> categories = ClientTodoManager.getCategories();

        int tabX = this.getX() + 2;

        // "All" tab
        boolean allSelected = selectedCategory == null;
        int allColor = allSelected ? 0xAAFFFFFF : 0x44FFFFFF;
        int allWidth = font.width("All") + 10;
        graphics.fill(tabX, this.getY(), tabX + allWidth, this.getY() + this.height, allColor);
        graphics.drawString(font, "All", tabX + 5, this.getY() + (this.height - 8) / 2,
                allSelected ? 0xFFFFAA00 : 0xFFCCCCCC, false);
        tabX += allWidth + 3;

        // Category tabs
        for (Category cat : categories) {
            if (tabX - scrollOffset > this.getX() + this.width - 20) break;

            int tabWidth = font.width(cat.getName()) + 10;
            boolean isSelected = cat.getName().equals(selectedCategory);
            int bgColor = isSelected ? ((cat.getColor() & 0x00FFFFFF) | 0x88000000) : 0x33FFFFFF;

            int renderX = tabX - scrollOffset;
            if (renderX >= this.getX()) {
                graphics.fill(renderX, this.getY(), renderX + tabWidth, this.getY() + this.height, bgColor);
                int textColor = isSelected ? 0xFFFFFFFF : (cat.getColor() | 0xFF000000);
                graphics.drawString(font, cat.getName(), renderX + 5, this.getY() + (this.height - 8) / 2,
                        textColor, false);
            }
            tabX += tabWidth + 3;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY)) return false;

        Font font = Minecraft.getInstance().font;
        List<Category> categories = ClientTodoManager.getCategories();

        int tabX = this.getX() + 2;

        // Check "All" tab
        int allWidth = font.width("All") + 10;
        if (mouseX >= tabX && mouseX <= tabX + allWidth) {
            selectedCategory = null;
            onCategoryChanged.accept(null);
            return true;
        }
        tabX += allWidth + 3;

        // Check category tabs
        for (Category cat : categories) {
            int tabWidth = font.width(cat.getName()) + 10;
            int renderX = tabX - scrollOffset;
            if (mouseX >= renderX && mouseX <= renderX + tabWidth) {
                selectedCategory = cat.getName();
                onCategoryChanged.accept(cat.getName());
                return true;
            }
            tabX += tabWidth + 3;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.isMouseOver(mouseX, mouseY)) {
            scrollOffset = Math.max(0, scrollOffset - (int)(delta * 20));
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {}
}
