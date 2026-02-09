package com.todolist.mod.neoforge;

import com.todolist.mod.Constants;
import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.client.KeyBindings;
import com.todolist.mod.client.gui.HudEditScreen;
import com.todolist.mod.client.gui.InventoryResourceOverlay;
import com.todolist.mod.client.gui.TodoHudOverlay;
import com.todolist.mod.client.gui.TodoScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Client-side event handlers for NeoForge 1.21.1.
 * Handles key binding registration, HUD overlay, tick events, and inventory overlay rendering.
 */
public class NeoForgeClientEvents {

    /**
     * Registers mod bus events (key mappings, GUI layers).
     * Called from the mod constructor on the client side.
     */
    public static void registerModBusEvents(IEventBus modBus) {
        modBus.addListener(NeoForgeClientEvents::onKeyRegister);
        modBus.addListener(NeoForgeClientEvents::onRegisterGuiLayers);
    }

    // ========================
    // Mod Bus Events
    // ========================

    private static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.OPEN_TODO_KEY);
        event.register(KeyBindings.TOGGLE_HUD_KEY);
        event.register(KeyBindings.EDIT_HUD_KEY);
    }

    private static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.CHAT,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "todo_hud"),
                (graphics, deltaTracker) -> {
                    Minecraft mc = Minecraft.getInstance();
                    TodoHudOverlay.render(graphics, mc.getWindow().getGuiScaledWidth(),
                            mc.getWindow().getGuiScaledHeight());
                }
        );
    }

    // ========================
    // NeoForge Bus Events (registered as a class)
    // ========================

    public static class ForgeBusEvents {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            while (KeyBindings.OPEN_TODO_KEY.consumeClick()) {
                mc.setScreen(new TodoScreen());
            }

            while (KeyBindings.TOGGLE_HUD_KEY.consumeClick()) {
                TodoHudOverlay.toggleVisibility();
            }

            while (KeyBindings.EDIT_HUD_KEY.consumeClick()) {
                mc.setScreen(new HudEditScreen());
            }
        }

        @SubscribeEvent
        public static void onPlayerJoinServer(ClientPlayerNetworkEvent.LoggingIn event) {
            ClientTodoManager.onJoinServer();
        }

        @SubscribeEvent
        public static void onPlayerLeaveServer(ClientPlayerNetworkEvent.LoggingOut event) {
            ClientTodoManager.onDisconnect();
        }

        @SubscribeEvent
        public static void onScreenRender(ScreenEvent.Render.Post event) {
            if (event.getScreen() instanceof AbstractContainerScreen<?> containerScreen) {
                Minecraft mc = Minecraft.getInstance();
                InventoryResourceOverlay.render(event.getGuiGraphics(),
                        mc.getWindow().getGuiScaledWidth(),
                        mc.getWindow().getGuiScaledHeight(),
                        event.getMouseX(), event.getMouseY(),
                        containerScreen.getGuiLeft(),
                        containerScreen.getGuiTop(),
                        containerScreen.getXSize());
            }
        }
    }
}
