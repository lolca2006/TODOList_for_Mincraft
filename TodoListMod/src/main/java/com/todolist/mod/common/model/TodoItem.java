package com.todolist.mod.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private List<ResourceRequirement> resources;
    private int sortOrder;

    public TodoItem() {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.category = "General";
        this.visibility = TodoVisibility.PRIVATE;
        this.resources = new ArrayList<>();
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

    // --- Resource management ---

    public List<ResourceRequirement> getResources() {
        if (resources == null) resources = new ArrayList<>();
        return resources;
    }

    public void setResources(List<ResourceRequirement> resources) {
        this.resources = resources != null ? new ArrayList<>(resources) : new ArrayList<>();
    }

    public void addResource(String itemId, int count) {
        // Merge with existing if same item
        for (ResourceRequirement req : getResources()) {
            if (req.getItemId().equals(itemId)) {
                req.setCount(req.getCount() + count);
                return;
            }
        }
        getResources().add(new ResourceRequirement(itemId, count));
    }

    public void removeResource(String itemId) {
        getResources().removeIf(r -> r.getItemId().equals(itemId));
    }

    public ResourceRequirement getResourceByItemId(String itemId) {
        return getResources().stream()
                .filter(r -> r.getItemId().equals(itemId))
                .findFirst().orElse(null);
    }

    public int getTotalResourceCount() {
        return getResources().stream().mapToInt(ResourceRequirement::getCount).sum();
    }

    // --- Legacy compatibility (for old code that used String lists) ---

    public List<String> getRequiredItems() {
        return getResources().stream()
                .map(ResourceRequirement::getItemId)
                .collect(Collectors.toList());
    }

    public String getItemId() {
        return resources != null && !resources.isEmpty() ? resources.get(0).getItemId() : null;
    }

    public void setItemId(String itemId) {
        if (resources == null) resources = new ArrayList<>();
        resources.clear();
        if (itemId != null && !itemId.isEmpty()) {
            resources.add(new ResourceRequirement(itemId, 1));
        }
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
