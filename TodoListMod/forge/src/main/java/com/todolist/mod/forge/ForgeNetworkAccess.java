package com.todolist.mod.forge;

import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.common.model.TodoItem;
import com.todolist.mod.common.model.TodoVisibility;
import com.todolist.mod.forge.network.*;
import com.todolist.mod.platform.INetworkAccess;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public class ForgeNetworkAccess implements INetworkAccess {
    @Override
    public void sendAddTodo(String text, String category, TodoVisibility vis, List<ResourceRequirement> resources) {
        ForgeNetworkHandler.sendToServer(new AddTodoPacket(text, category, vis, resources));
    }
    @Override
    public void sendToggleTodo(UUID todoId) {
        ForgeNetworkHandler.sendToServer(new ToggleTodoPacket(todoId));
    }
    @Override
    public void sendDeleteTodo(UUID todoId) {
        ForgeNetworkHandler.sendToServer(new DeleteTodoPacket(todoId));
    }
    @Override
    public void sendEditTodo(UUID todoId, String text, String cat, TodoVisibility vis, List<ResourceRequirement> res) {
        ForgeNetworkHandler.sendToServer(new EditTodoPacket(todoId, text, cat, vis, res));
    }
    @Override
    public void sendReorderTodo(UUID todoId, int newOrder) {
        ForgeNetworkHandler.sendToServer(new ReorderTodoPacket(todoId, newOrder));
    }
    @Override
    public void sendRequestSync() {
        ForgeNetworkHandler.sendToServer(new RequestSyncPacket());
    }
    @Override
    public void sendAssignTodo(UUID todoId, UUID assignTo, String assignToName) {
        ForgeNetworkHandler.sendToServer(new AssignTodoPacket(todoId, assignTo, assignToName));
    }
    @Override
    public void sendShareTodo(UUID todoId, TodoVisibility vis) {
        ForgeNetworkHandler.sendToServer(new ShareTodoPacket(todoId, vis));
    }
    @Override
    public void sendSyncToPlayer(ServerPlayer player, List<TodoItem> items) {
        ForgeNetworkHandler.sendToPlayer(new SyncTodoListPacket(items), player);
    }
    @Override
    public void sendUpdateToAll(String action, TodoItem item) {
        TodoUpdatePacket.Action a = TodoUpdatePacket.Action.valueOf(action);
        ForgeNetworkHandler.sendToAll(new TodoUpdatePacket(a, item));
    }
    @Override
    public void sendRemoveToAll(UUID todoId) {
        ForgeNetworkHandler.sendToAll(new TodoUpdatePacket(TodoUpdatePacket.Action.REMOVE, todoId));
    }
}
