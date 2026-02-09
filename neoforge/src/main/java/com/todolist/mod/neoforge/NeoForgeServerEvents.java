package com.todolist.mod.neoforge;

import com.todolist.mod.Constants;
import com.todolist.mod.server.data.ServerTodoManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/**
 * Server lifecycle event handlers for NeoForge 1.21.1.
 * Manages ServerTodoManager initialization, shutdown, and player sync on join.
 */
public class NeoForgeServerEvents {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ServerTodoManager.init(event.getServer());
        Constants.LOGGER.info("Todo List server data initialized");
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerTodoManager.shutdown();
        Constants.LOGGER.info("Todo List server data saved");
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerTodoManager.syncToPlayer(player);
        }
    }
}
