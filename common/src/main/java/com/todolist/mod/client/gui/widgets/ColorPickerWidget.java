package com.todolist.mod.client.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ColorPickerWidget extends AbstractWidget {
    private static final int[] COLORS = {
            0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55,
            0xFF55FF55, 0xFF55FFFF, 0xFF5555FF, 0xFFAA55FF,
            0xFFFF55FF, 0xFF888888, 0xFFBB6622, 0xFF44BB44,
            0xFF4488FF, 0xFFDD44DD, 0xFFDDDD44, 0xFF44DDDD
    };

    private int selectedColor;
    private final Consumer<Integer> onColorSelected;
    private final int colsPerRow = 8;

    public ColorPickerWidget(int x, int y, int width, int height, int initialColor, Consumer<Integer> onColorSelected) {
        super(x, y, width, height, Component.literal("Color Picker"));
        this.selectedColor = initialColor;
        this.onColorSelected = onColorSelected;
    }

    public int getSelectedColor() { return selectedColor; }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int cellSize = Math.min(this.width / colsPerRow, this.height / 2);

        for (int i = 0; i < COLORS.length; i++) {
            int col = i % colsPerRow;
            int row = i / colsPerRow;
            int cx = this.getX() + col * cellSize;
            int cy = this.getY() + row * cellSize;

            graphics.fill(cx, cy, cx + cellSize - 1, cy + cellSize - 1, COLORS[i]);

            // Selection border
            if (COLORS[i] == selectedColor) {
                graphics.fill(cx - 1, cy - 1, cx + cellSize, cy, 0xFFFFFFFF);
                graphics.fill(cx - 1, cy + cellSize - 1, cx + cellSize, cy + cellSize, 0xFFFFFFFF);
                graphics.fill(cx - 1, cy, cx, cy + cellSize - 1, 0xFFFFFFFF);
                graphics.fill(cx + cellSize - 1, cy, cx + cellSize, cy + cellSize - 1, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY)) return false;

        int cellSize = Math.min(this.width / colsPerRow, this.height / 2);
        int col = (int)(mouseX - this.getX()) / cellSize;
        int row = (int)(mouseY - this.getY()) / cellSize;
        int index = row * colsPerRow + col;

        if (index >= 0 && index < COLORS.length) {
            selectedColor = COLORS[index];
            if (onColorSelected != null) onColorSelected.accept(selectedColor);
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {}
}
