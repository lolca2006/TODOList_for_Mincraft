package com.todolist.mod.forge;

import com.todolist.mod.Constants;
import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.client.KeyBindings;
import com.todolist.mod.client.gui.HudEditScreen;
import com.todolist.mod.client.gui.InventoryResourceOverlay;
import com.todolist.mod.client.gui.TodoHudOverlay;
import com.todolist.mod.client.gui.TodoScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;

public class ForgeClientEvents {

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(KeyBindings.OPEN_TODO_KEY);
            event.register(KeyBindings.TOGGLE_HUD_KEY);
            event.register(KeyBindings.EDIT_HUD_KEY);
        }

        @SubscribeEvent
        public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll(Constants.MOD_ID + "_hud", (forgeGui, graphics, partialTick, width, height) -> {
                TodoHudOverlay.render(graphics, width, height);
            });
        }
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
    public static class ForgeBusEvents {

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

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
