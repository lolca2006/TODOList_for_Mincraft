package com.todolist.mod.fabric;

import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.client.KeyBindings;
import com.todolist.mod.client.gui.HudEditScreen;
import com.todolist.mod.client.gui.InventoryResourceOverlay;
import com.todolist.mod.client.gui.TodoHudOverlay;
import com.todolist.mod.client.gui.TodoScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class TodoListModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register keybindings
        KeyBindingHelper.registerKeyBinding(KeyBindings.OPEN_TODO_KEY);
        KeyBindingHelper.registerKeyBinding(KeyBindings.TOGGLE_HUD_KEY);
        KeyBindingHelper.registerKeyBinding(KeyBindings.EDIT_HUD_KEY);

        // Register client networking
        FabricNetworkHandler.registerClientReceivers();

        // Load categories on startup
        ClientTodoManager.loadCategories();

        // Client tick for key handling
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (KeyBindings.OPEN_TODO_KEY.consumeClick()) {
                client.setScreen(new TodoScreen());
            }

            while (KeyBindings.TOGGLE_HUD_KEY.consumeClick()) {
                TodoHudOverlay.toggleVisibility();
            }

            while (KeyBindings.EDIT_HUD_KEY.consumeClick()) {
                client.setScreen(new HudEditScreen());
            }
        });

        // HUD overlay rendering
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            Minecraft mc = Minecraft.getInstance();
            TodoHudOverlay.render(graphics, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        });

        // Inventory resource overlay
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                ScreenEvents.afterRender(screen).register((screen1, graphics, mouseX, mouseY, tickDelta) -> {
                    InventoryResourceOverlay.render(graphics,
                            client.getWindow().getGuiScaledWidth(),
                            client.getWindow().getGuiScaledHeight(),
                            mouseX, mouseY,
                            containerScreen.leftPos,
                            containerScreen.topPos,
                            containerScreen.imageWidth);
                });
            }
        });

        // Player connection events
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientTodoManager.onJoinServer();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientTodoManager.onDisconnect();
        });
    }
}
