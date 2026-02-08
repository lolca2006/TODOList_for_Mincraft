package com.todolist.mod.platform;

import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.common.model.TodoItem;
import com.todolist.mod.common.model.TodoVisibility;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public interface INetworkAccess {
    // Client → Server
    void sendAddTodo(String text, String category, TodoVisibility vis, List<ResourceRequirement> resources);
    void sendToggleTodo(UUID todoId);
    void sendDeleteTodo(UUID todoId);
    void sendEditTodo(UUID todoId, String text, String cat, TodoVisibility vis, List<ResourceRequirement> res);
    void sendReorderTodo(UUID todoId, int newOrder);
    void sendRequestSync();
    void sendAssignTodo(UUID todoId, UUID assignTo, String assignToName);
    void sendShareTodo(UUID todoId, TodoVisibility vis);

    // Server → Client
    void sendSyncToPlayer(ServerPlayer player, List<TodoItem> items);
    void sendUpdateToAll(String action, TodoItem item);
    void sendRemoveToAll(UUID todoId);
}
