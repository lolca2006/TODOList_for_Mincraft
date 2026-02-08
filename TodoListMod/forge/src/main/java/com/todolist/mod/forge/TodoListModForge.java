package com.todolist.mod.forge;

import com.todolist.mod.Constants;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(Constants.MOD_ID)
public class TodoListModForge {
    public TodoListModForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onCommonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ForgeConfigHandler.CLIENT_SPEC);

        if (FMLEnvironment.dist.isClient()) {
            ForgeClientSetup.init(modBus);
        }

        MinecraftForge.EVENT_BUS.register(this);
        Constants.LOGGER.info("Todo List Mod v4.0 loaded! (Forge)");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ForgeNetworkHandler::register);
    }
}
