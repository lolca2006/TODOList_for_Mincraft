package com.todolist.mod.common.network;

import com.todolist.mod.server.data.ServerTodoManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSyncPacket {
    public RequestSyncPacket() {}

    public static void encode(RequestSyncPacket msg, FriendlyByteBuf buf) {}

    public static RequestSyncPacket decode(FriendlyByteBuf buf) {
        return new RequestSyncPacket();
    }

    public static void handle(RequestSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerTodoManager.syncToPlayer(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
