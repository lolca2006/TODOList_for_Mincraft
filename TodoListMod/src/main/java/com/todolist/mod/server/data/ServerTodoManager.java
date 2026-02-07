package com.todolist.mod.server.data;

import com.todolist.mod.TodoListMod;
import com.todolist.mod.common.data.TodoSerializer;
import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.common.model.TodoItem;
import com.todolist.mod.common.model.TodoList;
import com.todolist.mod.common.model.TodoVisibility;
import com.todolist.mod.common.network.SyncTodoListPacket;
import com.todolist.mod.common.network.TodoUpdatePacket;
import com.todolist.mod.forge.ForgeNetworkHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerTodoManager {
    private static final Map<UUID, TodoList> playerLists = new ConcurrentHashMap<>();
    private static final TodoList sharedList = new TodoList();
    private static Path dataDir;
    private static MinecraftServer server;

    public static void init(MinecraftServer srv) {
        server = srv;
        dataDir = srv.getWorldPath(LevelResource.ROOT).resolve("data").resolve("todolistmod");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            TodoListMod.LOGGER.error("Failed to create todo data directory", e);
        }
        loadAll();
    }

    public static void shutdown() {
        saveAll();
        playerLists.clear();
        sharedList.clear();
        server = null;
    }

    // --- CRUD Operations ---

    public static synchronized void addTodo(ServerPlayer player, String text, String category, TodoVisibility visibility, List<ResourceRequirement> resources) {
        if (text == null || text.trim().isEmpty()) return;

        TodoItem item = new TodoItem(text.trim(), player.getUUID(), player.getGameProfile().getName());
        item.setCategory(category != null ? category : "General");
        item.setVisibility(visibility);
        if (resources != null && !resources.isEmpty()) {
            item.setResources(resources);
        }

        if (visibility == TodoVisibility.SHARED) {
            sharedList.add(item);
        } else {
            getOrCreatePlayerList(player.getUUID()).add(item);
        }

        save(player.getUUID());
        saveShared();

        // Broadcast update
        if (visibility == TodoVisibility.SHARED) {
            ForgeNetworkHandler.sendToAll(new TodoUpdatePacket(TodoUpdatePacket.Action.ADD, item));
        } else {
            syncToPlayer(player);
        }
    }

    public static synchronized void toggleTodo(ServerPlayer player, UUID todoId) {
        TodoItem item = findTodo(player.getUUID(), todoId);
        if (item == null) return;

        // Check permission: only creator, assignee, or shared items can be toggled
        if (!canModify(player, item)) return;

        item.toggleCompleted(player.getUUID(), player.getGameProfile().getName());

        save(player.getUUID());
        saveShared();

        if (item.getVisibility() == TodoVisibility.SHARED) {
            ForgeNetworkHandler.sendToAll(new TodoUpdatePacket(TodoUpdatePacket.Action.UPDATE, item));
        } else {
            syncToPlayer(player);
        }
    }

    public static synchronized void deleteTodo(ServerPlayer player, UUID todoId) {
        TodoItem item = findTodo(player.getUUID(), todoId);
        if (item == null) return;
        if (!canModify(player, item)) return;

        boolean wasShared = item.getVisibility() == TodoVisibility.SHARED;

        if (wasShared) {
            sharedList.remove(todoId);
        }
        TodoList playerList = playerLists.get(player.getUUID());
        if (playerList != null) {
            playerList.remove(todoId);
        }

        save(player.getUUID());
        saveShared();

        if (wasShared) {
            ForgeNetworkHandler.sendToAll(new TodoUpdatePacket(TodoUpdatePacket.Action.REMOVE, todoId));
        } else {
            syncToPlayer(player);
        }
    }

    public static synchronized void assignTodo(ServerPlayer player, UUID todoId, UUID assignTo, String assignToName) {
        TodoItem item = findTodo(player.getUUID(), todoId);
        if (item == null) return;
        if (!canModify(player, item)) return;

        item.setAssignedTo(assignTo);
        item.setAssignedToName(assignToName);

        save(player.getUUID());
        saveShared();

        if (item.getVisibility() == TodoVisibility.SHARED) {
            ForgeNetworkHandler.sendToAll(new TodoUpdatePacket(TodoUpdatePacket.Action.UPDATE, item));
        } else {
            syncToPlayer(player);
            // Also sync to the assigned player if they're online
            if (assignTo != null && server != null) {
                ServerPlayer assignedPlayer = server.getPlayerList().getPlayer(assignTo);
                if (assignedPlayer != null && !assignedPlayer.getUUID().equals(player.getUUID())) {
                    syncToPlayer(assignedPlayer);
                }
            }
        }
    }

    public static synchronized void setVisibility(ServerPlayer player, UUID todoId, TodoVisibility visibility) {
        TodoItem item = findTodo(player.getUUID(), todoId);
        if (item == null) return;
        if (!player.getUUID().equals(item.getCreatedBy())) return; // Only creator can change visibility

        TodoVisibility oldVisibility = item.getVisibility();
        item.setVisibility(visibility);

        // Move between lists
        if (oldVisibility == TodoVisibility.PRIVATE && visibility == TodoVisibility.SHARED) {
            TodoList playerList = playerLists.get(player.getUUID());
            if (playerList != null) playerList.remove(todoId);
            sharedList.add(item);
        } else if (oldVisibility == TodoVisibility.SHARED && visibility == TodoVisibility.PRIVATE) {
            sharedList.remove(todoId);
            getOrCreatePlayerList(player.getUUID()).add(item);
        }

        save(player.getUUID());
        saveShared();

        // Sync all affected players
        if (visibility == TodoVisibility.SHARED || oldVisibility == TodoVisibility.SHARED) {
            syncToAllPlayers();
        } else {
            syncToPlayer(player);
        }
    }

    public static synchronized void editTodo(ServerPlayer player, UUID todoId, String text, String category, TodoVisibility newVisibility, List<ResourceRequirement> resources) {
        TodoItem item = findTodo(player.getUUID(), todoId);
        if (item == null) return;
        if (!canModify(player, item)) return;

        item.setText(text);
        item.setCategory(category);
        item.setResources(resources);

        TodoVisibility oldVisibility = item.getVisibility();
        if (!oldVisibility.equals(newVisibility) && player.getUUID().equals(item.getCreatedBy())) {
            item.setVisibility(newVisibility);
            if (oldVisibility == TodoVisibility.PRIVATE && newVisibility == TodoVisibility.SHARED) {
                TodoList playerList = playerLists.get(player.getUUID());
                if (playerList != null) playerList.remove(todoId);
                sharedList.add(item);
            } else if (oldVisibility == TodoVisibility.SHARED && newVisibility == TodoVisibility.PRIVATE) {
                sharedList.remove(todoId);
                getOrCreatePlayerList(player.getUUID()).add(item);
            }
        }

        save(player.getUUID());
        saveShared();

        if (item.getVisibility() == TodoVisibility.SHARED || oldVisibility == TodoVisibility.SHARED) {
            syncToAllPlayers();
        } else {
            syncToPlayer(player);
        }
    }

    public static synchronized void reorderTodo(ServerPlayer player, UUID todoId, int newSortOrder) {
        TodoItem item = findTodo(player.getUUID(), todoId);
        if (item == null) return;
        if (!canModify(player, item)) return;

        item.setSortOrder(newSortOrder);
        save(player.getUUID());
        saveShared();
        syncToPlayer(player);
    }

    // --- Sync ---

    public static void syncToPlayer(ServerPlayer player) {
        List<TodoItem> visibleItems = getVisibleItems(player.getUUID());
        ForgeNetworkHandler.sendToPlayer(new SyncTodoListPacket(visibleItems), player);
    }

    public static void syncToAllPlayers() {
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncToPlayer(player);
        }
    }

    private static List<TodoItem> getVisibleItems(UUID playerUuid) {
        List<TodoItem> result = new ArrayList<>();

        // Add private items
        TodoList playerList = playerLists.get(playerUuid);
        if (playerList != null) {
            result.addAll(playerList.getItems());
        }

        // Add shared items
        result.addAll(sharedList.getItems());

        // Add items assigned to this player from other players' lists
        for (Map.Entry<UUID, TodoList> entry : playerLists.entrySet()) {
            if (!entry.getKey().equals(playerUuid)) {
                for (TodoItem item : entry.getValue().getItems()) {
                    if (playerUuid.equals(item.getAssignedTo())) {
                        result.add(item);
                    }
                }
            }
        }

        return result;
    }

    // --- Helpers ---

    private static boolean canModify(ServerPlayer player, TodoItem item) {
        UUID playerUuid = player.getUUID();
        return playerUuid.equals(item.getCreatedBy()) ||
               playerUuid.equals(item.getAssignedTo()) ||
               item.getVisibility() == TodoVisibility.SHARED;
    }

    private static TodoItem findTodo(UUID playerUuid, UUID todoId) {
        // Search in player's private list
        TodoList playerList = playerLists.get(playerUuid);
        if (playerList != null) {
            TodoItem item = playerList.getById(todoId);
            if (item != null) return item;
        }
        // Search in shared list
        TodoItem shared = sharedList.getById(todoId);
        if (shared != null) return shared;

        // Search all lists (for assigned items)
        for (TodoList list : playerLists.values()) {
            TodoItem item = list.getById(todoId);
            if (item != null) return item;
        }
        return null;
    }

    private static TodoList getOrCreatePlayerList(UUID playerUuid) {
        return playerLists.computeIfAbsent(playerUuid, k -> new TodoList());
    }

    // --- Persistence ---

    private static void loadAll() {
        // Load shared list
        Path sharedFile = dataDir.resolve("shared.json");
        if (Files.exists(sharedFile)) {
            try {
                String json = Files.readString(sharedFile);
                TodoList loaded = TodoSerializer.todoListFromJson(json);
                sharedList.setItems(loaded.getItems());
                TodoListMod.LOGGER.info("Loaded {} shared todos", sharedList.size());
            } catch (Exception e) {
                TodoListMod.LOGGER.error("Failed to load shared todos", e);
            }
        }

        // Load player lists
        try (Stream<Path> paths = Files.list(dataDir)) {
            paths.filter(p -> p.getFileName().toString().endsWith(".json"))
                 .filter(p -> !p.getFileName().toString().equals("shared.json"))
                 .forEach(p -> {
                     try {
                         String filename = p.getFileName().toString();
                         UUID playerUuid = UUID.fromString(filename.replace(".json", ""));
                         String json = Files.readString(p);
                         TodoList list = TodoSerializer.todoListFromJson(json);
                         playerLists.put(playerUuid, list);
                         TodoListMod.LOGGER.info("Loaded {} todos for player {}", list.size(), playerUuid);
                     } catch (Exception e) {
                         TodoListMod.LOGGER.error("Failed to load player todos from {}", p, e);
                     }
                 });
        } catch (IOException e) {
            TodoListMod.LOGGER.error("Failed to list todo data files", e);
        }
    }

    public static void saveAll() {
        saveShared();
        for (UUID playerUuid : playerLists.keySet()) {
            save(playerUuid);
        }
    }

    private static void save(UUID playerUuid) {
        TodoList list = playerLists.get(playerUuid);
        if (list == null) return;
        try {
            Path file = dataDir.resolve(playerUuid.toString() + ".json");
            Files.writeString(file, TodoSerializer.todoListToJson(list));
        } catch (Exception e) {
            TodoListMod.LOGGER.error("Failed to save todos for player {}", playerUuid, e);
        }
    }

    private static void saveShared() {
        try {
            Path file = dataDir.resolve("shared.json");
            Files.writeString(file, TodoSerializer.todoListToJson(sharedList));
        } catch (Exception e) {
            TodoListMod.LOGGER.error("Failed to save shared todos", e);
        }
    }
}
