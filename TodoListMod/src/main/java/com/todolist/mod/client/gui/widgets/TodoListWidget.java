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
    private int dragIndex = -1;
    private int dragTargetIndex = -1;
    private boolean isDragging = false;
    private double dragStartY = 0;
    private long dragStartTime = 0;

    public TodoListWidget(Minecraft mc, int width, int height, int top, int bottom, int itemHeight) {
        super(mc, width, height, top, bottom, itemHeight);
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }

    public void refreshItems() {
        this.clearEntries();
        List<TodoItem> items = ClientTodoManager.getTodoList().getItems();

        for (TodoItem item : items) {
            // Category filter
            if (categoryFilter != null && !categoryFilter.isEmpty()) {
                if (!categoryFilter.equals(item.getCategory())) continue;
            }
            // Search filter
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
        return this.x0 + this.width - 6;
    }

    // Expose methods for TodoItemWidget
    public int getRowLeft() {
        return this.x0;
    }

    public int getRowTop(int index) {
        return this.y0 - (int)this.getScrollAmount() + index * this.itemHeight + this.headerHeight + 4;
    }

    public int getItemHeight() {
        return this.itemHeight;
    }

    // --- Drag and Drop ---

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Determine which entry was clicked
            int entryIndex = getEntryAtPosition(mouseX, mouseY) != null ?
                    this.children().indexOf(getEntryAtPosition(mouseX, mouseY)) : -1;
            if (entryIndex >= 0) {
                // Check if click is on the left drag handle area (first 14 pixels)
                int left = getRowLeft();
                if (mouseX >= left - 5 && mouseX <= left + 3) {
                    dragIndex = entryIndex;
                    dragStartY = mouseY;
                    dragStartTime = System.currentTimeMillis();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && dragIndex >= 0) {
            // Start dragging after small threshold
            if (!isDragging && Math.abs(mouseY - dragStartY) > 4) {
                isDragging = true;
            }
            if (isDragging) {
                // Calculate target index based on mouse position
                int newTarget = -1;
                for (int i = 0; i < this.children().size(); i++) {
                    int rowTop = getRowTop(i);
                    int rowBottom = rowTop + this.itemHeight;
                    if (mouseY >= rowTop && mouseY < rowBottom) {
                        newTarget = i;
                        break;
                    }
                }
                if (newTarget >= 0 && newTarget != dragIndex) {
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
            // Perform the reorder
            TodoItemWidget draggedEntry = this.children().get(dragIndex);
            TodoItem draggedItem = draggedEntry.getTodoItem();

            // Send reorder packet with new sort order
            ClientTodoManager.reorderTodo(draggedItem.getId(), dragTargetIndex);

            // Update local list immediately for responsiveness
            List<TodoItem> items = ClientTodoManager.getTodoList().getItems();
            int fromIdx = items.indexOf(draggedItem);
            if (fromIdx >= 0) {
                items.remove(fromIdx);
                int toIdx = Math.min(dragTargetIndex, items.size());
                items.add(toIdx, draggedItem);
                // Update sort orders
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Draw drag indicator
        if (isDragging && dragTargetIndex >= 0) {
            int targetTop = getRowTop(dragTargetIndex);
            int left = getRowLeft();
            int right = left + getRowWidth();
            // Draw a blue line at the target position
            if (dragTargetIndex > dragIndex) {
                targetTop += this.itemHeight;
            }
            graphics.fill(left, targetTop - 1, right, targetTop + 1, 0xFF4488FF);
            // Small arrow indicators
            graphics.fill(left, targetTop - 3, left + 4, targetTop + 3, 0xFF4488FF);
            graphics.fill(right - 4, targetTop - 3, right, targetTop + 3, 0xFF4488FF);
        }

        // Draw drag handle hints on hover (small dots on left edge)
        if (!isDragging) {
            for (int i = 0; i < this.children().size(); i++) {
                int rowTop = getRowTop(i);
                int left = getRowLeft();
                if (mouseX >= left - 5 && mouseX <= left + 3 &&
                        mouseY >= rowTop && mouseY < rowTop + this.itemHeight) {
                    // Draw 3 dots as drag handle
                    int dotX = left - 2;
                    graphics.fill(dotX, rowTop + 4, dotX + 2, rowTop + 6, 0xFFAAAAAA);
                    graphics.fill(dotX, rowTop + 8, dotX + 2, rowTop + 10, 0xFFAAAAAA);
                    graphics.fill(dotX, rowTop + 12, dotX + 2, rowTop + 14, 0xFFAAAAAA);
                }
            }
        }
    }
}
