package com.todolist.mod.common.network;

import com.todolist.mod.server.data.ServerTodoManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ToggleTodoPacket {
    private final UUID todoId;

    public ToggleTodoPacket(UUID todoId) {
        this.todoId = todoId;
    }

    public static void encode(ToggleTodoPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.todoId);
    }

    public static ToggleTodoPacket decode(FriendlyByteBuf buf) {
        return new ToggleTodoPacket(buf.readUUID());
    }

    public static void handle(ToggleTodoPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerTodoManager.toggleTodo(player, msg.todoId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
