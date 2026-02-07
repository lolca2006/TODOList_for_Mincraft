package com.todolist.mod.client;

import net.minecraftforge.eventbus.api.IEventBus;

public class ClientSetup {
    public static void init(IEventBus modBus) {
        ClientTodoManager.loadCategories();
    }
}
