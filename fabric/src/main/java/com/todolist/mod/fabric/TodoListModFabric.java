package com.todolist.mod.fabric;

import com.todolist.mod.Constants;
import com.todolist.mod.server.data.ServerTodoManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public class TodoListModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricNetworkHandler.registerServerReceivers();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ServerTodoManager.init(server);
            Constants.LOGGER.info("Todo List server data initialized");
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ServerTodoManager.shutdown();
            Constants.LOGGER.info("Todo List server data saved");
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            ServerTodoManager.syncToPlayer(player);
        });

        Constants.LOGGER.info("Todo List Mod v4.0 loaded! (Fabric)");
    }
}
