package com.todolist.mod.common.model;

import java.util.Objects;

public class ResourceRequirement {
    private String itemId;
    private int count;
    private boolean collected;

    public ResourceRequirement() {
        this.count = 1;
    }

    public ResourceRequirement(String itemId, int count) {
        this.itemId = itemId;
        this.count = Math.max(1, count);
    }

    public ResourceRequirement(String itemId, int count, boolean collected) {
        this.itemId = itemId;
        this.count = Math.max(1, count);
        this.collected = collected;
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = Math.max(1, count); }
    public boolean isCollected() { return collected; }
    public void setCollected(boolean collected) { this.collected = collected; }

    public ResourceRequirement copy() {
        return new ResourceRequirement(itemId, count, collected);
    }

    /**
     * Merge another requirement into this one (adds counts if same item).
     */
    public void merge(ResourceRequirement other) {
        if (other != null && Objects.equals(this.itemId, other.itemId)) {
            this.count += other.count;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceRequirement that = (ResourceRequirement) o;
        return Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }

    @Override
    public String toString() {
        return count + "x " + itemId;
    }
}
