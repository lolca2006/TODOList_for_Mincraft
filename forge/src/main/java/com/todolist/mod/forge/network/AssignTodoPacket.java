package com.todolist.mod.forge.network;

import com.todolist.mod.server.data.ServerTodoManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class AssignTodoPacket {
    private final UUID todoId;
    private final UUID assignTo;
    private final String assignToName;

    public AssignTodoPacket(UUID todoId, UUID assignTo, String assignToName) {
        this.todoId = todoId;
        this.assignTo = assignTo;
        this.assignToName = assignToName;
    }

    public static void encode(AssignTodoPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.todoId);
        buf.writeBoolean(msg.assignTo != null);
        if (msg.assignTo != null) {
            buf.writeUUID(msg.assignTo);
            buf.writeUtf(msg.assignToName != null ? msg.assignToName : "");
        }
    }

    public static AssignTodoPacket decode(FriendlyByteBuf buf) {
        UUID todoId = buf.readUUID();
        boolean hasAssignee = buf.readBoolean();
        UUID assignTo = hasAssignee ? buf.readUUID() : null;
        String name = hasAssignee ? buf.readUtf() : null;
        return new AssignTodoPacket(todoId, assignTo, name);
    }

    public static void handle(AssignTodoPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerTodoManager.assignTodo(player, msg.todoId, msg.assignTo, msg.assignToName);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
