package com.todolist.mod.forge.network;

import com.todolist.mod.common.model.TodoVisibility;
import com.todolist.mod.server.data.ServerTodoManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ShareTodoPacket {
    private final UUID todoId;
    private final TodoVisibility visibility;

    public ShareTodoPacket(UUID todoId, TodoVisibility visibility) {
        this.todoId = todoId;
        this.visibility = visibility;
    }

    public static void encode(ShareTodoPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.todoId);
        buf.writeEnum(msg.visibility);
    }

    public static ShareTodoPacket decode(FriendlyByteBuf buf) {
        return new ShareTodoPacket(buf.readUUID(), buf.readEnum(TodoVisibility.class));
    }

    public static void handle(ShareTodoPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerTodoManager.setVisibility(player, msg.todoId, msg.visibility);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
