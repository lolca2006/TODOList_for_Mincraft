package com.todolist.mod.forge.network;

import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.common.model.TodoVisibility;
import com.todolist.mod.server.data.ServerTodoManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class EditTodoPacket {
    private final UUID todoId;
    private final String text;
    private final String category;
    private final TodoVisibility visibility;
    private final List<ResourceRequirement> resources;

    public EditTodoPacket(UUID todoId, String text, String category, TodoVisibility visibility, List<ResourceRequirement> resources) {
        this.todoId = todoId;
        this.text = text;
        this.category = category;
        this.visibility = visibility;
        this.resources = resources != null ? resources : new ArrayList<>();
    }

    public static void encode(EditTodoPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.todoId);
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

    public static EditTodoPacket decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        String text = buf.readUtf();
        String category = buf.readUtf();
        TodoVisibility vis = buf.readEnum(TodoVisibility.class);
        int count = buf.readInt();
        List<ResourceRequirement> resources = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String itemId = buf.readUtf();
            int itemCount = buf.readInt();
            boolean collected = buf.readBoolean();
            resources.add(new ResourceRequirement(itemId, itemCount, collected));
        }
        return new EditTodoPacket(id, text, category, vis, resources);
    }

    public static void handle(EditTodoPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerTodoManager.editTodo(player, msg.todoId, msg.text, msg.category, msg.visibility, msg.resources);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
