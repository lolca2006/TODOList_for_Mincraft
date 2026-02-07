package com.todolist.mod.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TodoItem {
    private UUID id;
    private String text;
    private boolean completed;
    private String category;
    private UUID createdBy;
    private String createdByName;
    private UUID assignedTo;
    private String assignedToName;
    private UUID completedBy;
    private String completedByName;
    private long createdAt;
    private long completedAt;
    private TodoVisibility visibility;
    private List<String> requiredItems;
    private int sortOrder;

    public TodoItem() {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.category = "General";
        this.visibility = TodoVisibility.PRIVATE;
        this.requiredItems = new ArrayList<>();
        this.sortOrder = 0;
    }

    public TodoItem(String text, UUID createdBy, String createdByName) {
        this();
        this.text = text;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String name) { this.createdByName = name; }
    public UUID getAssignedTo() { return assignedTo; }
    public void setAssignedTo(UUID assignedTo) { this.assignedTo = assignedTo; }
    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String name) { this.assignedToName = name; }
    public UUID getCompletedBy() { return completedBy; }
    public void setCompletedBy(UUID completedBy) { this.completedBy = completedBy; }
    public String getCompletedByName() { return completedByName; }
    public void setCompletedByName(String name) { this.completedByName = name; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
    public TodoVisibility getVisibility() { return visibility; }
    public void setVisibility(TodoVisibility visibility) { this.visibility = visibility; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public List<String> getRequiredItems() {
        if (requiredItems == null) requiredItems = new ArrayList<>();
        return requiredItems;
    }
    public void setRequiredItems(List<String> items) { this.requiredItems = items != null ? new ArrayList<>(items) : new ArrayList<>(); }
    public void addRequiredItem(String itemId) { getRequiredItems().add(itemId); }
    public void removeRequiredItem(String itemId) { getRequiredItems().remove(itemId); }

    // Legacy compat
    public String getItemId() {
        return requiredItems != null && !requiredItems.isEmpty() ? requiredItems.get(0) : null;
    }
    public void setItemId(String itemId) {
        if (requiredItems == null) requiredItems = new ArrayList<>();
        requiredItems.clear();
        if (itemId != null && !itemId.isEmpty()) requiredItems.add(itemId);
    }

    public void toggleCompleted(UUID playerUuid, String playerName) {
        this.completed = !this.completed;
        if (this.completed) {
            this.completedBy = playerUuid;
            this.completedByName = playerName;
            this.completedAt = System.currentTimeMillis();
        } else {
            this.completedBy = null;
            this.completedByName = null;
            this.completedAt = 0;
        }
    }
}
