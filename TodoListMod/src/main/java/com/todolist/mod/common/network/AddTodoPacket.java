package com.todolist.mod.common.network;

import com.todolist.mod.common.model.TodoVisibility;
import com.todolist.mod.server.data.ServerTodoManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AddTodoPacket {
    private final String text;
    private final String category;
    private final TodoVisibility visibility;
    private final String itemId;

    public AddTodoPacket(String text, String category, TodoVisibility visibility, String itemId) {
        this.text = text;
        this.category = category;
        this.visibility = visibility;
        this.itemId = itemId != null ? itemId : "";
    }

    public static void encode(AddTodoPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.text);
        buf.writeUtf(msg.category);
        buf.writeEnum(msg.visibility);
        buf.writeUtf(msg.itemId);
    }

    public static AddTodoPacket decode(FriendlyByteBuf buf) {
        String text = buf.readUtf();
        String category = buf.readUtf();
        TodoVisibility visibility = buf.readEnum(TodoVisibility.class);
        String itemId = buf.readUtf();
        return new AddTodoPacket(text, category, visibility, itemId.isEmpty() ? null : itemId);
    }

    public static void handle(AddTodoPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerTodoManager.addTodo(player, msg.text, msg.category, msg.visibility, msg.itemId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
