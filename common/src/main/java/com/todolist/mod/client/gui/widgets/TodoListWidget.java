package com.todolist.mod.client.gui.widgets;

import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.common.model.TodoItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;

import java.util.List;

public class TodoListWidget extends ObjectSelectionList<TodoItemWidget> {
    private String categoryFilter = null;
    private String searchFilter = "";

    // Drag state
    private int dragIndex = -1;
    private int dragTargetIndex = -1;
    private boolean isDragging = false;
    private double dragStartY = 0;
    private double dragMouseY = 0;

    // Drag handle width in pixels
    public static final int DRAG_HANDLE_WIDTH = 18;

    public TodoListWidget(Minecraft mc, int width, int height, int top, int itemHeight) {
        super(mc, width, height, top, itemHeight);
    }

    public void refreshItems() {
        this.clearEntries();
        List<TodoItem> items = ClientTodoManager.getTodoList().getItems();

        for (TodoItem item : items) {
            if (categoryFilter != null && !categoryFilter.isEmpty()) {
                if (!categoryFilter.equals(item.getCategory())) continue;
            }
            if (!searchFilter.isEmpty()) {
                if (!item.getText().toLowerCase().contains(searchFilter.toLowerCase())) continue;
            }
            this.addEntry(new TodoItemWidget(item, this));
        }
    }

    public void setCategoryFilter(String category) {
        this.categoryFilter = category;
        refreshItems();
    }

    public void setSearchFilter(String search) {
        this.searchFilter = search;
        refreshItems();
    }

    public String getCategoryFilter() { return categoryFilter; }

    @Override
    public int getRowWidth() {
        return this.width - 12;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }

    public int getRowLeft() { return this.getX(); }

    public int getRowTop(int index) {
        return this.getY() - (int) this.getScrollAmount() + index * this.itemHeight + this.headerHeight + 4;
    }

    public int getItemHeight() { return this.itemHeight; }

    // --- Drag and Drop ---

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            TodoItemWidget entry = getEntryAtPosition(mouseX, mouseY);
            if (entry != null) {
                int entryIndex = this.children().indexOf(entry);
                int left = getRowLeft();
                // Click in the drag handle area (left 18px)
                if (entryIndex >= 0 && mouseX >= left && mouseX <= left + DRAG_HANDLE_WIDTH) {
                    dragIndex = entryIndex;
                    dragStartY = mouseY;
                    dragMouseY = mouseY;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && dragIndex >= 0) {
            dragMouseY = mouseY;
            if (!isDragging && Math.abs(mouseY - dragStartY) > 3) {
                isDragging = true;
            }
            if (isDragging) {
                // Find target index
                int newTarget = -1;
                for (int i = 0; i < this.children().size(); i++) {
                    int rowTop = getRowTop(i);
                    int rowMid = rowTop + this.itemHeight / 2;
                    if (mouseY < rowMid) {
                        newTarget = i;
                        break;
                    }
                }
                if (newTarget < 0) newTarget = this.children().size() - 1;
                if (newTarget != dragIndex) {
                    dragTargetIndex = newTarget;
                }
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isDragging && dragIndex >= 0 && dragTargetIndex >= 0 && dragIndex != dragTargetIndex) {
            TodoItemWidget draggedEntry = this.children().get(dragIndex);
            TodoItem draggedItem = draggedEntry.getTodoItem();

            ClientTodoManager.reorderTodo(draggedItem.getId(), dragTargetIndex);

            // Reorder local list immediately
            List<TodoItem> items = ClientTodoManager.getTodoList().getItems();
            int fromIdx = items.indexOf(draggedItem);
            if (fromIdx >= 0) {
                items.remove(fromIdx);
                int toIdx = Math.min(dragTargetIndex, items.size());
                items.add(toIdx, draggedItem);
                for (int i = 0; i < items.size(); i++) {
                    items.get(i).setSortOrder(i);
                }
            }
            refreshItems();
        }
        dragIndex = -1;
        dragTargetIndex = -1;
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        int left = getRowLeft();
        int right = left + getRowWidth();

        // Draw drag handles for all entries
        for (int i = 0; i < this.children().size(); i++) {
            int rowTop = getRowTop(i);
            if (rowTop < this.getY() - this.itemHeight || rowTop > this.getY() + this.getHeight()) continue; // off screen

            boolean handleHovered = !isDragging &&
                    mouseX >= left && mouseX <= left + DRAG_HANDLE_WIDTH &&
                    mouseY >= rowTop && mouseY < rowTop + this.itemHeight;
            boolean isBeingDragged = isDragging && i == dragIndex;

            // Handle background on hover
            if (handleHovered || isBeingDragged) {
                graphics.fill(left, rowTop, left + DRAG_HANDLE_WIDTH, rowTop + this.itemHeight,
                        isBeingDragged ? 0x44FFFFFF : 0x22FFFFFF);
            }

            // Draw 6-dot grip icon
            int dotColor = (handleHovered || isBeingDragged) ? 0xCCCCCCCC : 0x55888888;
            int dotX = left + 5;
            int dotY = rowTop + (this.itemHeight / 2) - 5;
            for (int row = 0; row < 3; row++) {
                graphics.fill(dotX, dotY + row * 4, dotX + 2, dotY + row * 4 + 2, dotColor);
                graphics.fill(dotX + 4, dotY + row * 4, dotX + 6, dotY + row * 4 + 2, dotColor);
            }
        }

        // Draw drop indicator line
        if (isDragging && dragTargetIndex >= 0) {
            int lineY;
            if (dragTargetIndex <= dragIndex) {
                lineY = getRowTop(dragTargetIndex);
            } else {
                lineY = getRowTop(dragTargetIndex) + this.itemHeight;
            }
            // Blue indicator line
            graphics.fill(left, lineY - 1, right, lineY + 1, 0xFF4488FF);
            // Arrow ends
            graphics.fill(left, lineY - 3, left + 6, lineY + 3, 0xFF4488FF);
            graphics.fill(right - 6, lineY - 3, right, lineY + 3, 0xFF4488FF);
        }

        // Draw ghost overlay for dragged item
        if (isDragging && dragIndex >= 0 && dragIndex < this.children().size()) {
            int ghostY = (int) dragMouseY - this.itemHeight / 2;
            graphics.fill(left, ghostY, right, ghostY + this.itemHeight, 0x444488FF);
        }
    }
}
