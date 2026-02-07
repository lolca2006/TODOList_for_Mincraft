package com.todolist.mod.integration.jei;

import com.todolist.mod.TodoListMod;
import com.todolist.mod.client.gui.TodoEditScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class TodoJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(TodoListMod.MODID, "jei_plugin");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        // Register TodoEditScreen so JEI knows about it and can render overlays
        registration.addGuiScreenHandler(TodoEditScreen.class, guiScreen -> null);
    }
}
