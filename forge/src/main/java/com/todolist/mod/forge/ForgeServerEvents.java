package com.todolist.mod.forge;

import com.todolist.mod.Constants;
import com.todolist.mod.server.data.ServerTodoManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ForgeServerEvents {

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
