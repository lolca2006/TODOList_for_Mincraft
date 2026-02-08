package com.todolist.mod.forge.network;

import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.common.data.TodoSerializer;
import com.todolist.mod.common.model.TodoItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class TodoUpdatePacket {
    public enum Action { ADD, REMOVE, UPDATE }

    private final Action action;
    private final String json;
    private final UUID todoId;

    public TodoUpdatePacket(Action action, TodoItem item) {
        this.action = action;
        this.json = item != null ? TodoSerializer.itemsToJson(List.of(item)) : "[]";
        this.todoId = item != null ? item.getId() : null;
    }

    public TodoUpdatePacket(Action action, UUID todoId) {
        this.action = action;
        this.json = "[]";
        this.todoId = todoId;
    }

    private TodoUpdatePacket(Action action, String json, UUID todoId) {
        this.action = action;
        this.json = json;
        this.todoId = todoId;
    }

    public static void encode(TodoUpdatePacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeUtf(msg.json, 65536);
        buf.writeBoolean(msg.todoId != null);
        if (msg.todoId != null) buf.writeUUID(msg.todoId);
    }

    public static TodoUpdatePacket decode(FriendlyByteBuf buf) {
        Action action = buf.readEnum(Action.class);
        String json = buf.readUtf(65536);
        boolean hasId = buf.readBoolean();
        UUID todoId = hasId ? buf.readUUID() : null;
        return new TodoUpdatePacket(action, json, todoId);
    }

    public static void handle(TodoUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            switch (msg.action) {
                case ADD, UPDATE -> {
                    List<TodoItem> items = TodoSerializer.itemsFromJson(msg.json);
                    if (!items.isEmpty()) {
                        ClientTodoManager.receiveUpdate(items.get(0));
                    }
                }
                case REMOVE -> {
                    if (msg.todoId != null) {
                        ClientTodoManager.receiveRemove(msg.todoId);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
