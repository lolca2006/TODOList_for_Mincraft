package com.todolist.mod.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TodoList {
    private List<TodoItem> items;

    public TodoList() {
        this.items = new ArrayList<>();
    }

    public TodoList(List<TodoItem> items) {
        this.items = new ArrayList<>(items);
    }

    public List<TodoItem> getItems() { return items; }
    public void setItems(List<TodoItem> items) { this.items = new ArrayList<>(items); }

    public void add(TodoItem item) {
        items.add(item);
    }

    public boolean remove(UUID todoId) {
        return items.removeIf(item -> item.getId().equals(todoId));
    }

    public TodoItem getById(UUID todoId) {
        return items.stream()
                .filter(item -> item.getId().equals(todoId))
                .findFirst()
                .orElse(null);
    }

    public List<TodoItem> getByCategory(String category) {
        return items.stream()
                .filter(item -> category.equals(item.getCategory()))
                .collect(Collectors.toList());
    }

    public List<TodoItem> getByVisibility(TodoVisibility visibility) {
        return items.stream()
                .filter(item -> item.getVisibility() == visibility)
                .collect(Collectors.toList());
    }

    public List<TodoItem> getAssignedTo(UUID playerUuid) {
        return items.stream()
                .filter(item -> playerUuid.equals(item.getAssignedTo()))
                .collect(Collectors.toList());
    }

    public List<TodoItem> getCreatedBy(UUID playerUuid) {
        return items.stream()
                .filter(item -> playerUuid.equals(item.getCreatedBy()))
                .collect(Collectors.toList());
    }

    public List<TodoItem> getVisibleTo(UUID playerUuid) {
        return items.stream()
                .filter(item ->
                    item.getVisibility() == TodoVisibility.SHARED ||
                    playerUuid.equals(item.getCreatedBy()) ||
                    playerUuid.equals(item.getAssignedTo())
                )
                .collect(Collectors.toList());
    }

    public long getCompletedCount() {
        return items.stream().filter(TodoItem::isCompleted).count();
    }

    public int size() { return items.size(); }
    public boolean isEmpty() { return items.isEmpty(); }
    public void clear() { items.clear(); }
}
