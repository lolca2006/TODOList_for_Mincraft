package com.todolist.mod.common.network;

import com.todolist.mod.server.data.ServerTodoManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ReorderTodoPacket {
    private final UUID todoId;
    private final int newSortOrder;

    public ReorderTodoPacket(UUID todoId, int newSortOrder) {
        this.todoId = todoId;
        this.newSortOrder = newSortOrder;
    }

    public static void encode(ReorderTodoPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.todoId);
        buf.writeInt(msg.newSortOrder);
    }

    public static ReorderTodoPacket decode(FriendlyByteBuf buf) {
        return new ReorderTodoPacket(buf.readUUID(), buf.readInt());
    }

    public static void handle(ReorderTodoPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerTodoManager.reorderTodo(player, msg.todoId, msg.newSortOrder);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
