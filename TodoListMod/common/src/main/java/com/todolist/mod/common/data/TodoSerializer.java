package com.todolist.mod.common.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.todolist.mod.common.model.Category;
import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.common.model.TodoItem;
import com.todolist.mod.common.model.TodoList;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class TodoSerializer {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
            .registerTypeAdapter(ResourceRequirement.class, new ResourceRequirementTypeAdapter())
            .create();

    public static String itemsToJson(List<TodoItem> items) {
        return GSON.toJson(items);
    }

    public static List<TodoItem> itemsFromJson(String json) {
        Type listType = new TypeToken<List<TodoItem>>() {}.getType();
        List<TodoItem> result = GSON.fromJson(json, listType);
        return result != null ? result : List.of();
    }

    public static String todoListToJson(TodoList list) {
        return GSON.toJson(list.getItems());
    }

    public static TodoList todoListFromJson(String json) {
        return new TodoList(itemsFromJson(json));
    }

    public static String categoriesToJson(List<Category> categories) {
        return GSON.toJson(categories);
    }

    public static List<Category> categoriesFromJson(String json) {
        Type listType = new TypeToken<List<Category>>() {}.getType();
        List<Category> result = GSON.fromJson(json, listType);
        return result != null ? result : Category.defaults();
    }

    public static Gson getGson() { return GSON; }
}
