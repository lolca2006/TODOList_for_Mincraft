package com.todolist.mod.fabric;

import com.todolist.mod.Constants;
import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.common.data.TodoSerializer;
import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.common.model.TodoItem;
import com.todolist.mod.common.model.TodoVisibility;
import com.todolist.mod.platform.INetworkAccess;
import com.todolist.mod.server.data.ServerTodoManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FabricNetworkHandler implements INetworkAccess {

    // Channel IDs
    private static final ResourceLocation ADD_TODO = new ResourceLocation(Constants.MOD_ID, "add_todo");
    private static final ResourceLocation TOGGLE_TODO = new ResourceLocation(Constants.MOD_ID, "toggle_todo");
    private static final ResourceLocation DELETE_TODO = new ResourceLocation(Constants.MOD_ID, "delete_todo");
    private static final ResourceLocation EDIT_TODO = new ResourceLocation(Constants.MOD_ID, "edit_todo");
    private static final ResourceLocation REORDER_TODO = new ResourceLocation(Constants.MOD_ID, "reorder_todo");
    private static final ResourceLocation REQUEST_SYNC = new ResourceLocation(Constants.MOD_ID, "request_sync");
    private static final ResourceLocation ASSIGN_TODO = new ResourceLocation(Constants.MOD_ID, "assign_todo");
    private static final ResourceLocation SHARE_TODO = new ResourceLocation(Constants.MOD_ID, "share_todo");
    private static final ResourceLocation SYNC_TODO_LIST = new ResourceLocation(Constants.MOD_ID, "sync_list");
    private static final ResourceLocation TODO_UPDATE = new ResourceLocation(Constants.MOD_ID, "todo_update");

    // --- Server-side receivers (Client → Server packets) ---

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(ADD_TODO, (server, player, handler, buf, responseSender) -> {
            String text = buf.readUtf();
            String category = buf.readUtf();
            TodoVisibility vis = buf.readEnum(TodoVisibility.class);
            List<ResourceRequirement> resources = readResources(buf);
            server.execute(() -> ServerTodoManager.addTodo(player, text, category, vis, resources));
        });

        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_TODO, (server, player, handler, buf, responseSender) -> {
            UUID todoId = buf.readUUID();
            server.execute(() -> ServerTodoManager.toggleTodo(player, todoId));
        });

        ServerPlayNetworking.registerGlobalReceiver(DELETE_TODO, (server, player, handler, buf, responseSender) -> {
            UUID todoId = buf.readUUID();
            server.execute(() -> ServerTodoManager.deleteTodo(player, todoId));
        });

        ServerPlayNetworking.registerGlobalReceiver(EDIT_TODO, (server, player, handler, buf, responseSender) -> {
            UUID todoId = buf.readUUID();
            String text = buf.readUtf();
            String category = buf.readUtf();
            TodoVisibility vis = buf.readEnum(TodoVisibility.class);
            List<ResourceRequirement> resources = readResources(buf);
            server.execute(() -> ServerTodoManager.editTodo(player, todoId, text, category, vis, resources));
        });

        ServerPlayNetworking.registerGlobalReceiver(REORDER_TODO, (server, player, handler, buf, responseSender) -> {
            UUID todoId = buf.readUUID();
            int newOrder = buf.readInt();
            server.execute(() -> ServerTodoManager.reorderTodo(player, todoId, newOrder));
        });

        ServerPlayNetworking.registerGlobalReceiver(REQUEST_SYNC, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> ServerTodoManager.syncToPlayer(player));
        });

        ServerPlayNetworking.registerGlobalReceiver(ASSIGN_TODO, (server, player, handler, buf, responseSender) -> {
            UUID todoId = buf.readUUID();
            boolean hasAssignee = buf.readBoolean();
            UUID assignTo = hasAssignee ? buf.readUUID() : null;
            String name = hasAssignee ? buf.readUtf() : null;
            server.execute(() -> ServerTodoManager.assignTodo(player, todoId, assignTo, name));
        });

        ServerPlayNetworking.registerGlobalReceiver(SHARE_TODO, (server, player, handler, buf, responseSender) -> {
            UUID todoId = buf.readUUID();
            TodoVisibility vis = buf.readEnum(TodoVisibility.class);
            server.execute(() -> ServerTodoManager.setVisibility(player, todoId, vis));
        });
    }

    // --- Client-side receivers (Server → Client packets) ---

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_TODO_LIST, (client, handler, buf, responseSender) -> {
            String json = buf.readUtf(262144);
            client.execute(() -> {
                List<TodoItem> items = TodoSerializer.itemsFromJson(json);
                ClientTodoManager.receiveSync(items);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(TODO_UPDATE, (client, handler, buf, responseSender) -> {
            String action = buf.readUtf();
            String json = buf.readUtf(65536);
            boolean hasId = buf.readBoolean();
            UUID todoId = hasId ? buf.readUUID() : null;
            client.execute(() -> {
                switch (action) {
                    case "ADD", "UPDATE" -> {
                        List<TodoItem> items = TodoSerializer.itemsFromJson(json);
                        if (!items.isEmpty()) {
                            ClientTodoManager.receiveUpdate(items.get(0));
                        }
                    }
                    case "REMOVE" -> {
                        if (todoId != null) {
                            ClientTodoManager.receiveRemove(todoId);
                        }
                    }
                }
            });
        });
    }

    // --- INetworkAccess: Client → Server ---

    @Override
    public void sendAddTodo(String text, String category, TodoVisibility vis, List<ResourceRequirement> resources) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(text);
        buf.writeUtf(category);
        buf.writeEnum(vis);
        writeResources(buf, resources);
        ClientPlayNetworking.send(ADD_TODO, buf);
    }

    @Override
    public void sendToggleTodo(UUID todoId) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(todoId);
        ClientPlayNetworking.send(TOGGLE_TODO, buf);
    }

    @Override
    public void sendDeleteTodo(UUID todoId) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(todoId);
        ClientPlayNetworking.send(DELETE_TODO, buf);
    }

    @Override
    public void sendEditTodo(UUID todoId, String text, String cat, TodoVisibility vis, List<ResourceRequirement> res) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(todoId);
        buf.writeUtf(text);
        buf.writeUtf(cat);
        buf.writeEnum(vis);
        writeResources(buf, res);
        ClientPlayNetworking.send(EDIT_TODO, buf);
    }

    @Override
    public void sendReorderTodo(UUID todoId, int newOrder) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(todoId);
        buf.writeInt(newOrder);
        ClientPlayNetworking.send(REORDER_TODO, buf);
    }

    @Override
    public void sendRequestSync() {
        ClientPlayNetworking.send(REQUEST_SYNC, PacketByteBufs.create());
    }

    @Override
    public void sendAssignTodo(UUID todoId, UUID assignTo, String assignToName) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(todoId);
        buf.writeBoolean(assignTo != null);
        if (assignTo != null) {
            buf.writeUUID(assignTo);
            buf.writeUtf(assignToName != null ? assignToName : "");
        }
        ClientPlayNetworking.send(ASSIGN_TODO, buf);
    }

    @Override
    public void sendShareTodo(UUID todoId, TodoVisibility vis) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(todoId);
        buf.writeEnum(vis);
        ClientPlayNetworking.send(SHARE_TODO, buf);
    }

    // --- INetworkAccess: Server → Client ---

    @Override
    public void sendSyncToPlayer(ServerPlayer player, List<TodoItem> items) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(TodoSerializer.itemsToJson(items), 262144);
        ServerPlayNetworking.send(player, SYNC_TODO_LIST, buf);
    }

    @Override
    public void sendUpdateToAll(String action, TodoItem item) {
        var server = ServerTodoManager.getServer();
        if (server == null) return;

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(action);
        buf.writeUtf(item != null ? TodoSerializer.itemsToJson(List.of(item)) : "[]", 65536);
        buf.writeBoolean(item != null);
        if (item != null) buf.writeUUID(item.getId());

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(p, TODO_UPDATE, PacketByteBufs.copy(buf));
        }
    }

    @Override
    public void sendRemoveToAll(UUID todoId) {
        var server = ServerTodoManager.getServer();
        if (server == null) return;

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf("REMOVE");
        buf.writeUtf("[]", 65536);
        buf.writeBoolean(true);
        buf.writeUUID(todoId);

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(p, TODO_UPDATE, PacketByteBufs.copy(buf));
        }
    }

    private static void writeResources(FriendlyByteBuf buf, List<ResourceRequirement> resources) {
        if (resources == null) resources = List.of();
        buf.writeInt(resources.size());
        for (ResourceRequirement req : resources) {
            buf.writeUtf(req.getItemId());
            buf.writeInt(req.getCount());
            buf.writeBoolean(req.isCollected());
        }
    }

    private static List<ResourceRequirement> readResources(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<ResourceRequirement> resources = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String itemId = buf.readUtf();
            int count = buf.readInt();
            boolean collected = buf.readBoolean();
            resources.add(new ResourceRequirement(itemId, count, collected));
        }
        return resources;
    }
}
