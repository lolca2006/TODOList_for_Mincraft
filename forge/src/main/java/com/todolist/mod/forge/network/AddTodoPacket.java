package com.todolist.mod.forge.network;

import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.common.model.TodoVisibility;
import com.todolist.mod.server.data.ServerTodoManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AddTodoPacket {
    private final String text;
    private final String category;
    private final TodoVisibility visibility;
    private final List<ResourceRequirement> resources;

    public AddTodoPacket(String text, String category, TodoVisibility visibility, List<ResourceRequirement> resources) {
        this.text = text;
        this.category = category;
        this.visibility = visibility;
        this.resources = resources != null ? resources : new ArrayList<>();
    }

    public static void encode(AddTodoPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.text);
        buf.writeUtf(msg.category);
        buf.writeEnum(msg.visibility);
        buf.writeInt(msg.resources.size());
        for (ResourceRequirement req : msg.resources) {
            buf.writeUtf(req.getItemId());
            buf.writeInt(req.getCount());
            buf.writeBoolean(req.isCollected());
        }
    }

    public static AddTodoPacket decode(FriendlyByteBuf buf) {
        String text = buf.readUtf();
        String category = buf.readUtf();
        TodoVisibility visibility = buf.readEnum(TodoVisibility.class);
        int size = buf.readInt();
        List<ResourceRequirement> resources = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String itemId = buf.readUtf();
            int count = buf.readInt();
            boolean collected = buf.readBoolean();
            resources.add(new ResourceRequirement(itemId, count, collected));
        }
        return new AddTodoPacket(text, category, visibility, resources);
    }

    public static void handle(AddTodoPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerTodoManager.addTodo(player, msg.text, msg.category, msg.visibility, msg.resources);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
