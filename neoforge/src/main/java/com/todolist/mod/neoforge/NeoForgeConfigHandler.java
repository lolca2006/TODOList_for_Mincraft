package com.todolist.mod.neoforge;

import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoForgeConfigHandler {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ClientConfig CLIENT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CLIENT = new ClientConfig(builder);
        CLIENT_SPEC = builder.build();
    }

    public static class ClientConfig {
        public final ModConfigSpec.IntValue hudX;
        public final ModConfigSpec.IntValue hudY;
        public final ModConfigSpec.DoubleValue hudOpacity;
        public final ModConfigSpec.DoubleValue hudScale;
        public final ModConfigSpec.BooleanValue hudVisible;
        public final ModConfigSpec.IntValue hudMaxItems;
        public final ModConfigSpec.BooleanValue showCompletedInHud;
        public final ModConfigSpec.BooleanValue playCompletionSound;
        public final ModConfigSpec.ConfigValue<String> hudActiveCategory;
        public final ModConfigSpec.BooleanValue inventoryOverlayEnabled;
        public final ModConfigSpec.IntValue inventoryOverlayMaxItems;

        ClientConfig(ModConfigSpec.Builder builder) {
            builder.comment("HUD Overlay Settings").push("hud");

            hudX = builder
                    .comment("X position of the HUD overlay")
                    .defineInRange("x", 10, 0, 10000);
            hudY = builder
                    .comment("Y position of the HUD overlay")
                    .defineInRange("y", 10, 0, 10000);
            hudOpacity = builder
                    .comment("Opacity of the HUD overlay (0.0 = transparent, 1.0 = opaque)")
                    .defineInRange("opacity", 0.7, 0.0, 1.0);
            hudScale = builder
                    .comment("Scale of the HUD overlay")
                    .defineInRange("scale", 1.0, 0.5, 3.0);
            hudVisible = builder
                    .comment("Whether the HUD overlay is visible")
                    .define("visible", true);
            hudMaxItems = builder
                    .comment("Maximum number of items to show in the HUD overlay")
                    .defineInRange("maxItems", 8, 1, 20);
            showCompletedInHud = builder
                    .comment("Show completed items in the HUD overlay")
                    .define("showCompleted", false);

            builder.pop();

            builder.comment("General Settings").push("general");

            playCompletionSound = builder
                    .comment("Play a sound when completing a task")
                    .define("completionSound", true);
            hudActiveCategory = builder
                    .comment("Active category filter in HUD (empty = all)")
                    .define("activeCategory", "");

            builder.pop();

            builder.comment("Inventory Overlay Settings").push("inventoryOverlay");

            inventoryOverlayEnabled = builder
                    .comment("Show resource overlay when inventory is open")
                    .define("enabled", true);
            inventoryOverlayMaxItems = builder
                    .comment("Maximum resources to show in inventory overlay")
                    .defineInRange("maxItems", 15, 1, 50);

            builder.pop();
        }
    }
}
