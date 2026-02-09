package com.todolist.mod.neoforge;

import com.todolist.mod.common.data.TodoSerializer;
import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.common.model.TodoItem;
import com.todolist.mod.common.model.TodoVisibility;
import com.todolist.mod.platform.INetworkAccess;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public class NeoForgeNetworkAccess implements INetworkAccess {

    @Override
    public void sendAddTodo(String text, String category, TodoVisibility vis, List<ResourceRequirement> resources) {
        NeoForgeNetworkHandler.sendToServer(
                new NeoForgeNetworkHandler.AddTodoPayload(text, category, vis, resources != null ? resources : List.of()));
    }

    @Override
    public void sendToggleTodo(UUID todoId) {
        NeoForgeNetworkHandler.sendToServer(new NeoForgeNetworkHandler.ToggleTodoPayload(todoId));
    }

    @Override
    public void sendDeleteTodo(UUID todoId) {
        NeoForgeNetworkHandler.sendToServer(new NeoForgeNetworkHandler.DeleteTodoPayload(todoId));
    }

    @Override
    public void sendEditTodo(UUID todoId, String text, String cat, TodoVisibility vis, List<ResourceRequirement> res) {
        NeoForgeNetworkHandler.sendToServer(
                new NeoForgeNetworkHandler.EditTodoPayload(todoId, text, cat, vis, res != null ? res : List.of()));
    }

    @Override
    public void sendReorderTodo(UUID todoId, int newOrder) {
        NeoForgeNetworkHandler.sendToServer(new NeoForgeNetworkHandler.ReorderTodoPayload(todoId, newOrder));
    }

    @Override
    public void sendRequestSync() {
        NeoForgeNetworkHandler.sendToServer(new NeoForgeNetworkHandler.RequestSyncPayload());
    }

    @Override
    public void sendAssignTodo(UUID todoId, UUID assignTo, String assignToName) {
        NeoForgeNetworkHandler.sendToServer(
                new NeoForgeNetworkHandler.AssignTodoPayload(todoId, assignTo, assignToName));
    }

    @Override
    public void sendShareTodo(UUID todoId, TodoVisibility vis) {
        NeoForgeNetworkHandler.sendToServer(new NeoForgeNetworkHandler.ShareTodoPayload(todoId, vis));
    }

    @Override
    public void sendSyncToPlayer(ServerPlayer player, List<TodoItem> items) {
        String json = TodoSerializer.itemsToJson(items);
        NeoForgeNetworkHandler.sendToPlayer(new NeoForgeNetworkHandler.SyncTodoListPayload(json), player);
    }

    @Override
    public void sendUpdateToAll(String action, TodoItem item) {
        NeoForgeNetworkHandler.TodoUpdatePayload.Action a =
                NeoForgeNetworkHandler.TodoUpdatePayload.Action.valueOf(action);
        NeoForgeNetworkHandler.sendToAll(new NeoForgeNetworkHandler.TodoUpdatePayload(a, item));
    }

    @Override
    public void sendRemoveToAll(UUID todoId) {
        NeoForgeNetworkHandler.sendToAll(
                new NeoForgeNetworkHandler.TodoUpdatePayload(
                        NeoForgeNetworkHandler.TodoUpdatePayload.Action.REMOVE, todoId));
    }
}
