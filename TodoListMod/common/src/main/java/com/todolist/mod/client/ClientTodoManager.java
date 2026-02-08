package com.todolist.mod.client;

import com.todolist.mod.Constants;
import com.todolist.mod.common.data.TodoSerializer;
import com.todolist.mod.common.model.Category;
import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.common.model.TodoItem;
import com.todolist.mod.common.model.TodoList;
import com.todolist.mod.common.model.TodoVisibility;
import com.todolist.mod.platform.Services;
import net.minecraft.client.Minecraft;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientTodoManager {
    private static final TodoList todoList = new TodoList();
    private static List<Category> categories = new ArrayList<>(Category.defaults());
    private static boolean connectedToServer = false;

    public static TodoList getTodoList() { return todoList; }
    public static List<Category> getCategories() { return categories; }
    public static void setCategories(List<Category> cats) { categories = new ArrayList<>(cats); saveCategories(); }

    public static void onJoinServer() {
        connectedToServer = true;
        todoList.clear();
        loadCategories();
        Services.NETWORK.sendRequestSync();
    }

    public static void onDisconnect() {
        connectedToServer = false;
        todoList.clear();
    }

    // --- Receive from server ---

    public static void receiveSync(List<TodoItem> items) {
        todoList.setItems(items);
        Constants.LOGGER.info("Synced {} todos from server", items.size());
    }

    public static void receiveUpdate(TodoItem item) {
        TodoItem existing = todoList.getById(item.getId());
        if (existing != null) {
            todoList.remove(item.getId());
        }
        todoList.add(item);
    }

    public static void receiveRemove(UUID todoId) {
        todoList.remove(todoId);
    }

    // --- Send to server ---

    public static void addTodo(String text, String category, TodoVisibility visibility, List<ResourceRequirement> resources) {
        Services.NETWORK.sendAddTodo(text, category, visibility, resources);
    }

    public static void toggleTodo(UUID todoId) {
        Services.NETWORK.sendToggleTodo(todoId);
    }

    public static void deleteTodo(UUID todoId) {
        Services.NETWORK.sendDeleteTodo(todoId);
    }

    public static void assignTodo(UUID todoId, UUID assignTo, String assignToName) {
        Services.NETWORK.sendAssignTodo(todoId, assignTo, assignToName);
    }

    public static void shareTodo(UUID todoId, TodoVisibility visibility) {
        Services.NETWORK.sendShareTodo(todoId, visibility);
    }

    public static void editTodo(UUID todoId, String text, String category, TodoVisibility visibility, List<ResourceRequirement> resources) {
        Services.NETWORK.sendEditTodo(todoId, text, category, visibility, resources);
    }

    public static void reorderTodo(UUID todoId, int newSortOrder) {
        Services.NETWORK.sendReorderTodo(todoId, newSortOrder);
    }

    // --- Categories (client-side only) ---

    public static Category getCategoryByName(String name) {
        return categories.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static int getCategoryColor(String name) {
        Category cat = getCategoryByName(name);
        return cat != null ? cat.getColor() : 0xFFFFFFFF;
    }

    private static Path getCategoriesFile() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("todolistmod_categories.json");
    }

    public static void loadCategories() {
        Path file = getCategoriesFile();
        if (Files.exists(file)) {
            try {
                String json = Files.readString(file);
                categories = TodoSerializer.categoriesFromJson(json);
            } catch (Exception e) {
                Constants.LOGGER.error("Failed to load categories", e);
                categories = new ArrayList<>(Category.defaults());
            }
        } else {
            categories = new ArrayList<>(Category.defaults());
            saveCategories();
        }
    }

    public static void saveCategories() {
        Path file = getCategoriesFile();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, TodoSerializer.categoriesToJson(categories));
        } catch (Exception e) {
            Constants.LOGGER.error("Failed to save categories", e);
        }
    }
}
