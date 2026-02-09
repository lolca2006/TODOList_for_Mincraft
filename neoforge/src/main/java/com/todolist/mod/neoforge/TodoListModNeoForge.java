package com.todolist.mod.neoforge;

import com.todolist.mod.Constants;
import com.todolist.mod.client.ClientTodoManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Constants.MOD_ID)
public class TodoListModNeoForge {

    public TodoListModNeoForge(IEventBus modBus, ModContainer modContainer) {
        // Register client config
        modContainer.registerConfig(ModConfig.Type.CLIENT, NeoForgeConfigHandler.CLIENT_SPEC);

        // Register networking payloads on the mod event bus
        modBus.addListener(NeoForgeNetworkHandler::onRegisterPayloads);

        // Register server lifecycle events on the NeoForge event bus
        NeoForge.EVENT_BUS.register(NeoForgeServerEvents.class);

        // Client-side initialization
        if (FMLEnvironment.dist.isClient()) {
            NeoForgeClientEvents.registerModBusEvents(modBus);
            NeoForge.EVENT_BUS.register(NeoForgeClientEvents.ForgeBusEvents.class);
            ClientTodoManager.loadCategories();
        }

        Constants.LOGGER.info("Todo List Mod loaded! (NeoForge 1.21.1)");
    }
}
