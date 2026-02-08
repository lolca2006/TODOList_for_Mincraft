package com.todolist.mod.forge;

import com.todolist.mod.Constants;
import com.todolist.mod.forge.network.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class ForgeNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel CHANNEL;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(Constants.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        int id = 0;
        CHANNEL.registerMessage(id++, RequestSyncPacket.class,
                RequestSyncPacket::encode, RequestSyncPacket::decode, RequestSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++, SyncTodoListPacket.class,
                SyncTodoListPacket::encode, SyncTodoListPacket::decode, SyncTodoListPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(id++, AddTodoPacket.class,
                AddTodoPacket::encode, AddTodoPacket::decode, AddTodoPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++, ToggleTodoPacket.class,
                ToggleTodoPacket::encode, ToggleTodoPacket::decode, ToggleTodoPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++, DeleteTodoPacket.class,
                DeleteTodoPacket::encode, DeleteTodoPacket::decode, DeleteTodoPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++, AssignTodoPacket.class,
                AssignTodoPacket::encode, AssignTodoPacket::decode, AssignTodoPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++, ShareTodoPacket.class,
                ShareTodoPacket::encode, ShareTodoPacket::decode, ShareTodoPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++, TodoUpdatePacket.class,
                TodoUpdatePacket::encode, TodoUpdatePacket::decode, TodoUpdatePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(id++, EditTodoPacket.class,
                EditTodoPacket::encode, EditTodoPacket::decode, EditTodoPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++, ReorderTodoPacket.class,
                ReorderTodoPacket::encode, ReorderTodoPacket::decode, ReorderTodoPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToAll(Object msg) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
    }
}
