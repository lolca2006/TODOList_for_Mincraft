package com.todolist.mod.integration.jei;

import com.todolist.mod.TodoListMod;
import com.todolist.mod.client.gui.ResourcePickerScreen;
import com.todolist.mod.client.gui.TodoEditScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class TodoJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(TodoListMod.MODID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        JEIBridge.setRuntime(jeiRuntime);
    }

    @Override
    public void onRuntimeUnavailable() {
        JEIBridge.setRuntime(null);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        // Register our screens so JEI can render overlays alongside them
        registration.addGuiScreenHandler(TodoEditScreen.class, guiScreen -> null);
        registration.addGuiScreenHandler(ResourcePickerScreen.class, guiScreen -> null);

        // Register ghost ingredient handler - allows dragging items from JEI into TodoEditScreen
        registration.addGhostIngredientHandler(TodoEditScreen.class, new TodoEditGhostHandler());
    }
}
