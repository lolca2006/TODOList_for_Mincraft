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
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FabricNetworkHandler implements INetworkAccess {

    // --- Payload types ---

    public record AddTodoPayload(String text, String category, TodoVisibility vis, List<ResourceRequirement> resources) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<AddTodoPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "add_todo"));
        public static final StreamCodec<FriendlyByteBuf, AddTodoPayload> CODEC = StreamCodec.of(
                (buf, p) -> { buf.writeUtf(p.text); buf.writeUtf(p.category); buf.writeEnum(p.vis); writeResources(buf, p.resources); },
                buf -> new AddTodoPayload(buf.readUtf(), buf.readUtf(), buf.readEnum(TodoVisibility.class), readResources(buf))
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ToggleTodoPayload(UUID todoId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ToggleTodoPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "toggle_todo"));
        public static final StreamCodec<FriendlyByteBuf, ToggleTodoPayload> CODEC = StreamCodec.of(
                (buf, p) -> buf.writeUUID(p.todoId), buf -> new ToggleTodoPayload(buf.readUUID())
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record DeleteTodoPayload(UUID todoId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<DeleteTodoPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "delete_todo"));
        public static final StreamCodec<FriendlyByteBuf, DeleteTodoPayload> CODEC = StreamCodec.of(
                (buf, p) -> buf.writeUUID(p.todoId), buf -> new DeleteTodoPayload(buf.readUUID())
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record EditTodoPayload(UUID todoId, String text, String category, TodoVisibility vis, List<ResourceRequirement> resources) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<EditTodoPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "edit_todo"));
        public static final StreamCodec<FriendlyByteBuf, EditTodoPayload> CODEC = StreamCodec.of(
                (buf, p) -> { buf.writeUUID(p.todoId); buf.writeUtf(p.text); buf.writeUtf(p.category); buf.writeEnum(p.vis); writeResources(buf, p.resources); },
                buf -> new EditTodoPayload(buf.readUUID(), buf.readUtf(), buf.readUtf(), buf.readEnum(TodoVisibility.class), readResources(buf))
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ReorderTodoPayload(UUID todoId, int newOrder) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ReorderTodoPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "reorder_todo"));
        public static final StreamCodec<FriendlyByteBuf, ReorderTodoPayload> CODEC = StreamCodec.of(
                (buf, p) -> { buf.writeUUID(p.todoId); buf.writeInt(p.newOrder); },
                buf -> new ReorderTodoPayload(buf.readUUID(), buf.readInt())
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record RequestSyncPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RequestSyncPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "request_sync"));
        public static final StreamCodec<FriendlyByteBuf, RequestSyncPayload> CODEC = StreamCodec.of(
                (buf, p) -> {}, buf -> new RequestSyncPayload()
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record AssignTodoPayload(UUID todoId, UUID assignTo, String assignToName) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<AssignTodoPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "assign_todo"));
        public static final StreamCodec<FriendlyByteBuf, AssignTodoPayload> CODEC = StreamCodec.of(
                (buf, p) -> { buf.writeUUID(p.todoId); buf.writeBoolean(p.assignTo != null); if (p.assignTo != null) { buf.writeUUID(p.assignTo); buf.writeUtf(p.assignToName != null ? p.assignToName : ""); } },
                buf -> { UUID id = buf.readUUID(); boolean has = buf.readBoolean(); return new AssignTodoPayload(id, has ? buf.readUUID() : null, has ? buf.readUtf() : null); }
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ShareTodoPayload(UUID todoId, TodoVisibility vis) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ShareTodoPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "share_todo"));
        public static final StreamCodec<FriendlyByteBuf, ShareTodoPayload> CODEC = StreamCodec.of(
                (buf, p) -> { buf.writeUUID(p.todoId); buf.writeEnum(p.vis); },
                buf -> new ShareTodoPayload(buf.readUUID(), buf.readEnum(TodoVisibility.class))
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record SyncTodoListPayload(String json) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncTodoListPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sync_list"));
        public static final StreamCodec<FriendlyByteBuf, SyncTodoListPayload> CODEC = StreamCodec.of(
                (buf, p) -> buf.writeUtf(p.json, 262144),
                buf -> new SyncTodoListPayload(buf.readUtf(262144))
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record TodoUpdatePayload(String action, String json, UUID todoId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<TodoUpdatePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "todo_update"));
        public static final StreamCodec<FriendlyByteBuf, TodoUpdatePayload> CODEC = StreamCodec.of(
                (buf, p) -> { buf.writeUtf(p.action); buf.writeUtf(p.json, 65536); buf.writeBoolean(p.todoId != null); if (p.todoId != null) buf.writeUUID(p.todoId); },
                buf -> { String action = buf.readUtf(); String json = buf.readUtf(65536); boolean hasId = buf.readBoolean(); return new TodoUpdatePayload(action, json, hasId ? buf.readUUID() : null); }
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // --- Registration ---

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(AddTodoPayload.TYPE, AddTodoPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleTodoPayload.TYPE, ToggleTodoPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DeleteTodoPayload.TYPE, DeleteTodoPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(EditTodoPayload.TYPE, EditTodoPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ReorderTodoPayload.TYPE, ReorderTodoPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestSyncPayload.TYPE, RequestSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AssignTodoPayload.TYPE, AssignTodoPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ShareTodoPayload.TYPE, ShareTodoPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(SyncTodoListPayload.TYPE, SyncTodoListPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TodoUpdatePayload.TYPE, TodoUpdatePayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(AddTodoPayload.TYPE, (payload, context) -> {
            context.player().server.execute(() -> ServerTodoManager.addTodo(context.player(), payload.text(), payload.category(), payload.vis(), payload.resources()));
        });
        ServerPlayNetworking.registerGlobalReceiver(ToggleTodoPayload.TYPE, (payload, context) -> {
            context.player().server.execute(() -> ServerTodoManager.toggleTodo(context.player(), payload.todoId()));
        });
        ServerPlayNetworking.registerGlobalReceiver(DeleteTodoPayload.TYPE, (payload, context) -> {
            context.player().server.execute(() -> ServerTodoManager.deleteTodo(context.player(), payload.todoId()));
        });
        ServerPlayNetworking.registerGlobalReceiver(EditTodoPayload.TYPE, (payload, context) -> {
            context.player().server.execute(() -> ServerTodoManager.editTodo(context.player(), payload.todoId(), payload.text(), payload.category(), payload.vis(), payload.resources()));
        });
        ServerPlayNetworking.registerGlobalReceiver(ReorderTodoPayload.TYPE, (payload, context) -> {
            context.player().server.execute(() -> ServerTodoManager.reorderTodo(context.player(), payload.todoId(), payload.newOrder()));
        });
        ServerPlayNetworking.registerGlobalReceiver(RequestSyncPayload.TYPE, (payload, context) -> {
            context.player().server.execute(() -> ServerTodoManager.syncToPlayer(context.player()));
        });
        ServerPlayNetworking.registerGlobalReceiver(AssignTodoPayload.TYPE, (payload, context) -> {
            context.player().server.execute(() -> ServerTodoManager.assignTodo(context.player(), payload.todoId(), payload.assignTo(), payload.assignToName()));
        });
        ServerPlayNetworking.registerGlobalReceiver(ShareTodoPayload.TYPE, (payload, context) -> {
            context.player().server.execute(() -> ServerTodoManager.setVisibility(context.player(), payload.todoId(), payload.vis()));
        });
    }

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(SyncTodoListPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                List<TodoItem> items = TodoSerializer.itemsFromJson(payload.json());
                ClientTodoManager.receiveSync(items);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(TodoUpdatePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                switch (payload.action()) {
                    case "ADD", "UPDATE" -> {
                        List<TodoItem> items = TodoSerializer.itemsFromJson(payload.json());
                        if (!items.isEmpty()) {
                            ClientTodoManager.receiveUpdate(items.get(0));
                        }
                    }
                    case "REMOVE" -> {
                        if (payload.todoId() != null) {
                            ClientTodoManager.receiveRemove(payload.todoId());
                        }
                    }
                }
            });
        });
    }

    // --- INetworkAccess: Client → Server ---

    @Override
    public void sendAddTodo(String text, String category, TodoVisibility vis, List<ResourceRequirement> resources) {
        ClientPlayNetworking.send(new AddTodoPayload(text, category, vis, resources != null ? resources : List.of()));
    }

    @Override
    public void sendToggleTodo(UUID todoId) {
        ClientPlayNetworking.send(new ToggleTodoPayload(todoId));
    }

    @Override
    public void sendDeleteTodo(UUID todoId) {
        ClientPlayNetworking.send(new DeleteTodoPayload(todoId));
    }

    @Override
    public void sendEditTodo(UUID todoId, String text, String cat, TodoVisibility vis, List<ResourceRequirement> res) {
        ClientPlayNetworking.send(new EditTodoPayload(todoId, text, cat, vis, res != null ? res : List.of()));
    }

    @Override
    public void sendReorderTodo(UUID todoId, int newOrder) {
        ClientPlayNetworking.send(new ReorderTodoPayload(todoId, newOrder));
    }

    @Override
    public void sendRequestSync() {
        ClientPlayNetworking.send(new RequestSyncPayload());
    }

    @Override
    public void sendAssignTodo(UUID todoId, UUID assignTo, String assignToName) {
        ClientPlayNetworking.send(new AssignTodoPayload(todoId, assignTo, assignToName));
    }

    @Override
    public void sendShareTodo(UUID todoId, TodoVisibility vis) {
        ClientPlayNetworking.send(new ShareTodoPayload(todoId, vis));
    }

    // --- INetworkAccess: Server → Client ---

    @Override
    public void sendSyncToPlayer(ServerPlayer player, List<TodoItem> items) {
        ServerPlayNetworking.send(player, new SyncTodoListPayload(TodoSerializer.itemsToJson(items)));
    }

    @Override
    public void sendUpdateToAll(String action, TodoItem item) {
        var server = ServerTodoManager.getServer();
        if (server == null) return;
        String json = item != null ? TodoSerializer.itemsToJson(List.of(item)) : "[]";
        UUID id = item != null ? item.getId() : null;
        TodoUpdatePayload payload = new TodoUpdatePayload(action, json, id);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(p, payload);
        }
    }

    @Override
    public void sendRemoveToAll(UUID todoId) {
        var server = ServerTodoManager.getServer();
        if (server == null) return;
        TodoUpdatePayload payload = new TodoUpdatePayload("REMOVE", "[]", todoId);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(p, payload);
        }
    }

    // --- Helpers ---

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
            resources.add(new ResourceRequirement(buf.readUtf(), buf.readInt(), buf.readBoolean()));
        }
        return resources;
    }
}
