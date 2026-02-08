package com.todolist.mod.forge;

import com.todolist.mod.client.ClientTodoManager;
import net.minecraftforge.eventbus.api.IEventBus;

public class ForgeClientSetup {
    public static void init(IEventBus modBus) {
        ClientTodoManager.loadCategories();
    }
}
