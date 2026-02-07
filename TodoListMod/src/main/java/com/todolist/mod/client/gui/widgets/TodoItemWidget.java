package com.todolist.mod.client.gui.widgets;

import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.client.gui.TodoEditScreen;
import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.common.model.TodoItem;
import com.todolist.mod.common.model.TodoVisibility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class TodoItemWidget extends ObjectSelectionList.Entry<TodoItemWidget> {
    private final TodoItem item;
    private final TodoListWidget parent;
    private final Minecraft mc;

    public TodoItemWidget(TodoItem item, TodoListWidget parent) {
        this.item = item;
        this.parent = parent;
        this.mc = Minecraft.getInstance();
    }

    public TodoItem getTodoItem() { return item; }

    @Override
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                       int mouseX, int mouseY, boolean isHovered, float partialTick) {
        Font font = mc.font;

        // Background
        int bgColor = isHovered ? 0x44FFFFFF : (index % 2 == 0 ? 0x22FFFFFF : 0x11FFFFFF);
        graphics.fill(left, top, left + width, top + height, bgColor);

        // Offset for drag handle area
        int contentLeft = left + TodoListWidget.DRAG_HANDLE_WIDTH;

        // --- Checkbox ---
        int checkX = contentLeft + 2;
        int checkY = top + 3;
        int checkSize = 10;

        if (item.isCompleted()) {
            graphics.fill(checkX, checkY, checkX + checkSize, checkY + checkSize, 0xFF2D8B2D);
            graphics.fill(checkX + 1, checkY + 1, checkX + checkSize - 1, checkY + checkSize - 1, 0xFF3CBB3C);
            // Checkmark
            graphics.fill(checkX + 2, checkY + 5, checkX + 4, checkY + 8, 0xFFFFFFFF);
            graphics.fill(checkX + 4, checkY + 6, checkX + 8, checkY + 8, 0xFFFFFFFF);
            graphics.fill(checkX + 6, checkY + 3, checkX + 8, checkY + 7, 0xFFFFFFFF);
        } else {
            graphics.fill(checkX, checkY, checkX + checkSize, checkY + checkSize, 0xFF555555);
            graphics.fill(checkX + 1, checkY + 1, checkX + checkSize - 1, checkY + checkSize - 1, 0xFF1A1A1A);
        }

        int textX = checkX + checkSize + 4;

        // --- Resource icons with counts ---
        List<ResourceRequirement> resources = item.getResources();
        if (!resources.isEmpty()) {
            int maxIcons = Math.min(resources.size(), 3);
            for (int i = 0; i < maxIcons; i++) {
                ResourceRequirement req = resources.get(i);
                try {
                    var regItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(req.getItemId()));
                    if (regItem != null) {
                        graphics.pose().pushPose();
                        graphics.pose().scale(0.5f, 0.5f, 1.0f);
                        graphics.renderItem(new ItemStack(regItem), (int)(textX / 0.5f), (int)((top + 1) / 0.5f));
                        graphics.pose().popPose();
                        // Draw count overlay
                        if (req.getCount() > 1) {
                            String countStr = req.getCount() > 999 ? "999+" : String.valueOf(req.getCount());
                            graphics.pose().pushPose();
                            graphics.pose().scale(0.5f, 0.5f, 1.0f);
                            int cx = (int)((textX + 5) / 0.5f);
                            int cy = (int)((top + 7) / 0.5f);
                            graphics.drawString(font, countStr, cx, cy, 0xFFFFFF00, true);
                            graphics.pose().popPose();
                        }
                        textX += 12;
                    }
                } catch (Exception ignored) {}
            }
            if (resources.size() > 3) {
                graphics.drawString(font, "+" + (resources.size() - 3), textX, top + 4, 0xFF888888, false);
                textX += font.width("+" + (resources.size() - 3)) + 2;
            }
        }

        // --- Category tag ---
        String catName = item.getCategory();
        int catColor = ClientTodoManager.getCategoryColor(catName);
        int catTagWidth = font.width(catName) + 6;
        int catTagX = left + width - catTagWidth - 38;
        int catTagY = top + 3;
        graphics.fill(catTagX, catTagY, catTagX + catTagWidth, catTagY + 10, (catColor & 0x00FFFFFF) | 0x44000000);
        graphics.fill(catTagX, catTagY, catTagX + 1, catTagY + 10, catColor);
        graphics.drawString(font, catName, catTagX + 4, catTagY + 1, catColor, false);

        // --- Task text ---
        int maxTextWidth = catTagX - textX - 4;
        String displayText = item.getText();
        if (font.width(displayText) > maxTextWidth) {
            displayText = font.plainSubstrByWidth(displayText, maxTextWidth - 8) + "...";
        }

        int textColor = item.isCompleted() ? 0xFF777777 : 0xFFFFFFFF;
        if (item.isCompleted()) {
            graphics.drawString(font, "\u00A7m" + displayText, textX, top + 4, textColor, false);
        } else {
            graphics.drawString(font, displayText, textX, top + 4, textColor, false);
        }

        // --- Player info line ---
        if (item.getVisibility() == TodoVisibility.SHARED) {
            String info = "";
            if (item.isCompleted() && item.getCompletedByName() != null) {
                info = "\u2713 " + item.getCompletedByName();
            } else if (item.getAssignedToName() != null) {
                info = "\u2192 " + item.getAssignedToName();
            } else if (item.getCreatedByName() != null) {
                info = "by " + item.getCreatedByName();
            }
            if (!info.isEmpty()) {
                graphics.drawString(font, info, textX, top + 14, 0xFF888888, false);
            }
        }

        // --- Edit button ---
        int editX = left + width - 34;
        int editY = top + 3;
        boolean editHovered = mouseX >= editX && mouseX <= editX + 12 && mouseY >= editY && mouseY <= editY + 10;
        graphics.fill(editX, editY, editX + 12, editY + 10, editHovered ? 0x66FFFFFF : 0x22FFFFFF);
        graphics.drawString(font, "\u270E", editX + 2, editY + 1, editHovered ? 0xFFFFFFFF : 0xFFAAAAAA, false);

        // --- Delete button ---
        int delX = left + width - 18;
        int delY = top + 3;
        boolean delHovered = mouseX >= delX && mouseX <= delX + 12 && mouseY >= delY && mouseY <= delY + 10;
        graphics.fill(delX, delY, delX + 12, delY + 10, delHovered ? 0x44FF4444 : 0x22FFFFFF);
        graphics.drawString(font, "\u00D7", delX + 3, delY + 1, delHovered ? 0xFFFF6666 : 0xFFAA4444, false);

        // --- Visibility star ---
        if (item.getVisibility() == TodoVisibility.SHARED) {
            graphics.drawString(font, "\u2605", left + width - 34, top + 14, 0xFFFFCC00, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = parent.getRowLeft();
        int width = parent.getRowWidth();
        int top = parent.getRowTop(parent.children().indexOf(this));
        int contentLeft = left + TodoListWidget.DRAG_HANDLE_WIDTH;

        // Checkbox click
        int checkX = contentLeft + 2;
        int checkY = top + 3;
        if (mouseX >= checkX && mouseX <= checkX + 10 && mouseY >= checkY && mouseY <= checkY + 10) {
            ClientTodoManager.toggleTodo(item.getId());
            return true;
        }

        // Edit button
        int editX = left + width - 34;
        int editY = top + 3;
        if (mouseX >= editX && mouseX <= editX + 12 && mouseY >= editY && mouseY <= editY + 10) {
            mc.setScreen(new TodoEditScreen(mc.screen, item));
            return true;
        }

        // Delete button
        int delX = left + width - 18;
        int delY = top + 3;
        if (mouseX >= delX && mouseX <= delX + 12 && mouseY >= delY && mouseY <= delY + 10) {
            ClientTodoManager.deleteTodo(item.getId());
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public net.minecraft.network.chat.Component getNarration() {
        return net.minecraft.network.chat.Component.literal(item.getText());
    }
}
