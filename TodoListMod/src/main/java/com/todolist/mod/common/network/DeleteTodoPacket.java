package com.todolist.mod.common.network;

import com.todolist.mod.server.data.ServerTodoManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class DeleteTodoPacket {
    private final UUID todoId;

    public DeleteTodoPacket(UUID todoId) {
        this.todoId = todoId;
    }

    public static void encode(DeleteTodoPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.todoId);
    }

    public static DeleteTodoPacket decode(FriendlyByteBuf buf) {
        return new DeleteTodoPacket(buf.readUUID());
    }

    public static void handle(DeleteTodoPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerTodoManager.deleteTodo(player, msg.todoId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
