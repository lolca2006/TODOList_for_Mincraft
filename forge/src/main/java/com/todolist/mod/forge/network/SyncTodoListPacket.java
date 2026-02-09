package com.todolist.mod.forge.network;

import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.common.data.TodoSerializer;
import com.todolist.mod.common.model.TodoItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class SyncTodoListPacket {
    private final String json;

    public SyncTodoListPacket(List<TodoItem> items) {
        this.json = TodoSerializer.itemsToJson(items);
    }

    private SyncTodoListPacket(String json) {
        this.json = json;
    }

    public static void encode(SyncTodoListPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, 262144);
    }

    public static SyncTodoListPacket decode(FriendlyByteBuf buf) {
        return new SyncTodoListPacket(buf.readUtf(262144));
    }

    public static void handle(SyncTodoListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            List<TodoItem> items = TodoSerializer.itemsFromJson(msg.json);
            ClientTodoManager.receiveSync(items);
        });
        ctx.get().setPacketHandled(true);
    }
}
