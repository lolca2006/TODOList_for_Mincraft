package com.todolist.mod.neoforge;

import com.todolist.mod.Constants;
import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.common.data.TodoSerializer;
import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.common.model.TodoItem;
import com.todolist.mod.common.model.TodoVisibility;
import com.todolist.mod.server.data.ServerTodoManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * NeoForge 1.21.1 networking handler using CustomPacketPayload and StreamCodec.
 * All payloads are defined as records implementing CustomPacketPayload.
 */
public class NeoForgeNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    // ========================
    // Payload Definitions
    // ========================

    // --- Client -> Server Payloads ---

    public record RequestSyncPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RequestSyncPayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "request_sync"));
        public static final StreamCodec<ByteBuf, RequestSyncPayload> STREAM_CODEC =
                StreamCodec.unit(new RequestSyncPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record AddTodoPayload(String text, String category, TodoVisibility visibility,
                                 List<ResourceRequirement> resources) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<AddTodoPayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "add_todo"));
        public static final StreamCodec<FriendlyByteBuf, AddTodoPayload> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public AddTodoPayload decode(FriendlyByteBuf buf) {
                        String text = buf.readUtf();
                        String category = buf.readUtf();
                        TodoVisibility vis = buf.readEnum(TodoVisibility.class);
                        int size = buf.readInt();
                        List<ResourceRequirement> resources = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            String itemId = buf.readUtf();
                            int count = buf.readInt();
                            boolean collected = buf.readBoolean();
                            resources.add(new ResourceRequirement(itemId, count, collected));
                        }
                        return new AddTodoPayload(text, category, vis, resources);
                    }

                    @Override
                    public void encode(FriendlyByteBuf buf, AddTodoPayload payload) {
                        buf.writeUtf(payload.text());
                        buf.writeUtf(payload.category());
                        buf.writeEnum(payload.visibility());
                        buf.writeInt(payload.resources().size());
                        for (ResourceRequirement req : payload.resources()) {
                            buf.writeUtf(req.getItemId());
                            buf.writeInt(req.getCount());
                            buf.writeBoolean(req.isCollected());
                        }
                    }
                };

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ToggleTodoPayload(UUID todoId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ToggleTodoPayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "toggle_todo"));
        public static final StreamCodec<FriendlyByteBuf, ToggleTodoPayload> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public ToggleTodoPayload decode(FriendlyByteBuf buf) {
                        return new ToggleTodoPayload(buf.readUUID());
                    }

                    @Override
                    public void encode(FriendlyByteBuf buf, ToggleTodoPayload payload) {
                        buf.writeUUID(payload.todoId());
                    }
                };

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record DeleteTodoPayload(UUID todoId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<DeleteTodoPayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "delete_todo"));
        public static final StreamCodec<FriendlyByteBuf, DeleteTodoPayload> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public DeleteTodoPayload decode(FriendlyByteBuf buf) {
                        return new DeleteTodoPayload(buf.readUUID());
                    }

                    @Override
                    public void encode(FriendlyByteBuf buf, DeleteTodoPayload payload) {
                        buf.writeUUID(payload.todoId());
                    }
                };

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record EditTodoPayload(UUID todoId, String text, String category, TodoVisibility visibility,
                                  List<ResourceRequirement> resources) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<EditTodoPayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "edit_todo"));
        public static final StreamCodec<FriendlyByteBuf, EditTodoPayload> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public EditTodoPayload decode(FriendlyByteBuf buf) {
                        UUID id = buf.readUUID();
                        String text = buf.readUtf();
                        String category = buf.readUtf();
                        TodoVisibility vis = buf.readEnum(TodoVisibility.class);
                        int count = buf.readInt();
                        List<ResourceRequirement> resources = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            String itemId = buf.readUtf();
                            int itemCount = buf.readInt();
                            boolean collected = buf.readBoolean();
                            resources.add(new ResourceRequirement(itemId, itemCount, collected));
                        }
                        return new EditTodoPayload(id, text, category, vis, resources);
                    }

                    @Override
                    public void encode(FriendlyByteBuf buf, EditTodoPayload payload) {
                        buf.writeUUID(payload.todoId());
                        buf.writeUtf(payload.text());
                        buf.writeUtf(payload.category());
                        buf.writeEnum(payload.visibility());
                        buf.writeInt(payload.resources().size());
                        for (ResourceRequirement req : payload.resources()) {
                            buf.writeUtf(req.getItemId());
                            buf.writeInt(req.getCount());
                            buf.writeBoolean(req.isCollected());
                        }
                    }
                };

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ReorderTodoPayload(UUID todoId, int newOrder) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ReorderTodoPayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "reorder_todo"));
        public static final StreamCodec<FriendlyByteBuf, ReorderTodoPayload> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public ReorderTodoPayload decode(FriendlyByteBuf buf) {
                        return new ReorderTodoPayload(buf.readUUID(), buf.readInt());
                    }

                    @Override
                    public void encode(FriendlyByteBuf buf, ReorderTodoPayload payload) {
                        buf.writeUUID(payload.todoId());
                        buf.writeInt(payload.newOrder());
                    }
                };

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record AssignTodoPayload(UUID todoId, UUID assignTo,
                                    String assignToName) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<AssignTodoPayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "assign_todo"));
        public static final StreamCodec<FriendlyByteBuf, AssignTodoPayload> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public AssignTodoPayload decode(FriendlyByteBuf buf) {
                        UUID todoId = buf.readUUID();
                        boolean hasAssignee = buf.readBoolean();
                        UUID assignTo = hasAssignee ? buf.readUUID() : null;
                        String name = hasAssignee ? buf.readUtf() : null;
                        return new AssignTodoPayload(todoId, assignTo, name);
                    }

                    @Override
                    public void encode(FriendlyByteBuf buf, AssignTodoPayload payload) {
                        buf.writeUUID(payload.todoId());
                        buf.writeBoolean(payload.assignTo() != null);
                        if (payload.assignTo() != null) {
                            buf.writeUUID(payload.assignTo());
                            buf.writeUtf(payload.assignToName() != null ? payload.assignToName() : "");
                        }
                    }
                };

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ShareTodoPayload(UUID todoId, TodoVisibility visibility) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ShareTodoPayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "share_todo"));
        public static final StreamCodec<FriendlyByteBuf, ShareTodoPayload> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public ShareTodoPayload decode(FriendlyByteBuf buf) {
                        return new ShareTodoPayload(buf.readUUID(), buf.readEnum(TodoVisibility.class));
                    }

                    @Override
                    public void encode(FriendlyByteBuf buf, ShareTodoPayload payload) {
                        buf.writeUUID(payload.todoId());
                        buf.writeEnum(payload.visibility());
                    }
                };

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // --- Server -> Client Payloads ---

    public record SyncTodoListPayload(String json) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncTodoListPayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sync_todo_list"));
        public static final StreamCodec<FriendlyByteBuf, SyncTodoListPayload> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public SyncTodoListPayload decode(FriendlyByteBuf buf) {
                        return new SyncTodoListPayload(buf.readUtf(262144));
                    }

                    @Override
                    public void encode(FriendlyByteBuf buf, SyncTodoListPayload payload) {
                        buf.writeUtf(payload.json(), 262144);
                    }
                };

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record TodoUpdatePayload(Action action, String json, UUID todoId) implements CustomPacketPayload {
        public enum Action { ADD, REMOVE, UPDATE }

        public static final CustomPacketPayload.Type<TodoUpdatePayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "todo_update"));
        public static final StreamCodec<FriendlyByteBuf, TodoUpdatePayload> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public TodoUpdatePayload decode(FriendlyByteBuf buf) {
                        Action action = buf.readEnum(Action.class);
                        String json = buf.readUtf(65536);
                        boolean hasId = buf.readBoolean();
                        UUID todoId = hasId ? buf.readUUID() : null;
                        return new TodoUpdatePayload(action, json, todoId);
                    }

                    @Override
                    public void encode(FriendlyByteBuf buf, TodoUpdatePayload payload) {
                        buf.writeEnum(payload.action());
                        buf.writeUtf(payload.json(), 65536);
                        buf.writeBoolean(payload.todoId() != null);
                        if (payload.todoId() != null) {
                            buf.writeUUID(payload.todoId());
                        }
                    }
                };

        /** Convenience constructor for item-based updates (ADD, UPDATE) */
        public TodoUpdatePayload(Action action, TodoItem item) {
            this(action,
                    item != null ? TodoSerializer.itemsToJson(List.of(item)) : "[]",
                    item != null ? item.getId() : null);
        }

        /** Convenience constructor for REMOVE by id */
        public TodoUpdatePayload(Action action, UUID todoId) {
            this(action, "[]", todoId);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // ========================
    // Registration
    // ========================

    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Constants.MOD_ID).versioned(PROTOCOL_VERSION);

        // Client -> Server payloads
        registrar.playToServer(
                RequestSyncPayload.TYPE,
                RequestSyncPayload.STREAM_CODEC,
                NeoForgeNetworkHandler::handleRequestSync
        );
        registrar.playToServer(
                AddTodoPayload.TYPE,
                AddTodoPayload.STREAM_CODEC,
                NeoForgeNetworkHandler::handleAddTodo
        );
        registrar.playToServer(
                ToggleTodoPayload.TYPE,
                ToggleTodoPayload.STREAM_CODEC,
                NeoForgeNetworkHandler::handleToggleTodo
        );
        registrar.playToServer(
                DeleteTodoPayload.TYPE,
                DeleteTodoPayload.STREAM_CODEC,
                NeoForgeNetworkHandler::handleDeleteTodo
        );
        registrar.playToServer(
                EditTodoPayload.TYPE,
                EditTodoPayload.STREAM_CODEC,
                NeoForgeNetworkHandler::handleEditTodo
        );
        registrar.playToServer(
                ReorderTodoPayload.TYPE,
                ReorderTodoPayload.STREAM_CODEC,
                NeoForgeNetworkHandler::handleReorderTodo
        );
        registrar.playToServer(
                AssignTodoPayload.TYPE,
                AssignTodoPayload.STREAM_CODEC,
                NeoForgeNetworkHandler::handleAssignTodo
        );
        registrar.playToServer(
                ShareTodoPayload.TYPE,
                ShareTodoPayload.STREAM_CODEC,
                NeoForgeNetworkHandler::handleShareTodo
        );

        // Server -> Client payloads
        registrar.playToClient(
                SyncTodoListPayload.TYPE,
                SyncTodoListPayload.STREAM_CODEC,
                NeoForgeNetworkHandler::handleSyncTodoList
        );
        registrar.playToClient(
                TodoUpdatePayload.TYPE,
                TodoUpdatePayload.STREAM_CODEC,
                NeoForgeNetworkHandler::handleTodoUpdate
        );
    }

    // ========================
    // Server-side Handlers (Client -> Server)
    // ========================

    private static void handleRequestSync(RequestSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerTodoManager.syncToPlayer(player);
            }
        });
    }

    private static void handleAddTodo(AddTodoPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerTodoManager.addTodo(player, payload.text(), payload.category(),
                        payload.visibility(), payload.resources());
            }
        });
    }

    private static void handleToggleTodo(ToggleTodoPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerTodoManager.toggleTodo(player, payload.todoId());
            }
        });
    }

    private static void handleDeleteTodo(DeleteTodoPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerTodoManager.deleteTodo(player, payload.todoId());
            }
        });
    }

    private static void handleEditTodo(EditTodoPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerTodoManager.editTodo(player, payload.todoId(), payload.text(),
                        payload.category(), payload.visibility(), payload.resources());
            }
        });
    }

    private static void handleReorderTodo(ReorderTodoPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerTodoManager.reorderTodo(player, payload.todoId(), payload.newOrder());
            }
        });
    }

    private static void handleAssignTodo(AssignTodoPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerTodoManager.assignTodo(player, payload.todoId(), payload.assignTo(), payload.assignToName());
            }
        });
    }

    private static void handleShareTodo(ShareTodoPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerTodoManager.setVisibility(player, payload.todoId(), payload.visibility());
            }
        });
    }

    // ========================
    // Client-side Handlers (Server -> Client)
    // ========================

    private static void handleSyncTodoList(SyncTodoListPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            List<TodoItem> items = TodoSerializer.itemsFromJson(payload.json());
            ClientTodoManager.receiveSync(items);
        });
    }

    private static void handleTodoUpdate(TodoUpdatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            switch (payload.action()) {
                case ADD, UPDATE -> {
                    List<TodoItem> items = TodoSerializer.itemsFromJson(payload.json());
                    if (!items.isEmpty()) {
                        ClientTodoManager.receiveUpdate(items.get(0));
                    }
                }
                case REMOVE -> {
                    if (payload.todoId() != null) {
                        ClientTodoManager.receiveRemove(payload.todoId());
                    }
                }
            }
        });
    }

    // ========================
    // Send Helpers
    // ========================

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static void sendToPlayer(CustomPacketPayload payload, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToAll(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }
}
