package com.todolist.mod;

import com.mojang.logging.LogUtils;
import com.todolist.mod.client.ClientSetup;
import com.todolist.mod.forge.ForgeConfigHandler;
import com.todolist.mod.forge.ForgeNetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(TodoListMod.MODID)
public class TodoListMod {
    public static final String MODID = "todolistmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TodoListMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onCommonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ForgeConfigHandler.CLIENT_SPEC);

        if (FMLEnvironment.dist.isClient()) {
            ClientSetup.init(modBus);
        }

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Todo List Mod v2.0 loaded!");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ForgeNetworkHandler::register);
    }
}
